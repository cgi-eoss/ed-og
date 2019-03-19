package com.cgi.eoss.osiris.catalogue.files;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.geojson.Feature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import com.cgi.eoss.osiris.catalogue.CatalogueUri;
import com.cgi.eoss.osiris.catalogue.geoserver.GeoServerSpec;
import com.cgi.eoss.osiris.catalogue.geoserver.GeoserverService;
import com.cgi.eoss.osiris.catalogue.resto.RestoService;
import com.cgi.eoss.osiris.catalogue.util.GeoUtil;
import com.cgi.eoss.osiris.model.Collection;
import com.cgi.eoss.osiris.model.GeoserverLayer;
import com.cgi.eoss.osiris.model.OsirisFile;
import com.cgi.eoss.osiris.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.google.common.io.MoreFiles;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class FilesystemOutputProductService implements OutputProductService {
    private final Path outputProductBasedir;
    private final RestoService resto;
    private final GeoserverService geoserver;
    private final ObjectMapper jsonMapper;

    @Autowired
    public FilesystemOutputProductService(@Qualifier("outputProductBasedir") Path outputProductBasedir, RestoService resto, GeoserverService geoserver, ObjectMapper jsonMapper) {
        this.outputProductBasedir = outputProductBasedir;
        this.resto = resto;
        this.geoserver = geoserver;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public String getDefaultCollection() {
        return resto.getOutputProductsCollection();
    }
    
    @Override
    public OsirisFile ingest(String collection, User owner, String jobId, String crs, String geometry, OffsetDateTime startDateTime, OffsetDateTime endDateTime, Map<String, Object> properties, Path src) throws IOException {
        Path dest = outputProductBasedir.resolve(jobId).resolve(src);
        if (!src.equals(dest)) {
            if (Files.exists(dest)) {
                LOG.warn("Found already-existing output product, overwriting: {}", dest);
            }

            Files.createDirectories(dest.getParent());
            Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        LOG.info("Ingesting output at {}", dest);

        Path relativePath = outputProductBasedir.resolve(jobId).relativize(src);

        URI uri = CatalogueUri.OUTPUT_PRODUCT
                        .build(ImmutableMap.of("jobId", jobId, "filename", relativePath.toString().replaceAll(File.pathSeparator, "_")));
        long filesize = Files.size(dest);
        // Add automatically-determined properties
        properties.put("productIdentifier", jobId + "_" + relativePath.toString());
        properties.put("osirisUrl", uri);
        if (startDateTime != null) {
            properties.put("startDate", startDateTime.toString());
        }
        if (endDateTime != null) {
            properties.put("completionDate", endDateTime.toString());
        }
        // TODO Get the proper MIME type
        properties.put("resourceMimeType", "application/unknown");
        properties.put("resourceSize", Files.size(dest));
        properties.put("filename", relativePath.toFile().getName());
        properties.put("resourceChecksum", "sha256=" + MoreFiles.asByteSource(dest).hash(Hashing.sha256()));
        Map<String, Object> extraProperties;
        if ((properties.get("extraParams") == null)) {
            // TODO Add local extra properties if needed
            extraProperties = ImmutableMap.of();

        } else {
            Map<String, Object> existingExtraProperties = (Map<String, Object>) properties.get("extraParams");
            extraProperties = new HashMap<String, Object>();
            extraProperties.putAll(existingExtraProperties);
        }
        properties.put("extraParams", extraProperties);
        Feature feature = new Feature();
        feature.setId(jobId + "_" + relativePath.toString().replaceAll(File.pathSeparator, "_"));
        feature.setGeometry(Strings.isNullOrEmpty(geometry) ? GeoUtil.defaultGeometry() : GeoUtil.getGeoJsonGeometry(geometry));
        feature.setProperties(properties);

        UUID restoId;
        try {
            restoId = resto.ingestOutputProduct(collection, feature);
            LOG.info("Ingested output product with Resto id {} and URI {}", restoId, uri);
        } catch (Exception e) {
            LOG.error("Failed to ingest output product to Resto, continuing...", e);
            // TODO Add GeoJSON to OsirisFile model
            restoId = UUID.randomUUID();
        }
        
        GeoserverLayer geoserverLayer = null;
        if (properties.containsKey("geoServerSpec")) {
            GeoServerSpec geoServerSpec = (GeoServerSpec) properties.get("geoServerSpec");
            try {
                geoserverLayer = geoserver.ingest(dest, geoServerSpec, restoId);
                geoserverLayer.setOwner(owner);
            } catch (Exception e) {
                LOG.error("Failed to ingest output product to GeoServer, continuing...", e);
            }
        }

        OsirisFile osirisFile = new OsirisFile(uri, restoId);
        osirisFile.setOwner(owner);
        osirisFile.setFilesize(filesize);
        osirisFile.setType(OsirisFile.Type.OUTPUT_PRODUCT);
        osirisFile.setFilename(outputProductBasedir.relativize(dest).toString());
        if (geoserverLayer != null) {
            osirisFile.getGeoserverLayers().add(geoserverLayer);
        }
        return osirisFile;
    }

    @Override
    public Path provision(String jobId, String filename) throws IOException {
        Path outputPath = outputProductBasedir.resolve(jobId).resolve(filename);
        if (Files.exists(outputPath)) {
            LOG.warn("Found already-existing output product, may be overwritten: {}", outputPath);
        }
        Files.createDirectories(outputPath.getParent());
        return outputPath;
    }

    @Override
    public Set<Link> getOGCLinks(OsirisFile osirisFile) {
        Set<Link> links = new HashSet<Link>();
        for (GeoserverLayer geoserverLayer : osirisFile.getGeoserverLayers()) {
            switch (geoserverLayer.getStoreType()) {
                case MOSAIC:
                    links.add(new Link(getWMSLinkToFileInMosaic(osirisFile, geoserverLayer), "wms"));
                    break;
                case GEOTIFF:
                    links.add(new Link(getWMSLinkToLayer(osirisFile, geoserverLayer), "wms"));
                    break;
                case POSTGIS:
                    links.add(new Link(getWMSLinkToLayer(osirisFile, geoserverLayer), "wms"));
                    links.add(new Link(getWFSLinkToLayer(osirisFile, geoserverLayer), "wfs"));
                    break;
            }
        }
        return links;
    }

    private String getWMSLinkToFileInMosaic(OsirisFile osirisFile, GeoserverLayer geoserverLayer) {
        OffsetDateTime start = OffsetDateTime.of(LocalDateTime.of(LocalDate.ofEpochDay(0), LocalTime.MIDNIGHT), ZoneOffset.UTC);
        OffsetDateTime end = start.plusYears(140);
        
        return geoserver.getExternalUrl().newBuilder().addPathSegment(geoserverLayer.getWorkspace()).addPathSegment("wms")
                        .addQueryParameter("service", "WMS").addQueryParameter("version", "1.1.0")
                        .addQueryParameter("layers", geoserverLayer.getWorkspace() + ":" + geoserverLayer.getLayer())
                        .addQueryParameter("time", DateTimeFormatter.ISO_INSTANT.format(start) + "/" + DateTimeFormatter.ISO_INSTANT.format(end))
                        .addQueryParameter("cql_filter", "(location LIKE '%" + osirisFile.getFilename() + "%')")
                        .build().toString();
    }

    private String getWMSLinkToLayer(OsirisFile osirisFile, GeoserverLayer geoserverLayer) {
        return geoserver.getExternalUrl().newBuilder().addPathSegment(geoserverLayer.getWorkspace()).addPathSegment("wms")
                        .addQueryParameter("service", "WMS").addQueryParameter("version", "1.1.0")
                        .addQueryParameter("layers", geoserverLayer.getWorkspace() + ":" + geoserverLayer.getLayer())
                        .build().toString();
        
        
    }

    private String getWFSLinkToLayer(OsirisFile osirisFile, GeoserverLayer geoserverLayer) {
        return geoserver.getExternalUrl().newBuilder().addPathSegment(geoserverLayer.getWorkspace()).addPathSegment("wfs")
                        .addQueryParameter("service", "WFS").addQueryParameter("version", "1.0.0")
                        .addQueryParameter("typeName", geoserverLayer.getWorkspace() + ":" + geoserverLayer.getLayer())                        
                        .addQueryParameter("cql_filter", "(osiris_id  = '" + osirisFile.getRestoId() + "')")
                        .build().toString();
    }

    @Override
    public Resource resolve(OsirisFile file) {
        Path path = outputProductBasedir.resolve(file.getFilename());
        return new PathResource(path);
    }

    @Override
    public void delete(OsirisFile file) throws IOException {
        Path relativePath = Paths.get(file.getFilename());

        Files.deleteIfExists(outputProductBasedir.resolve(relativePath));

        resto.deleteOutputProduct(file.getRestoId());
        for (GeoserverLayer geoserverLayer: file.getGeoserverLayers()) {
            cleanUpGeoserverLayer(file, geoserverLayer);
        }
    }

    private void cleanUpGeoserverLayer(OsirisFile file, GeoserverLayer geoserverLayer) {
        switch (geoserverLayer.getStoreType()) {
            case GEOTIFF: {
                geoserver.deleteLayer(geoserverLayer.getWorkspace(), geoserverLayer.getLayer());
                geoserver.deleteCoverageStore(geoserverLayer.getWorkspace(), geoserverLayer.getStore());
                break;
            }
            case MOSAIC: {
                geoserver.deleteGranuleFromMosaic(geoserverLayer.getWorkspace(), geoserverLayer.getStore(), file.getFilename());
            }
            case POSTGIS:
                //Simply delete the layer - update of Postgis table is not supported in geoserver
                geoserver.deleteLayer(geoserverLayer.getWorkspace(), geoserverLayer.getLayer());
            
        }
        
    }

    @Override
    public boolean createCollection(Collection collection) {
        return resto.createOutputCollection(collection);

    }

    @Override
    public boolean deleteCollection(Collection collection) {
        return resto.deleteCollection(collection);

    }

}
