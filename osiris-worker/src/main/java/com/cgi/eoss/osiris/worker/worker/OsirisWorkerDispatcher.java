package com.cgi.eoss.osiris.worker.worker;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cgi.eoss.osiris.clouds.service.Node;
import com.cgi.eoss.osiris.clouds.service.StorageProvisioningException;
import com.cgi.eoss.osiris.queues.service.OsirisQueueService;
import com.cgi.eoss.osiris.rpc.LocalWorker;
import com.cgi.eoss.osiris.rpc.worker.ContainerExit;
import com.cgi.eoss.osiris.rpc.worker.ContainerExitCode;
import com.cgi.eoss.osiris.rpc.worker.ExitParams;
import com.cgi.eoss.osiris.rpc.worker.ExitWithTimeoutParams;
import com.cgi.eoss.osiris.rpc.worker.JobDockerConfig;
import com.cgi.eoss.osiris.rpc.worker.JobEnvironment;
import com.cgi.eoss.osiris.rpc.worker.JobError;
import com.cgi.eoss.osiris.rpc.worker.JobEvent;
import com.cgi.eoss.osiris.rpc.worker.JobEventType;
import com.cgi.eoss.osiris.rpc.worker.JobInputs;
import com.cgi.eoss.osiris.rpc.JobSpec;
import com.cgi.eoss.osiris.rpc.ResourceRequest;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * Service for executing OSIRIS (WPS) services inside Docker containers.
 * </p>
 */
@Log4j2
@Service
public class OsirisWorkerDispatcher {

    private OsirisQueueService queueService;
    
    private String workerId;

    private LocalWorker localWorker;
    
    private OsirisWorkerNodeManager nodeManager;

    private static final long QUEUE_SCHEDULER_INTERVAL_MS = 10L * 1000L;

    @Autowired
    public OsirisWorkerDispatcher(OsirisQueueService queueService, LocalWorker localWorker, @Qualifier("workerId") String workerId, OsirisWorkerNodeManager nodeManager) {
        this.queueService = queueService;
        this.localWorker = localWorker;
        this.workerId = workerId;
        this.nodeManager = nodeManager;

    }


    @Scheduled(fixedRate = QUEUE_SCHEDULER_INTERVAL_MS, initialDelay = 10000L)
    public void getNewJobs() {
        while (nodeManager.hasCapacity()) {
            LOG.debug("Checking for available jobs in the queue");
            JobSpec nextJobSpec = (JobSpec) queueService.receiveObjectNoWait(OsirisQueueService.jobQueueName);
            if (nextJobSpec != null) {
                LOG.info("Dequeued job {}", nextJobSpec.getJob().getId());
                nodeManager.reserveNodeForJob(nextJobSpec.getJob().getId());
                Thread t = new Thread(new JobExecutor(nextJobSpec, queueService));
                t.start();
            } else {
                LOG.debug("Job queue currently empty");
            }
        }
    }

    public interface JobUpdateListener {

        void jobUpdate(Object object);

    }

    @Data
    public class JobExecutor implements Runnable, JobUpdateListener {

        Map<String, Object> messageHeaders;
        private final JobSpec jobSpec;
        private final OsirisQueueService queueService;

        @Override
        public void run() {
            messageHeaders = new HashMap<>();
            messageHeaders.put("workerId", workerId);
            messageHeaders.put("jobId", jobSpec.getJob().getIntJobId());
            executeJob(jobSpec, this);

        }


        @Override
        public void jobUpdate(Object object) {
            queueService.sendObject(OsirisQueueService.jobUpdatesQueueName, messageHeaders, object);
        }

    }


    // Entry point after Job is dequeued
    private void executeJob(JobSpec jobSpec, JobUpdateListener jobUpdateListener) {
        String deviceId = null;
        Node jobNode = nodeManager.getJobNode(jobSpec.getJob().getId());
        try {
            jobUpdateListener.jobUpdate(JobEvent.newBuilder().setJobEventType(JobEventType.DATA_FETCHING_STARTED).build());
            JobInputs jobInputs = JobInputs.newBuilder().setJob(jobSpec.getJob()).addAllInputs(jobSpec.getInputsList()).build();
            JobEnvironment jobEnvironment = localWorker.prepareInputs(jobInputs);
            jobUpdateListener.jobUpdate(JobEvent.newBuilder().setJobEventType(JobEventType.DATA_FETCHING_COMPLETED).build());

            List<String> ports = new ArrayList<String>();
            ports.addAll(jobSpec.getExposedPortsList());

            List<String> binds = new ArrayList<String>();
            binds.add("/data/dl:/data/dl:ro");// TODO Do not bind everything, just the required folder (can be derived)
            binds.add(jobEnvironment.getWorkingDir() + "/OSIRIS-WPS-INPUT.properties:"
                    + "/home/worker/workDir/OSIRIS-WPS-INPUT.properties:ro");
            binds.add(jobEnvironment.getInputDir() + ":" + "/home/worker/workDir/inDir:ro");
            binds.add(jobEnvironment.getOutputDir() + ":" + "/home/worker/workDir/outDir:rw");
            binds.addAll(jobSpec.getUserBindsList());
            Map<String, String> environmentVariables = jobSpec.getEnvironmentVariablesMap();
            
            
            
            if (jobSpec.hasResourceRequest()) {
                ResourceRequest resourceRequest = jobSpec.getResourceRequest();
                int requiredStorage = resourceRequest.getStorage();
                String procDir = generateRandomDirName("proc");
                File storageTempDir = new File("/dockerStorage", procDir);
                deviceId = nodeManager.allocateStorageForJob(jobSpec.getJob().getId(), requiredStorage, storageTempDir.getAbsolutePath());
                binds.add(storageTempDir.getAbsolutePath() + ":" + "/home/worker/procDir:rw");
            }
            
            
            JobDockerConfig request =
                    JobDockerConfig.newBuilder().setJob(jobSpec.getJob()).setServiceName(jobSpec.getService().getName())
                            .setDockerImage(jobSpec.getService().getDockerImageTag()).addAllBinds(binds).addAllPorts(ports).putAllEnvironmentVariables(environmentVariables).build();
            localWorker.launchContainer(request);
            jobUpdateListener.jobUpdate(JobEvent.newBuilder().setJobEventType(JobEventType.PROCESSING_STARTED).build());
            int exitCode;
            if (jobSpec.getHasTimeout()) {
                ExitWithTimeoutParams exitRequest =
                        ExitWithTimeoutParams.newBuilder().setJob(jobSpec.getJob()).setTimeout(jobSpec.getTimeoutValue()).build();
                ContainerExitCode containerExitCode = localWorker.waitForContainerExitWithTimeout(exitRequest);
                exitCode = containerExitCode.getExitCode();
            } else {
                ExitParams exitRequest = ExitParams.newBuilder().setJob(jobSpec.getJob()).build();
                ContainerExitCode containerExitCode = localWorker.waitForContainerExit(exitRequest);
                exitCode = containerExitCode.getExitCode();
            }
            
            jobUpdateListener.jobUpdate(ContainerExit.newBuilder().setExitCode(exitCode).setJobEnvironment(jobEnvironment).build());
        } catch (Exception e) {
        	LOG.error("Error executing job", e);
            jobUpdateListener.jobUpdate(JobError.newBuilder().setErrorDescription(e.getMessage() != null? e.getMessage(): "Unknown error").build());
        } finally {
            if (jobSpec.hasResourceRequest()) {
                LOG.debug("Device id is: {}", deviceId);
                if (deviceId != null) {
                    try {
                        nodeManager.releaseStorageForJob(jobNode, jobSpec.getJob().getId(), deviceId);
                    } catch (StorageProvisioningException e) {
                       LOG.error("Exception releasing storage", e);
                    }
                }
            }
            localWorker.cleanUp(jobSpec.getJob());
        }
    }
    
    private static final SecureRandom random = new SecureRandom();
    private String generateRandomDirName(String prefix) {
        long n = random.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);
        String name = prefix + Long.toString(n);
        return name;
    }

}
