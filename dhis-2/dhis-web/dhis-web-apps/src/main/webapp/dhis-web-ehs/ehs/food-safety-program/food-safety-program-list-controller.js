//Controller for EHS Home
var trackerCapture = angular.module('trackerCapture');
trackerCapture.controller('FoodSafetyProgramlistController',
    function($scope,
             $location,
             AjaxCalls,
             $rootScope
          ) {


        AjaxCalls.getFoodSafetyProgram().then(function(data){
            $scope.foodsafetyprograms = data;

        });


        //$scope.names = ["Emil", "Tobias", "Linus"];
        $scope.foodSafety = function(value){
            $scope.value=value;
            AjaxCalls.setfoodsafetyid($scope.value);
            selection.load();
            $location.path('/food-safety-program').search();
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