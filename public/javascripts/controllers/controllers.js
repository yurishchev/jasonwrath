'use strict';

var controllers = angular.module('jwApp.controllers',
    ['jwApp.directives', 'jwApp.services']);


controllers.controller('HeaderCtrl', ['$scope',
    function ($scope) {
        $scope.openLoginDialog = function () {
            $('#loginModal').modal('show');
        };
    }]);


controllers.controller('LoginDialogCtrl', ['$scope', '$http',
    function ($scope, $http) {
        $scope.master = {};

        $scope.login = function () {
            if ($scope.loginForm.$invalid) {
                return;
            }
            $http.post($('form[name=loginForm]').attr('action') + 'Dialog', $.param($scope.user)).
                success(function (data, status) {
                    if (data.members && data.members.errors) {
                        $scope.errors = data.members.errors;
                    } else {
                        $('form[name=loginForm]').submit();
                    }
                }).
                error(function (data, status) {
                    $scope.errors = data || "Request failed";
                });
        };

        $('#loginModal').on('hidden.bs.modal', function (e) {
            $scope.user = angular.copy($scope.master);
            $scope.loginForm.$setPristine();
            $scope.errors = null;
        });
    }]);

controllers.controller('LoginCtrl', ['$scope',
    function ($scope) {
        $scope.login = function () {
            if ($scope.loginForm.$invalid) {
                return;
            }
            $('form[name=loginForm]').submit();
        };
    }]);

controllers.controller('RegistrationCtrl', ['$scope',
    function ($scope) {
        $scope.register = function () {
            if ($scope.registrationForm.$invalid) {
                return;
            }
            $('form[name=registrationForm]').submit();
        };
    }]);

controllers.controller('CountDownCtrl', ['$scope',
    function ($scope) {
        var currentDate = new Date(),
            availiableExamples = {
                set60daysFromNow: 60 * 24 * 60 * 60 * 1000,
                set5minFromNow: 5 * 60 * 1000,
                set2secFromNow: 2 * 1000
            };

        function callback(event) {
            var $this = $(this);
            $this.find('span#' + event.type).html(event.value);
        }

        $('div#clock').countdown(availiableExamples.set60daysFromNow + currentDate.valueOf(), callback);
    }]);

controllers.controller('AboutCtrl', ['$scope',
    function ($scope) {
    }]);

