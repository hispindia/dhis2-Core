/* global excelUpload, angular */

$.ajaxSetup({
    async: false
});
var favorites = [];
var length1;
var length2;
var length3;
var count = 0;

//Controller for excel importing
excelUpload.controller('ImportFacilitywiseController',
    function ($rootScope,
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
        DialogService) {

        $scope.orgUnitGroups = {};
        $scope.dataSets = {};
        $scope.templates = {};
        $scope.history = {};

        $scope.engAddress = ["", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"];

        $scope.confirmedUploads = [];

		/***** RETRIEVING ROOT JSON AND NEEDED DATA ***************************************** **/

        //templates
        $("#templateProgress").html("Retrieving all the saved templates...");
        ExcelMappingService.get('Excel-import-app-templates').then(function (tem) {
            if (!jQuery.isEmptyObject(tem))
                $scope.templates = tem;
            else
                $scope.templates = { templates: [] };

            console.log($scope.templates);

            //templates
            $("#templateProgress").html("Retrieving all the organisation units mapping data...");
            ExcelMappingService.get('Excel-import-app-orgunit-mapping').then(function (oum) {

                //history
                $("#templateProgress").html("Retrieving all the import history...");
                ExcelMappingService.get('Excel-import-app-history').then(function (his) {
                    $scope.history = jQuery.isEmptyObject(his) ? JSON.parse('{"history" : []}') : his;
                    console.log(his);
                   

                    $("#templateProgress").html("Fetching organisation unit groups...");
                    $("#templateProgress").html("Fetching all the data sets...");
        $.when(
            $.getJSON("../api/organisationUnitGroups.json?paging=false", {
                format: "json"
            }),
            
            $.getJSON("../api/dataSets.json?paging=false", {
                format: "json"
            }),
            $.getJSON("../api/dataElements.json?fields=id,name,shortName,categoryCombo[categoryOptionCombos[id,name]]&paging=false", {
                format: "json"
            }),
         
        ).then(function (ou, ds,ds1) {

           // organisation

           console.log( ou );
           /*****/
           var ouid = [];
                                   
                               var ids;
                               var dn;
                               for(var i=0;i<ou[0].organisationUnitGroups.length;i++){
                                   ids = ou[0].organisationUnitGroups[i].id;
                                   dn = ou[0].organisationUnitGroups[i].displayName;
                                   ouid.push([ids,dn]);
                               }
                              
                             
                               var uniques = new Array();
                               var itemsFound = {};
                              for(var i = 0, l = ouid.length; i < l; i++) {
                                   var stringified = JSON.stringify(ouid[i]);
                                   if(itemsFound[stringified]) { continue; }
                                   uniques.push(ouid[i]);
                                   itemsFound[stringified] = true;
                               }
           
                               var i; 
                               var n = uniques.length;
                               var tmp = new Array();
                               for (i=0; i<n; i++)
                               {
                                   
                                  tmp.push({
                                           id:uniques[i][0],
                                           displayName:uniques[i][1]
                                  });
                              }
           
                             var mObj = new Object;
                             mObj.uniques = tmp;
                             
                            $scope.orgUnitGroups = mObj.uniques;


            // data set
            console.log(ds);
            $scope.dataSets = ds[0].dataSets;

            //data element

            console.log(ds);
            $scope.dataElements = ds1[0].dataElements;

            $scope.generateEnglishAddresses();
            $scope.startBuilding();
            $("#loader").hide();

         
        }).fail(function (jqXHR, textStatus, errorThrown) {
            $("#templateProgress").html("Failed to fetch data elements ( " + errorThrown + " )");
        });

    });
});
});
        //**************************************************************************************************************

        //building UIs
        $scope.startBuilding = function () {
            $("#templateProgress").html("Making things ready...");
            $.each($scope.dataSets, function (i, d) {
                $("#imDataSetId").append("<option value='" + d.id + "' > " + d.displayName + " </option>");
            });

            $.each($scope.orgUnitGroups, function (i, o) {
                $("#imOrgUnitGrp").append("<option value='" + o.id + "' > " + o.displayName + " </option>");
            });
        };

        //**************************************************************************************************************

        $scope.generatePeriods = function () {

            if ($("#imDataSetId").val() != "") {
                var url = "../api/dataSets/" + $("#imDataSetId").val() + ".json?fields=periodType";
                $.get(url, function (d) {

                    //printing periods ------------------
                    var periodType = d.periodType;
                    var today = new Date();
                    var stDate = "01/01/" + "2014";
                    var endDate = "01/01/" + (today.getFullYear() + 1);

                    var periods = "";

                    if (periodType == "Daily")
                        periods = daily(stDate, endDate);
                    else if (periodType == "Weekly")
                        periods = weekly(stDate, endDate);
                    else if (periodType == "Monthly")
                        periods = monthly(stDate, endDate);
                    else if (periodType == "Yearly")
                        periods = yearly(stDate, endDate);
                    else if (periodType == "Quarterly")
                        periods = quartly(stDate, endDate);

                    $("#importPeriod").html("");
                    periods.split(";").forEach(function (p) {
                        var ps = periodType == 'Monthly' ? $scope.monthString(p) : p;
                        var h = "<option value='" + p + "'>" + ps + "</option>";
                        $("#importPeriod").append(h);
                    });

                    //prining templates ---------------------
                    var noTemplatesFound = true;
                    $('#importTemp').html("");
                    $scope.templates.templates.forEach(function (te) {
                        if (te.dataSet == $("#imDataSetId").val() && (te.orgUnitGroup == $("#imOrgUnitGrp").val() || $("#imOrgUnitGrp").val() == "all")) {
                            noTemplatesFound = false;
                            $('#importTemp').append($('<option>', {
                                value: te.id,
                                text: te.name
                            }));
                        }
                    });

                    if (noTemplatesFound) {
                        $('#importTemp').append($('<option>', {
                            value: -1,
                            text: "No templates found. Add one."
                        }));

                        $("#templatesDiv").removeClass("disabled");
                        $("#templatesDiv").addClass("disabled");
                    }
                    else {
                        $("#templatesDiv").removeClass("disabled");
                    }
                });

            }
        };



        var orgUnitGroupID ;
        var parentUnitID ;
        var parentvalues = [];
        var parentnames = [];
        var parentvalues1 = [];
        var parentnames1 = [];
        
        
        
                //----------------------------------------------------------------------------------------------
                 $scope.filterOrgUnits = function () {
        
                     orgUnitGroupID = $("#imOrgUnitGrp").val();
                     parentUnitID = $scope.selectedOrgUnit.id;
                    
        
                   $.when(
                        $.getJSON("../api/organisationUnits.json?paging=false&fields=id,name&filter=parent.id:eq:" + parentUnitID+"&paging=false", {
                            format: "json"
                        }),
                        $.getJSON("../api/organisationUnits.json?paging=false&fields=id,name&filter=organisationUnitGroups.id:eq:" + orgUnitGroupID+"&paging=false", {
                            format: "json"
                        }),
                      
                    ).then(function (ous, ous1) {
        
                      orgs(ous,ous1);
                    
                    })
                    
                }
        
                function orgs(ous,ous1)
                {
        
                    length1 = ous[0].organisationUnits.length;
                    for (var i = 0; i < length1; i++) {
                        parentvalues[i] = ous[0].organisationUnits[i].id;
                        parentnames[i] = ous[0].organisationUnits[i].name;
                    }
        
                    length2 = ous1[0].organisationUnits.length;
                    for (var j = 0; j < length2; j++) {
                        parentvalues1[j] = ous1[0].organisationUnits[j].id;
                        parentnames1[j] = ous1[0].organisationUnits[j].name;
                    }        
        
                    finalvalue(length1, parentvalues, parentnames,length2, parentvalues1, parentnames1);
                }
        
                
        
        function finalvalue(length1, parentvalues, parentnames,length2, parentvalues1, parentnames1){
        
                   
                    for (var a = 0; a < length1; a++) {
                        for (var b = 0; b < length2; b++) {
                            if (parentvalues[a] == parentvalues1[b] && parentnames[a] == parentnames1[b]) {
                                var storename = parentnames1[b];
                                var storedata = parentvalues1[b];
                                count++;
                                var myObj = {
                                    "ou":
                                        { "name": storename, "id": storedata }									//your id variable
                                };
                                console.log(myObj);
                                calcuate(myObj);
                            }
        
                        }
        
                    }
                    function calcuate(myObj) {
                        var htmlString = '';
                        $.each(myObj, function (i, ou) {

                            htmlString += '<tr> <td>' + myObj.ou.name + '</td> <td align="right"><input class="" style="width:75px;font-size:12px" id="' + myObj.ou.id + '" type="file" accept=".xls,.xlsx"/></td> </tr>';
                        });
                        $("#confirmedUploadsContent").append(htmlString);
                        $("#confirmedUploadsDiv").attr("style", "width:300px;display:inline-block;float:right;max-height:500px;overflow-y:auto;padding:30px 10px 30px 10px");
        
                        $.each(myObj, function (i, ou) {
                            var elementID = myObj.ou.id;
                            var fileID = document.getElementById(elementID);
                            fileID.addEventListener('change', function (e) {
                                handleInputFile(e, ou);
                            }, false);
                        });
        
                        $("#confirmedUploadsDiv").attr("style", "width:300px;display:inline-block;float:right;height:540px;overflow-y:auto;padding:30px 10px 30px 10px");
                        $("#confirmedUploadsDiv").removeClass("disabled");
                        $("#form1").addClass("disabled");
                        $("#templatesContentDiv").addClass("disabled");
                        $("#nextBtn").hide();
                        $("#imb").show();
                        $("#cancelBtn").removeClass("disabled");
                        $("#loader").fadeOut();
                    }
                    //}
                };

        $scope.monthString = function (pst) {
            var month = pst.substring(4, 6);
            var ms = "";

            if (month == "01")
                ms = "Jan";
            else if (month == "02")
                ms = "Feb";
            else if (month == "03")
                ms = "Mar";
            else if (month == "04")
                ms = "Apr";
            else if (month == "05")
                ms = "May";
            else if (month == "06")
                ms = "Jun";
            else if (month == "07")
                ms = "Jul";
            else if (month == "08")
                ms = "Aug";
            else if (month == "09")
                ms = "Sep";
            else if (month == "10")
                ms = "Oct";
            else if (month == "11")
                ms = "Nov";
            else if (month == "12")
                ms = "Dec";

            return ms + " " + pst.substring(0, 4);
        };

        //*****************************************************************************************

        // VALIDATIONS
        $scope.validatedMessage = [];
        $scope.isEverythingOK = true;

        $scope.validateAll = function (orgUnit, index) {
            var dataCells = [];
            			
            $("#templateProgress").html("Validating sheet : " + orgUnit.name);

            if (orgUnit.result) {
                orgUnit.result.forEach(function (r) {
                    var cell = {};
                    cell.address = r.split("=")[0];

                    if (r.split("=").length > 1)
                        cell.value = r.split("=")[1].slice(1).trim(); //There is an additional char in the value

                    dataCells.push(cell);
                    orgUnit.dataCells = dataCells;
                    $scope.confirmedUploads.orgUnits[index] = orgUnit;
                });
            } else {
                $scope.isEverythingOK = false;
                $scope.validatedMessage.push("Something wrong with " + orgUnit.name + " excel sheet.");
            }




			var selectedTemp = $scope.getTemplate($scope.confirmedUploads.TempVal);

            if (selectedTemp != "") {

                $.each(selectedTemp.DEMappings, function (i, dem) {

                    $("#templateProgress").html(orgUnit.name + " -> orgValidating data elements mapping - " + (i + 1) + " of " + selectedTemp.DEMappings.length);

                    if (!$scope.isDEAvailable(dem.metadata))
                        $scope.isEverythingOK = false;
                });				
            }

        };

        $scope.viewConflicts = function () {
            var htmlString = "";

            htmlString += "<ol>";

            $.each($scope.validatedMessage, function (i, m) {
                htmlString += "<li>" + m + "</li>";
            });

            htmlString += "</ol>";

            $("#confBdy").html(htmlString);
            $("#conflictModal").modal('show');
        };

        // to check if a data element is available while validating
        $scope.isDEAvailable = function (de) {
            var deId = de.split("-")[0];
            var coc = de.split("-").length > 1 ? de.split("-")[1] : "";

            var isDeFound = false;
            var isCocFound = false;

            $.each($scope.dataElements, function (i, d) {
                if (d.id == deId) {
                    isDeFound = true;

                    $.each(d.categoryCombo.categoryOptionCombos, function (i, c) {
                        if (c.id == coc) {
                            isCocFound = true;
                            return false;
                        }
                    });
                    return false;
                }
            });
            console.log(" de : " + isDeFound + " coc : " + isCocFound);

            if (!isDeFound) {
                $scope.validatedMessage.push("Data element " + deId + " is not found");
                return false;
            } else {
                if (!isCocFound) {
                    $scope.validatedMessage.push("COC " + coc + " of data element " + deId + " is not found");
                    return false;
                } else
                    return true;
            }
        };

        //****************************************************************************************************************
        //****************************************************************************************************************
        // IMPORT FUNCTION
        //****************************************************************************************************************
        //****************************************************************************************************************

        $scope.h = {};
        $scope.importData = function (orgUnit, index, callbackfunct) {
            var selectedTemp = $scope.getTemplate($scope.confirmedUploads.TempVal);
            var dataValues = [];
            $("#templateProgress").html(orgUnit.name + " -> preparing data values to import");
		
            // SOU - MDE
            if (selectedTemp.typeId == 2) {
                $scope.dp = [];
                for (var x = 0; x < selectedTemp.DEMappings.length; x++) {
                    var cellAddress = selectedTemp.DEMappings[x].cellAddress;

                    var dataValue = {};
                    var data5;
                    var value1;
                    var value2;
                    var value3;
                    var orgName;

                    dataValue.period = $scope.confirmedUploads.periodVal;
                    dataValue.dataElement = selectedTemp.DEMappings[x].metadata.split("-")[0];
                    dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                    dataValue.orgUnit = orgUnit.id;
                    orgName = orgUnit.name;
                    $.ajax({

                        type: "GET",
                        dataType: "json",
                        contentType: "application/json",
                        url: "../api/organisationUnits/" + orgUnit.id + ".json?fields=comment",
                        success: function (data5) {


                            factype = data5.comment;
                            factype = factype.substring(factype.indexOf(":") + 1).trim();
                            console.log(factype);

                        },
                        error: function (response) { }
                    });
                  
                    /************************************* FOR SC ************************************************************/
                    if (factype == "SC") {

                        if (dataValue.dataElement == "FIaGENXR3c5" || dataValue.dataElement == "fqM6fGLUqVD") {

                            dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                            $scope.dp.push(dataValue.value);

                            for (var i = 0; i < $scope.dp.length; i++) {

                                value1 = $scope.dp[0];
                                value2 = $scope.dp[1];

                            }
                            if (value1 == "") {
                                alert("For " + orgName + " organisation Delivery Point value is empty");
                                window.location.reload();
                                break;

                            }

                            if (value1 == "true") {
                                if (value2 == "") {
                                    alert("For " + orgName + " If Delivery point value is true then please select Level of Delivery point");
                                    window.location.reload();
                                    break;
                                }
                                else {
                                    dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                                    dataValue.orgUnit = orgUnit.id;
                                    dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                                    dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;
                                    if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                        dataValues.push(dataValue);
                                    }
                                }
                            }
                            if (value1 == "false") {
                                if (value2 == "") {
                                    dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                                    dataValue.orgUnit = orgUnit.id;
                                    dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                                    dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;
                                    if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                        dataValues.push(dataValue);
                                    }
                                }
                                else if (value2 == undefined) {
                                    dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                                    dataValue.orgUnit = orgUnit.id;
                                    dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                                    dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;
                                    if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                        dataValues.push(dataValue);
                                    }
                                }
                                else {
                                    alert("For " + orgName + " If Delivery point value is false then Level of Delivery point should not be selected");
                                    window.location.reload();
                                    break;
                                }
                            }


                        }

                        else {
                            dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                            dataValue.orgUnit = orgUnit.id;

                            dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);

                            dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;

                            if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                dataValues.push(dataValue);
                            }
                        }
                    }
                   
                    /********************************************************** FOR DH **************************************************/
                    else if (factype == "DH" || factype == "DWH" || factype == "OTH" || factype == "DMH" || factype == "DCH" || factype == "MC" || factype == "DH_TB" || factype == "DH_EYE") {

                        if (dataValue.dataElement == "FIaGENXR3c5" || dataValue.dataElement == "fqM6fGLUqVD") {

                            dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                            $scope.dp.push(dataValue.value);

                            for (var i = 0; i < $scope.dp.length; i++) {

                                value1 = $scope.dp[0];
                                value2 = $scope.dp[1];
                            }

                            if (value1 == "") {
                                alert("For " + orgName + " organisation Delivery Point value is empty");
                                window.location.reload();
                                break;

                            }

                            if (value1 == "true") {
                                if (value2 == "") {
                                    alert("For " + orgName + " If Delivery point value is true then please select Level of Delivery point");
                                    window.location.reload();
                                    break;
                                }
                                else {
                                    dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                                    dataValue.orgUnit = orgUnit.id;
                                    dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                                    dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;
                                    if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                        dataValues.push(dataValue);
                                    }
                                }
                            }
                            if (value1 == "false") {
                                if (value2 == "") {
                                    dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                                    dataValue.orgUnit = orgUnit.id;
                                    dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                                    dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;
                                    if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                        dataValues.push(dataValue);
                                    }
                                }
                                else if (value2 == undefined) {
                                    dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                                    dataValue.orgUnit = orgUnit.id;
                                    dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                                    dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;
                                    if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                        dataValues.push(dataValue);
                                    }
                                }
                                else {
                                    alert("For " + orgName + " If Delivery point value is false then Level of Delivery point should not be selected");
                                    window.location.reload();
                                    break;
                                }
                            }


                        }

                        else {
                            dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                            dataValue.orgUnit = orgUnit.id;

                            dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);

                            dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;

                            if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                dataValues.push(dataValue);
                            }
                        }
                    }

                 
                    /********************************************************** FOR CHC **************************************************/
                    else if (factype == "CHC" || factype == "BCHC" || factype == "UCHC") {


                        if (dataValue.dataElement == "FIaGENXR3c5" || dataValue.dataElement == "fqM6fGLUqVD" || dataValue.dataElement == "GpEwBknDwF9") {

                            dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                            $scope.dp.push(dataValue.value);

                            for (var i = 0; i < $scope.dp.length; i++) {

                                value1 = $scope.dp[2];
                                value2 = $scope.dp[1];
                                value3 = $scope.dp[0];
                            }
                            if (value3 == "") {
                                alert("For " + orgName + " organisation FRU value is empty");
                                window.location.reload();
                                break;
                            }
                            if (value1 == "") {
                                alert("For " + orgName + " organisation Delivery Point value is empty");
                                window.location.reload();
                                break;

                            }

                            if (value1 == "true") {
                                if (value2 == "") {
                                    alert("For " + orgName + " If Delivery point value is true then please select Level of Delivery point");
                                    window.location.reload();
                                    break;
                                }
                                else {
                                    dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                                    dataValue.orgUnit = orgUnit.id;
                                    dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                                    dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;
                                    if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                        dataValues.push(dataValue);
                                    }
                                }
                            }
                            if (value1 == "false") {
                                if (value2 == "") {
                                    dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                                    dataValue.orgUnit = orgUnit.id;
                                    dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                                    dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;
                                    if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                        dataValues.push(dataValue);
                                    }
                                }
                                else if (value2 == undefined) {
                                    dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                                    dataValue.orgUnit = orgUnit.id;
                                    dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                                    dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;
                                    if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                        dataValues.push(dataValue);
                                    }
                                }
                                else {
                                    alert("For " + orgName + " If Delivery point value is false then Level of Delivery point should not be selected");
                                    window.location.reload();
                                    break;
                                }
                            }


                        }

                        else {
                            dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                            dataValue.orgUnit = orgUnit.id;

                            dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);

                            dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;

                            if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                dataValues.push(dataValue);
                            }
                        }
                    }
                    /******************************************** FOR PHC ********************************************************/
                    else if (factype == "PHC" || factype == "BPHC" || factype == "UPHC" || factype == "NPHC" || factype == "APHC") {


                        if (dataValue.dataElement == "FIaGENXR3c5" || dataValue.dataElement == "fqM6fGLUqVD") {

                            dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                            $scope.dp.push(dataValue.value);

                            for (var i = 0; i < $scope.dp.length; i++) {

                                value1 = $scope.dp[0];
                                value2 = $scope.dp[1];
                            }

                            if (value1 == "") {
                                alert("For " + orgName + " organisation Delivery Point value is empty");
                                window.location.reload();
                                break;

                            }

                            if (value1 == "true") {
                                if (value2 == "") {
                                    alert("For " + orgName + " If Delivery point value is true then please select Level of Delivery point");
                                    window.location.reload();
                                    break;
                                }
                                else {
                                    dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                                    dataValue.orgUnit = orgUnit.id;
                                    dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                                    dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;
                                    if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                        dataValues.push(dataValue);
                                    }
                                }
                            }
                            if (value1 == "false") {
                                if (value2 == "") {
                                    dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                                    dataValue.orgUnit = orgUnit.id;
                                    dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                                    dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;
                                    if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                        dataValues.push(dataValue);
                                    }
                                }
                                else if (value2 == undefined) {
                                    dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                                    dataValue.orgUnit = orgUnit.id;
                                    dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                                    dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;
                                    if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                        dataValues.push(dataValue);
                                    }
                                }
                                else {
                                    alert("For " + orgName + " If Delivery point value is false then Level of Delivery point should not be selected");
                                    window.location.reload();
                                    break;
                                }
                            }


                        }

                        else {
                            dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                            dataValue.orgUnit = orgUnit.id;

                            dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);

                            dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;

                            if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                                dataValues.push(dataValue);
                            }
                        }
                    }

                    else {
                        dataValue.categoryOptionCombo = selectedTemp.DEMappings[x].metadata.split("-")[1];
                        dataValue.orgUnit = orgUnit.id;

                        dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);

                        dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;

                        if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                            dataValues.push(dataValue);
                        }
                    }
                }

                dataValue.value = $scope.getImportDataByAddress(cellAddress, orgUnit);
                dataValue.value = dataValue.value == "" ? "omit" : dataValue.value;

                if (dataValue.orgUnit != "" && dataValue.value != "omit") {
                    dataValues.push(dataValue);
                }
            }



            ///////////////////////////////////////////////////////////////////////
            console.log("dataValues : " + JSON.stringify(dataValues));
            //				}

            $("#templateProgress").html(orgUnit.name + " -> Importing data.. Please wait.. This may take several minutes..");

            var dataValueSet = {};
            dataValueSet.dataValues = dataValues;


            //making ready to import data
            $.get("../api/system/info", function (data) {

                $scope.h.time = data.serverDate.split("T")[0] + " (" + data.serverDate.split("T")[1].split(".")[0] + ")";
                $scope.h.orgUnits[index] = orgUnit.name;
        

                if ($scope.validatedMessage.length == 0 && $scope.isEverythingOK)
                    $scope.validatedMessage.push("Everything was perfect as per validations");

                $scope.h.orgUnits[index] = $scope.validatedMessage;

                $scope.h.orgUnits[index].stats = {};

                //saving data
                ExcelMappingService.importData(dataValueSet).then(function (tem) {
                    console.log("index : " + index);
                    console.log("no of orgUnits : " + $scope.confirmedUploads.orgUnits.length);
                    console.log(tem.data.importCount.updated);
                    console.log(tem.data.importCount.imported);
                    console.log(tem.data.importCount.ignored);

                    // complete registration
                    if (tem.data.importCount.updated > 0 || tem.data.importCount.imported > 0) {
                        for (var i = 0; i < $scope.confirmedUploads.orgUnits.length; i++) {
                            var dataSetCompleteParams = {
                                'ds': $("#imDataSetId").val(),
                                'pe': $("#importPeriod").val(),
                                'ou': $scope.confirmedUploads.orgUnits[i].id,
                                'multiOu': false
                            };

                            $.ajax({
                                url: '../api/completeDataSetRegistrations',
                                data: dataSetCompleteParams,
                                dataType: 'json',
                                type: 'post',
                                success: function (data, textStatus, xhr) {
                                    $("#dataSetRegistrationsComplete").html("SUCCESS");
                                    console.log("Registration Complete");
                                },
                                error: function (xhr, textStatus, errorThrown) {
                                    console.log("Error in Registration Complete");
                                    $("#dataSetRegistrationsComplete").html("IGNORED");
                                    if (409 == xhr.status || 500 == xhr.status) // Invalid value or locked
                                    {

                                    }
                                    else // Offline, keep local value
                                    {

                                    }
                                }
                            });

                            console.log(dataSetCompleteParams);

                            console.log($scope.confirmedUploads.orgUnits[i].id + " --" + $("#imDataSetId").val() + "--" + $("#importPeriod").val());
                        }

                    }
                    else {
                        $("#dataSetRegistrationsComplete").html("IGNORED");
                    }

                    $scope.h.stats.upc += tem.data.importCount.updated;
                    $scope.h.orgUnits[index].stats.upc = tem.data.importCount.updated;
                    $scope.h.stats.imc += tem.data.importCount.imported;
                    $scope.h.orgUnits[index].stats.imc = tem.data.importCount.imported;
                    $scope.h.stats.igc += tem.data.importCount.ignored;
                    $scope.h.orgUnits[index].stats.igc = tem.data.importCount.ignored;
                    $scope.history.history.push($scope.h);
                    $scope.storeHistory();

                    console.log("org upc : " + $scope.h.orgUnits[index].stats.upc);
                    console.log("org imc : " + $scope.h.orgUnits[index].stats.imc);
                    console.log("org igc : " + $scope.h.orgUnits[index].stats.igc);
                    console.log("upc stat : " + $scope.h.stats.upc);
                    console.log("imc stat : " + $scope.h.stats.imc);
                    console.log("igc stat : " + $scope.h.stats.igc);

                    if ($scope.confirmedUploads.orgUnits.length == (index + 1)) {
                        callbackfunct();
                    }
                });
            });
        };

        //****************************************************************************************************************

        $scope.getTemplate = function (id) {
            var t = "";

            $scope.templates.templates.forEach(function (te) {
                if (te.id == id)
                    t = te;
            });

            return t;
        };

        $scope.getImportData = function (rowNum, colNum) {
            var address = $scope.engAddress[colNum] + "" + rowNum;
            var val = "";
            return (val);
        };

        $scope.getImportDataByAddress = function (add, orgUnit) {
            var address = add;
            var val = "";

            orgUnit.dataCells.forEach(function (c) {
                if (c.address == address)
                    val = c.value;
            });
            console.log("value : " + val);
            return (val);
        };

        $scope.generateEnglishAddresses = function () {
            //generating more address notations for columns
            for (var x = 1; x < 27; x++) {
                for (var y = 1; y < 27; y++) {
                    $scope.engAddress.push($scope.engAddress[x] + "" + $scope.engAddress[y]);
                }
            }

            for (var x = 1; x < 27; x++) {
                for (var y = 1; y < 27; y++) {
                    for (var z = 1; z < 27; z++) {
                        $scope.engAddress.push($scope.engAddress[x] + "" + $scope.engAddress[y] + "" + $scope.engAddress[z]);
                    }
                }
            }

            for (var x = 1; x < 27; x++) {
                for (var y = 1; y < 27; y++) {
                    for (var z = 1; z < 27; z++) {
                        for (var u = 1; u < 27; u++) {
                            $scope.engAddress.push($scope.engAddress[x] + "" + $scope.engAddress[y] + "" + $scope.engAddress[z] + "" + $scope.engAddress[u]);
                        }
                    }
                }
            }
        };

	
        $scope.storeHistory = function () {
            ExcelMappingService.save('Excel-import-app-history', $scope.history).then(function (r) {
            });
        };

        $scope.validateUploads = function () {
            $("#loader").fadeIn();
            $scope.validatedMessage.length = 0;
            $scope.isEverythingOK = true;

            $scope.confirmedUploads.orgUnits.forEach(function (orgUnit, index) {
                $scope.validateAll(orgUnit, index);
            });

            if ($scope.isEverythingOK) {
                $("#ime").show();
            } else {
                $("#imd").show();
                $scope.viewConflicts();
            }

            $("#confirmedUploadsDiv").addClass("disabled");
            $("#imb").hide();
            $("#loader").fadeOut();
        };

        $scope.importUploads = function () {
            $("#loader").fadeIn();

            $scope.h.orgUnitGroup = $scope.confirmedUploads.orgUnitGrpName;
            $scope.h.dataSet = $scope.confirmedUploads.dataSetName;
            $scope.h.period = $scope.confirmedUploads.periodName;
            $scope.h.template = $scope.confirmedUploads.TempName;
            $scope.h.orgUnits = [];
            $scope.h.stats = {};
            $scope.h.stats.upc = 0;
            $scope.h.stats.imc = 0;
            $scope.h.stats.igc = 0;

            var callbackfunct = function () {
                $("#upc").html($scope.h.stats.upc);
                $("#imct").html($scope.h.stats.imc);
                $("#igc").html($scope.h.stats.igc);
                $("#stModal").modal('show');

                $("#loader").fadeOut();

            };

            $scope.confirmedUploads.orgUnits.forEach(function (orgUnit, index) {
                $scope.importData(orgUnit, index, callbackfunct);
            });

        };
    });