'use strict';

var directives = angular.module('jwApp.directives', []);

directives.directive('butterbar', ['$rootScope',
    function($rootScope) {
        return {
            link: function(scope, element, attrs) {
                element.addClass('hide');

                $rootScope.$on('$routeChangeStart', function() {
                    element.removeClass('hide');
                });

                $rootScope.$on('$routeChangeSuccess', function() {
                    element.addClass('hide');
                });
            }
        };
    }]);

directives.directive('focus',
    function() {
        return {
            link: function(scope, element, attrs) {
                element[0].focus();
            }
        };
    });

directives.directive('passwordStrength', ['$log',
    function() {
        return {
            require: 'ngModel',
            link: function(scope, elm, attrs, ctrl) {
                ctrl.$parsers.unshift(function(viewValue) {

                    var pwdValidLength = viewValue && viewValue.length >= 6;
                    var pwdHasLetter = viewValue && /[A-z]/.test(viewValue);
                    var pwdHasNumber = viewValue && /\d/.test(viewValue);

                    if(pwdValidLength && pwdHasLetter && pwdHasNumber) {
                        scope.pwdStrength = "strong";
                    } else if (pwdValidLength && (pwdHasLetter || pwdHasNumber)) {
                        scope.pwdStrength = "medium";
                    } else {
                        scope.pwdStrength = "weak";
                    }
                    ctrl.$setValidity('passwordStrength', true);
                    return viewValue;
                });
            }
    };
}]);

directives.directive("repeatPassword", function() {
    return {
        require: "ngModel",
        link: function(scope, elem, attrs, ctrl) {
            var otherInput = elem.inheritedData("$formController")[attrs.repeatPassword];

            ctrl.$parsers.push(function(value) {
                if(value === otherInput.$viewValue) {
                    ctrl.$setValidity("repeatPassword", true);
                    return value;
                }
                ctrl.$setValidity("repeatPassword", false);
            });

            otherInput.$parsers.push(function(value) {
                ctrl.$setValidity("repeatPassword", value === ctrl.$viewValue);
                return value;
            });
        }
    };
});