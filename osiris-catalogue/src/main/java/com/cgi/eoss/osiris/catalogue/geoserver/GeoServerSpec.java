package com.cgi.eoss.osiris.catalogue.geoserver;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeoServerSpec {

    private String workspace;

    private GeoServerType geoserverType;
    
    private String datastoreName;
    
    private String coverageName;
    
    private String layerName;
    
    private String crs;
    
    private String style;
}
