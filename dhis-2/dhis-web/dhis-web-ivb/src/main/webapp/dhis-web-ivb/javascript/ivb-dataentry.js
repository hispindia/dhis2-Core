var COLOR_GREEN = '#b9ffb9';
var COLOR_YELLOW = '#fffe8c';
var COLOR_RED = '#ff8a8a';
var COLOR_ORANGE = '#ff6600';
var COLOR_WHITE = '#ffffff';
var COLOR_GREY = '#cccccc';
var LocaleColor = 'black';
var organisationUnitUid = "";
var dataSetUid = 'HrCL2cRzGqv';
var links = [];
//var de_commentMap = new Object();


function getOrgUnitwithDataSet(orgUnitUid)
{
    organisationUnitUid = orgUnitUid;

    window.location.href="dashboard.action?orgUnitUid="+orgUnitUid;
}

function getDataSet( datasetUid )
{
    $("#orgUnitUid").val();

    if($("#orgUnitUid").val() != null)
    {
        window.location.href="singleCountry.action?dataSetId="+datasetUid+"&orgUnitUid="+$("#orgUnitUid").val();
    }
    else
    {
        window.location.href="singleCountry.action?dataSetId="+datasetUid;
    }
}


function numberToCurreny(value){
    return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function currnecyToNumber(value){
    return value.replace(/\s*,\s*/g, "");
}
function numberToPercentageFormat(value){
        return value+"%";
}

function percentageToNumberFormat(value){
     return value.replace(/\s*%\s*/g, "");
}

function altRows(id){
    if(document.getElementsByTagName){
        if(document.getElementById(id) != null)
        {
            var table = document.getElementById(id);
            var rows = table.getElementsByTagName("tr");

            for(i = 0; i < rows.length; i++){
                if(i % 2 == 0){
                    rows[i].className = "evenrowcolor";
                }else{
                    rows[i].className = "oddrowcolor";
                }
            }
        }
    }
}
window.onload=function(){
    altRows('alternatecolor');
}

function periodIs()
{
    var period = document.getElementById("selectedPeriodId").value;
    $('#contentDiv').load("getReportsPage.action",
        {
            selectedPeriod:period
        }
        , function( ){
            altRows('alternatecolor');
        });

}

function ajax_login()
{
    $( '#login_button' ).bind( 'click', function()
    {
        var username = $( '#username' ).val();
        var password = $( '#password' ).val();

        $.post( '../dhis-web-commons-security/login.action', {
            'j_username' : username,
            'j_password' : password
        } ).success( function()
            {
                var ret = dhis2.availability.syncCheckAvailability();

                if ( !ret )
                {
                    alert( i18n_ajax_login_failed );
                }
            } );
    } );
}
var flag = true;
var oldpresentValue="";
function saveRegionValue(existingComment, existingvalue, dataelement ,source ,  option ,count, value, type)
{
    if(flag==true){
        oldpresentValue=existingvalue;
        flag=false;
    }

    var presentValue = document.getElementById(value+'').value;

    /*var oldComment = existingComment;
    var presentComment = document.getElementById(commentAreaId).value;
*/
    var commentAreaId = dataelement+"-"+source+"-"+option+"-"+count+"-comment";

    for (var i = 0; i < commentRequiredDeList.length; i++)
    {
        if(dataelement == commentRequiredDeList[i]){
            if(oldpresentValue.trim() != null ) {
                if(oldpresentValue.toLowerCase() != presentValue.toLowerCase())
                {
                    showWarningMessage( "Please enter some comment" );
                    document.getElementById(commentAreaId).style.backgroundColor='#FFFF33';
                }
            }
        }
    }
    oldpresentValue = document.getElementById(value+'').value;

    var period = document.getElementById("selectedPeriodId").value;
    var fieldId = '#'+value;
    if(type == 'bool')
    {
        var s = document.getElementById(value+'');
        var value1 = s.options[s.selectedIndex].value;

        if( value1 == "-1")
        {
            alert("Please Select One Option Value");
            s.options[0].selected = true;
            return true;
        }
    }
    else if(type == 'string')
    {
        var value1 = document.getElementById(value+'').value;
    }
    if(type == 'date')
    {
        var value1 = document.getElementById(value+'').value;
        var dateReg1 = /^\d{4}-(0[1-9]|1[1-2]|10)$/;
        var dateReg2 = /^\d{4}$/;
        var dateReg3 = /^\d{4}-Q[1-4]$/;
        var dateReg4 = /^\d{4}-(0[1-9]|1[1-2]|10)-(0[1-9]|1[0-9]|2[0-9]|30|31)$/;

        if( value1 == "na" || value1 == "NA" || value1 == "" || value1.match(dateReg1) || value1.match(dateReg2) || value1.match(dateReg3) || value1.match(dateReg4) )
        { }
        else
        {
            alert("Please Enter date in given format YYYY or YYYY-Qn or YYYY-MM or YYYY-MM-DD or na or NA e.g. 2012 or 2012-Q1 or 2012-01 or 2012-01-01");
            document.getElementById(value+'').value = "";
            return true;
        }
    }
    else if(type.trim() == 'int')
    {
        var value1 = document.getElementById(value+'').value;

        value1 = currnecyToNumber(value1);  //RegExp to replace special character ,
        value1 = percentageToNumberFormat(value1);

        if( isNaN(value1))
        {
            alert("Please Enter Numeric Value");
            document.getElementById(value+'').value = "";
            return true;
        }
        else
        {
            for (var i = 0; i < percentageRequiredList.length; i++)
            {
                if(dataelement == percentageRequiredList[i])
                {
                    document.getElementById(value+'').value = numberToPercentageFormat(value1);
                    return;
                }
            }
//            value1 =  value1.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
            document.getElementById(value+'').value = numberToCurreny(value1);
        }
    }
     else
    {
        var value1 = document.getElementById(value+'').value;
    }
    var defaultValue = document.getElementById(value+'').defaultValue;

//    value1 = value1.replace(/\s*,\s*/g, "");  //RegExp to replace special character ,
//    value1 = value1.replace(/\s*%\s*/g, "");

    if(value1 == "na" || value1 == "NA"){
        value1="";
    }

    console.log("final value to save in db "+value1);

    if(defaultValue != value1)
    {
        var conflict;
        if(document.getElementById("conflict").checked)
        {
            conflict = "checked";
        }
        else
        {
            conflict = "null";
        }
        var dataValue = {
            'dataElementId' : dataelement,
            'optionComboId' : option,
            'organisationUnitId' : source,
            'periodId' : period,
            'value' : value1,
            'conflict': conflict
        };
        jQuery.ajax( {
            url: 'saveValue.action',
            data: dataValue,
            dataType: 'json',
            success: handleSuccess,
            error: handleError
        } );
    }
    function handleSuccess( json )
    {
        var code = json.c;

        if ( code == 0 ) // Value successfully saved on server
        {
            markValue( fieldId, COLOR_GREEN );
        }
        else if ( code == 2 )
        {
            markValue( fieldId, COLOR_RED );
            window.alert( i18n_saving_value_failed_dataset_is_locked );
        }
        else // Server error during save
        {
            markValue( fieldId, COLOR_RED );
            window.alert( i18n_saving_value_failed_status_code + '\n\n' + code );
        }
    }

    function handleError( jqXHR, textStatus, errorThrown )
    {
        markValue( fieldId, COLOR_RED );
    }

    function markValue( fieldId, color )
    {
        jQuery( fieldId ).css( 'background-color', color );
    }

}
//var flag1 = true;
function saveRegionValueOption(existingComment,existingvalue, dataelement ,source , option ,count,value)
{

    var existingVal = existingvalue;

    var oldComment = existingComment;
    var commentAreaId = dataelement+"-"+source+"-"+option+"-"+count+"-comment";
    var presentComment = document.getElementById(commentAreaId).value;

    var period = document.getElementById("selectedPeriodId").value;
    var s = document.getElementById(value+'');
    var value1 = s.options[s.selectedIndex].value;

    if(value1 == "-1")
    {
        alert("Please Selet at least one option");
        s.options[0].selected = true;
        return true;
    }
    else
    {
        for (var i = 0; i < commentRequiredDeList.length; i++)
        {
            if(dataelement == commentRequiredDeList[i]){

                if(existingVal.trim() != null || existingVal.trim() != "" ) {
                    if(existingVal.toLowerCase() != value1.toLowerCase())
                    {
                        showWarningMessage( "Please enter some comment" );
                        document.getElementById(commentAreaId).style.backgroundColor='#FFFF33';
                    }
                }

            }
        }

        /*    var commentAreadId = dataelement+"-"+source+"-"+option+"-comment";
         de_commentMap[dataelement] = commentAreadId;*/

        var defaultValue = document.getElementById(value+'').defaultValue;
        if(defaultValue != value1)
        {
            var conflict;
            if(document.getElementById("conflict").checked)
            {
                conflict = "checked";
            }
            else
            {
                conflict = "null";
            }
            var fieldId = '#'+value;
            var dataValue = {
                'dataElementId' : dataelement,
                'optionComboId' : option,
                'organisationUnitId' : source,
                'periodId' : period,
                'value' : value1,
                'conflict': conflict
            };
            jQuery.ajax( {
                url: 'saveValue.action',
                data: dataValue,
                dataType: 'json',
                success: handleSuccess2,
                error: handleSuccess2
            } );
        }
    }

    function handleSuccess2( json )
    {
        var code = json.c;

        if ( code == 0 ) // Value successfully saved on server
        {
            markValue2( fieldId, COLOR_GREEN );
        }
        else if ( code == 2 )
        {
            markValue2( fieldId, COLOR_RED );
            window.alert( i18n_saving_value_failed_dataset_is_locked );
        }
        else // Server error during save
        {
            markValue2( fieldId, COLOR_RED );
            window.alert( i18n_saving_value_failed_status_code + '\n\n' + code );
        }
    }

    function handleError2( jqXHR, textStatus, errorThrown )
    {

        markValue2( fieldId, COLOR_RED );
    }

    function markValue2( fieldId, color )
    {
        jQuery( fieldId ).css( 'background-color', color );
    }

}

function setCommentBoxSize( comment )
{

    document.getElementById(comment+'').style.height = "20px";
}

function saveComment(existingDeValue, existingCommentValue, dataelement ,source , option ,count,value,comment,type)
{

    var period = document.getElementById("selectedPeriodId").value;
    var value1 = document.getElementById(value+'').value;
    var comment1 = document.getElementById(comment+'').value;
    var defaultValue = document.getElementById(comment+'').defaultValue;

     /*document.getElementById(comment+'').style.height = "20px";
     document.getElementById(comment+'').title = document.getElementById(comment+'').value;
     */
    var conflict;
    if(document.getElementById("conflict").checked)
    {
        conflict = "checked";
    }
    else
    {
        conflict = "null";
    }
    var fieldId = '#'+comment;

    if(defaultValue != comment1){
        var dataValue = {
            'dataElementId' : dataelement,
            'optionComboId' : option,
            'organisationUnitId' : source,
            'periodId' : period,
            'value' : value1,
            'comment': comment1,
            'conflict': conflict
        };
        jQuery.ajax( {
            url: 'saveComment.action',
            data: dataValue,
            dataType: 'json',
            success: handleSuccess1,
            error: handleError1
        } );
    }
    function handleSuccess1( json )
    {
        var code = json.c;

        if ( code == 0 ) // Value successfully saved on server
        {
            markValue1( fieldId, COLOR_GREEN );
        }
        else if ( code == 2 )
        {
            markValue1( fieldId, COLOR_RED );
            window.alert( i18n_saving_value_failed_dataset_is_locked );
        }
        else // Server error during save
        {
            markValue1( fieldId, COLOR_RED );
            window.alert( i18n_saving_value_failed_status_code + '\n\n' + code );
        }
    }

    function handleError1( jqXHR, textStatus, errorThrown )
    {

        markValue1( fieldId, COLOR_RED );
    }

    function markValue1( fieldId, color )
    {
        jQuery( fieldId ).css( 'background-color', color );
    }
}
function saveCommentOption(existingDeValue, existingCommentValue,dataelement ,source , option ,count,value,comment)
{

 //   saveRegionValueOption(existingCommentValue,existingDeValue, dataelement ,source , option ,count,value)

    var period = document.getElementById("selectedPeriodId").value;
    var s = document.getElementById(value+'');
    var value1 = s.options[s.selectedIndex].value;

    var comment1 = document.getElementById(comment+'').value;


    if(value1 == "-1")
    {
        value1 = "";
    }
    var conflict;
    if(document.getElementById("conflict").checked)
    {
        conflict = "checked";
    }
    else
    {
        conflict = "null";
    }

    //document.getElementById(comment+'').style.height = "20px";
    //document.getElementById(comment+'').title = document.getElementById(comment+'').value;
    var fieldId = '#'+comment;
    var defaultValue = document.getElementById(comment+'').defaultValue;

    if(defaultValue != comment1){
        var dataValue = {
            'dataElementId' : dataelement,
            'optionComboId' : option,
            'organisationUnitId' : source,
            'periodId' : period,
            'value' : value1,
            'comment': comment1,
            'conflict': conflict
        };
        jQuery.ajax( {
            url: 'saveComment.action',
            data: dataValue,
            dataType: 'json',
            success: handleSuccess1,
            error: handleError1
        } );
    }
    function handleSuccess1( json )
    {
        var code = json.c;

        if ( code == 0 ) // Value successfully saved on server
        {
            markValue1( fieldId, COLOR_GREEN );
        }
        else if ( code == 2 )
        {
            markValue1( fieldId, COLOR_RED );
            window.alert( i18n_saving_value_failed_dataset_is_locked );
        }
        else // Server error during save
        {
            markValue1( fieldId, COLOR_RED );
            window.alert( i18n_saving_value_failed_status_code + '\n\n' + code );
        }
    }

    function handleError1( jqXHR, textStatus, errorThrown )
    {

        markValue1( fieldId, COLOR_RED );
    }

    function markValue1( fieldId, color )
    {
        jQuery( fieldId ).css( 'background-color', color );
    }
}
function copyValueAndComment( dataelement, source, option, count, value, comment )
{
    var period = document.getElementById("selectedPeriodId").value;
    var value1 = document.getElementById(value+'').value;
    var comment1 = document.getElementById(comment+'').value;
    var dataValue = {
        'dataElementId' : dataelement,
        'optionComboId' : option,
        'organisationUnitId' : source,
        'periodId' : period,
        'value' : value1,
        'comment': comment1
    };
    jQuery.ajax( {
        url: 'copyValueAndComment.action',
        data: dataValue,
        dataType: 'json',
        success: handleSuccess,
        error: handleError
    } );
    function handleSuccess( json )
    {
        showSuccessMessage( "Copied" );
        document.getElementById('overlay').style.visibility = 'visible';
        window.location.href = window.location.href ;
    }
    function handleError( json )
    {
        showWarningMessage( "Not Copied" );
    }
}
function copyRegionValueAndComment(dataelement ,source , option ,count,value,comment,userName)
{
    var today = new Date();
    var dd = today.getDate();
    var mm = today.getMonth()+1;//January is 0!`

    var yyyy = today.getFullYear();
    if(dd<10){dd='0'+dd}
    if(mm<10){mm='0'+mm}

    var today = yyyy+'-'+mm+'-'+dd;

    var period = document.getElementById("selectedPeriodId").value;
    var value1 = document.getElementById(value+'').value;
    var comment1 = document.getElementById(comment+'').value;
    var dataValue = {
        'dataElementId' : dataelement,
        'optionComboId' : option,
        'organisationUnitId' : source,
        'periodId' : period,
        'value' : value1,
        'comment': comment1
    };
    jQuery.ajax( {
        url: 'copyValueAndComment.action',
        data: dataValue,
        dataType: 'json',
        success: handleSuccess,
        error: handleError
    } );
    function handleSuccess( json )
    {
        showSuccessMessage( "Copied" );
        $("#currentUser_"+value).html(userName+'('+today+')');
        $("#copy_"+value).attr("src","images/disable_copy.png");
        $("#copy_"+value).parent().removeAttr("href");

    }
    function handleError( json )
    {
        showWarningMessage( "Not Copied" );
    }
}
function copyValueAndCommentOption(dataelement ,source , option ,count,value,comment)
{
    var period = document.getElementById("selectedPeriodId").value;
    var s = document.getElementById(value+'');
    var value1 = s.options[s.selectedIndex].value;
    if(value1 == "-1")
    {
        alert("Please Select at least one option");
    }
    else
    {
        if(document.getElementById(comment+'').value == null)
        {
            var comment1 = "";
        }
        else
        {
            var comment1 = document.getElementById(comment+'').value;
        }

        var dataValue = {
            'dataElementId' : dataelement,
            'optionComboId' : option,
            'organisationUnitId' : source,
            'periodId' : period,
            'value' : value1,
            'comment': comment1
        };
        jQuery.ajax( {
            url: 'copyValueAndComment.action',
            data: dataValue,
            dataType: 'json',
            success: handleSuccess1,
            error: handleError1
        } );

    }
    function handleSuccess1( json )
    {
        showSuccessMessage( "Copied" );
        document.getElementById('overlay').style.visibility = 'visible';
        window.location.href = window.location.href ;
    }
    function handleError1( json )
    {
        showWarningMessage( "Not Copied" );
    }
}
function copyRegionValueAndCommentOption(dataelement ,source , option ,count,value,comment,userName)
{
    var today = new Date();
    var dd = today.getDate();
    var mm = today.getMonth()+1;//January is 0!`

    var yyyy = today.getFullYear();
    if(dd<10){dd='0'+dd}
    if(mm<10){mm='0'+mm}

    var today = yyyy+'-'+mm+'-'+dd;
    var period = document.getElementById("selectedPeriodId").value;
    var s = document.getElementById(value+'');
    var value1 = s.options[s.selectedIndex].value;
    if(value1 == "-1")
    {
        alert("Please Select at least one option");
    }
    else
    {
        if(document.getElementById(comment+'').value == null)
        {
            var comment1 = "";
        }
        else
        {
            var comment1 = document.getElementById(comment+'').value;
        }

        var dataValue = {
            'dataElementId' : dataelement,
            'optionComboId' : option,
            'organisationUnitId' : source,
            'periodId' : period,
            'value' : value1,
            'comment': comment1
        };
        jQuery.ajax( {
            url: 'copyValueAndComment.action',
            data: dataValue,
            dataType: 'json',
            success: handleSuccess1,
            error: handleError1
        } );

    }
    function handleSuccess1( json )
    {
        showSuccessMessage( "Copied" );
        $("#currentUser_"+value).html(userName+'('+today+')');
        $("#copy_"+value).attr("src","images/disable_copy.png");
        $("#copy_"+value).parent().removeAttr("href");
    }
    function handleError1( json )
    {
        showWarningMessage( "Not Copied" );
    }
}
function getPeriod()
{
    var value = document.getElementById('periodType').value;
    var dataValue = {
        'periodType' : value
    };
    jQuery.ajax( {
        url: 'getPeriod.action',
        data: dataValue,
        dataType: 'json'
    } );
}
function getHistory(currentPeriod,dataElement,orgUnit,optionSet,value, updateStatus,status)
{
    var conflict = "";
    if(document.getElementById("conflict") != null && document.getElementById("conflict").checked == true)
    {
        conflict = "skip conflict";
    }
    else
    {
        conflict = "conflict";
    }

    var period = document.getElementById("selectedPeriodId").value;
    if(value == "")
    {
        $.fancybox.open('#history');
    }
    else{

        document.getElementById('overlay').style.visibility = 'visible';
        $('#history').load("getHistoryAction.action",
            {
                dataElementId : dataElement,
                optionComboId : optionSet,
                organisationUnitId : orgUnit,
                periodId : period,
                updateStatus : updateStatus,
                selectedPeriod : currentPeriod,
                conflict : conflict,
                checkNumberType:status
            }
            , function( ){
                document.getElementById('overlay').style.visibility = 'hidden';
                altRows('alternatecolor2');
                $.fancybox.open('#history');
            });
    }
}
function getHelp(indicatorId,countryUid)
{
    $('#help').load("getHelpAction.action",
        {
            indicatorId : indicatorId,
            countryUid : countryUid,
        }
        , function( ){
            $.fancybox.open('#help');
            altRows('alternatecolor1');
        });
}
function addTechnical(dataElement,organisationUnit,dataElementCategoryOptionCombo,dataElementName,dataEntryId)
{
    var period = document.getElementById("selectedPeriodId").value;
    $('#technical').load("getTechnicalAction.action",
        {
            dataElementId : dataElement,
            optionComboId : dataElementCategoryOptionCombo,
            organisationUnitId : organisationUnit,
            periodId : period
        }
        , function( ){
            altRows('alternatecolor3');
            document.getElementById("dataElementId").value = dataElement;
            document.getElementById("dataElementCategoryOptionComboId").value = dataElementCategoryOptionCombo;
            document.getElementById("organisationUnitId").value = organisationUnit;
            document.getElementById("dataValue").value = document.getElementById(dataEntryId).value
            $.fancybox.open('#technical');
        });
}
function saveTechnical()
{

    if($("#techAssisId").val() == null || $("#techAssisId").val() == "")
    {
        alert('Please Enter Technical Assistance');
        return true;
    }
    else
    {
        var period = document.getElementById("selectedPeriodId").value;
        var dataValue = {
            'dataElementId' : document.getElementById("dataElementId").value,
            'optionComboId' : document.getElementById("dataElementCategoryOptionComboId").value,
            'organisationUnitId' : document.getElementById("organisationUnitId").value,
            'periodId' : period,
            'value' : document.getElementById("dataValue").value,
            'comment' : document.getElementById("techAssisId").value
        };
        jQuery.ajax( {
            url: 'saveTechnicalAssistance.action',
            data: dataValue,
            dataType: 'json',
            success: closeDialog
        } );
    }
    function closeDialog( json )
    {
        $.fancybox.close('#technical');
    }
}

function addEventListeners()
{
    $( '[class="valueDate"]' ).each( function( i )
    {
        var id = $( this ).attr( 'id' );
        datePicker( id );
    });
}

function getIndicatorHelp(indicatorId,countryUid,selectedPeriod,userName)
{
    $('#indicatorHelp').load("getIndicatorHelpAction.action",
        {
            indicatorId : indicatorId,
            countryUid : countryUid,
            selectedPeriod : selectedPeriod,
            userName : userName
        }
        , function( ){
            $.fancybox.open('#indicatorHelp');
            altRows('alternatecolor1');
            altRows('alternatecolor2');
        });
}


