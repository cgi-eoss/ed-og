package com.cgi.eoss.osiris.orchestrator;

import com.cgi.eoss.osiris.catalogue.CatalogueConfig;
import com.cgi.eoss.osiris.costing.CostingConfig;
import com.cgi.eoss.osiris.harvesters.HarvestersConfig;
import com.cgi.eoss.osiris.orchestrator.service.CachingWorkerFactory;
import com.cgi.eoss.osiris.orchestrator.service.OsirisServiceLauncher;
import com.cgi.eoss.osiris.persistence.PersistenceConfig;
import com.cgi.eoss.osiris.persistence.service.WorkerLocatorExpressionDataService;
import com.cgi.eoss.osiris.queues.QueuesConfig;
import com.cgi.eoss.osiris.rpc.InProcessManagedChannelProvider;
import com.cgi.eoss.osiris.rpc.InProcessRpcConfig;
import com.cgi.eoss.osiris.rpc.OsirisServerClient;
import com.cgi.eoss.osiris.scheduledjobs.ScheduledJobsConfig;
import com.cgi.eoss.osiris.search.SearchConfig;
import com.cgi.eoss.osiris.security.SecurityConfig;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
/**
 * <p>Spring configuration for the OSIRIS Orchestrator component.</p>
 * <p>Manages access to distributed workers and provides the {@link OsirisServiceLauncher} RPC service.</p>
 */
@Configuration
@Import({
        PropertyPlaceholderAutoConfiguration.class,

        CatalogueConfig.class,
        CostingConfig.class,
        HarvestersConfig.class,
        InProcessRpcConfig.class,
        PersistenceConfig.class,
        QueuesConfig.class,
        ScheduledJobsConfig.class,
        SearchConfig.class,
        SecurityConfig.class
})
@EnableEurekaClient
@EnableScheduling
@ComponentScan(basePackageClasses = OrchestratorConfig.class)
public class OrchestratorConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }

    @Bean
    public ExpressionParser workerLocatorExpressionParser() {
        return new SpelExpressionParser();
    }
    
    @Bean
    public OsirisServerClient osirisServerClient(ManagedChannelBuilder inProcessChannelBuilder) {
    	return new OsirisServerClient(new InProcessManagedChannelProvider(inProcessChannelBuilder));
    }

    @Bean
    public CachingWorkerFactory workerFactory(DiscoveryClient discoveryClient,
                                       @Value("${osiris.orchestrator.worker.eurekaServiceId:osiris worker}") String workerServiceId,
                                       ExpressionParser workerLocatorExpressionParser,
                                       WorkerLocatorExpressionDataService workerLocatorExpressionDataService,
                                       @Value("${osiris.orchestrator.worker.defaultWorkerExpression:\"LOCAL\"}") String defaultWorkerExpression) {
        return new CachingWorkerFactory(discoveryClient, workerServiceId, workerLocatorExpressionParser, workerLocatorExpressionDataService, defaultWorkerExpression);
    }
}
