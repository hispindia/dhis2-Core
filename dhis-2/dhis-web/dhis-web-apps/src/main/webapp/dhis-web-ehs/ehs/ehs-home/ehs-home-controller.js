//Controller for EHS Home
var trackerCapture = angular.module('trackerCapture');
trackerCapture.controller('EHSHomeTypesController',
        function($scope,
                $location,
                 AjaxCalls,
                 $rootScope) {
            $scope.userrole = [];


            $scope.useroperatorregistry=false;
            $scope.userfoodsafetyprogram=false;
            $scope.userwatersafetyprogram=false;
                AjaxCalls.getuserrole().then(function (data1) {


                    $scope.userrole = data1.data.userCredentials.userRoles[0].id;
                    $scope.userprogram = "";
                    AjaxCalls.getuserroleprogram($scope.userrole).then(function (data2) {

                        AjaxCalls.getFoodSafetyProgram().then(function(data) {
                            $scope.foodsafetyprograms = data;
                        for (i = 0; i < data2.data.programs.length; i++) {
                            $scope.userprogram += " " + data2.data.programs[i].id;
                        }

                        for(j=0;j< $scope.foodsafetyprograms.length;j++){
                           if($scope.userprogram.includes($scope.foodsafetyprograms[j].id))
                            { $scope.userfoodsafetyprogram=true;}
                        }


                    });

                        AjaxCalls.getWaterSafetyProgram().then(function(data1){
                            $scope.watersafetyprograms = data1;

                            for (i = 0; i < data2.data.programs.length; i++) {
                                $scope.userprogram += " " + data2.data.programs[i].id;
                            }

                            for(j=0;j< $scope.watersafetyprograms.length;j++){
                                if($scope.userprogram.includes($scope.watersafetyprograms[j].id))
                                { $scope.userwatersafetyprogram=true;}
                            }

                        });


                        // operator registry//


                        AjaxCalls.getoperatorregistry().then(function(data4){

                            for(j=0;j< data4.length;j++){
                                if($scope.userprogram.includes(data4[j].id))
                                { $scope.useroperatorregistry=true;}
                            }

                        });


                        //end//




                    });

            });
        

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