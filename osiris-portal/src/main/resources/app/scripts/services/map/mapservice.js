/**
 * @ngdoc service
 * @name osirisApp.MapService
 * @description
 * # MapService
 * Service in the osirisApp.
 */
define(['../../osirismodules', 'ol'], function (osirismodules, ol) {
    'use strict';

    osirismodules.service('MapService', ['osirisProperties', function (osirisProperties) {

        var mousePositionControl = new ol.control.MousePosition({
            coordinateFormat: function(coord) {
                coord[0] = (coord[0] + 180) % 360 - 180;
                return coord[0].toFixed(4) + ', ' + coord[1].toFixed(4);
            },//ol.coordinate.createStringXY(4),
            projection: 'EPSG:4326',
            undefinedHTML: '&nbsp;'
        });

        var layerOSM = new ol.layer.Tile({
            name: 'Open Street',
            source: new ol.source.OSM()
        });

        var layerMapBox = new ol.layer.Tile({
            name: 'MapBox',
            source: new ol.source.XYZ({
                tileSize: [512, 512],
                url: osirisProperties.MAPBOX_URL
            })
        });

        var map = new ol.Map({
            interactions: ol.interaction.defaults(),
            controls: ol.control.defaults({
                attributionOptions: ({
                    collapsible: false
                })
            }).extend([
                new ol.control.ScaleLine(),
                mousePositionControl
            ]),
            target: document.createElement('div'),
            layers: [layerMapBox],
            view: new ol.View({
                center: ol.proj.fromLonLat([0, 51.28]),
                zoom: 4
            })
        });

        this.getMap = function() {
            return map;
        };

        this.fitExtent = function(extent) {
            let mapSize = map.getSize();
            if (mapSize.some(d => !d)) {
                mapSize = [1000, 1000];
            }
            map.getView().fit(extent, mapSize);
        };

        this.setBackgroundLayer = function(layer) {
            map.getLayers().removeAt(0);
            if(layer === 'OSM') {
                map.getLayers().insertAt(0, layerOSM);
            } else if (layer === 'MB') {
                map.getLayers().insertAt(0, layerMapBox);
            }
        };

        this.getBackgroundLayer = function() {
            return map.getLayers(0).get('name');
        };

    }]);
});
