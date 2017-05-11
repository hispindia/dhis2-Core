//Controller for EHS Home
var trackerCapture = angular.module('trackerCapture');
trackerCapture.controller('FoodSafetyProgramController',
        function($scope,
                $location) {


    $scope.showEstablishmentsRegistration = function(){
        var key="tITlMGNJTbJ";
        selection.load();
        $location.path('/establishments-registration').search(key);
    };
    $scope.showOperatorsRegistration = function(){
        var key="tITlMGNJTbJ";
        selection.load();
        $location.path('/operators-registration').search(key);
    };
    /*
    $scope.overdueEvents = function(){   
        selection.load();
        $location.path('/overdue-events').search();
    };   
    
    $scope.upcomingEvents = function(){
        selection.load();
        $location.path('/upcoming-events').search();
    };
    */
});