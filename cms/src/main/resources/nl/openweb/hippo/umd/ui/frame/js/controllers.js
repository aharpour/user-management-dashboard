'use strict';

/* Controllers */

angular.module('myApp.controllers', [])
    .controller('Home', ['$scope', '$location', '$http', function($scope, $location, $http) {
    	$http.get('../rest/api/greeting').
    	success(function(data, status, headers, config) {
    		$scope.greetings = data;
    	}).error(function(data, status, headers, config) {
    		if (status == 401) {
    			$scope.greetings = data.message;
    		}
    	});	
    	$scope.template = './templates/home.html';
    }])
;
