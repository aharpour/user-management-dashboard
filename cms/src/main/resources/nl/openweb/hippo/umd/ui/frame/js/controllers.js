'use strict';

/* Controllers */

angular.module('myApp.controllers', ['myApp.services'])
  .controller('copy', [ '$scope', '$http', '$state', 'gracefulLoader', function($scope, $http, $state, gracefulLoader) {
      
      
      $scope.source = {
          availableUsers: [],
          selected: null
      };
      $scope.target = {
          availableUsers: [],
          selected: null
      };
      $scope.report = null;
      $scope.submit = function() {
        if ($scope.source.selected != null && $scope.target.selected != null) {
          gracefulLoader.post('../rest/api/users/copy/groups', {
            target: $scope.target.selected,
            source: $scope.source.selected,
          }, function(data) {
            $scope.report = JSON.stringify(data, null, 2);
          });
        } else {
          alert('you must select a source and a target user.');
        }
        
      }
      
   
      gracefulLoader.fetch('../rest/api/users/list', function(data) {
        $scope.source.availableUsers = data.source;
        $scope.target.availableUsers = data.target;
      });
      
      
    }])
    .controller('overviews', [ '$scope', '$http', function($scope, $http) {}
    ]);
