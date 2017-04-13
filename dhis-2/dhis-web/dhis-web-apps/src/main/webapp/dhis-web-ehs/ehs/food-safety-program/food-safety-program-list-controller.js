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

			AjaxCalls.getuserrole().then(function(data1) {


				$scope.userrole=data1.data.userCredentials.userRoles[0].id;
				$scope.userprogram="";
				AjaxCalls.getuserroleprogram($scope.userrole).then(function(data2)
				{
					for(i=0;i<data2.data.programs.length;i++)
					{ $scope.userprogram+=" " +data2.data.programs[i].id ;}

				});
			});
		




        });


        //$scope.names = ["Emil", "Tobias", "Linus"];
        $scope.foodSafety = function(value){
            $scope.value=value;
            if($scope.value =="tITlMGNJTbJ")
			{
				AjaxCalls.setfoodsafetyid($scope.value);
            selection.load();
			
				$location.path('/food-safety-program').search();
			}
			else if($scope.value == "XmLabeuEeEB")
			{
				AjaxCalls.setfoodsafetyid($scope.value);
            selection.load();
			
				$location.path('/food-handlers').search();
			}
			
			else if($scope.value == "TeBSCKYRo3q")
			{
				AjaxCalls.setfoodsafetyid($scope.value);
            selection.load();
			
				$location.path('/mass-events').search();
			}
			else if($scope.value == "v33qL1Mzre5")
			{
				AjaxCalls.setfoodsafetyid($scope.value);
            selection.load();
			
				$location.path('/slaughter-houses').search();
			}
			
			
			
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