'use strict';

/**
 * @ngdoc overview
 * @name smartiApp
 * @description
 * # smartiApp
 *
 * Main module of the application.
 */
angular
  .module('smartiApp', [
    'ngRoute',
    'ngAnimate',
    'ngMessages',
    'ui.codemirror',
    'ui.bootstrap',
    'ngFileUpload',
    'toastr'
  ])
  .constant('ENV', {
    serviceBaseUrl: '.'
  })
  .config(function ($routeProvider, $httpProvider) {
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

    $routeProvider
      .when('/', {
        templateUrl: 'views/overview.html',
        controller: 'OverviewCtrl',
        controllerAs: 'overview'
      })
      .when('/login', {
        templateUrl: 'views/login.html',
        controller: 'LoginCtrl',
        controllerAs: '$ctrl'
      })
      .when('/client/:id?', {
        templateUrl: 'views/client.html',
        controller: 'ClientCtrl',
        resolve: {
          client:function(ClientService,Client,$route){
            if($route.current.params.id) {
              if(!$route.current.params.clone) {
                return ClientService.getById($route.current.params.id);
              } else {
                return ClientService.getById($route.current.params.id,true);
              }
            } else {
              return new Client();
            }
          }
        }
      })
      .when('/client/:clientId/security', {
        templateUrl: 'views/client-security.html',
        controller: 'ClientSecurityCtrl',
        controllerAs: '$ctrl',
        resolve: {
          client: function ($route, ClientService) {
            return ClientService.getById($route.current.params.clientId);
          }
        }
      })
      .when('/client/:clientId/conversations', {
        templateUrl: 'views/conversations.html',
        controller: 'ConversationCtrl',
        controllerAs: '$ctrl',
        resolve: {
          client: function ($route, ClientService) {
            return ClientService.getById($route.current.params.clientId);
          }
        }
      })
      .when('/client/:clientId/conversations/:conversationId', {
        templateUrl: 'views/conversationEdit.html',
        controller: 'ConversationEditCtrl',
        controllerAs: '$ctrl',
        resolve: {
          client: function ($route, ClientService) {
            return ClientService.getById($route.current.params.clientId);
          },
          conversation: function($route, ConversationService) {
            return ConversationService.getConversation($route.current.params.conversationId);
          }
        }
      })
      .when('/user', {
        templateUrl: 'views/user-management.html',
        controller: 'UserManagementCtrl',
        controllerAs: '$ctrl'
      })
      .otherwise({
        redirectTo: '/'
      });
  });
