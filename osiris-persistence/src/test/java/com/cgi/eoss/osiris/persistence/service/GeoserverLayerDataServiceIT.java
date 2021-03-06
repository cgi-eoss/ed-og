package com.cgi.eoss.osiris.persistence.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import com.cgi.eoss.osiris.model.GeoserverLayer;
import com.cgi.eoss.osiris.model.GeoserverLayer.StoreType;
import com.cgi.eoss.osiris.model.OsirisFile;
import com.cgi.eoss.osiris.model.User;
import com.cgi.eoss.osiris.persistence.PersistenceConfig;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceConfig.class})
@TestPropertySource("classpath:test-persistence.properties")
@Transactional
public class GeoserverLayerDataServiceIT {
    @Autowired
    private OsirisFileDataService fileDataService;
    
    @Autowired
    private GeoserverLayerDataService geoserverLayerDataService;
    
    
    @Autowired
    private UserDataService userService;

    @Test
    public void testSaveGeoserverLayer() throws Exception {
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
        osirisFile2.setOwner(owner2);

        GeoserverLayer geoserverLayer = new GeoserverLayer(owner, "test", "test", StoreType.MOSAIC);
        osirisFile.getGeoserverLayers().add(geoserverLayer);
        fileDataService.syncGeoserverLayersAndSave(osirisFile);
        fileDataService.save(ImmutableSet.of(osirisFile2));
        assertThat(geoserverLayerDataService.getAll().size(), is (1));
        GeoserverLayer layer = geoserverLayerDataService.getAll().get(0);
        assertThat(layer.getFiles().size(), is (1));
    }
    
    
    @Test
    public void testUpdateGeoserverLayer() throws Exception {
        User owner = new User("owner-uid");
        userService.save(ImmutableSet.of(owner));

        OsirisFile osirisFile1 = new OsirisFile();
        osirisFile1.setUri(URI.create("osiris://osirisFile"));
        osirisFile1.setRestoId(UUID.randomUUID());
        osirisFile1.setOwner(owner);
        
        GeoserverLayer geoserverLayer1 = new GeoserverLayer(owner, "test", "test", StoreType.MOSAIC);
        osirisFile1.getGeoserverLayers().add(geoserverLayer1);
        
        fileDataService.syncGeoserverLayersAndSave(osirisFile1);
        
        assertThat(geoserverLayerDataService.getAll().size(), is (1));
        GeoserverLayer layer = geoserverLayerDataService.getAll().get(0);
        layer = geoserverLayerDataService.refreshFull(layer);
        assertThat(layer.getFiles().size(), is (1));
       
        OsirisFile osirisFile2 = new OsirisFile();
        osirisFile2.setUri(URI.create("osiris://osirisFile2"));
        osirisFile2.setRestoId(UUID.randomUUID());
        osirisFile2.setOwner(owner);
        
        GeoserverLayer geoserverLayer2 = new GeoserverLayer(owner, "test", "test", StoreType.POSTGIS);
        osirisFile2.getGeoserverLayers().add(geoserverLayer2);
        fileDataService.syncGeoserverLayersAndSave(osirisFile2);
        assertThat(geoserverLayerDataService.getAll().size(), is (1));
        layer = geoserverLayerDataService.getAll().get(0);
        layer = geoserverLayerDataService.refreshFull(layer);
        assertThat(layer.getFiles().size(), is (2));
    }

    @Test
    public void testDeleteGeoserverLayer() throws Exception {
        User owner = new User("owner-uid");
        userService.save(ImmutableSet.of(owner));

        OsirisFile osirisFile1 = new OsirisFile();
        osirisFile1.setUri(URI.create("osiris://osirisFile"));
        osirisFile1.setRestoId(UUID.randomUUID());
        osirisFile1.setOwner(owner);
        
        GeoserverLayer geoserverLayer1 = new GeoserverLayer(owner, "test", "test", StoreType.GEOTIFF);
        osirisFile1.getGeoserverLayers().add(geoserverLayer1);
        
        geoserverLayerDataService.syncGeoserverLayers(osirisFile1);
        fileDataService.save(ImmutableSet.of(osirisFile1));
        
        assertThat(geoserverLayerDataService.getAll().size(), is (1));
        GeoserverLayer layer = geoserverLayerDataService.getAll().get(0);
        layer = geoserverLayerDataService.refreshFull(layer);
        assertThat(layer.getFiles().size(), is (1));
       
        fileDataService.delete(osirisFile1);
        osirisFile1.getGeoserverLayers().forEach( l -> geoserverLayerDataService.delete(l));
        assertThat(geoserverLayerDataService.getAll().size(), is (0));
        
    }
}