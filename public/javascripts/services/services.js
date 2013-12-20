'use strict';

var services = angular.module('jwApp.services',
    ['ngResource']);

services.factory('connectedUser', function() {
    var connectedUser = {};

    return {
        getName: function() {
            return connectedUser.name;
        },
        setName: function(value) {
            connectedUser.name = value;
            console.log(connectedUser.name);
        },
        getObject: function() {
            return connectedUser;
        }
    }
});

services.factory('Recipe', ['$resource',
    function($resource) {
        return $resource('/recipes/:id', {id: '@id'});
    }]);

