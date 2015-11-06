'use strict';

var module = angular.module('myApp.services', ['ngProgress', 'ngDialog']);
module.value('version', '0.1');
module.factory('gracefulLoader', ['$http', '$state', '$rootScope', 'ngProgress', 'ngDialog', function($http, $state, $rootScope, $ngProgress, $ngDialog) {
      return {
        fetch : function(url, callBack) {
          $ngProgress.start();
          $http.get(url).success(
              function(data, status, headers, config) {
                $ngProgress.complete();
                callBack(data);
              }).error(function(data, status, headers, config) {
            $ngProgress.complete();
            var $scope = $rootScope.$new();
            $scope.data = data;
            $scope.statusCode = status;
            $ngDialog.open({
              template : 'templates/error.html',
              scope : $scope,
              preCloseCallback : function(value) {
                $state.reload();
              }
            });
          });
        },
        post : function(url, payload, callBack) {
          $ngProgress.start();
          $http.post(url, payload).success(
              function(data, status, headers, config) {
                $ngProgress.complete();
                callBack(data);
              }).error(function(data, status, headers, config) {
            $ngProgress.complete();
            var $scope = $rootScope.$new();
            $scope.data = data;
            $scope.statusCode = status;
            $ngDialog.open({
              template : 'templates/error.html',
              scope : $scope,
              preCloseCallback : function(value) {
                $state.reload();
              }
            });
          });
        },
        put : function(url, payload, callBack) {
          $ngProgress.start();
          $http.put(url, payload).success(
              function(data, status, headers, config) {
                $ngProgress.complete();
                callBack(data);
              }).error(function(data, status, headers, config) {
                $ngProgress.complete();
                var $scope = $rootScope.$new();
                $scope.data = data;
                $scope.statusCode = status;
                $ngDialog.open({
                  template : 'templates/error.html',
                  scope : $scope,
                  preCloseCallback : function(value) {
                    $state.reload();
                  }
                });
              });
        }

      };
    } ]);
