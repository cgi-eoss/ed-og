/**
 * @ngdoc function
 * @name osirisApp.controller:CommunityManageJobCtrl
 * @description
 * # CommunityManageJobCtrl
 * Controller of the osirisApp
 */

'use strict';

define(['../../../osirismodules', 'ol', 'clipboard'], function (osirismodules, ol, clipboard) {

    osirismodules.controller('CommunityManageSystematicProcessingCtrl', ['CommunityService', 'SystematicService', 'AoiService', 'MapService', 'CommonService', 'MessageService', '$scope', '$location', function (CommunityService, SystematicService, AoiService, MapService, CommonService, MessageService, $scope, $location) {

        /* Get stored Jobs details */
        $scope.systematicParams = SystematicService.params.community;
        $scope.permissions = CommunityService.permissionTypes;
        $scope.item = "Systematic processing";

        /* Filters */
        $scope.toggleSharingFilters = function () {
            $scope.systematicParams.sharedGroupsDisplayFilters = !$scope.systematicParams.sharedGroupsDisplayFilters;
        };

        $scope.quickSharingSearch = function (item) {
            if (item.group.name.toLowerCase().indexOf(
                $scope.systematicParams.sharedGroupsSearchText.toLowerCase()) > -1) {
                return true;
            }
            return false;
        };

        $scope.refreshSystematicProcessing = function() {
            SystematicService.refreshSelectedSystematicProcessing('community');
        };

        $scope.showAreaOnMap = function(aoi) {
            try {
                var polygon = new ol.format.WKT().readGeometry(aoi[0]);
                AoiService.setSearchAoi({
                    geometry: JSON.parse(new ol.format.GeoJSON().writeGeometry(polygon))
                });

                polygon.transform('EPSG:4326', 'EPSG:3857');
                var extent = polygon.getExtent();
                if (extent) {
                    MapService.fitExtent(extent);
                    $location.path('/explorer');
                }
            } catch(e) {
                MessageService.addError('Unable to parse aoi', error);
            }
        }

        $scope.copyToClipboard = function(value) {
            clipboard.copy(value);
        }


    }]);
});

