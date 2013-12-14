'use strict';

var services = angular.module('jwApp.services',
    ['ngResource']);

services.factory('Recipe', ['$resource',
    function($resource) {
        return $resource('/recipes/:id', {id: '@id'});
    }]);

