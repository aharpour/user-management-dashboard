'use strict';

angular.module('myApp',
    [ 'ui.router', 'myApp.controllers']).config(
    [ '$urlRouterProvider', '$stateProvider', function($urlRouterProvider, $stateProvider) {
      $stateProvider.state('copy', {
        url: '/',
        templateUrl : 'templates/group-copy.html',
        controller : 'copy'
      });
      $stateProvider.state('overviews', {
        url: '/overviews',
        templateUrl : 'templates/overviews.html',
        controller : 'overviews'
      });

      $urlRouterProvider.otherwise('/');
      
} ]);