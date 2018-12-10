package com.cgi.eoss.osiris.api;

import static org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy.RepositoryDetectionStrategies.ANNOTATED;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcRegistrationsAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.cgi.eoss.osiris.catalogue.CatalogueConfig;
import com.cgi.eoss.osiris.costing.CostingConfig;
import com.cgi.eoss.osiris.model.Databasket;
import com.cgi.eoss.osiris.model.OsirisFile;
import com.cgi.eoss.osiris.model.OsirisService;
import com.cgi.eoss.osiris.model.OsirisServiceContextFile;
import com.cgi.eoss.osiris.model.OsirisServiceTemplate;
import com.cgi.eoss.osiris.model.OsirisServiceTemplateFile;
import com.cgi.eoss.osiris.model.Group;
import com.cgi.eoss.osiris.model.Job;
import com.cgi.eoss.osiris.model.JobConfig;
import com.cgi.eoss.osiris.model.Project;
import com.cgi.eoss.osiris.model.User;
import com.cgi.eoss.osiris.orchestrator.OrchestratorConfig;
import com.cgi.eoss.osiris.persistence.PersistenceConfig;
import com.cgi.eoss.osiris.rpc.InProcessRpcConfig;
import com.cgi.eoss.osiris.search.SearchConfig;
import com.cgi.eoss.osiris.security.SecurityConfig;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableList;

@Configuration
@Import({
        PropertyPlaceholderAutoConfiguration.class,
        RepositoryRestMvcAutoConfiguration.class,
        WebMvcAutoConfiguration.EnableWebMvcConfiguration.class,

        CatalogueConfig.class,
        CostingConfig.class,
        InProcessRpcConfig.class,
        OrchestratorConfig.class,
        PersistenceConfig.class,
        SearchConfig.class,
        SecurityConfig.class
})
@EnableJpaRepositories(basePackageClasses = ApiConfig.class)
@EnableWebMvc
@ComponentScan(basePackageClasses = ApiConfig.class)
public class ApiConfig {

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .modulesToInstall(
                        new GuavaModule(),
                        new Hibernate5Module(),
                        new JavaTimeModule());
    }

    @Bean
    public WebMvcRegistrationsAdapter webMvcRegistrationsHandlerMapping(@Value("${osiris.api.basePath:/api}") String apiBasePath) {
        return new WebMvcRegistrationsAdapter() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new RequestMappingHandlerMapping() {
                    @Override
                    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
                        Class<?> beanType = method.getDeclaringClass();
                        RestController restApiController = beanType.getAnnotation(RestController.class);
                        if (restApiController != null) {
                            PatternsRequestCondition apiPattern = new PatternsRequestCondition(apiBasePath).combine(mapping.getPatternsCondition());

                            mapping = new RequestMappingInfo(mapping.getName(), apiPattern,
                                    mapping.getMethodsCondition(), mapping.getParamsCondition(),
                                    mapping.getHeadersCondition(), mapping.getConsumesCondition(),
                                    mapping.getProducesCondition(), mapping.getCustomCondition());
                        }

                        super.registerHandlerMethod(handler, method, mapping);
                    }
                };
            }
        };
    }
    
  
    @Bean
    public RepositoryRestConfigurer repositoryRestConfigurer(@Value("${osiris.api.basePath:/api}") String apiBasePath) {
        return new RepositoryRestConfigurerAdapter() {
            @Override
            public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
                config.setRepositoryDetectionStrategy(ANNOTATED);
                config.setBasePath(apiBasePath);
                config.setDefaultMediaType(MediaTypes.HAL_JSON);
                // Ensure that the id attribute is returned for all API-mapped types
                ImmutableList.of(Group.class, JobConfig.class, Job.class, OsirisService.class, OsirisServiceContextFile.class, OsirisServiceTemplate.class, OsirisServiceTemplateFile.class, User.class, OsirisFile.class, Databasket.class, Project.class)
                        .forEach(config::exposeIdsFor);
            }
        };
    }
    
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurerAdapter() {
		    @Override
            public void configureContentNegotiation(ContentNegotiationConfigurer contentNegotiationConfigurer) {
		    	contentNegotiationConfigurer.defaultContentType(MediaTypes.HAL_JSON);
            }
        };
    }
    

    @Bean
    @ConditionalOnProperty(value = "osiris.api.security.cors.enabled", havingValue = "true", matchIfMissing = true)
    public CorsConfigurationSource corsConfigurationSource(@Value("${osiris.api.basePath:/api}") String apiBasePath) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration(apiBasePath + "/**", config);
        return source;
    }

    @Bean
    @ConditionalOnProperty(value = "osiris.api.security.cors.enabled", havingValue = "false")
    public CorsConfigurationSource disabledCorsConfiguration(@Value("${osiris.api.basePath:/api}") String apiBasePath) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        source.registerCorsConfiguration(apiBasePath + "/**", config);
        return source;
    }

    // Spring Security configuration for anonymous/open access
    @Bean
    @ConditionalOnProperty(value = "osiris.api.security.mode", havingValue = "NONE", matchIfMissing = true)
    public WebSecurityConfigurerAdapter webSecurityConfigurerAdapter() {
        return new WebSecurityConfigurerAdapter() {
            @Override
            protected void configure(HttpSecurity httpSecurity) throws Exception {
                httpSecurity
                        .authorizeRequests()
                        .anyRequest().anonymous();
                httpSecurity
                        .csrf().disable();
                httpSecurity
                        .cors();
                httpSecurity
                        .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            }
        };
    }

}
