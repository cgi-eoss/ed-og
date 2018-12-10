package com.cgi.eoss.osiris.api.controllers;

import com.cgi.eoss.osiris.api.ApiConfig;
import com.cgi.eoss.osiris.api.ApiTestConfig;
import com.cgi.eoss.osiris.model.OsirisService;
import com.cgi.eoss.osiris.model.OsirisServiceContextFile;
import com.cgi.eoss.osiris.model.Role;
import com.cgi.eoss.osiris.model.User;
import com.cgi.eoss.osiris.persistence.service.ServiceDataService;
import com.cgi.eoss.osiris.persistence.service.ServiceFileDataService;
import com.cgi.eoss.osiris.persistence.service.UserDataService;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.BaseEncoding;
import com.jayway.jsonpath.JsonPath;
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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ApiConfig.class, ApiTestConfig.class})
@AutoConfigureMockMvc
@TestPropertySource("classpath:test-api.properties")
@Transactional
public class ServiceFilesApiIT {

    @Autowired
    private ServiceDataService serviceDataService;

    @Autowired
    private ServiceFileDataService serviceFileDataService;

    @Autowired
    private UserDataService userDataService;

    @Autowired
    private MockMvc mockMvc;

    private User osirisUser;
    private User osirisExpertUser;
    private User osirisContentAuthority;
    private OsirisService svc;

    @Before
    public void setUp() {
        osirisUser = new User("osiris-user");
        osirisUser.setRole(Role.USER);
        osirisExpertUser = new User("osiris-expert-user");
        osirisExpertUser.setRole(Role.EXPERT_USER);
        osirisContentAuthority = new User("osiris-content-authority");
        osirisContentAuthority.setRole(Role.CONTENT_AUTHORITY);

        userDataService.save(ImmutableSet.of(osirisUser, osirisExpertUser, osirisContentAuthority));

        svc = new OsirisService("service-1", osirisExpertUser, "dockerTag");
        svc.setStatus(OsirisService.Status.AVAILABLE);
        serviceDataService.save(svc);
    }

    @Test
    public void testSave() throws Exception {
        String svcUrl = getServiceUrl();

        byte[] testFile1Bytes = Files.readAllBytes(Paths.get(ServiceFilesApiIT.class.getResource("/testFile1").toURI()));
        String testFile1b64 = BaseEncoding.base64().encode(testFile1Bytes);
        byte[] testFile2Bytes = Files.readAllBytes(Paths.get(ServiceFilesApiIT.class.getResource("/testFile2").toURI()));
        String testFile2b64 = BaseEncoding.base64().encode(testFile2Bytes);

        String newFile1 = "{\"filename\": \"testFile1\", \"content\": \"" + testFile1b64 + "\", \"service\": \"" + svcUrl + "\"}";
        String newFile2 = "{\"filename\": \"testFile2\", \"content\": \"" + testFile2b64 + "\", \"service\": \"" + svcUrl + "\"}";

        mockMvc.perform(post("/api/serviceFiles").header("REMOTE_USER", osirisExpertUser.getName()).content(newFile1))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/serviceFiles/\\d+$")));
        mockMvc.perform(post("/api/serviceFiles").header("REMOTE_USER", osirisExpertUser.getName()).content(newFile2))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/serviceFiles/\\d+$")));

        List<OsirisServiceContextFile> serviceFiles = serviceFileDataService.findByService(svc);
        assertThat(serviceFiles.size(), is(2));
        assertThat(serviceFiles.get(0).getContent(), is(new String(testFile1Bytes)));
        assertThat(serviceFiles.get(1).getContent(), is(new String(testFile2Bytes)));
    }

    @Test
    public void testFindByService() throws Exception {
        // Shortcut to populate data
        testSave();
        String svcUrl = getServiceUrl();

        // Results are filtered by ACL
        mockMvc.perform(get("/api/serviceFiles/search/findByService").param("service", svcUrl).header("REMOTE_USER", osirisUser.getName()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/serviceFiles/search/findByService").param("service", svcUrl).header("REMOTE_USER", osirisContentAuthority.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.serviceFiles").isArray())
                .andExpect(jsonPath("$._embedded.serviceFiles.length()").value(2));
    }

    private String getServiceUrl() throws Exception {
        return JsonPath.compile("$._links.self.href").read(
                mockMvc.perform(get("/api/services/" + svc.getId()).header("REMOTE_USER", osirisContentAuthority.getName()))
                        .andReturn().getResponse().getContentAsString()
        );
    }

}