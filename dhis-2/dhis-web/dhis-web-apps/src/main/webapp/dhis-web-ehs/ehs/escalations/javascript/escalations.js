
window.onload=function(){
	jQuery('#listTEIDiv').dialog({autoOpen: false});	
}
var table;

//-----------------------------------------------------------------------------
//go to TEI DashBoard
//-----------------------------------------------------------------------------
function goToDashboard( psiUid, teiUid, programUid, orgUnitUid) 
{
	//alert( psiUid +" -- "+ teiUid +" -- "+ programUid + " -- " + orgUnitUid);  
	 
	//http://127.0.0.1:8080/dhis-web-ehs/index.html#/dashboard?tei=Cmne3cD5ZCk&program=tITlMGNJTbJ&ou=oN6etUABBaX
	
	$.postJSON(
			"updateEscalationsStatus.action",
    	    {
    	        "psiUid": psiUid   
    	    },
    	    function( json )
    	    { 
    	    	//alert( json.response );
    	    	if ( json.response == "success" )
    	    	{
    	    		var url = '../dhis-web-ehs/index.html#/dashboard?tei=' + teiUid + "&program=" + programUid + "&ou=" + orgUnitUid+ "&psiUid=" + psiUid;
    	    		  
    	    		window.location.href = url;
    	    	}
    	    }
    	);
	
	
	
	
	/*
	var url = '../dhis-web-ehs/index.html#/dashboard?tei=' + teiUid + "&program=" + programUid + "&ou=" + orgUnitUid+ "&psiUid=" + psiUid;
	  
	window.location.href = url;
	*/
	
}


//-----------------------------------------------------------------------------
//update
//-----------------------------------------------------------------------------
function filterEscalation() 
{
	var programId = $('#programId').val();  
	//var escalationStartDate = $('#escalationStartDate').val();
	//var escalationEndDate = $('#escalationEndDate').val();
	
	var escalationStartDate = document.getElementById("escalationStartDate").value;
	var escalationEndDate = document.getElementById("escalationEndDate").value;
	
	var establishmentName = $('#establishmentName').val();
	var establishmentType = $('#establishmentType').val();
	var ownerName = $('#ownerName').val();
	
	//var decisionStartDate = $('#decisionStartDate').val();
	//var decisionEndDate = $('#decisionEndDate').val();
	
	var decisionStartDate = document.getElementById("decisionStartDate").value;
	var decisionEndDate = document.getElementById("decisionEndDate").value;
	
	var escalationStatus = $('#escalationStatus').val();
	var listAll = false;

	//alert( escalationStartDate +" -- "+ escalationEndDate +" -- "+ decisionStartDate + " -- " + decisionEndDate);  
	
	var url = 'escalationsList.action?escalationStartDate=' + escalationStartDate + "&escalationEndDate=" + escalationEndDate +  "&decisionStartDate=" + decisionStartDate + "&decisionEndDate=" + decisionEndDate +
	  			"&programId=" + programId + "&establishmentType=" + establishmentType + "&establishmentName=" + establishmentName  + "&ownerName=" + ownerName + "&escalationStatus=" + escalationStatus + "&listAll = " + listAll;
	  
	  //url += months ? 'months=' + months + '&' : '';
	  //url += selfRegistered ? 'selfRegistered=' + selfRegistered + '&' : '';
	  //url += invitationStatus ? 'invitationStatus=' + invitationStatus + '&' : '';
	  
	  window.location.href = url;
}

/*
function clearSearch()
{
	alert("Clear");
	$("search_cat").empty();
	document.getElementById("scheduleStartDate").value="";
	document.getElementById("scheduleEndDate").value="";
	document.getElementById("programId").value="";
	document.getElementById("inspectionType").value="";
	document.getElementById("assignedInspector").value="";
	
	//document.getElementById("textarea").value="";
}
*/



function reSetSearch() 
{
	document.getElementById("programId").value="";
	document.getElementById("escalationStartDate").value="";
	document.getElementById("escalationEndDate").value="";
	document.getElementById("establishmentName").value="";
	document.getElementById("establishmentType").value="";
	document.getElementById("ownerName").value="";
	document.getElementById("decisionStartDate").value="";
	document.getElementById("decisionEndDate").value="";
	document.getElementById("escalationStatus").value="";
}


//$("clearButton").click(function()
//{	alert("Clear");
//    
//	$("search_cat").empty();
//});


//-----------------------------------------------------------------------------
//update
//-----------------------------------------------------------------------------
function allScheduleInspection() 
{
	var listAll = true;  
	var url = 'schedulingInspectionsList.action?listAll=' + listAll;
	
	  
	  //url += months ? 'months=' + months + '&' : '';
	  //url += selfRegistered ? 'selfRegistered=' + selfRegistered + '&' : '';
	  //url += invitationStatus ? 'invitationStatus=' + invitationStatus + '&' : '';
	  
	window.location.href = url;
}




// -----------------------------------------------------------------------------
// update
// -----------------------------------------------------------------------------

function showUpdateScheduledInspectionForm( programStageInstanceId, teiName, teiDistrictName, teiCommunityName ) {
  location.href = 'showUpdateScheduledInspectionForm.action?programStageInstanceId=' + programStageInstanceId + "&teiName=" + teiName + "&teiDistrictName=" + teiDistrictName + "&teiCommunityName=" + teiCommunityName;
}

//-----------------------------------------------------------------------------
// delete
//-----------------------------------------------------------------------------
function removeScheduledInspection( id, teiName, teiDistrictName, teiCommunityName ) 
{
	
	  var completeName = teiName + " District " + teiDistrictName + " Community " + teiCommunityName;
	  removeItem( id, completeName, i18n_confirm_delete, 'removeScheduledInspection.action');
}







//'showUpdateASHABeneficiaryForm.action?beneficiaryId='+ beneficiaryId + "&periodId=" + periodId + "&ashaId=" + ashaId + "&selectedPeriodId=" + selectedPeriodId
//<a href="javascript:showUpdateScheduledInspectionForm( '$programStageInstance.id','$!teiValueMap.get( $nameKey )', '$!teiValueMap.get( $districtKey )', '$!teiValueMap.get( $communityKey )' )" title='$i18n.getString( "update" )'><img src="../images/edit.png" alt='$i18n.getString( "update" )'></a>
//<a href="javascript:deleteScheduledInspection( '$programStageInstance.id','$!teiValueMap.get( $nameKey )', '$!teiValueMap.get( $districtKey )', '$!teiValueMap.get( $communityKey )' )" title='$i18n.getString( "delete" )'>img src="../images/delete.png" alt='$i18n.getString( "delete" )'></a>
//


//------------------------------------------------------------------------------
//Get Communities
//------------------------------------------------------------------------------

/*
function getCommunityList()
{	
	
	var districtName = $( '#districtName' ).val();
	if ( districtName == "" )
	{
		setHeaderDelayMessage( i18n_program_not_selected );
		return false;
	}
	
	else
	{
		$.post("getChildrenOrganisationList.action",
				{
					parentOrgUnitName:districtName
				},
				function(data)
				{
					populateCommunityList( data );
				},'xml');
	}
}


function populateCommunityList( data )
{
		
	var communities = document.getElementById("communityName");
	clearList( communities );
	
	var communityList = data.getElementsByTagName("organisationUnit");
		
	for ( var i = 0; i <  communityList.length; i++ )
	{
		var id = communityList[ i ].getElementsByTagName("id")[0].firstChild.nodeValue;
		var name = communityList[ i ].getElementsByTagName("name")[0].firstChild.nodeValue;
		
		var option = document.createElement("option");
		option.value = name;
		option.text = name;
		option.title = name;
		communities.add(option, null);
	} 
}
*/


//------------------------------------------------------------------------------
//Get Communities
//------------------------------------------------------------------------------

function getCommunityList()
{	
	$( '#dataEntryFormDiv' ).html( '' );
	
	var dataSetId = $( '#dataSetId' ).val();
	
	var communities = document.getElementById("communityName");
	clearList( communities );
	
	addOptionToList( communities, '', '[ Please Select ]' );
	
//	//var url = 'loadPeriods.action?dataSetId=' + dataSetId;
//	$.getJSON( url, function( json ) {
//    	for ( i in json.periods ) {
//    		addOptionToList( list, json.periods[i].isoDate, json.periods[i].name );
//    	}
//    } );
	
	
	var districtName = $( '#districtName' ).val();
	
	//alert( programId );
	if ( districtName == "" )
	{
		setHeaderDelayMessage( i18n_program_not_selected );
		return false;
	}

	else
	{
		$.getJSON( "getChildrenOrganisationList.action", {
			"parentOrgUnitName": districtName
			},
			function( json ) {
				for ( i in json.organisationUnits ) {
		    		addOptionToList( communities, json.organisationUnits[i].name, json.organisationUnits[i].name );
		    	}
				
			} );
	}
}











// ------------------------------------------------------------------------------
// Get ProgramStages
// ------------------------------------------------------------------------------

function getProgramStages()
{	
	
	var programId = $( '#programId' ).val();
	
	//alert( programId );
	if ( programId == "" )
	{
		setHeaderDelayMessage( i18n_program_not_selected );
		return false;
	}
	
	else
	{
		$.post("getprogramStageList.action",
				{
					programId:programId
				},
				function(data)
				{
					populateProgramStageList( data );
				},'xml');
	}
}


function populateProgramStageList( data )
{
		
	var programStages = document.getElementById("programStageId");
	clearList( programStages );
	
	var programStageList = data.getElementsByTagName("programStage");
		
	for ( var i = 0; i <  programStageList.length; i++ )
	{
		var id = programStageList[ i ].getElementsByTagName("id")[0].firstChild.nodeValue;
		var name = programStageList[ i ].getElementsByTagName("name")[0].firstChild.nodeValue;
		
		var option = document.createElement("option");
		option.value = id;
		option.text = name;
		option.title = name;
		programStages.add(option, null);
	} 
}


function getDistrictCommunity()
{
	var selTeiId = $( '#trackedEntityInstanceId' ).val();

	$.post("getTEIAttributeDetails.action",
			{
				selTeiId : selTeiId
			},
			function (data)
			{
				getTEIAttributeDetailsRecevied(data);
			},'xml');

}

function getTEIAttributeDetailsRecevied(xmlObject)
{
		
	document.getElementById("district").value = "";
	document.getElementById("community").value = "";
	var teiAttributes = xmlObject.getElementsByTagName("attribute");

    for ( var i = 0; i < teiAttributes.length; i++ )
    {
        var districtName = teiAttributes[ i ].getElementsByTagName("districtName")[0].firstChild.nodeValue;
        var communityName = teiAttributes[ i ].getElementsByTagName("communityName")[0].firstChild.nodeValue;
		
        document.getElementById("district").value = districtName;
        document.getElementById("community").value = communityName;
    }    		
}


/*
function getDistrictCommunity()
{	
	var selTeiId = $( '#trackedEntityInstanceId' ).val();
	alert( selTeiId );
	
	var districtMap = new Object();
	var communityMap = new Object();
	
	#foreach( $teId in $teiIds )
		districtMap["$teId"+":" +"698"] = '$teiValueMap.get($teId + ":" + 698)';
		communityMap["$teId"+":" +"699"] = '$teiValueMap.get($teId + ":" + 698)';
	#end
	
	
	document.getElementById("district").value = districtMap[selTeiId+":" +"698"];
	document.getElementById("community").value = communityMap[selTeiId+":" +"699"];
	
}
*/


function SEARCH(){

	var $rows = $('#demo tr');
	$('#search').keyup(function() {
		var val = $.trim($(this).val()).replace(/ +/g, ' ').toLowerCase();

		$rows.show().filter(function() {
			var text = $(this).text().replace(/\s+/g, ' ').toLowerCase();
			return !~text.indexOf(val);
		}).hide();
	});
}

function close1(){

	var modal = document.getElementById('modal');
	modal.style.display = "none";
}

function loadTEIList() 
{
	var programId = $( '#programId' ).val();
	
	//alert( programId );
	if ( programId == "" || programId == " ")
	{
		setHeaderDelayMessage( i18n_program_not_selected );
		return false;
	}
	
	jQuery('#listTEIDiv').dialog('destroy').remove();
	jQuery('<div id="listTEIDiv">' ).load( 'showTEIList.action?programId='+programId ).dialog({
		title: i18n_establishment_list,
		maximize: true,
		closable: true,
		modal:true,
		overlay:{background:'#000000', opacity:0.1},
		width: 900,
		height: 500
	});

}

function selectDetails( teiId, teiName, teiDistrict, teiCommunity )
{
	alert( teiId + "--" + teiName );
	
	document.getElementById("trackedEntityInstanceId").value = "";
	document.getElementById("teiName").value = "";
	document.getElementById("district").value = "";
	document.getElementById("community").value = "";
	
	document.getElementById("trackedEntityInstanceId").value = teiId;
	document.getElementById("teiName").value = teiName;
	document.getElementById("district").value = teiDistrict;
    document.getElementById("community").value = teiCommunity;
	
    
    
    jQuery('#listTEIDiv').dialog('close');
   
    
}




function myFunction() {

	getorgunit().then(function (json){


		var promiseQueue = [];
		for(var i=0;i<json.organisationUnits.length;i++)
		{promiseQueue.push(myFunction1(json.organisationUnits[i].id));}
		$.when.apply($, promiseQueue) // happens now
			.then(function () {
				console.log(promiseQueue);
				document.getElementById("demo").innerHTML = table;
				var modal = document.getElementById('modal');
				modal.style.display = "block";

			}, function (e) {
				console.log("promise queue error : "+e);
			});

	});


	/*	var table="<table><tr onclick=" +'update()'+"><td>hello</td><td> hiiiiii</td></tr onclick=" +'update()'+"><tr><td>kab</td><td>ab</td></tr></table>";
	 document.getElementById("demo").innerHTML = table;
	 var modal = document.getElementById('modal');
	 modal.style.display = "block";

	 */



}
function update()
{
	function highlight(e) {
		if (selected[0]) selected[0].className = '';
		e.target.parentNode.className = 'selected';
		fnselect()
	}

	var table = document.getElementById('demo'),
		selected = table.getElementsByClassName('selected');

	table.onclick = highlight;

	function fnselect(){
		//console.log($("tr.selected td:nth-child(1)" ).html());
		//document.getElementById("TEIID").value =$("tr.selected td:nth-child(1)" ).html();
		document.getElementById("TEIID").value =$("tr.selected td:nth-child(2)" ).html();
		document.getElementById("community").value =$("tr.selected td:nth-child(5)" ).html();
		document.getElementById("district").value =$("tr.selected td:nth-child(6)" ).html();
		//document.getElementById("community").value =$("tr.selected td:nth-child(1)" ).html();
		alert($("tr.selected td:nth-child(1)" ).html());

	}
	close1();
}
function sortArr(orderArr, attributeArr) {
	var result = [];
	for (var i = 0; i < orderArr.length; i++) {
		var check = 0;
		var item = {};
		item["name"] = orderArr[i];
		for (var j = 0; j < attributeArr.length; j++) {
			if (orderArr[i] == attributeArr[j].displayName) {
				item["value"] = attributeArr[j].value;
				check = 1;
				break;
			}
		}
		if (check == 0) {
			item["value"] = " ";
		}
		result.push(item);
	}
	return result;
}
function myFunction1(org) {
	var trackeentityinfoalll=[];
	table="<table><th>ID</th><th>Name</th><th>Establishment Type-Food service</th><th>Country</th><th>District</th><th>City/Community</th>";
	var def = $.Deferred();
	var orderArr = ["Name", "Establishment Type-Food service", "Country", "District", "City/Community"];
//getorgunit().then(function (json){
	//$.get("../api/organisationUnits?fields=id,name&filter=level:eq:3&paging=false", function (json) {


	var trackeentityinfoall=[];
	teInstance(org).then(function (json1){
		//$.get("../api/trackedEntityInstances?program=tITlMGNJTbJ&ou="+json.organisationUnits[i].id, function (json1) {


		console.log("hello");
		if(json1.trackedEntityInstances.length)
		{table+="<tr onclick=" +'update()'+">";
			var trackeentityinfo=[];
			trackeentityinfo.push(json1.trackedEntityInstances[0].trackedEntityInstance);
			table+="<td>"+ json1.trackedEntityInstances[0].trackedEntityInstance + "</td>";
			var result = sortArr(orderArr, json1.trackedEntityInstances[0].attributes);
			for( var k=0; k<result.length;k++) {
				table += "<td>" + result[k].value + "</td>";
				/*	if (json1.trackedEntityInstances[0].attributes[k].displayName == "Name") {
				 table += "<td>" + json1.trackedEntityInstances[0].attributes[k].value + "</td>";
				 trackeentityinfo.push(json1.trackedEntityInstances[0].attributes[k].value);
				 break;
				 }
				 else if (json1.trackedEntityInstances[0].attributes[k].displayName == "Establishment Type-Food service") {
				 table += "<td>" + json1.trackedEntityInstances[0].attributes[k].value + "</td>";
				 trackeentityinfo.push(json1.trackedEntityInstances[0].attributes[k].value);
				 break;
				 }
				 else if (json1.trackedEntityInstances[0].attributes[k].displayName == "Country") {
				 table += "<td>" + json1.trackedEntityInstances[0].attributes[k].value + "</td>";
				 trackeentityinfo.push(json1.trackedEntityInstances[0].attributes[k].value);
				 break;
				 }
				 else if (json1.trackedEntityInstances[0].attributes[k].displayName == "District") {
				 table += "<td>" + json1.trackedEntityInstances[0].attributes[k].value + "</td>";
				 trackeentityinfo.push(json1.trackedEntityInstances[0].attributes[k].value);
				 break;
				 }
				 else if (json1.trackedEntityInstances[0].attributes[k].displayName == "City/Community") {
				 table += "<td>" + json1.trackedEntityInstances[0].attributes[k].value + "</td>";
				 trackeentityinfo.push(json1.trackedEntityInstances[0].attributes[k].value);
				 break;
				 }*/


			}





			if(trackeentityinfo.length>0)
			{table+="</tr>";
				trackeentityinfoall.push(trackeentityinfo);}
		}
		def.resolve();
		//console.log(json.organisationUnits.length);
		//console.log(json1.length);
		trackeentityinfoalll.push(trackeentityinfoall);

	});


	table+="</table>";

//	});
	return def.promise();

}

function getorgunit(){
	var def = $.Deferred();
	$.ajax({
		type: "GET",
		dataType: "json",
		contentType: "application/json",
		url: '../api/organisationUnits?fields=id,name&filter=level:eq:3&paging=false.json',
		success: function (data) {
			def.resolve(data);
		}
	});
	return def;
}
function teInstance(org){
	var def = $.Deferred();
	$.ajax({
		type: "GET",
		dataType: "json",
		contentType: "application/json",
		url: '../api/trackedEntityInstances?program=tITlMGNJTbJ&ou='+org,
		success: function (data) {
			def.resolve(data);
		}
	});
	return def;
}