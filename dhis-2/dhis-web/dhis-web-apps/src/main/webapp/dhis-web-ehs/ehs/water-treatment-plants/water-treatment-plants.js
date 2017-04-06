//Controller for EHS Home
var trackerCapture = angular.module('trackerCapture');
trackerCapture.controller('WaterSafetyWaterTreatmentPlants',
        function($scope,
                $location) {


    $scope.showWaterEstablishmentsRegistration = function(){
        selection.load();
        $location.path('/water-establishments-registration').search();
    };
    $scope.showOperatorsRegistration = function(){
        var key="JlYxDLMZJhK"
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