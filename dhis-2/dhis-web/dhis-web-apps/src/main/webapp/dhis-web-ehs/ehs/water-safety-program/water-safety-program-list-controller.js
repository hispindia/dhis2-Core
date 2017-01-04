//Controller for EHS Home
var trackerCapture = angular.module('trackerCapture');
trackerCapture.controller('WaterSafetyProgramlistController',
    function($scope,
             AjaxCalls,
             $location) {




        AjaxCalls.getWaterSafetyProgram().then(function(data){
            $scope.foodsafetyprograms = data;

        });
        $scope.waterSafety = function(value){
            $scope.value=value;
            AjaxCalls.setfoodsafetyid($scope.value);
            selection.load();
            $location.path('/water-safety-program').search();
            // $location.path('/establishments-registration').search();

            // selection.load();
            //  $location.path('/establishments-registration').search();
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