
// ----------------------------------------------------------------------
// Get Reports
// ----------------------------------------------------------------------

function getReports( orgUnitIds, reportTypeName ) 
{
	var periodTypeList = document.getElementById('periodTypeId');
	var periodType = periodTypeList.options[periodTypeList.selectedIndex].value;
	
	document.getElementById("ouNameTB").value = "";
	/*
	if ( periodType != "NA" && orgUnitIds != null && reportTypeName != "" ) 
	{
		var url = "getReports.action?periodType=" + periodType + "&ouId=" + orgUnitIds + "&reportType=" + reportTypeName;

		var request = new Request();
		request.setResponseTypeXML('report');
		request.setCallbackSuccess(getReportsReceived);
		request.send(url); 
	}
	*/
	
	if (  periodType != "NA" && orgUnitIds != null && reportTypeName != "" ) 
	{
		document.generateReportForm.generate.disabled=false;
		
		$.post("getBirtReports.action",
			{
				periodType : periodType,
				orgUnitId : orgUnitIds[0],
				reportType : reportTypeName
			},
			function (data)
			{
				getReportsReceived(data);
			},'xml');
	} 
	else 
	{
		document.generateReportForm.generate.disabled=true;
		
		clearList( selectedReportId );
	}


}

function getReportsReceived( xmlObject ) 
{
	var selectedReportId = document.getElementById("selectedReportId");
	var orgUnitName = document.getElementById("ouNameTB");
	var orgUnitUidValue = document.getElementById("orgUnitUid");
	
	var ouLavel = "";
	
	clearList(selectedReportId);

	var reports = xmlObject.getElementsByTagName("report");
	for ( var i = 0; i < reports.length; i++) 
	{
		var id = reports[i].getElementsByTagName("id")[0].firstChild.nodeValue;
		var name = reports[i].getElementsByTagName("name")[0].firstChild.nodeValue;
		var reportDesignFileName = reports[i].getElementsByTagName("reportDesignFileName")[0].firstChild.nodeValue;
		
		
		var ouName = reports[i].getElementsByTagName("ouName")[0].firstChild.nodeValue;
		var orgUnitUid = reports[i].getElementsByTagName("orgUnitUid")[0].firstChild.nodeValue;
		
		ouLavel = reports[i].getElementsByTagName("ouLavel")[0].firstChild.nodeValue;
		
		orgUnitName.value = ouName;
		orgUnitUidValue.value = orgUnitUid;
		
		$("#selectedReportId").append("<option value='"+ reportDesignFileName +"'>" + name + "</option>");
		
		//$("#reportList").append("<option value='"+ id +"'>" + name + "</option>");
	}
	
	document.generateReportForm.generate.disabled=false;

	//alert( ouLavel );
}