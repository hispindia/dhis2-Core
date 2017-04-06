//Controller for EHS Home
var trackerCapture = angular.module('trackerCapture');
trackerCapture.controller('FoodSafetyProgramHandlers',
        function($scope,
                $location) {


    $scope.showEstablishmentsRegistration = function(){
        selection.load();
        $location.path('/establishments-registration').search();
    };
    $scope.showOperatorsRegistration = function(){
        var key="XmLabeuEeEB";
        selection.load();
       // $location.path('/operators-registration').search(key);
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