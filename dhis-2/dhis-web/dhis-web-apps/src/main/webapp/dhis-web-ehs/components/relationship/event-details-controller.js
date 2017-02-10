/* global trackerCapture, angular */

var trackerCapture = angular.module('trackerCapture');
trackerCapture.controller('EventDetailsController',
        function($scope,
                $rootScope,
                $modalInstance,
                selectedTei,
                selectedProgram,
                ProgramStageEventService) {


      $scope.programStageUid = 'APrTTktTDOf';
      //$scope.teiUid = 'KsJSPgQywKX';
      $scope.teiUid = selectedTei;
      $scope.programStageDataElements = [];
      $scope.programStageName = "";
      $scope.eventDataValueMap = [];
      $scope.lastUpdatedEventDataValue = {};

      alert( selectedTei + " --- " + selectedProgram.id );
      console.log( selectedTei + " --- " + selectedProgram.id );

      if( $scope.programStageUid != undefined ){
        //  $scope.selectedCountry1=selectedcountry.displayName;
        ProgramStageEventService.getProgramStageDataElements( $scope.programStageUid ).then(function(programStage){
          $scope.programStageName = programStage.name;
          $scope.programStageDataElements = programStage.programStageDataElements;

        });
      }

      if($scope.programStageUid != undefined && $scope.teiUid != undefined){
          ProgramStageEventService.getLastUpdatedEventDetails($scope.teiUid, $scope.programStageUid ).then(function(events){

            $scope.programStageUid;
            $scope.teiUid;
            $scope.lastUpdatedEventDataValue = events.events[0].dataValues;


            angular.forEach($scope.lastUpdatedEventDataValue, function(dataValue){
              $scope.lastUpdatedEventDataValue[dataValue.dataElement] = dataValue.value;
            });
          });
      }













   // close popUp Window
    $scope.closeWindow = function () {
        $modalInstance.close();
    };

});