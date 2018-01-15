/* global excelUpload, angular */

//Controller for home
excelUpload.controller('HomeController',function($scope,userService) {

	$scope.accessAuthority = false;
	$scope.tempAccessAuthority = false;

	userService.getCurrentUser().then(function (responseUser){
		$scope.currentUser = responseUser;
		//$scope.currentUserName = responseUser.userCredentials.username;
		$scope.currentUserName = responseUser.userCredentials.username;// for 2.20
		$scope.currentUserRoles = responseUser.userCredentials.userRoles;
		$scope.superUserAuthority = "";
		for(var i=0 ; i < $scope.currentUserRoles.length; i++){
			$scope.currentUserRoleAuthorities = responseUser.userCredentials.userRoles[i].authorities;
			for(var j = 0 ; j < $scope.currentUserRoleAuthorities.length; j++)
			{
				if(  $scope.currentUserRoleAuthorities[j] === "ALL")
				{
					//$scope.accessAuthority = true;
					$scope.superUserAuthority = "YES";
					break;
				}
			}
		}
		console.log("Current User Uid  --" + $scope.currentUser.id + "  Current User Name  --" + $scope.currentUserName);

		 if(  $scope.currentUserName === 'admin' || $scope.superUserAuthority === "YES" )
		 {
			 $scope.accessAuthority = true;
		 }

		//console.log("accessAuthority --" + $scope.accessAuthority);
	});
	
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