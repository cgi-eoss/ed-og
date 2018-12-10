package com.cgi.eoss.osiris.api.controllers;

import com.cgi.eoss.osiris.api.ApiConfig;
import com.cgi.eoss.osiris.api.ApiTestConfig;
import com.cgi.eoss.osiris.model.CostingExpression;
import com.cgi.eoss.osiris.model.OsirisService;
import com.cgi.eoss.osiris.model.JobConfig;
import com.cgi.eoss.osiris.model.Role;
import com.cgi.eoss.osiris.model.User;
import com.cgi.eoss.osiris.persistence.service.CostingExpressionDataService;
import com.cgi.eoss.osiris.persistence.service.JobConfigDataService;
import com.cgi.eoss.osiris.persistence.service.ServiceDataService;
import com.cgi.eoss.osiris.persistence.service.UserDataService;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ApiConfig.class, ApiTestConfig.class})
@AutoConfigureMockMvc
@TestPropertySource("classpath:test-api.properties")
@Transactional
public class EstimateCostApiIT {
    @Autowired
    private UserDataService dataService;

    @Autowired
    private ServiceDataService serviceDataService;

    @Autowired
    private JobConfigDataService jobConfigDataService;

    @Autowired
    private CostingExpressionDataService costingExpressionDataService;

    @Autowired
    private MockMvc mockMvc;

    private User osirisAdmin;

    @Before
    public void setUp() {
        osirisAdmin = new User("osiris-admin");
        osirisAdmin.setRole(Role.ADMIN);

        dataService.save(ImmutableSet.of(osirisAdmin));
    }

    @Test
    public void testEstimateJobConfigCost() throws Exception {
        OsirisService svc = new OsirisService("osirisService", osirisAdmin, "dockerTag");
        serviceDataService.save(svc);
        JobConfig jobConfig = new JobConfig();
        jobConfig.setOwner(osirisAdmin);
        jobConfig.setService(svc);
        jobConfigDataService.save(jobConfig);

        mockMvc.perform(get("/api/estimateCost/jobConfig/" + jobConfig.getId()).header("REMOTE_USER", osirisAdmin.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedCost").value(1))
                .andExpect(jsonPath("$.currentWalletBalance").value(greaterThan(1)));

        CostingExpression costingExpression = CostingExpression.builder()
                .type(CostingExpression.Type.SERVICE)
                .associatedId(svc.getId())
                .costExpression("1")
                .estimatedCostExpression("service.name.length() * 20")
                .build();
        costingExpressionDataService.save(costingExpression);

        mockMvc.perform(get("/api/estimateCost/jobConfig/" + jobConfig.getId()).header("REMOTE_USER", osirisAdmin.getName()))
                .andExpect(status().is(402))
                .andExpect(jsonPath("$.estimatedCost").value(260))
                .andExpect(jsonPath("$.currentWalletBalance").value(lessThan(260)));
    }

}
