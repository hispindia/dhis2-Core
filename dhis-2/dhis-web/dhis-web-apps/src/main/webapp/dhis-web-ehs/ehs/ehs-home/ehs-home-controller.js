//Controller for EHS Home
var trackerCapture = angular.module('trackerCapture');
trackerCapture.controller('EHSHomeTypesController',
        function($scope,
                $location) {

    // food-safety-program
    $scope.foodSafetyProgram = function(){
        //selection.load();
        $location.path('/food-safety-program-list').search();
        //$location.path('/food-safety-program').search();
    };
    // water-safety-program
    $scope.waterSafetyProgram = function(){
        //selection.load();
        $location.path('/water-safety-program-list').search();
    };

    /*
    $scope.programStatistics = function(){   
        selection.load();
        $location.path('/program-statistics').search();
    };
    
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