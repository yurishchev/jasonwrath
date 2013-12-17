'use strict';

var app = angular.module('jwApp', [
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute',
    'jwApp.services',
    'jwApp.directives',
    'jwApp.controllers'
]);

app.config(['$routeProvider', '$httpProvider', function ($routeProvider, $httpProvider) {
    $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';
    $routeProvider.
        when('/', {
            controller: 'CountDownCtrl',
            templateUrl: '/public/views/countDown.html'
        }).when('/about', {
            controller: 'AboutCtrl',
            templateUrl: '/public/views/about.html'
        }).otherwise({redirectTo: '/'});
}]);
