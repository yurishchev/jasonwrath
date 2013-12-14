'use strict';

var controllers = angular.module('jwApp.controllers',
    ['jwApp.directives', 'jwApp.services']);

controllers.controller('CountDownCtrl', ['$scope',
    function ($scope) {
        var currentDate = new Date(),
            availiableExamples = {
                set60daysFromNow: 60 * 24 * 60 * 60 * 1000,
                set5minFromNow  : 5 * 60 * 1000,
                set2secFromNow  : 2 * 1000
            };

        function callback(event) {
            var $this = $(this);
            $this.find('span#'+event.type).html(event.value);
        }

        $('div#clock').countdown(availiableExamples.set60daysFromNow + currentDate.valueOf(), callback);
    }]);

controllers.controller('AboutCtrl', ['$scope',
    function ($scope) {
    }]);
