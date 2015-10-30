'use strict';

/* Controllers */

angular.module('myApp.controllers', [])
  .controller('copy', [ '$scope', '$http', function($scope, $http) {
      
      $scope.availableUsers = [];
      $scope.source = null;
      $scope.target = null;
      $scope.report = null;
      $scope.submit = function() {
        if ($scope.source != null && $scope.target != null) {
          data: { test: 'test' }
          $http.post('../rest/api/users/copy/groups', {
            target: $scope.target,
            source: $scope.source,
          }).then(function(respouse) {
            $scope.report = JSON.stringify(respouse.data, null, 2);
          }, function(e) {
            console.log(e);
            alert('Error');
          });
        } else {
          alert('you must select a source and a target user.');
        }
        
      }
      
      
      $http.get('../rest/api/users/list').then(function(respouse) {
        $scope.availableUsers = respouse.data;
      }, function(e) {
        console.log(e);
        alert('Error');
      } );
      
    }])
    .controller('overviews', [ '$scope', '$http', function($scope, $http) {}
    ]);
