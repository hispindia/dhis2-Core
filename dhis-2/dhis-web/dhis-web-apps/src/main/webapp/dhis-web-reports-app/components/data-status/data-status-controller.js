/**
 *
 */
 
reportsApp.controller('DataStatusController',
    function ($rootScope,$scope, $http, $location,reportSettingService,reportsService,
              ReportAppSectionSettingService,periodService,$window,organisationUnitGroupService,sqlviewservice,
              userService,ReportConfigurationService,DataSetService,$timeout,OrganisationUnitService)
    {

        $scope.REPORT_APP_CONFIGURATION_KEY = "reportApp-configuration-json";
        $scope.ReportAppConfigurationSettings = {};
        $scope.ReportAppConfigurationSettings.parameters=[];
		
		//Links
		$scope.selOrgUnit = selection.getSelected();
		$scope.filteredOrgUnitList=[];
            OrganisationUnitService.getAllChildrenOfSelectedOrgUnit( $scope.selOrgUnit[0] ).then(function(orgUnitList){
                    $scope.allChildrenList = orgUnitList.organisationUnits;
                    angular.forEach( $scope.allChildrenList, function(child){
                        
                                $scope.filteredOrgUnitList.push( child.id );
                            
                    });
                    console.log( "filtered OrgUnit List length  is -- " + $scope.filteredOrgUnitList.length );
                }
            );
		
		$scope.basicUrl = "../api/sqlViews/";
		//Sql Views ID
		
		
		// savita modified//
		sqlviewservice.getAll().then(function(data){
                $scope.sqlViews = data.sqlViews;
			
			for(var i=0;i<data.sqlViews.length;i++)
		{
		if($scope.sqlViews[i].name=="DS App Comments")
		{$scope.commentsSV=$scope.sqlViews[i].id;
	
		
		}
		if($scope.sqlViews[i].name=="DS App Comments without Zero")
		{$scope.commentsExZeroSV=$scope.sqlViews[i].id;

		}
		
		
		if($scope.sqlViews[i].name=="DS App Data Status")
		{$scope.dataStatusSV=$scope.sqlViews[i].id;
		
		
		
		}
		
		if($scope.sqlViews[i].name=="DS App Data Status without Zero")
		{$scope.dataStatusExZeroSV=$scope.sqlViews[i].id;
          
		
		}
		
		if($scope.sqlViews[i].name=="DS App Data Summary")
		{$scope.dataSummarySV=$scope.sqlViews[i].id;
		
		
		
		}
		if($scope.sqlViews[i].name=="DS App Data Summary without Zero")
		{$scope.dataSummaryExZeroSV=$scope.sqlViews[i].id;
		 
		
		
		}
		
		if($scope.sqlViews[i].name=="DS App User Details")
		{$scope.userDetailsSV=$scope.sqlViews[i].id;
	
		
		}
		
		
		if($scope.sqlViews[i].name=="DS App User Details without Zero")
		{$scope.userDetailsExZeroSV=$scope.sqlViews[i].id;
		
		}
		
		
			if($scope.sqlViews[i].name=="DS App Periods")
		{
			$scope.periodsSV=$scope.sqlViews[i].id;
		
		}


		if($scope.sqlViews[i].name=="GetOrgUnitId")
		{
            $scope.orgUnitSV=$scope.sqlViews[i].id;
		}

            if($scope.sqlViews[i].name=="CategoryComboId")
            {
                $scope.CategoryComboId=$scope.sqlViews[i].id;
            }
            if($scope.sqlViews[i].name=="getdataElementid")
            {
                $scope.getdataElementid=$scope.sqlViews[i].id;
            }

		}
			
			
            });
			// end //
		
		
		
		
		
		/*
		
		$scope.dataStatusSV = "wDF06I0SurY";
		$scope.dataStatusExZeroSV = "y00FsRQnIaV"; // UID of DATA STATUS query that exclude zero
		$scope.dataSummarySV = "aOLO8CmT8Dr";		
		$scope.dataSummaryExZeroSV = "c9EMNDXjO6b";		
		$scope.userDetailsSV = "GQo5qTSAfh9";
		$scope.userDetailsExZeroSV = "NOb6bsEgoAZ";
		$scope.commentsSV = "GKqUIOeRz3H";		
		$scope.commentsExZeroSV = "HrDi1FJJNOa";
		$scope.periodsSV = "JT7ZRK7atS6";
		*/
		
		//$scope.dataStatusSV = "";
		//$scope.dataStatusSV = "";
		/*
		$scope.dataStatusSV = "fSBkIBD9aib";
		$scope.periodsSV = "UBIPGPj8ZQU";
		$scope.dataSummarySV = "QGLvQLUeYRP";
		$scope.userDetailsSV = "Q75nGmjmbnH";
		$scope.commentsSV = "JQrrf2m6vj1";*/
		
        ReportConfigurationService.getAllReportConfiguration($scope.REPORT_APP_CONFIGURATION_KEY).then(function(conf){
            for (var count in conf.parameters){
                $scope.ReportAppConfigurationSettings.parameters[conf.parameters[count].key] = conf.parameters[count];
            }
        });

        $scope.currentSelection = {
            "orgUnit":"",
            "orgUnitName":"",
            "dataStatusReport":"",
            "includeZero":true,
            "dataSet":"",
            "startPeriodMonth":"",
            "startPeriodYear":"",
            "endPeriodMonth":"",
            "endPeriodYear":""
        };


        var clearAllValues = function(){
            $scope.currentSelection={
                "orgUnit":"",
                "orgUnitName":"",
                "dataStatusReport":"",
                "includeZero":true,
                "dataSet":"",
                "startPeriodMonth":"",
                "startPeriodYear":"",
                "endPeriodMonth":"",
                "endPeriodYear":""
            }
        };


        DataSetService.getAllDataSet().then(function(data){
                $scope.dataSets = data.dataSets;
                $scope.updatePeriods();
            }
        );
		$.ajaxSetup({
            async:false
        });

        $scope.updatePeriods = function(){
            if(true){
                var currentDate = new Date();
                $scope.monthList = periodService.getMonthList();
                $scope.yearList = periodService.getYearListBetweenTwoYears(1900,currentDate.getFullYear());

            }else{
                $scope.periodList = periodService.getLast12Months();
            }
        };

        $scope.listenToOuChange = function(){
            //clearAllValues();
            $timeout(function() {
                $scope.selectedOrgUnit = selection.getSelected();
                $scope.currentSelection.orgUnit = $scope.selectedOrgUnit;
                OrganisationUnitService.getOrgUnitNameAndLevelByUid( $scope.selectedOrgUnit ).then(function(data){
                        $scope.currentSelection.orgUnitName = data.organisationUnits[0].name;
                        ReportConfigurationService.getAllReportConfiguration().then(function (resultData) {
                            if(resultData != "") {
                                $scope.configurationParameters = resultData;
                                $scope.currentSelection.dataStatusReport = $scope.ReportAppConfigurationSettings.parameters['ds_status_report'].value;
                            }
                        });
                    }
                );
				url=  $scope.basicUrl + $scope.orgUnitSV + "/data.json?var=orgUnitId:"+$scope.selectedOrgUnit +"";
				console.log(url);
			
				$.get(url, function(data){
				
				$scope.organisationunitid_1=data.rows[0];
				
				
				});



            }, 10);
			
			$scope.showHideButtons();
        };

        selection.setListenerFunction($scope.listenToOuChange);

        $scope.generateDataStatusReport = function(){
            //alert( $scope.currentReport.period.year );
            //alert( $scope.currentReport.section );
            //alert( $scope.currentReport.id );
            //alert( $scope.currentReport.section );

            var date = new Date();
            var currentMonthFirstDay = new Date(date.getFullYear(), date.getMonth(), 1);
            var currentMonthLastDay = new Date(date.getFullYear(), date.getMonth() + 1, 0);
            var selectedStartPeriod = "";
            var lastDayOfSelStartPeriod = "";
            var lastDayOfSelEndPeriod = "";

            //alert( $scope.currentSelection.includeZero);


            var isValidated = "true";
            if( $scope.currentSelection.orgUnitName === "" || $scope.currentSelection.orgUnitName === undefined )
            {
                alert( "Please select organisation unit");
                isValidated = "false";
                return;
            }
            else if( $scope.currentSelection.dataStatusReport === "" || $scope.currentSelection.dataStatusReport === undefined )
            {
                alert( "Please set data set Status Report in Report Configuration page");
                isValidated = "false";
                return;
            }
            else if( $scope.currentSelection.dataSet === "" || $scope.currentSelection.dataSet === undefined )
            {
                alert( "Please select data set");
                isValidated = "false";
                return;
            }
            else if( $scope.currentSelection.startPeriodMonth === "" || $scope.currentSelection.startPeriodMonth === undefined )
            {
                alert( "Please select start period month");
                isValidated = "false";
                return;
            }

            else if( $scope.currentSelection.startPeriodYear === "" || $scope.currentSelection.startPeriodYear === undefined )
            {
                alert( "Please select start period year");
                isValidated = "false";
                return;
            }
            else if( $scope.currentSelection.endPeriodMonth === "" || $scope.currentSelection.endPeriodMonth === undefined )
            {
                alert( "Please select end period month");
                isValidated = "false";
                return;
            }

            else if( $scope.currentSelection.endPeriodYear === "" || $scope.currentSelection.endPeriodYear === undefined )
            {
                alert( "Please select end period year");
                isValidated = "false";
                return;
            }
            if( $scope.currentSelection.startPeriodMonth != undefined && $scope.currentSelection.startPeriodYear != undefined  )
            {
                var selStartYear = $scope.currentSelection.startPeriodYear;
                var selStartMonth = $scope.currentSelection.startPeriodMonth;
                selectedStartPeriod = new Date( selStartYear + "-" + selStartMonth + "-01" );
                lastDayOfSelStartPeriod = new Date(selectedStartPeriod.getFullYear(), selectedStartPeriod.getMonth() + 1, 0);

                if( lastDayOfSelStartPeriod > currentMonthLastDay )
                {
                    alert( "You can not select future period for start period");
                    isValidated = "false";
                    return;
                }
            }
            if( $scope.currentSelection.endPeriodMonth != undefined && $scope.currentSelection.endPeriodYear != undefined  )
            {
                var selEndYear = $scope.currentSelection.endPeriodYear;
                var selEndMonth = $scope.currentSelection.endPeriodMonth;
                var selectedEndPeriod = new Date( selEndYear + "-" + selEndMonth + "-01" );
                lastDayOfSelEndPeriod = new Date(selectedEndPeriod.getFullYear(), selectedEndPeriod.getMonth() + 1, 0);

                if( lastDayOfSelEndPeriod > currentMonthLastDay )
                {
                    alert( "You can not select future period for end period");
                    isValidated = "false";
                    return;
                }
            }

            if( $scope.currentSelection.startPeriodMonth != undefined && $scope.currentSelection.startPeriodYear != undefined
                    && $scope.currentSelection.endPeriodMonth != undefined && $scope.currentSelection.endPeriodYear != undefined )
            {
                var selStartYear = $scope.currentSelection.startPeriodYear;
                var selStartMonth = $scope.currentSelection.startPeriodMonth;
                var selectedStartPeriod = new Date( selStartYear + "-" + selStartMonth + "-01" );

                var selEndYear = $scope.currentSelection.endPeriodYear;
                var selEndMonth = $scope.currentSelection.endPeriodMonth;
                var selectedEndPeriod = new Date( selEndYear + "-" + selEndMonth + "-01" );

                if( selectedStartPeriod > selectedEndPeriod )
                {
                    alert( "Start period should not be greater then end period");
                    isValidated = "false";
                    return;
                }
            }

            /*
            else if( $scope.currentReport.id != undefined )
            {
                var date = new Date();
                var currentMonthFirstDay = new Date(date.getFullYear(), date.getMonth(), 1);
                var currentMonthLastDay = new Date(date.getFullYear(), date.getMonth() + 1, 0);


                if( $scope.reportSettingMapping[$scope.currentReport.id].periodType =='monthly')
                {

                    if( $scope.currentReport.period.month === "" || $scope.currentReport.period.month === undefined )
                    {
                        alert( "Please select month");
                        isValidated = "false";
                        return;
                    }
                    else if( $scope.currentReport.period.year === "" || $scope.currentReport.period.year === undefined )
                    {
                        alert( "Please select year");
                        isValidated = "false";
                        return;
                    }

                    else if( $scope.currentReport.period.month != undefined && $scope.currentReport.period.year != undefined  )
                    {
                        var selYear = $scope.currentReport.period.year;
                        var selMonth = $scope.currentReport.period.month;
                        var selectedPeriod = new Date( selYear + "-" + selMonth + "-01" );
                        var lastDayOfSelPeriod = new Date(selectedPeriod.getFullYear(), selectedPeriod.getMonth() + 1, 0);



                        if( lastDayOfSelPeriod > currentMonthLastDay )
                        {
                            alert( "You can not select future period");
                            isValidated = "false";
                            return;
                        }
                    }
                }
                else if( $scope.reportSettingMapping[$scope.currentReport.id].periodType =='start-end' )
                {
                    var selStartDate = new Date( $scope.currentReport.startDate );
                    var selEndDate = new Date( $scope.currentReport.endDate );
                    var startDateYear = $scope.currentReport.startDate.split("-")[0];
                    var endDateYear = $scope.currentReport.endDate.split("-")[0];

                    var diffInYear = endDateYear - startDateYear;

                    if( $scope.currentReport.startDate === "" || $scope.currentReport.startDate === undefined )
                    {
                        alert( "Please select start date");
                        isValidated = "false";
                        return;
                    }
                    else if( $scope.currentReport.endDate === "" || $scope.currentReport.endDate === undefined )
                    {
                        alert( "Please select end date");
                        isValidated = "false";
                        return;
                    }

                    else if( selStartDate > selEndDate )
                    {
                        alert( "Start date should not be greater then end date");
                        isValidated = "false";
                        return;
                    }
                    else if( diffInYear > 5 )
                    {
                        alert( "Date difference should not greater then 5 year");
                        isValidated = "false";
                        return;
                    }
                }

            }
            */
            /*
            else
            {
                //alert(isValidated);
                var selOrgUnit = selection.getSelected();
                var selDataSetUid = $scope.currentSelection.dataSet;
                var selStartPeriod = $scope.currentSelection.startPeriodYear + "" + $scope.currentSelection.startPeriodMonth;
                var selEndPeriod = $scope.currentSelection.endPeriodYear + "" + $scope.currentSelection.endPeriodMonth;

                alert("selOrgUnit - " + selOrgUnit + "- selDataSetUid - " + selDataSetUid + " - selStartPeriod - " + selStartPeriod + " - selEndPeriod - " + selEndPeriod );

                //$window.location.href = "../dhis-web-reporting/generateHtmlReport.action?uid="+$scope.currentReport.id+"&pe="+$scope.currentReport.period.year+""+$scope.currentReport.period.month+"&ou="+ selection.getSelected()+"&sd="+$scope.currentReport.startDate+"&ed="+$scope.currentReport.endDate;
            }
            */

            if( isValidated === "true")
            {
                //alert(isValidated);
                var selOrgUnit = selection.getSelected();
                var selDataSetUid = $scope.currentSelection.dataSet;
                var selStartPeriod = $scope.currentSelection.startPeriodYear + "" + $scope.currentSelection.startPeriodMonth;
                var selEndPeriod = $scope.currentSelection.endPeriodYear + "" + $scope.currentSelection.endPeriodMonth;
                var reportUid = $scope.currentSelection.dataStatusReport;
                var includeZero = $scope.currentSelection.includeZero;
                OrganisationUnitService.getOrganisationUnitLevelLength().then(function(data){
                        $scope.level = data.organisationUnitLevels.length;
                        //console.log( "Level Length is -- " + $scope.level);
                    }
                );

                DataSetService.getDataSetPeriodTypeAndSource( selDataSetUid ).then(function(data){
                        $scope.dataSetPeriodType = data.dataSets[0].periodType;
                        $scope.dataSetSource = data.dataSets[0].organisationUnits;

                        //console.log( "dataSetPeriodType is -- " + $scope.dataSetPeriodType );
                        //console.log( "dataSetSource length  is -- " + $scope.dataSetSource.length );

                        sessionStorage.setItem('selOrgUnit',selOrgUnit);
                        sessionStorage.setItem('selDataSetUid',selDataSetUid);
                        sessionStorage.setItem('dataSetPeriodType',$scope.dataSetPeriodType);
                        sessionStorage.setItem('dataSetSource',$scope.dataSetSource);
                        sessionStorage.setItem('selStartPeriod',selStartPeriod);
                        sessionStorage.setItem('selEndPeriod',selEndPeriod);
                        sessionStorage.setItem('selectedStartPeriod',selectedStartPeriod);
                        sessionStorage.setItem('lastDayOfSelStartPeriod',lastDayOfSelStartPeriod);
                        sessionStorage.setItem('lastDayOfSelEndPeriod',lastDayOfSelEndPeriod);

                        //alert("selOrgUnit - " + selOrgUnit + "- selDataSetUid - " + selDataSetUid + " - selStartPeriod - " + selStartPeriod + " - selEndPeriod - " + selEndPeriod );

                        //window.location.href = '../dhis-web-reports-app/index.html#/data-status-result';
                        //alert( "Report Uid - " + reportUid );

                        $window.location.href = "../dhis-web-reporting/generateHtmlReport.action?uid="+reportUid+"&orgUnitUID="+selOrgUnit+"&dataSetUID="+selDataSetUid+"&dataSetPeriodType="+$scope.dataSetPeriodType
                        +"&startDate="+selStartPeriod+"&endDate="+selEndPeriod+"&includeZero="+includeZero ;


                        /*
                        $window.location.href = "../dhis-web-reporting/generateHtmlReport.action?uid=DUZxcstfQNm&orgUnitUID="+selOrgUnit+"&dataSetUID="+selDataSetUid+"&dataSetPeriodType="+$scope.dataSetPeriodType
                        +"&startDate="+selStartPeriod+"&endDate="+selEndPeriod ;
                        */

                    }
                );


                //$window.location.href = "../dhis-web-reporting/generateHtmlReport.action?uid="+$scope.currentReport.id+"&pe="+$scope.currentReport.period.year+""+$scope.currentReport.period.month+"&ou="+ selection.getSelected()+"&sd="+$scope.currentReport.startDate+"&ed="+$scope.currentReport.endDate;
            }


        }
		
		//Results
		
		$scope.allOrgUnitChildren = [];
		$scope.allPeriods = [];
		$scope.compulsoryDECount = 1;
		$scope.totalOrgLevels = 1;
		$scope.grandParentLevel = 1;
		$scope.grandParentName="";
        //*****************************************************************************
        //GET END PERIOD
        //*****************************************************************************
		function getEndPeriod(startPeriodYear,startPeriodMonth)
		{
			var date="",EndDate;
			if(startPeriodMonth=="01" || startPeriodMonth=="03" || startPeriodMonth=="05" || startPeriodMonth=="07" || startPeriodMonth=="08" || startPeriodMonth=="10" || startPeriodMonth=="12")
			{
                date=31;
			}
			else if(startPeriodMonth=="02")
			{
				if(startPeriodYear/4==0)
				{
					date=29;
				}
				else
				{
					date=28;
				}

			}
            else if(startPeriodMonth=="04" || startPeriodMonth=="06" || startPeriodMonth=="09" || startPeriodMonth=="11")
            {
                date=30;
            }

            EndDate=startPeriodYear+startPeriodMonth+date;

			return EndDate;
		}


		//*****************************************************************************
		//Data Summary Result
		//*****************************************************************************
					
		$scope.showDataSummary = function(){

            $("#coverLoad").show();
			$("#headTitle").html("Data Summary - Data Sets");
			$("#tableContent").html("");
			$("#modal-header").fadeIn();
			$("#modal-body").delay(300).fadeIn();
			$("#modal-footer").delay(600).fadeIn();
			$("#resultModal").delay(900).fadeIn();
			$("#dwnLoad").fadeOut();
			
			//passing variables to query
			var selOrgUnit = selection.getSelected();
			var selDataSetUid = $scope.currentSelection.dataSet;
			var selStartPeriod = $scope.currentSelection.startPeriodYear + "" + $scope.currentSelection.startPeriodMonth + "01";

			var selEndPeriod = getEndPeriod($scope.currentSelection.endPeriodYear, $scope.currentSelection.endPeriodMonth);

			var includeZero = $scope.currentSelection.includeZero;
			$scope.organisationUnits_id=[];
			
			//var url_1=;
			$.get( "../api/dataSets/"+ selDataSetUid +".json",function(json){
						for(var i=0;i<json.organisationUnits.length;i++)
						{
							for(var j=0;j<$scope.allOrgUnitChildren.length;j++)
							{
								if(json.organisationUnits[i].id==$scope.allOrgUnitChildren[j].uid)
								$scope.organisationUnits_id.push(json.organisationUnits[i].id);
							}
						}
					});
					
			if( includeZero )
				var url = $scope.basicUrl + $scope.dataSummarySV + "/data.json?";
			else
				var url = $scope.basicUrl + $scope.dataSummaryExZeroSV + "/data.json?";
			
			url+= "var=compulsoryDECount:" + $scope.compulsoryDECount + ",dataSetUid:" + selDataSetUid + ",orgUnitUid:" + selOrgUnit + ",startDate:" + selStartPeriod + ",endDate:" + selEndPeriod + ",orgUnitUids:" +$scope.organisationunitid_1[0] ;	
			
			console.log(url);
			
			$.get(url, function(data){
			//$.get("summary.json", function(data){
				var summaryData = data.rows;
				
				var totPeriods = $scope.allPeriods.length + 2 ;
				
				var htmlString = "";
				htmlString += "<tr style = 'background:#eee'><td colspan = '"+ totPeriods +"'  style='padding:2px 15px'> <b>Root Organisation Unit : </b>" + $scope.currentSelection.orgUnitName +"</td></tr>";
				htmlString += "<tr style = 'background:#eee'><td colspan = '"+ totPeriods +"'  style='padding:2px 15px'> <b>Data Set : </b>" + $("#dataSetId option:selected").text() +"</td></tr>";
				var durationString = $scope.monthString($scope.currentSelection.startPeriodYear + "-" + $scope.currentSelection.startPeriodMonth ) + " to " + $scope.monthString($scope.currentSelection.endPeriodYear + "-" + $scope.currentSelection.endPeriodMonth );	
				htmlString += "<tr style = 'background:#eee'><td colspan = '"+ totPeriods +"'  style='padding:2px 15px'> <b>Duration : </b>" + durationString +"</td></tr>";
				htmlString += "<tr style = 'background:#fff'><td colspan = '"+ totPeriods +"' ></td></tr>";
				
				htmlString += "<tr><td  style = 'background:#99FF99;padding:0'></td><td colspan='"+ (totPeriods-1) +"'  style='padding:0 15px'> Entered </td></tr>";
				htmlString += "<tr><td  style = 'background:#FFCCCC;padding:0'></td><td colspan='"+ (totPeriods-1) +"'  style='padding:0 15px'> Not Entered </td></tr>";
				htmlString += "<tr><td  style = 'background:#eee;padding:0'></td><td colspan='"+ (totPeriods-1) +"'  style='padding:0 15px'> Not Assigned </td></tr>";
				
				htmlString += "<tr style = 'background:#fff'><td colspan = '"+ totPeriods +"' ></td></tr>";
				
				htmlString += "<tr style = 'background:#fff'><td colspan = '"+ totPeriods +"' ></td></tr>";
				
				htmlString += "<tr style = 'background:#ddd'><th style='min-width:150px'>Organisation Units</th>";
				
				$scope.allPeriods.forEach(function(p){
					htmlString += "<th style='min-width:100px;max-width:100px;'>" + $scope.monthString( p[0])  + "</th>";					
				});
				
				htmlString += "</tr>";
				var ParentName_1="",ParentName_2="",ParentName_3="",ParentName_4="",ParentName_5="",ParentName_6="",ParentName_7="",ParentName_8="";
				var currentStatus= 0;
				var currentColor = "#eeeeee";
				var statusText = "";
				
				$scope.final_org=[];
					
				$scope.allOrgUnitChildren.forEach(function(org){
					
					/**$scope.organisationUnits_id.forEach(function(id){
					{
						if($scope.organisationUnits_id[i]==org.uid)
						$scope.final_org.push($scope.organisationUnits_id[i]);
							
					});**/
					
					var orgNameWithBreaks = "";
					var totBreaks = org.level - $scope.grandParentLevel;
					var grandParentName1=$scope.grandParentName;
					
					if(totBreaks==0)
					{
						ParentName_1=org.name;
						orgNameWithBreaks=ParentName_1;
					}
					else if(totBreaks==1)
					{
						ParentName_2=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks = ParentName_1+" / "+ParentName_2;
					}
					else if(totBreaks==2)
					{
						ParentName_3=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3;
					}
					else if(totBreaks==3)
					{
						ParentName_4=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3+" / "+ParentName_4;
					}
					else if(totBreaks==4)
					{
						ParentName_4=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3+" / "+ParentName_4+" / "+ParentName_5;
					}
					else if(totBreaks==5)
					{
						ParentName_6=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3+" / "+ParentName_4+" / "+ParentName_5+" / "+ParentName_6;
					}
					else if(totBreaks==6)
					{
						ParentName_7=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3+" / "+ParentName_4+" / "+ParentName_5+" / "+ParentName_6+" / "+ParentName_7;
					}
					$scope.compulsoryDECount = 1;
		
					
					
					//orgNameWithBreaks += org.name;
					
					htmlString += "<tr><td style='padding:0 15px'>" + orgNameWithBreaks + "</td>";
					$scope.final_org_id=[];
					/**$scope.organisationUnits_id.forEach(function(data_id){
							if(data_id==org.uid)
							{
								$scope.final_org_id.push(data_id);
								currentStatus = 0;
								statusText = "";
								currentColor = "#ded2e3";//light purple
								
							}
							else{
								currentStatus = 0;
								statusText = "";
								currentColor = "#eeeeee";//grey
						
							}
							});**/
						$scope.allPeriods.forEach(function(pr){
					
							
								if($scope.GetUID($scope.organisationUnits_id,org.uid))
								{
											currentStatus = 0;
											statusText = "";
											currentColor = "#FFCCCC";//light purple
									
										var val= $scope.isOrgFound_1( org.uid, summaryData ) ;
											if( val)
											{
											currentStatus = 0;
											currentColor = "#FFCCCC";//pink
											statusText = "0(" + $scope.compulsoryDECount + ")";
											
											summaryData.forEach(function(sdata){
												
												if( sdata[3] == org.uid && sdata[1] == pr[0] )
												{
													currentStatus= sdata[4]/$scope.compulsoryDECount;
													statusText = sdata[4] + "(" + $scope.compulsoryDECount + ")";
													//console.log("currentStatus"+currentStatus+" "+"statusText"+statusText)		
													}
													
											
										});
										
										if( currentStatus >= 1 )
											currentColor = "#99FF99";//green	
									}
									//var index=i;

									//$scope.organisationUnits_id.splice(index, 1);
									
								}
								else
								{
									currentStatus = 0;
									statusText = "";
									currentColor = "#eeeeee";//grey
								}
								
							htmlString += "<td style='background:"+ currentColor +"'  style='padding:2px 15px;border-color: #0000ff;'></td>";
					
							})
						htmlString += "</tr>";					
				});
               $("#tableContent").html(htmlString);
				$("#dwnLoad").fadeIn();
				$("#coverLoad").hide();
			});
		};
	
		//*****************************************************************************
		//Data Status Result
		//*****************************************************************************
		
		
		$scope.showDataStatus = function(){
			$("#tableContent").html("");
			$("#coverLoad").show();
			$("#headTitle").html("Data Status -  Data Sets");
			$("#modal-header").fadeIn();
			$("#modal-body").delay(300).fadeIn();
			$("#modal-footer").delay(600).fadeIn();
			$("#resultModal").delay(900).fadeIn();
			$("#dwnLoad").fadeOut();
			
			
			//passing variables to query
			var selOrgUnit = selection.getSelected();
			var selDataSetUid = $scope.currentSelection.dataSet;
			var selStartPeriod = $scope.currentSelection.startPeriodYear + "" + $scope.currentSelection.startPeriodMonth + "01";
            var selEndPeriod = getEndPeriod($scope.currentSelection.endPeriodYear, $scope.currentSelection.endPeriodMonth);
            var includeZero = $scope.currentSelection.includeZero;
			$scope.organisationUnits_id=[];
			$scope.catcombo=[];$scope.dataSetElement_len="";

            newurl1=$scope.basicUrl + $scope.getdataElementid + "/data.json?var=dataSetID:"+selDataSetUid+"";

            $.get(newurl1, function(data){

                $scope.dataSetID=data.rows[0];


            });
			var newurl=$scope.basicUrl+$scope.CategoryComboId+"/data.json?var=datasetelementid:"+$scope.dataSetID[0]+"";
            $.get(newurl ,function(json) {

                $scope.compulsoryDECount=json.height;


            });


                $.get("../api/dataSets/"+ selDataSetUid +".json?fields=*,dataSetElements[id,categoryCombo]&skipPaging=true" ,function(json){


						for(var i=0;i<json.organisationUnits.length;i++)
						{
							for(var j=0;j<$scope.allOrgUnitChildren.length;j++)
							{
								if(json.organisationUnits[i].id==$scope.allOrgUnitChildren[j].uid)
								$scope.organisationUnits_id.push(json.organisationUnits[i].id);
							}
						}
					});

				$scope.OrgUnit_uid	=$scope.filteredOrgUnitList.toString();
			if( includeZero )
				var url = $scope.basicUrl + $scope.dataStatusSV + "/data.json?";
			else
				var url = $scope.basicUrl + $scope.dataStatusExZeroSV + "/data.json?";
			
			url+= "var=compulsoryDECount:" + $scope.compulsoryDECount + ",dataSetUid:" + selDataSetUid + ",orgUnitUids:" +$scope.organisationunitid_1[0] + ",startDate:" + selStartPeriod + ",endDate:" + selEndPeriod;	;	
			
			console.log(url);
			
			$.get(url, function(data){
			//$.get("status.json", function(data){
				var summaryData = data.rows;
				
				var totPeriods = $scope.allPeriods.length + 2 ;
				
				var htmlString = "";
				htmlString += "<tr style = 'background:#eee'><td colspan = '"+ totPeriods +"'  style='padding:2px 15px'> <b>Root Organisation Unit : </b>" + $scope.currentSelection.orgUnitName +"</td></tr>";
				htmlString += "<tr style = 'background:#eee'><td colspan = '"+ totPeriods +"'  style='padding:2px 15px'> <b>Data Set : </b>" + $("#dataSetId option:selected").text() +"</td></tr>";
				htmlString += "<tr style = 'background:#eee'><td colspan = '"+ totPeriods +"'  style='padding:2px 15px'> <b>Total Data Elements : </b>" + $scope.compulsoryDECount  +"</td></tr>";
				var durationString = $scope.monthString($scope.currentSelection.startPeriodYear + "-" + $scope.currentSelection.startPeriodMonth ) + " to " + $scope.monthString($scope.currentSelection.endPeriodYear + "-" + $scope.currentSelection.endPeriodMonth );	
				htmlString += "<tr style = 'background:#eee'><td colspan = '"+ totPeriods +"'  style='padding:2px 15px'> <b>Duration : </b>" + durationString +"</td></tr>";
				htmlString += "<tr style = 'background:#fff'><td colspan = '"+ totPeriods +"' ></td></tr>";
				
				htmlString += "<tr><td  style = 'background:#66FF99;padding:0'></td><td colspan='"+ (totPeriods-1) +"'  style='padding:0 15px'> Completed (75+)% </td></tr>";
				htmlString += "<tr><td  style = 'background:#FF99FF;padding:0'></td><td colspan='"+ (totPeriods-1) +"'  style='padding:0 15px'> Partially Completed (41-75)% </td></tr>";
				htmlString += "<tr><td  style = 'background:#FFFF99;padding:0'></td><td colspan='"+ (totPeriods-1) +"'  style='padding:0 15px'> Not Completed(1-40)% </td></tr>";
				htmlString += "<tr><td  style = 'background:#FF9999;padding:0'></td><td colspan='"+ (totPeriods-1) +"'  style='padding:0 15px'> Not Entered (0)% </td></tr>";
				htmlString += "<tr><td  style = 'background:#eee;padding:0'></td><td colspan='"+ (totPeriods-1) +"'  style='padding:0 15px'> Not Assigned </td></tr>";
				
				
				htmlString += "<tr style = 'background:#fff'><td colspan = '"+ totPeriods +"' ></td></tr>";
				
				htmlString += "<tr style = 'background:#ddd'><th style='min-width:150px'>Organisation Units</th>";
				
				$scope.allPeriods.forEach(function(p){
					htmlString += "<th style='min-width:100px;max-width:100px;'>" + $scope.monthString( p[0])  + "</th>";					
				});
				
				htmlString += "</tr>";
				var ParentName_1="",ParentName_2="",ParentName_3="",ParentName_4="",ParentName_5="",ParentName_6="",ParentName_7="",ParentName_8="";
				var currentStatus= 0;
				var currentColor = "#eeeeee";
				var statusText = "";
				
				$scope.allOrgUnitChildren.forEach(function(org){
					
					var orgNameWithBreaks = "";
					var totBreaks = org.level - $scope.grandParentLevel;
					
					for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks += "|-------------";
					if(totBreaks==0)
					{
						ParentName_1=org.name;
						orgNameWithBreaks=ParentName_1;
					}
					else if(totBreaks==1)
					{
						ParentName_2=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks = ParentName_1+" / "+ParentName_2;
					}
					else if(totBreaks==2)
					{
						ParentName_3=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3;
					}
					else if(totBreaks==3)
					{
						ParentName_4=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3+" / "+ParentName_4;
					}
					else if(totBreaks==4)
					{
						ParentName_4=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3+" / "+ParentName_4+" / "+ParentName_5;
					}
					else if(totBreaks==5)
					{
						ParentName_6=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3+" / "+ParentName_4+" / "+ParentName_5+" / "+ParentName_6;
					}
					else if(totBreaks==6)
					{
						ParentName_7=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3+" / "+ParentName_4+" / "+ParentName_5+" / "+ParentName_6+" / "+ParentName_7;
					}
					
		
					//orgNameWithBreaks += org.name;
					
					htmlString += "<tr><td style='padding:0 15px'>" + orgNameWithBreaks + "</td>";
									
					$scope.allPeriods.forEach(function(pr){
						if($scope.GetUID($scope.organisationUnits_id,org.uid))
								{
											currentStatus = 0;
											statusText = "";
											currentColor = "#FF9999";//light purple
									
									 
									if( $scope.isOrgFound_1( org.uid, summaryData ) )
									{
										statusText = 0;//"0(" + $scope.compulsoryDECount + ")";
										
										summaryData.forEach(function(sdata){
											if( sdata[3] == org.uid && sdata[1] == pr[0] )
											{
												currentStatus= sdata[4]/$scope.compulsoryDECount*100;
												statusText =  Math.ceil(currentStatus) ;//+ "(" + $scope.compulsoryDECount + ")";
											}
										});
										
										if( currentStatus >= 75 )
											currentColor = "#66FF99";
										else if( currentStatus >= 41 )
											currentColor = "#FF99FF";
										else if( currentStatus >= 1 )
											currentColor = "#FFFF99";
										else
											currentColor = "#FF9999";	
									}
								}
								else
								{
									currentStatus = "";
									statusText = "";
									currentColor = "#eeeeee";//grey
								}
								
						htmlString += "<td style='background:"+ currentColor +";padding:0 15px'>"+statusText +"</td>";
					});
										
					htmlString += "</tr>";
				});
				
				$("#tableContent").html(htmlString);
				$("#dwnLoad").fadeIn();
				$("#coverLoad").hide();
			});
		};
		
		//*****************************************************************************
		//Data Status - DEG Result
		//*****************************************************************************
		$scope.showDataStatusDEG = function(){
			$("#coverLoad").show();
			$("#headTitle").html("Data Status - Data Element Groups");
			$("#resultModal").modal('show');	
			$("#dwnLoad").fadeOut();
		};
		
		//*****************************************************************************
		//User Details Result
		//*****************************************************************************
		$scope.showUserDetails = function(){
			$("#tableContent").html("");
			$("#coverLoad").show();
			$("#headTitle").html("User Details - Latest");
			$("#modal-header").fadeIn();
			$("#modal-body").delay(300).fadeIn();
			$("#modal-footer").delay(600).fadeIn();
			$("#resultModal").delay(900).fadeIn();
			$("#dwnLoad").fadeOut();
			
			//passing variables to query
			var selOrgUnit = selection.getSelected();
			var selDataSetUid = $scope.currentSelection.dataSet;
			var selStartPeriod = $scope.currentSelection.startPeriodYear + "" + $scope.currentSelection.startPeriodMonth + "01";
			var selEndPeriod = $scope.currentSelection.endPeriodYear + "" + $scope.currentSelection.endPeriodMonth + "01";	
			var includeZero = $scope.currentSelection.includeZero;
			
			if( includeZero )
				var url = $scope.basicUrl + $scope.userDetailsSV + "/data.json?";
			else
				var url = $scope.basicUrl + $scope.userDetailsExZeroSV + "/data.json?";
			
			url+= "var=dataSetUid:" + selDataSetUid + ",orgUnitUid:" + selOrgUnit  + ",startDate:" + selStartPeriod + ",endDate:" + selEndPeriod;	;	
			
			console.log(url);
			
			$.get(url, function(data){
			//$.get("views.json", function(data){				
				var summaryData = data.rows;
				
				var currentPeriodId = -1;
				var currentPeriodStartDate = -1;
				var currentUser = "";
				
				
				var summaryData = data.rows;
				
				var totPeriods = $scope.allPeriods.length + 2 ;
				
				var htmlString = "";
				htmlString += "<tr style = 'background:#eee'><td colspan = '"+ totPeriods +"'  style='padding:2px 15px'> <b>Root Organisation Unit : </b>" + $scope.currentSelection.orgUnitName +"</td></tr>";
				htmlString += "<tr style = 'background:#eee'><td colspan = '"+ totPeriods +"'  style='padding:2px 15px'> <b>Data Set : </b>" + $("#dataSetId option:selected").text() +"</td></tr>";
				var durationString = $scope.monthString($scope.currentSelection.startPeriodYear + "-" + $scope.currentSelection.startPeriodMonth ) + " to " + $scope.monthString($scope.currentSelection.endPeriodYear + "-" + $scope.currentSelection.endPeriodMonth );	
				htmlString += "<tr style = 'background:#eee'><td colspan = '"+ totPeriods +"'  style='padding:2px 15px'> <b>Duration : </b>" + durationString +"</td></tr>";
				htmlString += "<tr style = 'background:#fff'><td colspan = '"+ totPeriods +"' ></td></tr>";
				
				
				htmlString += "<tr style = 'background:#fff'><td colspan = '3' ></td></tr>";
				
				htmlString += "<tr style = 'background:#ddd'><th style='min-width:150px'>Organisation Units</th>";
				
				$scope.allPeriods.forEach(function(p){
					htmlString += "<th style='min-width:100px;max-width:100px;'>" + $scope.monthString( p[0])  + "</th>";					
				});
				
				htmlString += "</tr>";
				
				var currentUser= "";
				var currentColor = "#fff";
				var ParentName_1="",ParentName_2="",ParentName_3="",ParentName_4="",ParentName_5="",ParentName_6="",ParentName_7="",ParentName_8="";
				
				$scope.allOrgUnitChildren.forEach(function(org){
					
					var orgNameWithBreaks = "";
					
					var orgNameWithBreaks = "";
					var totBreaks = org.level - $scope.grandParentLevel;
					
					for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks += "|-------------";
					if(totBreaks==0)
					{
						ParentName_1=org.name;
						orgNameWithBreaks=ParentName_1;
					}
					else if(totBreaks==1)
					{
						ParentName_2=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks = ParentName_1+" / "+ParentName_2;
					}
					else if(totBreaks==2)
					{
						ParentName_3=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3;
					}
					else if(totBreaks==3)
					{
						ParentName_4=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3+" / "+ParentName_4;
					}
					else if(totBreaks==4)
					{
						ParentName_4=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3+" / "+ParentName_4+" / "+ParentName_5;
					}
					else if(totBreaks==5)
					{
						ParentName_6=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3+" / "+ParentName_4+" / "+ParentName_5+" / "+ParentName_6;
					}
					else if(totBreaks==6)
					{
						ParentName_7=org.name;
						for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks =  ParentName_1+" / "+ParentName_2+" / "+ParentName_3+" / "+ParentName_4+" / "+ParentName_5+" / "+ParentName_6+" / "+ParentName_7;
					}
					
					//var totBreaks = org.level - $scope.grandParentLevel;
					
					//for( var x = 0 ; x < totBreaks ; x++ )
						//orgNameWithBreaks += "|-------------";
					
					//orgNameWithBreaks += org.name;
					
					htmlString += "<tr><td style='padding:0 15px'>" + orgNameWithBreaks + "</td>";
					$scope.allPeriods.forEach(function(pr){
						currentUser = "";
						currentColor = "#eee";
						
						if( $scope.isOrgFound( org.uid, summaryData ) )
						{
							currentUser = "-";
							currentColor = "#fff";
							
							summaryData.forEach(function(sdata){
								if( sdata[0] == org.uid && sdata[1] == pr[0] )
								{
									currentUser= sdata[2];
								}
							});
						}
						htmlString += "<td style='background:"+ currentColor +";padding:0 15px'>" + currentUser + "</td>";
					});
					htmlString += "</tr>";
				});
				
				$("#tableContent").html(htmlString);
				$("#dwnLoad").fadeIn();
				$("#coverLoad").hide();
			});
		};
		
		//*****************************************************************************
		//Validation Status Result
		//*****************************************************************************
		$scope.showValidationStatus = function(){
			$("#coverLoad").show();
			$("#headTitle").html("Validation Status");
			$("#resultModal").modal('show');			
		};
		
		//*****************************************************************************
		//Comments Result
		//*****************************************************************************
		$scope.showComments = function(){
			$("#tableContent").html("");
			$("#coverLoad").show();
			$("#headTitle").html("Comments");
			$("#modal-header").fadeIn();
			$("#modal-body").delay(300).fadeIn();
			$("#modal-footer").delay(600).fadeIn();
			$("#resultModal").delay(900).fadeIn();
			$("#dwnLoad").fadeOut();
			
			//passing variables to query
			var selOrgUnit = selection.getSelected();
			var selDataSetUid = $scope.currentSelection.dataSet;
			var selStartPeriod = $scope.currentSelection.startPeriodYear + "" + $scope.currentSelection.startPeriodMonth + "01";
			var selEndPeriod = $scope.currentSelection.endPeriodYear + "" + $scope.currentSelection.endPeriodMonth + "01";	
			var includeZero = $scope.currentSelection.includeZero;
			
			if( includeZero )
				var url = $scope.basicUrl + $scope.commentsSV + "/data.json?";
			else
				var url = $scope.basicUrl + $scope.commentsExZeroSV + "/data.json?";
			
			url+= "var=dataSetUid:" + selDataSetUid + ",orgUnitUid:" + selOrgUnit  + ",startDate:" + selStartPeriod + ",endDate:" + selEndPeriod;	;	
			
			console.log(url);
			
			$.get(url, function(data){
			//$.get("comments.json", function(data){				
				var summaryData = data.rows;
				
				var currentPeriodId = -1;
				var currentPeriodStartDate = -1;
				var currentUser = "";
				
				
				var summaryData = data.rows;
				
				var totPeriods = $scope.allPeriods.length + 2 ;
				
				var htmlString = "";
				htmlString += "<tr style = 'background:#eee'><td colspan = '"+ totPeriods +"'  style='padding:2px 15px'> <b>Root Organisation Unit : </b>" + $scope.currentSelection.orgUnitName +"</td></tr>";
				htmlString += "<tr style = 'background:#eee'><td colspan = '"+ totPeriods +"'  style='padding:2px 15px'> <b>Data Set : </b>" + $("#dataSetId option:selected").text() +"</td></tr>";
				var durationString = $scope.monthString($scope.currentSelection.startPeriodYear + "-" + $scope.currentSelection.startPeriodMonth ) + " to " + $scope.monthString($scope.currentSelection.endPeriodYear + "-" + $scope.currentSelection.endPeriodMonth );	
				htmlString += "<tr style = 'background:#eee'><td colspan = '"+ totPeriods +"'  style='padding:2px 15px'> <b>Duration : </b>" + durationString +"</td></tr>";
				htmlString += "<tr style = 'background:#fff'><td colspan = '"+ totPeriods +"' ></td></tr>";
				
				
				htmlString += "<tr style = 'background:#fff'><td colspan = '"+ totPeriods +"' ></td></tr>";
				
				htmlString += "<tr style = 'background:#ddd'><th style='min-width:150px'>Organisation Units</th>";
				
				$scope.allPeriods.forEach(function(p){
					htmlString += "<th style='min-width:100px;max-width:100px;'>" + $scope.monthString( p[0])  + "</th>";					
				});
				
				htmlString += "</tr>";
				
				var currentComment= "";
				var currentColor = "#fff";
				
				$scope.allOrgUnitChildren.forEach(function(org){
					var orgNameWithBreaks = "";
					var totBreaks = org.level - $scope.grandParentLevel;
					
					for( var x = 0 ; x < totBreaks ; x++ )
						orgNameWithBreaks += "|-------------";
					
					orgNameWithBreaks += org.name;
					
					htmlString += "<tr><td style='padding:0 15px'>" + orgNameWithBreaks + "</td>";
					$scope.allPeriods.forEach(function(pr){
						currentComment = "";
						currentColor = "#eee";
						if( $scope.isOrgFound( org.uid, summaryData ) )
						{
								currentComment = "-";
								currentColor = "#fff";
								
								summaryData.forEach(function(sdata){
									if( sdata[0] == org.uid && sdata[1] == pr[0] )
									{
										currentComment= sdata[2];
									}
									if( currentComment == "" )
									currentComment = "-";
								htmlString += "<td style='background:"+ currentColor +";padding:0 15px'>" + currentComment + "</td>";
								});
								
								
						}
						
					});
					htmlString += "</tr>";
				});
				
				$("#tableContent").html(htmlString);
				$("#dwnLoad").fadeIn();
				$("#coverLoad").hide();
			});
		};
		
		// common functions
		
		//*****************************************************************************
		//Check whether an organisation found in the result set
		//*****************************************************************************
		$scope.isOrgFound = function( org, rs ){
			var isFound = false;			
			rs.forEach(function(sdata){				
				if( sdata[0] == org )
					isFound = true;
				
				//console.log( sdata[0] + " -- " + org + " -- " + isFound );
			});
			return isFound;
		};
		
		$scope.isOrgFound_1 = function( org, rs ){
			var isFound = false;			
			rs.forEach(function(sdata){				
				if( sdata[3] == org )
					isFound = true;
				
				//console.log( sdata[0] + " -- " + org + " -- " + isFound );
			});
			return isFound;
		};
		
		$scope.GetUID=function(orguid,uid){
			var isFound = false;	
			orguid.forEach(function(id){
				if(id==uid)
					isFound = true;
			});
				return isFound;			
			
		}
		//*****************************************************************************
		//Show and Hide buttons
		//*****************************************************************************
		$scope.showHideButtons = function(){
			$("#loading").show();
			if( 
					$scope.currentSelection.orgUnitName &&
					$scope.currentSelection.dataSet  &&
					$scope.currentSelection.startPeriodMonth &&
					$scope.currentSelection.startPeriodYear &&
					$scope.currentSelection.endPeriodMonth &&
					$scope.currentSelection.endPeriodYear		
			)
			{
				$.get("../api/organisationUnits/"+ selection.getSelected() +".json?fields=id,name,level,children[id,name,level,children[id,name,level,children[id,name,level,children[id,name,level,children[id,name,level,children[id,name,level,children[id,name,level,children[id,name,level]]]]]" , function(data){
					//console.log( "../api/organisationUnits/"+ selection.getSelected() +".json?fields=id,name,level,children[id,name,level,children[id,name,level,children[id,name,level,children[id,name,level,children[id,name,children[id,name,children[id,name,children[id,name]]]]]" );
					$scope.extractOrgUnits(data);
					//console.log(data);
					//console.log( $scope.allOrgUnitChildren );
				})
				.then(function(){
					$.get("../api/dataSets/"+ $scope.currentSelection.dataSet +".json" ,function(json){
						if( json.dataSetElements.length == 0 )
							$scope.compulsoryDECount = 1;
						else
						$scope.compulsoryDECount = json.dataSetElements.length;
					});
				})
				.then(function(){
					var selStartPeriod = $scope.currentSelection.startPeriodYear + "" + $scope.currentSelection.startPeriodMonth + "01";
					var selEndPeriod = $scope.currentSelection.endPeriodYear + "" + $scope.currentSelection.endPeriodMonth + "01";
					
					$.get("../api/sqlViews/"+ $scope.periodsSV +"/data.json?var=startDate:"+ selStartPeriod +",endDate:" + selEndPeriod +",dataSetUidForLevel:" + $scope.currentSelection.dataSet , function(pr){						
						$scope.allPeriods = pr.rows;
						//console.log( $scope.allPeriods )
						
						$("#btn1").fadeIn();
						$("#btn2").fadeIn();
						$("#btn3").fadeIn();
						$("#btn4").fadeIn();
						$("#btn5").fadeIn();
						$("#btn6").fadeIn();
						$("#loading").hide();
					});
				});
			}
			else
			{
				$("#btn1").fadeOut();
				$("#btn2").fadeOut();
				$("#btn3").fadeOut();
				$("#btn4").fadeOut();
				$("#btn5").fadeOut();
				$("#btn6").fadeOut();
				$("#loading").hide();
			}
		};
		
		
		//*****************************************************************************
		//Extracting orgUnit meta data for labelling purpose
		//*****************************************************************************		
		$scope.extractOrgUnits = function( data ){
			$scope.allOrgUnitChildren = [];
			var grandParent = {};
			grandParent.uid = data.id; grandParent.name = data.name, grandParent.level = data.level;
			$scope.grandParentLevel = data.level;
			$scope.grandParentName=data.name;
			$scope.allOrgUnitChildren.push(grandParent);
			
			$scope.totalOrgLevels = 1;
			
			//level 2
			data.children.forEach(function(o){
				var orgUnit = {};
				orgUnit.uid = o.id; orgUnit.name = o.name, orgUnit.level = o.level;				
				$scope.allOrgUnitChildren.push(orgUnit);
				
				if( $scope.totalOrgLevels < 2 )
					$scope.totalOrgLevels = 2;
				
				//level3
				o.children.forEach(function(a){
					var orgUnit2 = {};
					orgUnit2.uid = a.id; orgUnit2.name = a.name, orgUnit2.level = a.level;
					$scope.allOrgUnitChildren.push(orgUnit2);
					
					if( $scope.totalOrgLevels < 3 )
						$scope.totalOrgLevels = 3;				
					
					//level4
					a.children.forEach(function(b){
						var orgUnit3 = {};
						orgUnit3.uid = b.id; orgUnit3.name = b.name, orgUnit3.level = b.level;
						$scope.allOrgUnitChildren.push(orgUnit3);
					
						if( $scope.totalOrgLevels < 4 )
							$scope.totalOrgLevels = 4;
				
						//level5
						b.children.forEach(function(c){
							var orgUnit4 = {};
							orgUnit4.uid = c.id; orgUnit4.name = c.name, orgUnit4.level = c.level;
							$scope.allOrgUnitChildren.push(orgUnit4);
					
							if( $scope.totalOrgLevels < 5 )
								$scope.totalOrgLevels = 5;
				
							//level6
							c.children.forEach(function(d){
								var orgUnit5 = {};
								orgUnit5.uid = d.id; orgUnit5.name = d.name, orgUnit5.level = d.level;
								$scope.allOrgUnitChildren.push(orgUnit5);
						
								if( $scope.totalOrgLevels < 6 )
									$scope.totalOrgLevels = 6;
				
								//level7
								d.children.forEach(function(e){
									var orgUnit6 = {};
									orgUnit6.uid = e.id; orgUnit6.name = e.name, orgUnit6.level = e.level;
									$scope.allOrgUnitChildren.push(orgUnit6);
														
									if( $scope.totalOrgLevels < 7 )
										$scope.totalOrgLevels = 7;				
				
									//level8
									e.children.forEach(function(f){
										var orgUnit7 = {};
										orgUnit7.uid = f.id; orgUnit7.name = f.name, orgUnit7.level = f.level;
										$scope.allOrgUnitChildren.push(orgUnit7);								
								
										if( $scope.totalOrgLevels < 8 )
											$scope.totalOrgLevels = 8;
				
									});
								});
							});
						});
					});
				});
			});
		};
		//*****************************************************************************
		//Format Date
		//*****************************************************************************
		$scope.formatDate = function( date ){
			var dateString = "";
			dateString += date.getDate()+"/";
			dateString += (date.getMonth() + 1 ) +"/";
			dateString += date.getFullYear() + " at ";
			
			if( date.getHours() > 12 )
				dateString += ( date.getHours() - 12 ) + ":" + date.getMinutes() + "PM";
			else if( date.getHours() == 12 )
				dateString += "12:" + date.getMinutes() + "PM";
			else
				dateString += date.getHours() + ":" + date.getMinutes() + "AM";
			
			return dateString;
		};
		
		//*****************************************************************************
		//Month String
		//*****************************************************************************
		$scope.monthString = function( dte ){
			
			var monthNumber = parseInt(dte.split("-")[1]);
			var monthName = "";
			
			if( monthNumber == 1)
				monthName = "Jan";
			else if( monthNumber == 2)
				monthName = "Feb";
			else if( monthNumber == 3)
				monthName = "Mar";
			else if( monthNumber == 4)
				monthName = "Apr";
			else if( monthNumber == 5)
				monthName = "May";
			else if( monthNumber == 6)
				monthName = "Jun";
			else if( monthNumber == 7)
				monthName = "Jul";
			else if( monthNumber == 8)
				monthName = "Aug";
			else if( monthNumber == 9)
				monthName = "Sep";
			else if( monthNumber == 10)
				monthName = "Oct";
			else if( monthNumber == 11)
				monthName = "Nov";
			else if( monthNumber == 12)
				monthName = "Dec";
			
			return monthName + " " + dte.split("-")[0];
		};
    });