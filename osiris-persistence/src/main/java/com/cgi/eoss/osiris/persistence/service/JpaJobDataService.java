package com.cgi.eoss.osiris.persistence.service;

import com.cgi.eoss.osiris.model.OsirisService;
import com.cgi.eoss.osiris.model.Job;
import com.cgi.eoss.osiris.model.Job.Status;
import com.cgi.eoss.osiris.model.JobConfig;
import com.cgi.eoss.osiris.model.User;
import com.cgi.eoss.osiris.persistence.dao.OsirisEntityDao;
import com.cgi.eoss.osiris.persistence.dao.JobDao;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cgi.eoss.osiris.model.QJob.job;

@Service
@Transactional(readOnly = true)
public class JpaJobDataService extends AbstractJpaDataService<Job> implements JobDataService {

    private final JobDao dao;

    private final JobConfigDataService jobConfigDataService;

    private final UserDataService userDataService;

    private final ServiceDataService serviceDataService;

    @Autowired
    public JpaJobDataService(JobDao jobDao, JobConfigDataService jobConfigDataService, UserDataService userDataService, ServiceDataService serviceDataService) {
        this.dao = jobDao;
        this.jobConfigDataService = jobConfigDataService;
        this.userDataService = userDataService;
        this.serviceDataService = serviceDataService;
    }

    @Override
    OsirisEntityDao<Job> getDao() {
        return dao;
    }

    @Override
    Predicate getUniquePredicate(Job entity) {
        return job.extId.eq(entity.getExtId());
    }

    @Override
    public List<Job> findByOwner(User user) {
        return dao.findByOwner(user);
    }

    @Override
    public List<Job> findByService(OsirisService service) {
        return dao.findByConfig_Service(service);
    }
    
    @Override
    public List<Job> findByStatusAndGuiUrlNotNull(Status status) {
        return dao.findByStatusAndGuiUrlNotNull(status);
    }

    @Override
    public List<Job> findByOwnerAndService(User user, OsirisService service) {
        return dao.findByOwnerAndConfig_Service(user, service);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Job buildNew(String extId, String ownerId, String serviceId, String jobConfigLabel, Multimap<String, String> inputs, Job parentJob) {
        User owner = userDataService.getByName(ownerId);
        OsirisService service = serviceDataService.getByName(serviceId);

        JobConfig config = new JobConfig(owner, service);
        config.setLabel(Strings.isNullOrEmpty(jobConfigLabel) ? null : jobConfigLabel);
        config.setInputs(inputs);
        config.setParent(parentJob);

        return buildNew(jobConfigDataService.save(config), extId, owner, parentJob);
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Job buildNew(String extId, String ownerId, String serviceId, String jobConfigLabel, Multimap<String, String> inputs) {
    		return buildNew(extId, ownerId, serviceId, jobConfigLabel, inputs, null);
    }

    private Job buildNew(JobConfig jobConfig, String extId, User owner, Job parentJob) {
        return dao.save(new Job(jobConfig, extId, owner, parentJob));
    }
    
    public Job reload(Long id) {
        Job job = this.getById(id);
        job.getOwner().getGroups().size();
        job.getOutputFiles().size();
        return job;
    }
    
    

}
