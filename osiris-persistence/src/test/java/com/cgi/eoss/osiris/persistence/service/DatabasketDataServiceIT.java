package com.cgi.eoss.osiris.persistence.service;

import com.cgi.eoss.osiris.model.Databasket;
import com.cgi.eoss.osiris.model.OsirisFile;
import com.cgi.eoss.osiris.model.User;
import com.cgi.eoss.osiris.persistence.PersistenceConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceConfig.class})
@TestPropertySource("classpath:test-persistence.properties")
@Transactional
public class DatabasketDataServiceIT {
    @Autowired
    private DatabasketDataService dataService;
    @Autowired
    private UserDataService userService;
    @Autowired
    private OsirisFileDataService fileService;

    @Test
    public void test() throws Exception {
        User owner = new User("owner-uid");
        User owner2 = new User("owner-uid2");
        userService.save(ImmutableSet.of(owner, owner2));

        OsirisFile osirisFile = new OsirisFile();
        osirisFile.setUri(URI.create("osiris://osirisFile"));
        osirisFile.setRestoId(UUID.randomUUID());
        osirisFile.setOwner(owner);

        OsirisFile osirisFile2 = new OsirisFile();
        osirisFile2.setUri(URI.create("osiris://osirisFile2"));
        osirisFile2.setRestoId(UUID.randomUUID());
        osirisFile2.setOwner(owner);

        fileService.save(ImmutableSet.of(osirisFile, osirisFile2));

        Databasket databasket = new Databasket();
        databasket.setName("Test Databasket");
        databasket.setOwner(owner);
        databasket.setFiles(ImmutableSet.of(osirisFile, osirisFile2));

        Databasket databasket2 = new Databasket();
        databasket2.setName("Test Databasket2");
        databasket2.setOwner(owner);
        databasket2.setFiles(ImmutableSet.of(osirisFile2));

        dataService.save(ImmutableSet.of(databasket, databasket2));

        assertThat(dataService.getAll(), is(ImmutableList.of(databasket, databasket2)));
        assertThat(dataService.getById(databasket.getId()), is(databasket));
        assertThat(dataService.getByIds(ImmutableSet.of(databasket.getId())), is(ImmutableList.of(databasket)));
        assertThat(dataService.isUniqueAndValid(new Databasket("Test Databasket", owner)), is(false));
        assertThat(dataService.isUniqueAndValid(new Databasket("Test Databasket3", owner)), is(true));
        assertThat(dataService.isUniqueAndValid(new Databasket("Test Databasket", owner2)), is(true));

        assertThat(dataService.search("databasket"), containsInAnyOrder(databasket, databasket2));
        assertThat(dataService.search("databasket2"), is(ImmutableList.of(databasket2)));
        assertThat(dataService.getByNameAndOwner("Test Databasket", owner), is(databasket));
        assertThat(dataService.findByOwner(owner), containsInAnyOrder(databasket2, databasket));
        assertThat(dataService.findByOwner(owner2), is(ImmutableList.of()));
        assertThat(dataService.findByFile(osirisFile), is(ImmutableList.of(databasket)));
        assertThat(dataService.findByFile(osirisFile2), containsInAnyOrder(databasket, databasket2));
    }

}
