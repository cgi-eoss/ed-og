package com.cgi.eoss.osiris.catalogue.geoserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.cgi.eoss.osiris.catalogue.IngestionException;
import com.cgi.eoss.osiris.catalogue.geoserver.model.Coverage;
import com.cgi.eoss.osiris.catalogue.geoserver.model.CoverageConfig;
import com.cgi.eoss.osiris.catalogue.geoserver.model.Coverages;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.HTTPUtils;
import it.geosolutions.geoserver.rest.decoder.RESTCoverageStore;
import it.geosolutions.geoserver.rest.manager.GeoServerRESTAbstractManager;

public class GeoserverMosaicManager extends GeoServerRESTAbstractManager {

	private String postgisHost;
	
	private int postgisPort;
	
	private String postgisDb;
	
	private String postgisUsername;
	
	private String postgisPassword;

	private GeoServerRESTReader reader;
	
	private static XmlMapper XML_MAPPER = new XmlMapper();
	
	public GeoserverMosaicManager(GeoServerRESTReader reader, URL url, String username, String password, String postgisHost, int postgisPort, String postgisDb, String postgisUsername, String postgisPassword) {
		super(url, username, password);
		this.reader = reader;
		this.postgisHost = postgisHost;
		this.postgisPort = postgisPort;
		this.postgisDb = postgisDb;
		this.postgisUsername = postgisUsername;
		this.postgisPassword = postgisPassword;
	}

	/**
	 * Creates an empty mosaic with a single coverage that will not be preconfigured at creation time.
	 * Such a coverage can be later configured after a granule has been added with the method {@link com.cgi.eoss.osiris.catalogue.geoserver.GeoserverMosaicManager#addGeoTiffToExternalMosaic(String, String, File)}
	 * , using the method {@link com.cgi.eoss.osiris.catalogue.geoserver.GeoserverMosaicManager#createCoverageIfNotExists(String, String, String)}
	 * */
	public void createEmptyMosaic(String workspace, String storeName, String coverageName, String timeRegexp) {
		if (!reader.existsWorkspace(workspace)) {
			throw new IngestionException("Workspace does not exist");
		}
		
		if (reader.existsCoveragestore(workspace, storeName)) {
			throw new IngestionException("Coverage store already exists");
		}
		
		String sUrl = gsBaseUrl + "/rest/workspaces/" + workspace + "/coveragestores/" + storeName
				+ "/file.imagemosaic?configure=none";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipOutputStream zos = new ZipOutputStream(baos)) {
			Properties timeregexProperties = new Properties();
			timeregexProperties.load(getClass().getResourceAsStream("timeregex.properties"));
			timeregexProperties.setProperty("regex", timeRegexp);
			ByteArrayOutputStream timeRegexBaos = new ByteArrayOutputStream();
	
			timeregexProperties.store(timeRegexBaos, "");
			ZipEntry timeRegexEntry = new ZipEntry("timeregex.properties");
			zos.putNextEntry(timeRegexEntry);
			zos.write(timeRegexBaos.toByteArray());
			zos.closeEntry();
	
			Properties indexerProperties = new Properties();
			indexerProperties.load(getClass().getResourceAsStream("indexer.properties"));
			indexerProperties.setProperty("Name", workspace + "_" + storeName + "_" + coverageName);
			ByteArrayOutputStream indexerBaos = new ByteArrayOutputStream();
	
			indexerProperties.store(indexerBaos, "");
			ZipEntry indexerEntry = new ZipEntry("indexer.properties");
			zos.putNextEntry(indexerEntry);
			zos.write(indexerBaos.toByteArray());
			zos.closeEntry();
	
			Properties datastoreProperties = new Properties();
			datastoreProperties.load(getClass().getResourceAsStream("datastore.properties"));
			datastoreProperties.setProperty("host", postgisHost);
			datastoreProperties.setProperty("port", String.valueOf(postgisPort));
			datastoreProperties.setProperty("database", postgisDb);
			datastoreProperties.setProperty("user", postgisUsername);
			datastoreProperties.setProperty("passwd", postgisPassword);
			
			ByteArrayOutputStream datastoreBaos = new ByteArrayOutputStream();
			datastoreProperties.store(datastoreBaos, "");
			ZipEntry dataStoreEntry = new ZipEntry("datastore.properties");
			zos.putNextEntry(dataStoreEntry);
			zos.write(datastoreBaos.toByteArray());
			zos.closeEntry();
			
			Coverages coverages = new Coverages();
			Coverage coverage = new Coverage();
			coverage.setName(coverageName);
			coverages.getCoverage().add(coverage);
			XML_MAPPER.writeValueAsBytes(coverages);
			ZipEntry covEntry = new ZipEntry("coverages.xml");
			zos.putNextEntry(covEntry);
			zos.write(XML_MAPPER.writeValueAsBytes(coverages));
			zos.closeEntry();
	
	
		} catch (IOException ioe) {
			throw new IngestionException("Cannot create empty mosaic");
		}
		ByteArrayRequestEntity entity = new ByteArrayRequestEntity(baos.toByteArray(), "application/zip");
		String sendResult = HTTPUtils.put(sUrl, entity, gsuser, gspass);
		if (sendResult == null) {
			throw new IngestionException("Cannot create empty mosaic");
		}
	}

	public RESTCoverageStore addGeoTiffToExternalMosaic(String workspace, final String storeName, File geoTiff)
			throws FileNotFoundException, IllegalArgumentException {
		String sUrl = gsBaseUrl + "/rest/workspaces/" + workspace + "/coveragestores/" + storeName
				+ "/external.imagemosaic";
		String sendResult = HTTPUtils.post(sUrl, geoTiff.toURI().toString(), "text/plain", gsuser, gspass);
		return RESTCoverageStore.build(sendResult);
	}

	public void createCoverageIfNotExists(String workspace, String storeName, String coverageName) throws IOException{
		// TODO Generate the coverage config
		String sUrl = gsBaseUrl + "/rest/workspaces/" + workspace + "/coveragestores/" + storeName
				+ "/coverages.xml";
		String sendResult = HTTPUtils.get(sUrl, gsuser, gspass);
		Coverages coverages = XML_MAPPER.readValue(sendResult, Coverages.class);
		Optional<Coverage> coverage = coverages.getCoverage().stream().filter(c -> c.getName().equals(coverageName)).findFirst();
		if (coverage.isPresent())
			return;
		
		String coveragesUrl = gsBaseUrl + "/rest/workspaces/" + workspace + "/coveragestores/" + storeName + "/coverages";
		
		CoverageConfig coverageConfig = XML_MAPPER.readValue(getClass().getResourceAsStream("coverageconfig.xml"), CoverageConfig.class);
		
		coverageConfig.setName(coverageName);
		coverageConfig.setNativeCoverageName(coverageName);
		System.out.println(XML_MAPPER.writeValueAsString(coverageConfig));
		String postResult = HTTPUtils.post(coveragesUrl, XML_MAPPER.writeValueAsString(coverageConfig), "text/xml", gsuser, gspass);
		if (postResult == null) {
			throw new IngestionException("Cannot create coverage");
		}
	}

	
    public void deleteGranuleFromMosaic(String workspace, String storeName, String location) {
        // TODO Implement this
        
    }
	
}
