/**
 * @ngdoc function
 * @name osirisApp.controller:ExplorerCtrl
 * @description
 * # ExplorerCtrl
 * Controller of the osirisApp
 */
'use strict';
define(['../../osirismodules'], function (osirismodules) {

    osirismodules.controller('ExplorerCtrl', ['$scope', '$mdDialog', 'TabService', 'MessageService', 'osirisProperties', 'CommonService', 'CommunityService', '$timeout', function ($scope, $mdDialog, TabService, MessageService, osirisProperties, CommonService, CommunityService, $timeout) {

        /* Set active page */
        $scope.navInfo = TabService.navInfo.explorer;
        $scope.bottombarNavInfo = TabService.navInfo.bottombar;
        $scope.bottomNavTabs = TabService.getBottomNavTabs();

        /* Active session message count */
        $scope.message = {};
        $scope.message.count = MessageService.countMessages();
        $scope.$on('update.messages', function(event) {
            $scope.message.count = MessageService.countMessages();
        });

        /* SIDE BAR */
        $scope.sideNavTabs = TabService.getExplorerSideNavs();

        function showSidebarArea() {
            $scope.navInfo.sideViewVisible = true;
        }

        $scope.hideSidebarArea = function () {
            $scope.navInfo.activeSideNav = undefined;
            $scope.navInfo.sideViewVisible = false;
        };

        $scope.toggleSidebar = function (tab) {
            if($scope.navInfo.activeSideNav === tab) {
                $scope.hideSidebarArea();
            } else {
                $scope.navInfo.activeSideNav = tab;
                showSidebarArea();
            }
        };

        $scope.$on('update.selectedService', function(event) {
            $scope.navInfo.activeSideNav = $scope.sideNavTabs.WORKSPACE;
            showSidebarArea();
        });
        /* END OF SIDE BAR */

        /** BOTTOM BAR **/
        $scope.displayTab = function(tab, allowedToClose) {
            if ($scope.navInfo.activeBottomNav === tab && allowedToClose !== false) {
                $scope.toggleBottomView();
            } else {
                $scope.bottombarNavInfo.bottomViewVisible = true;
                $timeout(function () {
                    $scope.navInfo.activeBottomNav = tab;
                }, 600);
            }
        };

        $scope.toggleBottomView = function () {
            $scope.bottombarNavInfo.bottomViewVisible = !$scope.bottombarNavInfo.bottomViewVisible;
        };

        $scope.getOpenedBottombar = function(){
            if($scope.bottombarNavInfo.bottomViewVisible){
                return $scope.navInfo.activeBottomNav;
            }
            else {
                return undefined;
            }
        };
        /** END OF BOTTOM BAR **/

        /* Show Result Metadata Modal */
        $scope.showMetadata = function($event, data) {
            function MetadataController($scope, $mdDialog, osirisProperties) {
                $scope.item = data;

                $scope.closeDialog = function() {
                    $mdDialog.hide();
                };
            }
            MetadataController.$inject = ['$scope', '$mdDialog', 'osirisProperties'];

            $mdDialog.show({
                controller: MetadataController,
                templateUrl: 'views/explorer/templates/metadata.tmpl.html',
                parent: angular.element(document.body),
                targetEvent: $event,
                clickOutsideToClose: true,
                locals: {
                    items: $scope.items
                }
            });
        };

        /* Share Object Modal */
        $scope.sharedObject = {};
        $scope.shareObjectDialog = function($event, item, type, serviceName, serviceMethod) {
            CommunityService.getObjectGroups(item, type).then(function(groups){
                CommonService.shareObjectDialog($event, item, type, groups, serviceName, serviceMethod, 'explorer');
            });
        };

        $scope.hideContent = true;
        var map, sidenav, navbar;
        $scope.finishLoading = function(component) {
            switch(component) {
                case 'map':
                    map = true;
                    break;
                case 'sidenav':
                    sidenav = true;
                    break;
                case 'navbar':
                    navbar = true;
                    break;
            }

            if (map && sidenav && navbar) {
                $scope.hideContent = false;
            }
        };

    }]);
});
