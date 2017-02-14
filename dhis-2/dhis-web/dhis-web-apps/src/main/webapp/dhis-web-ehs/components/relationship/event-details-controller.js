/* global trackerCapture, angular */

var trackerCapture = angular.module('trackerCapture');
trackerCapture.controller('EventDetailsController',
        function($scope,$timeout,
                $rootScope,
                $modalInstance,
                selectedProgram,
                relatedTeis,
                ProgramStageEventService) {


      $scope.programStageUid = 'APrTTktTDOf';
      //$scope.teiUid = 'KsJSPgQywKX';
      //$scope.teiUid = selectedTei;
      $scope.relatedTeis = relatedTeis;

      $scope.programStageDataElements = [];
      $scope.programStageName = "";
      $scope.eventDataValueMap = [];
      $scope.lastUpdatedEventDataValue = [];

      //alert( relatedTeis + " --- " + selectedProgram.id );
      //console.log( selectedTei + " --- " + selectedProgram.id );

      if( $scope.programStageUid != undefined ){
        //  $scope.selectedCountry1=selectedcountry.displayName;
        ProgramStageEventService.getProgramStageDataElements( $scope.programStageUid ).then(function(programStage){
          $scope.programStageName = programStage.name;
          $scope.programStageDataElements = programStage.programStageDataElements;

        });
      }
          //$timeout( function (){

            if($scope.programStageUid != undefined && $scope.relatedTeis != undefined){

              for( var i=0; i<$scope.relatedTeis.length; i++)
              {
                var tempTEI = $scope.relatedTeis[i].trackedEntityInstance;

                //$timeout( function (){

                //$http.get('../api/events.json?trackedEntityInstance=' + teiUid + '&programStage=' + programStageUid + "&order=eventDate:DESC&skipPaging=true"

                $.ajax({
                  async:false,
                  type: "GET",
                  url: '../api/events.json?trackedEntityInstance=' + tempTEI + '&programStage=' + $scope.programStageUid + "&order=eventDate:DESC&skipPaging=true",
                  success: function(response){

                    $scope.tempLastUpdatedEventDataValues = response.events[0].dataValues;

                    for( var j=0; j<$scope.tempLastUpdatedEventDataValues.length; j++)
                    {
                      if (!$scope.lastUpdatedEventDataValue[tempTEI]){
                        $scope.lastUpdatedEventDataValue[tempTEI] = [];
                      }
                      $scope.lastUpdatedEventDataValue[tempTEI][$scope.tempLastUpdatedEventDataValues[j].dataElement] = $scope.tempLastUpdatedEventDataValues[j].value;
                    }

                  },
                    error: function(response){
                  }

                });

                /*
                ProgramStageEventService.getLastUpdatedEventDetails( tempTEI, $scope.programStageUid ).then(function(events){

                    $scope.tempLastUpdatedEventDataValues = events.events[0].dataValues;

                    for( var j=0; j<$scope.tempLastUpdatedEventDataValues.length; j++)
                    {
                      if (!$scope.lastUpdatedEventDataValue[tempTEI]){
                        $scope.lastUpdatedEventDataValue[tempTEI] = [];
                      }
                      $scope.lastUpdatedEventDataValue[tempTEI][$scope.tempLastUpdatedEventDataValues[j].dataElement] = $scope.tempLastUpdatedEventDataValues[j].value;
                    }
                  });
                */
                //},200);


              }
            }

          //},2000);

   // close popUp Window
    $scope.closeWindow = function () {
        $modalInstance.close();
    };

});