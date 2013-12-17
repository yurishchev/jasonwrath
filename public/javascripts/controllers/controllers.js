'use strict';

var controllers = angular.module('jwApp.controllers',
    ['jwApp.directives', 'jwApp.services']);


controllers.controller('HeaderCtrl', ['$scope',
    function ($scope) {
        $scope.openLoginDialog = function () {
            $('#loginModal').modal('show');
        };
    }]);


controllers.controller('LoginCtrl', ['$scope', '$log',
    function ($scope, $log) {
        $scope.items = ['item1', 'item2', 'item3'];

        $scope.selected = {
            item: $scope.items[0]
        };

        $scope.login = function () {
            $log.info('Modal dismissed at: ' + $scope.selected.item);
            $('#loginModal').modal('hide');
        };
    }]);

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

