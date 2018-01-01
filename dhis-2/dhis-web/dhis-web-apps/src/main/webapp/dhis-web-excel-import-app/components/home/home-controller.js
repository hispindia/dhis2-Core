/* global excelUpload, angular */

//Controller for home
excelUpload.controller('HomeController',function($scope) {
	
	$scope.manageTemplateAction =  function(){
		window.location.assign("#manage-templates");
	};
	
	$scope.dataImportAction =  function(){
		window.location.assign("#data-import");
	};
	
	$scope.logsAction =  function(){
		window.location.assign("#logs");
	};

	$scope.settingAction =  function(){
		window.location.assign("#settings");
	};

	$scope.facilitywiseAction =  function(){
		javascript:window.location.reload(true);
		window.location.assign("#facilitywise");
	};
});