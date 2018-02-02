'use strict';

/**
 * @ngdoc function
 * @name smartiApp.controller:OverviewCtrl
 * @description
 * # OverviewCtrl
 * Controller of the smartiApp
 */
angular.module('smartiApp')
  .controller('OverviewCtrl', function ($scope, $window, $location, ClientService) {

    $scope.defaultClient = null;

    function listClients() {
      ClientService.list().then(function(clients){
        $scope.clients = clients;
        $scope.defaultClient = clients.filter(function (c) {
          return c.data.defaultClient || false;
        })[0] || null;
      })
    }

    $scope.edit = function(clientId) {
      $location.path('client/' + clientId);
    };

    $scope.clone = function(clientId) {
      $location.path('client/' + clientId).search('clone');
    };

    $scope.cloneDefaultClient = function () {
      if ($scope.defaultClient) {
        $scope.clone($scope.defaultClient.data.id);
      }
    };

    $scope.createClient = function() {
      $location.path('client');
    };

    $scope.saveClient = function(client) {
      client.save().then(listClients);
    };

    $scope.delete = function(client) {
      if($window.confirm("Do you realy want to delete " + client.data.name + "?")) {
        client.delete().then(listClients);
      }
    };

    $scope.manageConversations = function (clientId) {
      $location.path('client/' + clientId + '/conversations');
    };

    $scope.manageSecurity = function (clientId) {
      $location.path('client/' + clientId + '/security');
    };

    listClients();

  });
