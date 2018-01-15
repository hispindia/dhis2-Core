/* global excelUpload, angular */

//Controller for excel importing
excelUpload.controller('LogController',
        function($rootScope,
                $scope,
                $timeout,
                $route,
                $filter,
                ExcelMappingService,
                ValidationRuleService,
                CurrentSelection,
                ExcelReaderService,
                MetaDataFactory,
                orderByFilter,
                OrgUnitService,
				userService,
                DialogService) {
    
	$scope.history = {};
	$scope.accessAuthority = false;
	$scope.tempAccessAuthority = false;

	userService.getCurrentUser().then(function (responseUser) {
		$scope.currentUser = responseUser;
		//$scope.currentUserName = responseUser.userCredentials.username;
		$scope.currentUserName = responseUser.userCredentials.username;// for 2.20
		$scope.currentUserRoles = responseUser.userCredentials.userRoles;
		$scope.superUserAuthority = "";
		for (var i = 0; i < $scope.currentUserRoles.length; i++) {
			$scope.currentUserRoleAuthorities = responseUser.userCredentials.userRoles[i].authorities;
			for (var j = 0; j < $scope.currentUserRoleAuthorities.length; j++) {
				if ($scope.currentUserRoleAuthorities[j] === "ALL") {
					//$scope.accessAuthority = true;
					$scope.superUserAuthority = "YES";
					break;
				}
			}
		}
		console.log("Current User Uid  --" + $scope.currentUser.id + "  Current User Name  --" + $scope.currentUserName);

		if ($scope.currentUserName === 'admin' || $scope.superUserAuthority === "YES") {
			$scope.accessAuthority = true;
		}

		//console.log("accessAuthority --" + $scope.accessAuthority);
	});
	
	//history
	$("#templateProgress").html("Retrieving all the import history...");
	ExcelMappingService.get('Excel-import-app-history').then(function(his){
		$scope.history = jQuery.isEmptyObject( his ) ? JSON.parse('{"history" : []}') : his;
		
		var htmlString = "";
		
		$.each( $scope.history.history , function(i,h){
			htmlString += "<tr>";
			htmlString += "<td>" + h.dataSet + "</td>";
			htmlString += "<td>" + h.period + "</td>";
			htmlString += "<td>" + h.time + "</td>";
			htmlString += "<td>" + h.template + "</td>";
			htmlString += "<td> <button class='btn btn-info' onclick='viewStats( " + i + " )' style='padding : 0px;width:70px'> Stats </button> <button class='btn btn-warning' style='padding : 0px;width:70px' onclick='viewMessage( " + i + " )' > Message </button> </td>";
			htmlString += "</tr>";
		});
		
		$("#tblHis").append(htmlString);
		$("#loader").hide();
	});
	
	$scope.viewMessage = function(h){
		var htmlString = "";
		
		htmlString += "<ol>";
		
		$.each( $scope.history.history[h].message , function(i, m){
			htmlString += "<li>" + m + "</li>";
		});
		
		htmlString += "</ol>";
		
		$("#confBdy").html( htmlString );
		$("#conflictModal").modal('show');
	};
	
	$scope.viewStats = function(h){
		$("#upc").html($scope.history.history[h].stats.upc);
		$("#imct").html($scope.history.history[h].stats.imc);
		$("#igc").html($scope.history.history[h].stats.igc);
		$("#stModal").modal('show');
	};
	
});