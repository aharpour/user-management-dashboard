'use strict';

// Declare app level module which depends on filters, and services
angular.module('myApp',
		[ 'myApp.filters', 'myApp.services', 'myApp.directives', 'myApp.controllers', 'ngRoute', 'ngSanitize']).config(
		[ '$routeProvider', function($routeProvider) {
			$routeProvider.when('/', {
				controller : 'Home',
				template : '<div ng-include="template"></div>'
			});
			$routeProvider.otherwise({
				redriectTo : '#/'
			});
		} ]);
