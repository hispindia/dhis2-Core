function deleteDiscussion(id)
{
		var dataValue = {
        'dataValueAuditId' : id       
    	};
    	if (confirm('Are You Sure You Want to Delete')) {
   			jQuery.ajax( {
            url: 'removeHistory.action',
            data: dataValue,
            dataType: 'json',
            success: handleSuccess,
            error: handleError
        } );
		} else {   		
		}    	
    function handleSuccess( json )
    {
    	$("#dis_td_"+id).parent().remove();
    	technicalPaging();
    	$("#alternatecolor4").trigger("update");
    	showSuccessMessage( "Indicator Discussion Deleted" );
    }
    function handleError( json )
    {
    	showWarningMessage( json.message );
    }
}
function deleteTechnicalAssistance(id)
{
		var dataValue = {
        'dataValueAuditId' : id       
    	};
    	if (confirm('Are You Sure You Want to Delete')) {
   			jQuery.ajax( {
            url: 'removeHistory.action',
            data: dataValue,
            dataType: 'json',
            success: handleSuccess,
            error: handleError
        } );
		} else {   		
		}    	
    function handleSuccess( json )
    {
    	$("#tech_td_"+id).parent().remove();
    	technicalPaging();
    	$("#alternatecolor3").trigger("update");
    	showSuccessMessage( "Indicator History Deleted" );
    }
    function handleError( json )
    {
    	showWarningMessage( json.message );
    }
}

function deleteHistory(id)
{		
		var dataValue = {
        'dataValueAuditId' : id       
    	};
    	if (confirm('Are You Sure You Want to Delete')) {
   			jQuery.ajax( {
            url: 'removeHistory.action',
            data: dataValue,
            dataType: 'json',
            success: handleSuccess,
            error: handleError
        } );
		} else {   		
		}    	
    function handleSuccess( json )
    {
    	$("#td_"+id).parent().remove();
    	$("#alternatecolor2").trigger("update");
    	showSuccessMessage( "Indicator History Deleted" );
    }
    function handleError( json )
    {
    	showWarningMessage( json.message );
    }
}

function moveHistoryDataToCurrentPeriod( dataValueAuditId )
{
	var selectedPeriodId = document.getElementById('selectedPeriodId').value;
	
	var dataValue = {
			'dataValueAuditId' : dataValueAuditId,
			'selectedPeriodId' : selectedPeriodId
    	};
	
	if( confirm('Are you sure you want to move this data to current period') ) 
	{
		jQuery.ajax( 
				{
					url: 'moveHistoryDataToCurrentPeriod.action',
					data: dataValue,
					dataType: 'json',
					success: handleSuccess,
					error: handleError
				} );	
	} 
	
    function handleSuccess( json )
    {
    	if( json.c == "0" )
    	{
    		document.getElementById("curValueDiv").innerHTML = json.curValueDiv;
    		document.getElementById("curCommentDiv").innerHTML = json.curCommentDiv;
    		document.getElementById("curStoredByDiv").innerHTML = json.curStoredByDiv + "(" + json.curTimeStampDiv +")";
    		//alert( "Selected hisotry data is moved to current period" );
    	}
    	else if( json.c == "1" || json.c == "2" )
    	{
    		alert( "Selected hisotry data is not moved to current period" );
    	}    	
    }
    
    function handleError( json )
    {
    	showWarningMessage( json.message );
    }
}


function editComment(id,comment)
{	
	$("#comment_"+id).html('');
	$("#comment_"+id).append("<textarea cols='35' rows='1' id='textComment_"+id+"' onblur='javascript:saveCurrentComment(\""+id+"\",\""+comment+"\")' onload='javascript:textAreaAdjust(this)'>"+comment+"</textarea>");
	document.getElementById('textComment_'+id).focus()
}
function saveCurrentComment(id,comment)
{
	var text_comment = document.getElementById('textComment_'+id).value;
	var dataValue = {
        'dataValueAuditId' : id,
        'comment' : text_comment      
    	};
    jQuery.ajax( {
            url: 'editHistoryAction.action',
            data: dataValue,
            dataType: 'json',
            success: handleSuccess,
            error: handleError
        } );        
    function handleSuccess( json )
    {
    	$("#comment_"+id).html(text_comment);
    }
    function handleError( json )
    {
    	alert("error");
    }
	
}
function textAreaAdjust(o) {
    o.style.height = "1px";
    o.style.height = (25+o.scrollHeight)+"px";
}
function editDiscussionComment(id,comment)
{	
	$("#dis_"+id).html('');
	$("#dis_"+id).append("<textarea cols='35' rows='1' id='discussionTextComment_"+id+"' onblur='javascript:saveDiscussionComment(\""+id+"\",\""+comment+"\")' onload='javascript:textAreaAdjust(this)'>"+comment+"</textarea>");
	document.getElementById('technicalTextComment_'+id).focus()
}
function saveDiscussionComment(id,comment)
{
	var text_comment = document.getElementById('discussionTextComment_'+id).value;
	var dataValue = {
        'dataValueAuditId' : id,
        'comment' : text_comment      
    	};
    jQuery.ajax( {
            url: 'updateDiscussionAction.action',
            data: dataValue,
            dataType: 'json',
            success: handleSuccess,
            error: handleError
        } );        
    function handleSuccess( json )
    {
    	$("#dis_"+id).html(text_comment);
    }
    function handleError( json )
    {
    	alert("error");
    }
}
function editTechnicalComment(id,comment)
{	
	$("#techcomment_"+id).html('');
	$("#techcomment_"+id).append("<textarea cols='35' rows='1' id='technicalTextComment_"+id+"' onblur='javascript:saveTechCurrentComment(\""+id+"\",\""+comment+"\")' onload='javascript:textAreaAdjust(this)'>"+comment+"</textarea>");
	document.getElementById('technicalTextComment_'+id).focus()
}
function saveTechCurrentComment(id,comment)
{
	var text_comment = document.getElementById('technicalTextComment_'+id).value;
	var dataValue = {
        'dataValueAuditId' : id,
        'comment' : text_comment      
    	};
    jQuery.ajax( {
            url: 'unhideHistoryAction.action',
            data: dataValue,
            dataType: 'json',
            success: handleSuccess,
            error: handleError
        } );        
    function handleSuccess( json )
    {
    	$("#techcomment_"+id).html(text_comment);
    }
    function handleError( json )
    {
    	alert("error");
    }
}
function hideDataValueAudit(id,hideUnhideAuthority)
{		
		var dataValue = {
	        'dataValueAuditId' : id
	    	};
	    	if (confirm('Are You Sure You Want to Hide')) {
	    jQuery.ajax( {
	            url: 'editHistoryAction.action',
	            data: dataValue,
	            dataType: 'json',
	            success: handleSuccess,
	            error: handleError
	        } );
	       }else
	       {}        
	    function handleSuccess( json )
	    {
	    	if(hideUnhideAuthority == 'Yes')  
	    	{	    			    		
	    		$("#hide_"+id).attr("src","images/unhide.png");
	    		$("#link_hide_"+id).removeAttr("href");	    		
	    	}
	    	else if(hideUnhideAuthority == 'No')
	    	{
	    		$("#td_"+id).parent().remove();
	    		$("#alternatecolor2").trigger("update");
	    	} 						    	
	    	  	
	    }
	    function handleError( json )
	    {
	    	alert("error");
	    }				
}
function discussionHideDataValueAudit(id,hideUnhideAuthority)
{		
		var dataValue = {
	        'dataValueAuditId' : id
	    	};
	    	if (confirm('Are You Sure You Want to Hide')) {
	    jQuery.ajax( {
	            url: 'updateDiscussionAction.action',
	            data: dataValue,
	            dataType: 'json',
	            success: handleSuccess,
	            error: handleError
	        } );
	       }else
	       {}        
	    function handleSuccess( json )
	    {
	    	if(hideUnhideAuthority == 'Yes')  
	    	{
	    		$("#dis_hide_"+id).attr("src","images/unhide.png"); 
	    		$("#link_dishide_"+id).removeAttr("href");
	    	}
	    	else if(hideUnhideAuthority == 'No')
	    	{
	    		$("#dis_td_"+id).parent().remove();
	    		$("#alternatecolor4").trigger("update");
	    	} 						    	
	    	  	
	    }
	    function handleError( json )
	    {
	    	alert("error");
	    }				
}
function techHideDataValueAudit(id,hideUnhideAuthority)
{		
		var dataValue = {
	        'dataValueAuditId' : id
	    	};
	    	if (confirm('Are You Sure You Want to Hide')) {
	    jQuery.ajax( {
	            url: 'editHistoryAction.action',
	            data: dataValue,
	            dataType: 'json',
	            success: handleSuccess,
	            error: handleError
	        } );
	       }else
	       {}        
	    function handleSuccess( json )
	    {
	    	if(hideUnhideAuthority == 'Yes')  
	    	{
	    		$("#tech_hide_"+id).attr("src","images/unhide.png"); 
	    		$("#link_techhide_"+id).removeAttr("href");
	    	}
	    	else if(hideUnhideAuthority == 'No')
	    	{
	    		$("#tech_td_"+id).parent().remove();	    		
	    		$("#alternatecolor3").trigger("update");
	    	} 						    	
	    	  	
	    }
	    function handleError( json )
	    {
	    	alert("error");
	    }				
}
function unhideDataValueAudit(id)
{
		var dataValue = {
	        'dataValueAuditId' : id
	    	};
	    	if (confirm('Are You Sure You Want to Unhide')) {
	    jQuery.ajax( {
	            url: 'unhideHistoryAction.action',
	            data: dataValue,
	            dataType: 'json',
	            success: handleSuccess,
	            error: handleError
	        } );
	       }else
	       {}        
	    function handleSuccess( json )
	    {
	    	$("#unhide_"+id).attr("src","images/hide.png"); 
	    	$("#link_hide_"+id).removeAttr("href");	    	
	    }
	    function handleError( json )
	    {
	    	alert("error");
	    }				
}
function discussionUnhideDataValueAudit(id)
{
		var dataValue = {
	        'dataValueAuditId' : id
	    	};
	    	if (confirm('Are You Sure You Want to Unhide')) {
	    jQuery.ajax( {
	            url: 'unhideDiscussionAction.action',
	            data: dataValue,
	            dataType: 'json',
	            success: handleSuccess,
	            error: handleError
	        } );
	       }else
	       {}        
	    function handleSuccess( json )
	    {	    	
	    	$("#dis_unhide_"+id).attr("src","images/hide.png");
	    	$("#link_dishide_"+id).removeAttr("href");
	    }
	    function handleError( json )
	    {
	    	alert("error");
	    }				
}

function techUnhideDataValueAudit(id)
{
	var dataValue = {
        'dataValueAuditId' : id
    	};
    	if (confirm('Are You Sure You Want to Unhide')) {
    jQuery.ajax( {
            url: 'unhideHistoryAction.action',
            data: dataValue,
            dataType: 'json',
            success: handleSuccess,
            error: handleError
        } );
       }else
       {}        
    function handleSuccess( json )
    {	    	
    	$("#tech_unhide_"+id).attr("src","images/hide.png"); 
    	$("#link_techhide_"+id).removeAttr("href");
    }
    function handleError( json )
    {
    	alert("error");
    }				
}

function saveDataComment( dataElementId, orgUnitId, optionComboId, dataValue1, lastUpdated, sourceName, periodName, deleteAuthority, editAuthority, hideAuthority )
{
	var period = document.getElementById("selectedPeriodId").value;		
	var comment1 = document.getElementById('dvComment').value;	
	var fieldId = '#dvComment';
	
	var dataValue = {
        'dataElementId' : dataElementId,
        'optionComboId' : optionComboId,
        'organisationUnitId' : orgUnitId,
        'periodId' : period,
        'value' : dataValue1,
        'comment': comment1
    };
    if(comment1 == null || comment1 == "")
    {
    	alert('Please Enter Technical Assistance');
		return true;
    }
    else{    
    jQuery.ajax( {
            url: 'saveTechnicalAssistance.action',
            data: dataValue,
            dataType: 'json',
            success: handleSuccess1,
            error: handleError1
        } );
   }
    function handleSuccess1( json )
    {
    	document.getElementById('dvComment').value = '';
    	
    	$("#alternatecolor3").append("<tr>" +
    					"<td width='10%'>"+lastUpdated+"</td>" +
    					"<td width='8%'>"+sourceName+"</td>" +
    					"<td width='10%'>"+periodName+"</td>" +
    					"<td width='15%'>"+dataValue1+"</td>" +
    					"<td width='35%'>"+comment1+"</td>" +
    					"<td width='15%'></td>" +
    					"</tr>");
    	
    	altRows('alternatecolor3');
    	var pager2 = new PagerTech('alternatecolor3', 5);
    	pager2.init();
    	pager2.showPageNav('pager2', 'pagingControls2');
    	pager2.showPage(1);
    	   	
    }
    function handleError1( jqXHR, textStatus, errorThrown )
    {
    	alert("error");
    }
}
function saveDiscussion( dataElementId, orgUnitId, optionComboId, dataValue1, lastUpdated, sourceName, deleteAuthority, editAuthority, hideAuthority )
{
	var period = document.getElementById("selectedPeriodId").value;		
	var comment1 = document.getElementById('disComment').value;	
	var fieldId = '#disComment';
	
	var today = new Date();
	var dd = today.getDate();
	var mm = today.getMonth()+1;//January is 0!`

	var yyyy = today.getFullYear();
	if(dd<10){dd='0'+dd}
	if(mm<10){mm='0'+mm}
	
	var today = yyyy+'-'+mm+'-'+dd;
	
	var dataValue = {
        'dataElementId' : dataElementId,
        'optionComboId' : optionComboId,
        'organisationUnitId' : orgUnitId,
        'periodId' : period,
        'value' : dataValue1,
        'comment': comment1
    };
    if(comment1 == null || comment1 == "")
    {
    	alert('Please Enter Discussion Comment');
		return true;
    }
    else{    
    jQuery.ajax( {
            url: 'saveDiscussion.action',
            data: dataValue,
            dataType: 'json',
            success: handleSuccess1,
            error: handleError1
        } );
   }
    function handleSuccess1( json )
    {
    	document.getElementById('disComment').value = '';
    	
    	$("#alternatecolor4").append("<tr>" +    					
    					"<td width='15%'>"+sourceName+"</td>" +
    					"<td width='15%'>"+today+"</td>" +    					
    					"<td width='45%'>"+comment1+"</td>" +
    					"<td width='25%'></td>" +
    					"</tr>");
    	altRows('alternatecolor4');
    	var pager = new PagerDis('alternatecolor4', 5);
    	pager.init();
    	pager.showPageNav('pager', 'pagingControls3');
    	pager.showPage(1);
    	   	
    }
    function handleError1( jqXHR, textStatus, errorThrown )
    {       
    	alert("error");
    }

}
function closeDiscussion( dataElementId, orgUnitId, optionComboId, periodId )
{
	var dataValue = {
        'dataElementId' : dataElementId,
        'optionComboId' : optionComboId,
        'organisationUnitId' : orgUnitId,
        'periodId' : periodId
    };
	
	if (confirm('Are You Sure You Want to Close this Discussion')) {
			jQuery.ajax( {
        url: 'closeDiscussion.action',
        data: dataValue,
        dataType: 'json',
        success: handleSuccess,
        error: handleError
    } );
	} else {   		
	}   	
	
    function handleSuccess( json )
    {
    	$('#discussionComment').css('display', 'none');    	
    	$("#alternatecolor4").append("<tr id='disMsg'><td style='color:red;' colspan='4'><b>The discussion for this data element is currently closed.</b></td></tr>");
    	document.getElementById('closeDisButton').disabled = true;
    	document.getElementById('openDisButton').disabled = false;
    	altRows('alternatecolor4');
    }
    function handleError( jqXHR, textStatus, errorThrown )
    {       
    	alert("error");
    }
}
function openDiscussion(dataElementId,orgUnitId,optionComboId,periodId)
{	
	var dataValue = {
        'dataElementId' : dataElementId,
        'optionComboId' : optionComboId,
        'organisationUnitId' : orgUnitId,
        'periodId' : periodId
    };
	
	if (confirm('Are You Sure You Want to Open this Discussion')) {
			jQuery.ajax( {
        url: 'openDiscussion.action',
        data: dataValue,
        dataType: 'json',
        success: handleSuccess,
        error: handleError
    } );
	} else {   		
	}   	
	
    function handleSuccess( json )
    {
    	$('#discussionComment').css('display', 'block');    	
    	document.getElementById('closeDisButton').disabled = false;
    	document.getElementById('openDisButton').disabled = true;
    	$("#alternatecolor4 #disMsg").remove();    	
    }
    function handleError( jqXHR, textStatus, errorThrown )
    {       
    	alert("error");
    }
}