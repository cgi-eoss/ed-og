package com.cgi.eoss.osiris.search;

import com.cgi.eoss.osiris.catalogue.CatalogueConfig;
import com.cgi.eoss.osiris.persistence.PersistenceConfig;
import com.cgi.eoss.osiris.search.api.SearchFacade;
import com.cgi.eoss.osiris.search.api.SearchProvider;
import com.cgi.eoss.osiris.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collection;

@Configuration
@Import({
        PropertyPlaceholderAutoConfiguration.class,

        CatalogueConfig.class,
        PersistenceConfig.class,
        SecurityConfig.class
})
@ComponentScan(basePackageClasses = SearchConfig.class)
public class SearchConfig {

    @Value("${osiris.search.parametersFile:}")
    private String searchParametersFile;

    @Bean
    public SearchFacade searchFacade(Collection<SearchProvider> searchProviders) {
        return new SearchFacade(searchProviders, searchParametersFile);
    }

    @Bean
    public OkHttpClient httpClient() {
        return new OkHttpClient.Builder().build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModules(new GuavaModule(), new JavaTimeModule());
    }

}
