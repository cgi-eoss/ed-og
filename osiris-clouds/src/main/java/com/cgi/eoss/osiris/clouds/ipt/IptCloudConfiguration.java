package com.cgi.eoss.osiris.clouds.ipt;

import java.util.Properties;

import org.jclouds.ContextBuilder;
import org.jclouds.config.ContextLinking;
import org.jclouds.http.okhttp.config.OkHttpCommandExecutorServiceModule;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.keystone.config.KeystoneProperties;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.rest.ApiContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.cgi.eoss.osiris.clouds.ipt.persistence.KeypairRepository;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

import net.schmizz.sshj.SSHClient;

@Configuration
@Import({IptPersistenceConfiguration.class})
@ConditionalOnProperty(value = "osiris.clouds.ipt.enabled", havingValue = "true")
public class IptCloudConfiguration {

    @Value("${osiris.clouds.ipt.maxPoolSize:-1}")
    private int maxPoolSize;

    @Value("${osiris.clouds.ipt.os.identityEndpoint}")
    private String osIdentityEndpoint;

    @Value("${osiris.clouds.ipt.os.username}")
    private String osUsername;

    @Value("${osiris.clouds.ipt.os.password}")
    private String osPassword;

    @Value("${osiris.clouds.ipt.os.domainName}")
    private String osDomainName;

    @Value("${osiris.clouds.ipt.os.projectName}")
    private String osProjectName;
    
    @Value("${osiris.clouds.ipt.os.projectId}")
    private String osProjectId;

    @Value("${osiris.clouds.ipt.node.flavorName}")
    private String nodeFlavorName;

    @Value("${osiris.clouds.ipt.node.imageId}")
    private String nodeImageId;

    @Value("${osiris.clouds.ipt.node.provisionFloatingIp}")
    private boolean provisionFloatingIp;

    @Value("${osiris.clouds.ipt.node.floatingIpPool:floatingIpPool}")
    private String floatingIpPool;

    @Value("${osiris.clouds.ipt.node.securityGroupName}")
    private String securityGroupName;

    @Value("${osiris.clouds.ipt.node.sshUsername}")
    private String sshUsername;

    @Value("${osiris.clouds.ipt.node.networkId}")
    private String networkId;

    @Value("${osiris.clouds.ipt.node.nfsHost}")
    private String nfsHost;

    @Value("${osiris.clouds.ipt.node.additionalNfsMounts:#{null}}")
    private String additionalNfsMounts;

    @Value("${osiris.clouds.ipt.node.insecureRegistries:#{null}}")
    private String insecureRegistries;

    @Autowired
    KeypairRepository keypairRepository;

    @Bean
    public OpenstackAPIs openstackAPIs() {
    	String identity = osDomainName + ":" + osUsername; // tenantName:userName
        String credential = osPassword;

    	Iterable<Module> neutronModules = ImmutableSet.<Module>of(new SLF4JLoggingModule(), new OkHttpCommandExecutorServiceModule());
        final Properties neutronOverrides = new Properties();
        neutronOverrides.put(KeystoneProperties.KEYSTONE_VERSION, "3");
        neutronOverrides.put(KeystoneProperties.SCOPE, "project:" + osProjectName);
        
        ApiContext<NeutronApi> neutronContext = ContextBuilder.newBuilder("openstack-neutron")
                .endpoint(osIdentityEndpoint)
                .credentials(identity, credential)
                .modules(neutronModules)
                .overrides(neutronOverrides)
                .build();
        
    	Iterable<Module> novaModules = ImmutableSet.<Module>of(new SLF4JLoggingModule(),  ContextLinking.linkContext(neutronContext), new OkHttpCommandExecutorServiceModule());
        final Properties novaOverrides = new Properties();
        novaOverrides.put(KeystoneProperties.KEYSTONE_VERSION, "3");
        novaOverrides.put(KeystoneProperties.SCOPE, "project:" + osProjectName);
       

        NovaApi novaApi = ContextBuilder.newBuilder("openstack-nova")
                .endpoint(osIdentityEndpoint)
                .credentials(identity, credential)
                .modules(novaModules)
                .overrides(novaOverrides)
                .buildApi(NovaApi.class);
        
        return new OpenstackAPIs(neutronContext.getApi(), novaApi);
    }
    

    @Bean
    public SSHClient sshClient() {
        return new SSHClient();
    }

    @Bean
    public IptNodeFactory iptNodeFactory(OpenstackAPIs openstackAPIs) {
        IptNodeFactory iptNodeFactory = new IptNodeFactory(maxPoolSize, openstackAPIs,
                ProvisioningConfig.builder().defaultNodeFlavor(nodeFlavorName).floatingIpPool(floatingIpPool).nodeImageId(nodeImageId)
                        .sshUser(sshUsername).securityGroupName(securityGroupName).networkId(networkId).nfsHost(nfsHost)
                        .additionalNfsMounts(additionalNfsMounts).provisionFloatingIp(provisionFloatingIp)
                        .insecureRegistries(insecureRegistries).build(),
                keypairRepository);
        iptNodeFactory.init();
        return iptNodeFactory;
    }
}
