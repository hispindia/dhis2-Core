
dhis2.util.namespace( 'dhis2.de' );
dhis2.util.namespace( 'dhis2.de.api' );
dhis2.util.namespace( 'dhis2.de.event' );
dhis2.util.namespace( 'dhis2.de.cst' );

// API / methods to be used externally from forms / scripts

/**
 * Returns an object representing the currently selected state of the UI.
 * Contains properties for "ds", "pe", "ou" and the identifier of each
 * category with matching identifier values of the selected option.
 */
dhis2.de.api.getSelections = function() {
	var sel = dhis2.de.getCurrentCategorySelections();
	sel["ds"] = $( '#selectedDataSetId' ).val(),
	sel["pe"] = $( '#selectedPeriodId').val(),
	sel["ou"] = dhis2.de.currentOrganisationUnitId;	
	return sel;
}

// whether the browser is offline or not.
dhis2.de.isOffline = false;

// whether current user has any organisation units
dhis2.de.emptyOrganisationUnits = false;

// Identifiers for which zero values are insignificant, also used in entry.js
dhis2.de.significantZeros = [];

// Array with associative arrays for each data element, populated in select.vm
dhis2.de.dataElements = [];

// Associative array with [indicator id, expression] for indicators in form,
// also used in entry.js
dhis2.de.indicatorFormulas = [];

// Array with associative arrays for each data set, populated in select.vm
dhis2.de.dataSets = [];

// Maps input field to optionSet
dhis2.de.optionSets = {};

// Associative array with identifier and array of assigned data sets
dhis2.de.dataSetAssociationSets = [];

// Associate array with mapping between organisation unit identifier and data
// set association set identifier
dhis2.de.organisationUnitAssociationSetMap = [];

// Default category combo uid
dhis2.de.defaultCategoryCombo = undefined;

// Category combinations for data value attributes
dhis2.de.categoryCombos = {};

// Categories for data value attributes
dhis2.de.categories = {};

// LockExceptions
dhis2.de.lockExceptions = [];

// Array with keys {dataelementid}-{optioncomboid}-min/max with min/max values
dhis2.de.currentMinMaxValueMap = [];

// Indicates whether any data entry form has been loaded
dhis2.de.dataEntryFormIsLoaded = false;

// Indicates whether meta data is loaded
dhis2.de.metaDataIsLoaded = false;

// Currently selected organisation unit identifier
dhis2.de.currentOrganisationUnitId = null;

// Currently selected data set identifier
dhis2.de.currentDataSetId = null;

// Array with category objects, null if default category combo / no categories
dhis2.de.currentCategories = null;

// Current offset, next or previous corresponding to increasing or decreasing value
dhis2.de.currentPeriodOffset = 0;

// Current existing data value, prior to entry or modification
dhis2.de.currentExistingValue = null;

// Associative array with currently-displayed period choices, keyed by iso
dhis2.de.periodChoices = [];

// Periods locked because of data input periods start and end dates
dhis2.de.blackListedPeriods = [];

// Username of user who marked the current data set as complete if any
dhis2.de.currentCompletedByUser = null;

// Instance of the StorageManager
dhis2.de.storageManager = new StorageManager();

// Indicates whether current form is multi org unit
dhis2.de.multiOrganisationUnit = false;

// Indicates whether multi org unit is enabled on instance
dhis2.de.multiOrganisationUnitEnabled = false;

// Simple object to see if we have tried to fetch children DS for a parent before
dhis2.de.fetchedDataSets = {};

// "organisationUnits" object inherited from ouwt.js

// Constants

dhis2.de.cst.defaultType = 'INTEGER';
dhis2.de.cst.defaultName = '[unknown]';
dhis2.de.cst.dropDownMaxItems = 30;
dhis2.de.cst.formulaPattern = /#\{.+?\}/g;
dhis2.de.cst.separator = '.';
dhis2.de.cst.valueMaxLength = 50000;
dhis2.de.cst.metaData = 'dhis2.de.cst.metaData';
dhis2.de.cst.dataSetAssociations = 'dhis2.de.cst.dataSetAssociations';
dhis2.de.cst.downloadBatchSize = 5;

// Colors

dhis2.de.cst.colorGreen = '#b9ffb9';
dhis2.de.cst.colorYellow = '#fffe8c';
dhis2.de.cst.colorRed = '#ff8a8a';
dhis2.de.cst.colorOrange = '#ff6600';
dhis2.de.cst.colorWhite = '#fff';
dhis2.de.cst.colorGrey = '#ccc';
dhis2.de.cst.colorBorderActive = '#73ad72';
dhis2.de.cst.colorBorder = '#aaa';
dhis2.de.cst.colorLightGrey = '#dcdcdc';

// Form types

dhis2.de.cst.formTypeCustom = 'CUSTOM';
dhis2.de.cst.formTypeSection = 'SECTION';
dhis2.de.cst.formTypeMultiOrgSection = 'SECTION_MULTIORG';
dhis2.de.cst.formTypeDefault = 'DEFAULT';

// Events

dhis2.de.event.formLoaded = "dhis2.de.event.formLoaded";
dhis2.de.event.dataValuesLoaded = "dhis2.de.event.dataValuesLoaded";
dhis2.de.event.formReady = "dhis2.de.event.formReady";
dhis2.de.event.dataValueSaved = "dhis2.de.event.dataValueSaved";
dhis2.de.event.completed = "dhis2.de.event.completed";
dhis2.de.event.uncompleted = "dhis2.de.event.uncompleted";
dhis2.de.event.validationSucces = "dhis2.de.event.validationSuccess";
dhis2.de.event.validationError = "dhis2.de.event.validationError";

/**
 * Convenience method to be used from inside custom forms. When a function is
 * registered inside a form it will be loaded every time the form is loaded,
 * hence the need to unregister and the register the function.
 */
dhis2.de.on = function( event, fn )
{
    $( document ).off( event ).on( event, fn );
};

var DAO = DAO || {};

dhis2.de.getCurrentOrganisationUnit = function() 
{
    if ( $.isArray( dhis2.de.currentOrganisationUnitId ) ) 
    {
        return dhis2.de.currentOrganisationUnitId[0];
    }

    return dhis2.de.currentOrganisationUnitId;
};

DAO.store = new dhis2.storage.Store( {
    name: 'dhis2de',
    adapters: [ dhis2.storage.IndexedDBAdapter, dhis2.storage.DomSessionStorageAdapter, dhis2.storage.InMemoryAdapter ],
    objectStores: [ 'optionSets', 'forms' ]
} );

( function( $ ) {
    $.safeEach = function( arr, fn ) 
    {
        if ( arr )
        {
            $.each( arr, fn );
        }
    };
} )( jQuery );

$(document).bind('dhis2.online', function( event, loggedIn ) {
    dhis2.de.isOffline = false;

    if( loggedIn ) {
        dhis2.de.manageOfflineData();
    }
    else {
        var form = [
            '<form style="display:inline;">',
            '<label for="username">Username</label>',
            '<input name="username" id="username" type="text" style="width: 70px; margin-left: 10px; margin-right: 10px" size="10"/>',
            '<label for="password">Password</label>',
            '<input name="password" id="password" type="password" style="width: 70px; margin-left: 10px; margin-right: 10px" size="10"/>',
            '<button id="login_button" type="button">Login</button>',
            '</form>'
        ].join('');

        setHeaderMessage(form);
        dhis2.de.ajaxLogin();
    }
});

$(document).bind('dhis2.offline', function() {
    dhis2.de.isOffline = true;

    if( dhis2.de.emptyOrganisationUnits ) {
        setHeaderMessage(i18n_no_orgunits);
    }
    else {
        setHeaderMessage(i18n_offline_notification);
    }
});

/**
 * Page init. The order of events is:
 *
 * 1. Load ouwt 2. Load meta-data (and notify ouwt) 3. Check and potentially
 * download updated forms from server
 */
$( document ).ready( function()
{
	/* hide approval div */
	$( '#dHTApprovedDiv' ).hide();
	$( '#sHTApprovedDiv' ).hide();
	$( '#pMUAccountApprovedDiv' ).hide();
	/* hide approval div end */
	
    /**
     * Cache false necessary to prevent IE from caching by default.
     */
    $.ajaxSetup( {
        cache: false
    } );

    $( '#loaderSpan' ).show();

    $( '#orgUnitTree' ).one( 'ouwtLoaded', function( event, ids, names )
    {
        console.log( 'Ouwt loaded' );
                
        $.when( dhis2.de.getMultiOrgUnitSetting(), dhis2.de.loadMetaData(), dhis2.de.loadDataSetAssociations() ).done( function() {
        	dhis2.de.setMetaDataLoaded();
            organisationUnitSelected( ids, names );
        } );
    } );
} );

dhis2.de.manageOfflineData = function()
{
    if( dhis2.de.storageManager.hasLocalData() ) {
        var message = i18n_need_to_sync_notification
          + ' <button id="sync_button" type="button">' + i18n_sync_now + '</button>';

        setHeaderMessage(message);

        $('#sync_button').bind('click', dhis2.de.uploadLocalData);
    }
    else {
        if( dhis2.de.emptyOrganisationUnits ) {
            setHeaderMessage(i18n_no_orgunits);
        }
        else {
            setHeaderDelayMessage(i18n_online_notification);
        }
    }
};

dhis2.de.shouldFetchDataSets = function( ids ) {
    if( !dhis2.de.multiOrganisationUnitEnabled ) {
        return false;
    }

    if( !$.isArray(ids) || ids.length === 0 || (ids.length > 0 && dhis2.de.fetchedDataSets[ids[0]]) ) {
        return false;
    }

    var c = organisationUnits[ids[0]].c;

    if( $.isArray(c) && c.length > 0 && dhis2.de.organisationUnitAssociationSetMap[c[0]] ) {
        return false;
    }

    return true;
};

dhis2.de.getMultiOrgUnitSetting = function()
{
  return $.ajax({
    url: '../api/systemSettings?key=multiOrganisationUnitForms',
    dataType: 'json',
    async: false,
    type: 'GET',
    success: function( data ) {
      var isMultiOrgUnit = data && data.multiOrganisationUnitForms ? data.multiOrganisationUnitForms : false;
      dhis2.de.multiOrganisationUnitEnabled = isMultiOrgUnit;
      selection.setIncludeChildren(isMultiOrgUnit);
    }
  });
};

dhis2.de.ajaxLogin = function()
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
};

dhis2.de.loadMetaData = function()
{
    var def = $.Deferred();
	
    $.ajax( {
    	url: 'getMetaData.action',
    	dataType: 'json',
    	success: function( json )
	    {
	        sessionStorage[dhis2.de.cst.metaData] = JSON.stringify( json.metaData );
	    },
	    complete: function()
	    {
	        var metaData = JSON.parse( sessionStorage[dhis2.de.cst.metaData] );
	        dhis2.de.emptyOrganisationUnits = metaData.emptyOrganisationUnits;
	        dhis2.de.significantZeros = metaData.significantZeros;
	        dhis2.de.dataElements = metaData.dataElements;
	        dhis2.de.indicatorFormulas = metaData.indicatorFormulas;
	        dhis2.de.dataSets = metaData.dataSets;
	        dhis2.de.optionSets = metaData.optionSets;
	        dhis2.de.defaultCategoryCombo = metaData.defaultCategoryCombo;
	        dhis2.de.categoryCombos = metaData.categoryCombos;
	        dhis2.de.categories = metaData.categories;
	        dhis2.de.lockExceptions = metaData.lockExceptions;
	        def.resolve();
	    }
	} );
    
    return def.promise();
};

dhis2.de.loadDataSetAssociations = function()
{
	var def = $.Deferred();
	
	$.ajax( {
    	url: 'getDataSetAssociations.action',
    	dataType: 'json',
    	success: function( json )
	    {
	        sessionStorage[dhis2.de.cst.dataSetAssociations] = JSON.stringify( json.dataSetAssociations );
	    },
	    complete: function()
	    {
	        var metaData = JSON.parse( sessionStorage[dhis2.de.cst.dataSetAssociations] );
	        dhis2.de.dataSetAssociationSets = metaData.dataSetAssociationSets;
	        dhis2.de.organisationUnitAssociationSetMap = metaData.organisationUnitAssociationSetMap;	        
	        def.resolve();
	    }
	} );
	
	return def.promise();
};

dhis2.de.setMetaDataLoaded = function()
{
    dhis2.de.metaDataIsLoaded = true;
    $( '#loaderSpan' ).hide();
    console.log( 'Meta-data loaded' );

    dhis2.de.manageOfflineData();
    updateForms();
};

dhis2.de.discardLocalData = function() {
    if( confirm( i18n_remove_local_data ) ) {
        dhis2.de.storageManager.clearAllDataValues();
        hideHeaderMessage();
    }
};

dhis2.de.uploadLocalData = function()
{
    if ( !dhis2.de.storageManager.hasLocalData() )
    {
        return;
    }

    var dataValues = dhis2.de.storageManager.getAllDataValues();
    var completeDataSets = dhis2.de.storageManager.getCompleteDataSets();

    setHeaderWaitMessage( i18n_uploading_data_notification );

    var dataValuesArray = dataValues ? Object.keys( dataValues ) : [];
    var completeDataSetsArray = completeDataSets ? Object.keys( completeDataSets ) : [];

    function pushCompleteDataSets( array )
    {
        if ( array.length < 1 )
        {
            return;
        }

        var key = array[0];
        var value = completeDataSets[key];

        console.log( 'Uploaded complete data set: ' + key + ', with value: ' + value );

        $.ajax( {
            url: '../api/26/completeDataSetRegistrations',
            data: value,
            dataType: 'json',
            success: function( data, textStatus, jqXHR )
            {
            	dhis2.de.storageManager.clearCompleteDataSet( value );
                console.log( 'Successfully saved complete dataset with value: ' + value );
                ( array = array.slice( 1 ) ).length && pushCompleteDataSets( array );

                if ( array.length < 1 )
                {
                    setHeaderDelayMessage( i18n_sync_success );
                }
            },
            error: function( jqXHR, textStatus, errorThrown )
            {
            	if ( 409 === xhr.status || 500 === xhr.status ) // Invalid value or locked
            	{
            		// Ignore value for now TODO needs better handling for locking
            		
            		dhis2.de.storageManager.clearCompleteDataSet( value );
            	}
            	else // Connection lost during upload
        		{
                    var message = i18n_sync_failed
                        + ' <button id="sync_button" type="button">' + i18n_sync_now + '</button>'
                        + ' <button id="discard_button" type="button">' + i18n_discard + '</button>';

                    setHeaderMessage( message );

                    $( '#sync_button' ).bind( 'click', dhis2.de.uploadLocalData );
                    $( '#discard_button' ).bind( 'click', dhis2.de.discardLocalData );
        		}
            }
        } );
    }

    ( function pushDataValues( array )
    {
        if ( array.length < 1 )
        {
            setHeaderDelayMessage( i18n_online_notification );

            pushCompleteDataSets( completeDataSetsArray );

            return;
        }

        var key = array[0];
        var value = dataValues[key];

        if ( value !== undefined && value.value !== undefined && value.value.length > 254 )
        {
            value.value = value.value.slice(0, 254);
        }

        console.log( 'Uploading data value: ' + key + ', with value: ' + value );

        $.ajax( {
            url: '../api/dataValues',
            data: value,
            dataType: 'text',
            type: 'post',
            success: function( data, textStatus, xhr )
            {
            	dhis2.de.storageManager.clearDataValueJSON( value );
                console.log( 'Successfully saved data value with value: ' + value );
                ( array = array.slice( 1 ) ).length && pushDataValues( array );

                if ( array.length < 1 && completeDataSetsArray.length > 0 )
                {
                    pushCompleteDataSets( completeDataSetsArray );
                }
                else
                {
                    setHeaderDelayMessage( i18n_sync_success );
                }
            },
            error: function( xhr, textStatus, errorThrown )
            {
            	if ( 403 == xhr.status || 409 === xhr.status || 500 === xhr.status ) // Invalid value or locked
            	{
            		// Ignore value for now TODO needs better handling for locking
            		
            		dhis2.de.storageManager.clearDataValueJSON( value );
            	}
            	else // Connection lost during upload
            	{
	                var message = i18n_sync_failed
                    + ' <button id="sync_button" type="button">' + i18n_sync_now + '</button>'
                    + ' <button id="discard_button" type="button">' + i18n_discard + '</button>';

	                setHeaderMessage( message );

	                $( '#sync_button' ).bind( 'click', dhis2.de.uploadLocalData );
                  $( '#discard_button' ).bind( 'click', dhis2.de.discardLocalData );
            	}
            }
        } );
    } )( dataValuesArray );
};

dhis2.de.addEventListeners = function()
{
    $( '.entryfield, .entrytime' ).each( function( i )
    {
        var id = $( this ).attr( 'id' );
        var isTimeField = $( this ).hasClass('entrytime');

        // If entry field is a date picker, remove old target field, and change id
        if( /-dp$/.test( id ) )
        {
            var dpTargetId = id.substring( 0, id.length - 3 );
            $( '#' + dpTargetId ).remove();
            $( this ).attr( 'id', dpTargetId ).calendarsPicker( 'destroy' );
            id = dpTargetId;
        }

        var split = dhis2.de.splitFieldId( id );
        var dataElementId = split.dataElementId;
        var optionComboId = split.optionComboId;
        dhis2.de.currentOrganisationUnitId = split.organisationUnitId;

        var type = getDataElementType( dataElementId );

        $( this ).unbind( 'focus' );
        $( this ).unbind( 'blur' );
        $( this ).unbind( 'change' );
        $( this ).unbind( 'dblclick' );
        $( this ).unbind( 'keyup' );

        $( this ).focus( valueFocus );

        $( this ).blur( valueBlur );

        $( this ).change( function()
        {
            saveVal( dataElementId, optionComboId, id );
        } );

        $( this ).dblclick( function()
        {
            viewHist( dataElementId, optionComboId );
        } );

        $( this ).keyup( function( event )
        {
            keyPress( event, this );
        } );

        if ( ( type === 'DATE' || type === 'DATETIME' ) && !isTimeField )
        {
            // Fake event, needed for valueBlur / valueFocus when using date-picker
            var fakeEvent = {
                target: {
                    id: id + '-dp'
                }
            };

            dhis2.period.picker.createInstance( '#' + id, false, false, {
                onSelect: function() {
                    saveVal( dataElementId, optionComboId, id, fakeEvent.target.id );
                },
                onClose: function() {
                    valueBlur( fakeEvent );
                },
                onShow: function() {
                    valueFocus( fakeEvent );
                },
                minDate: null,
                maxDate: null
            } );
        }
    } );

    $( '.entryselect' ).each( function()
    {
        var id = $( this ).attr( 'id' );
        var split = dhis2.de.splitFieldId( id );

        var dataElementId = split.dataElementId;
        var optionComboId = split.optionComboId;
        var name = dataElementId + "-" + optionComboId + "-val";

        $( this ).unbind( 'click' );
        
        $( this ).click( function()
        {
            if ( $(this).hasClass( "checked" ) )
            {
                $( this ).removeClass( "checked" );
                $( this ).prop('checked', false );
            }
            else
            {
                $(  '[name='+ name +']' ).each( function()
                {
                    $( this ).removeClass( 'checked' );
                    $( this ).prop( 'checked', false );
                });

                $( this ).prop( 'checked', true );
                $( this ).addClass( 'checked' );
            }

            saveBoolean( dataElementId, optionComboId, id );
        } );
    } );

    $( '.entrytrueonly' ).each( function( i )
    {
        var id = $( this ).attr( 'id' );
        var split = dhis2.de.splitFieldId( id );

        var dataElementId = split.dataElementId;
        var optionComboId = split.optionComboId;

        $( this ).unbind( 'focus' );
        $( this ).unbind( 'change' );

        $( this ).focus( valueFocus );
        $( this ).blur( valueBlur );

        $( this ).change( function()
        {
            saveTrueOnly( dataElementId, optionComboId, id );
        } );
    } );

    $( '.commentlink' ).each( function( i )
    {
        var id = $( this ).attr( 'id' );
        var split = dhis2.de.splitFieldId( id );

        var dataElementId = split.dataElementId;
        var optionComboId = split.optionComboId;

        $( this ).unbind( 'click' );

        $( this ).attr( "src", "../images/comment.png" );
        $( this ).attr( "title", i18n_view_comment );

        $( this ).css( "cursor", "pointer" );

        $( this ).click( function()
        {
            viewHist( dataElementId, optionComboId );
        } );
    } );

    $( '.entryfileresource' ).each( function()
    {
        $( this ).fileEntryField();
    } );
}

dhis2.de.resetSectionFilters = function()
{
    $( '#filterDataSetSectionDiv' ).hide();
    $( '.formSection' ).show();
}

dhis2.de.clearSectionFilters = function()
{
    $( '#filterDataSetSection' ).children().remove();
    $( '#filterDataSetSectionDiv' ).hide();
    $( '.formSection' ).show();
}

dhis2.de.clearPeriod = function()
{
    clearListById( 'selectedPeriodId' );
    dhis2.de.clearEntryForm();
}

dhis2.de.clearEntryForm = function()
{
    $( '#contentDiv' ).html( '' );

    dhis2.de.currentPeriodOffset = 0;

    dhis2.de.dataEntryFormIsLoaded = false;

    $( '#completenessDiv' ).hide();
    $( '#infoDiv' ).hide();
	
	/* hide approval div */
	$( '#dHTApprovedDiv' ).hide();
	$( '#sHTApprovedDiv' ).hide();
	$( '#pMUAccountApprovedDiv' ).hide();
	/* hide approval div end */
	
}

dhis2.de.loadForm = function()
{
	var dataSetId = dhis2.de.currentDataSetId;
	
	dhis2.de.currentOrganisationUnitId = selection.getSelected()[0];

    if ( !dhis2.de.multiOrganisationUnit  )
    {
        dhis2.de.storageManager.formExists( dataSetId ).done( function( value ) 
        {           
	    	if ( value ) 
	    	{
	            console.log( 'Loading form locally: ' + dataSetId );
	
	            dhis2.de.storageManager.getForm( dataSetId ).done( function( html ) 
	            {
	                $( '#contentDiv' ).html( html );

	                if ( dhis2.de.dataSets[dataSetId].renderAsTabs )
	                {
	                    $( "#tabs" ).tabs({
							activate: function(){
								//populate section row/column totals
								dhis2.de.populateRowTotals();
								dhis2.de.populateColumnTotals();
							}
						});
	                }

	                dhis2.de.enableSectionFilter();	               
	                $( document ).trigger( dhis2.de.event.formLoaded, dhis2.de.currentDataSetId );

	                loadDataValues();
                    var table = $( '.sectionTable' );
                    table.floatThead({
                        position: 'absolute',
                        top: 44,
                        zIndex: 9
                    });



                  dhis2.de.insertOptionSets();
                  dhis2.de.enableDEDescriptionEvent();

	            } );
	        } 
	    	else {
                dhis2.de.storageManager.formExistsRemotely( dataSetId ).done( function( value ) {
                    console.log( 'Loading form remotely: ' + dataSetId );

       	            dhis2.de.storageManager.getForm( dataSetId ).done( function( html )
       	            {
       	                $( '#contentDiv' ).html( html );

       	                if ( dhis2.de.dataSets[dataSetId].renderAsTabs )
       	                {
       	                    $( "#tabs" ).tabs({
								activate: function(){
									//populate section row/column totals
									dhis2.de.populateRowTotals();
									dhis2.de.populateColumnTotals();
								}
							});
       	                }

       	                dhis2.de.enableSectionFilter();
       	                $( document ).trigger( dhis2.de.event.formLoaded, dhis2.de.currentDataSetId );

       	                loadDataValues();
       	                dhis2.de.insertOptionSets();
                        dhis2.de.enableDEDescriptionEvent();
       	            } );
                });
            }
        } );
    }
    else
    {
        console.log( 'Loading form remotely: ' + dataSetId );

        $( '#contentDiv' ).load( 'loadForm.action', 
        {
            dataSetId : dataSetId,
            multiOrganisationUnit: dhis2.de.multiOrganisationUnit ? dhis2.de.getCurrentOrganisationUnit() : ''
        }, 
        function() 
        {
            if ( !dhis2.de.multiOrganisationUnit )
            {
                if ( dhis2.de.dataSets[dataSetId].renderAsTabs ) 
                {
                    $( "#tabs" ).tabs({
						activate: function(){
							//populate section row/column totals
							dhis2.de.populateRowTotals();
							dhis2.de.populateColumnTotals();
						}
					});
                }

                dhis2.de.enableSectionFilter();
            }
            else
            {
                $( '#currentOrganisationUnit' ).html( i18n_no_organisationunit_selected );
            }

            dhis2.de.insertOptionSets();

            loadDataValues();
        } );
    }
}

//------------------------------------------------------------------------------
// Section filter
//------------------------------------------------------------------------------

dhis2.de.enableSectionFilter = function()
{
    var $sectionHeaders = $( '.formSection .cent h3' );
    dhis2.de.clearSectionFilters();

    if ( $sectionHeaders.size() > 1)
    {
        $( '#filterDataSetSection' ).append( "<option value='all'>" + i18n_show_all_sections + "</option>" );

        $sectionHeaders.each( function( idx, value ) 
        {
            $( '#filterDataSetSection' ).append( "<option value='" + idx + "'>" + value.innerHTML + "</option>" );
        } );

        $( '#filterDataSetSectionDiv' ).show();
    }
    else
    {
        $( '#filterDataSetSectionDiv' ).hide();
    }
}

dhis2.de.filterOnSection = function()
{
    var $filterDataSetSection = $( '#filterDataSetSection' );    
    var value = $filterDataSetSection.val();
    
    if ( value == 'all' )
    {
        $( '.formSection' ).show();
    }
    else
    {        
        $( '.formSection' ).hide();
        $( $( '.formSection' )[value] ).show();
    }
}

dhis2.de.filterInSection = function( $this )
{
    var $tbody = $this.closest('.sectionTable').find("tbody");    
    var thisTable = $tbody.parent().get(0);           
    var $trTarget = $tbody.find( 'tr');

    if ( $this.val() == '' )
    {
        $trTarget.show();
    }
    else
    {
        var $trTargetChildren = $trTarget.find( 'td:first-child' );

        $trTargetChildren.each( function( idx, item ) 
        {
            var text1 = $this.val().toUpperCase();
            var text2 = $( item ).find( 'span' ).html();
            
            if( text2 && text2 != "")
            {
                text2 = text2.toUpperCase();

                if ( text2.indexOf( text1 ) >= 0 )
                {
                    $( item ).parent().show();
                }
                else
                {
                    $( item ).parent().hide();
                }
            }
            
        } );
    }

    refreshZebraStripes( $tbody );
    $.each($( '.sectionTable' ), function(index, table){
        if(table == thisTable) return;
        $(table).trigger( 'reflow' );
    });
    
    dhis2.de.populateColumnTotals();
}

//------------------------------------------------------------------------------
// Supportive methods
//------------------------------------------------------------------------------

/**
 * Splits an id based on the multi org unit variable.
 */
dhis2.de.splitFieldId = function( id )
{
    var split = {};

    if ( dhis2.de.multiOrganisationUnit )
    {
        split.organisationUnitId = id.split( '-' )[0];
        split.dataElementId = id.split( '-' )[1];
        split.optionComboId = id.split( '-' )[2];
    }
    else
    {
        split.organisationUnitId = dhis2.de.getCurrentOrganisationUnit();
        split.dataElementId = id.split( '-' )[0];
        split.optionComboId = id.split( '-' )[1];
    }

    return split;
}

function refreshZebraStripes( $tbody )
{
    $tbody.find( 'tr:not([colspan]):visible:even' ).find( 'td:first-child' ).removeClass( 'reg alt' ).addClass( 'alt' );
    $tbody.find( 'tr:not([colspan]):visible:odd' ).find( 'td:first-child' ).removeClass( 'reg alt' ).addClass( 'reg' );
}

function getDataElementType( dataElementId )
{
	if ( dhis2.de.dataElements[dataElementId] != null )
	{
		return dhis2.de.dataElements[dataElementId];
	}

	console.log( 'Data element not present in data set, falling back to default type: ' + dataElementId );
	return dhis2.de.cst.defaultType;
}

function getDataElementName( dataElementId )
{
	var span = $( '#' + dataElementId + '-dataelement' );

	if ( span != null )
	{
		return span.text();
	}

    console.log( 'Data element not present in form, falling back to default name: ' + dataElementId );
	return dhis2.de.cst.defaultName;
}

function getOptionComboName( optionComboId )
{
	var span = $( '#' + optionComboId + '-optioncombo' );

	if ( span != null )
	{
		return span.text();
	}

    console.log( 'Category option combo not present in form, falling back to default name: ' + optionComboId );
	return dhis2.de.cst.defaultName;
}

function arrayChunk( array, size )
{
    if ( !array || !array.length )
    {
        return [];
    }

    if ( !size || size < 1 )
    {
        return array;
    }

    var groups = [];
    var chunks = array.length / size;
    for ( var i = 0, j = 0; i < chunks; i++, j += size )
    {
        groups[i] = array.slice(j, j + size);
    }

    return groups;
}
// ----------------------------------------------------------------------------
// OrganisationUnit Selection
// -----------------------------------------------------------------------------

/**
 * Callback for changes in organisation unit selections.
 */
function organisationUnitSelected( orgUnits, orgUnitNames, children )
{
	if ( dhis2.de.metaDataIsLoaded == false )
	{
	    return false;
	}

  if( dhis2.de.shouldFetchDataSets(orgUnits) ) {
    dhis2.de.fetchDataSets( orgUnits[0] ).always(function() {
        setDisplayNamePreferences();
        selection.responseReceived();
    });

    return false;
  }

	dhis2.de.currentOrganisationUnitId = orgUnits[0];
    var organisationUnitName = orgUnitNames[0];

    $( '#selectedOrganisationUnit' ).val( organisationUnitName );
    $( '#currentOrganisationUnit' ).html( organisationUnitName );

    dhis2.de.getOrFetchDataSetList().then(function(data) {
        var dataSetList = data;

        $( '#selectedDataSetId' ).removeAttr( 'disabled' );

        var dataSetId = $( '#selectedDataSetId' ).val();
        var periodId = $( '#selectedPeriodId').val();

        clearListById( 'selectedDataSetId' );
        addOptionById( 'selectedDataSetId', '-1', '[ ' + i18n_select_data_set + ' ]' );

        var dataSetValid = false;
        var multiDataSetValid = false;

        $.safeEach( dataSetList, function( idx, item )
        {
        	if ( item )
        	{
	            addOptionById( 'selectedDataSetId', item.id, item.name );
	
	            if ( dataSetId == item.id )
	            {
	                dataSetValid = true;
	            }
        	}
        } );

        if ( children )
        {
            var childrenDataSets = getSortedDataSetListForOrgUnits( children );

            if ( childrenDataSets && childrenDataSets.length > 0 )
            {
                $( '#selectedDataSetId' ).append( '<optgroup label="' + i18n_childrens_forms + '">' );

                $.safeEach( childrenDataSets, function( idx, item )
                {
                    if ( dataSetId == item.id && dhis2.de.multiOrganisationUnit )
                    {
                        multiDataSetValid = true;
                    }

                    $( '<option />' ).attr( 'data-multiorg', true ).attr( 'value', item.id).html( item.name ).appendTo( '#selectedDataSetId' );
                } );

                $( '#selectDataSetId' ).append( '</optgroup>' );
            }
        }

        if ( !dhis2.de.multiOrganisationUnit && dataSetValid && dataSetId ) {
            $( '#selectedDataSetId' ).val( dataSetId ); // Restore selected data set

            if ( dhis2.de.inputSelected() && dhis2.de.dataEntryFormIsLoaded ) {
                dhis2.de.resetSectionFilters();
                showLoader();
                loadDataValues();
            }
        }
        else if ( dhis2.de.multiOrganisationUnit && multiDataSetValid && dataSetId ) {
            $( '#selectedDataSetId' ).val( dataSetId ); // Restore selected data set
            dataSetSelected();
        }
        else {
        	dhis2.de.multiOrganisationUnit = false;
            dhis2.de.currentDataSetId = null;

            dhis2.de.clearSectionFilters();
            dhis2.de.clearPeriod();
            dhis2.de.clearAttributes();
        }
        
        var dsl = document.getElementById( 'selectedDataSetId' );
        
        if ( dsl && dsl.options && dsl.options.length == 2 )
        {
            $( '#selectedDataSetId' ).val( dsl.options[1].value );
            dataSetSelected();
        }
        
    });

}

/**
 * Fetch data-sets for a orgUnit + data-sets for its children.
 *
 * @param {String} ou Organisation Unit ID to fetch data-sets for
 * @returns {$.Deferred}
 */
dhis2.de.fetchDataSets = function( ou )
{
    var def = $.Deferred();
    var fieldsParam = encodeURIComponent('id,dataSets[id],children[id,dataSets[id]]');

    $.ajax({
        type: 'GET',
        url: '../api/organisationUnits/' + ou + '?fields=' + fieldsParam
    }).done(function(data) {
        dhis2.de._updateDataSets(data);

        data.children.forEach(function( item ) {
            dhis2.de._updateDataSets(item);
        });

        dhis2.de.fetchedDataSets[ou] = true;
        def.resolve(data);
    });

    return def.promise();
};

/**
 * Internal method that will go through all data-sets on the object and add them to
 * {@see dhis2.de.dataSetAssociationSets} and {@see dhis2.de.organisationUnitAssociationSetMap}.
 *
 * @param {Object} ou Object that matches the format id,dataSets[id].
 * @private
 */
dhis2.de._updateDataSets = function( ou ) {
    var dataSets = [];

    ou.dataSets.forEach(function( item ) {
        dataSets.push(item.id);
    });

    dhis2.de.dataSetAssociationSets[Object.keys(dhis2.de.dataSetAssociationSets).length] = dataSets;
    dhis2.de.organisationUnitAssociationSetMap[ou.id] = Object.keys(dhis2.de.dataSetAssociationSets).length - 1;
};

/**
 * Get a list of sorted data-sets for a orgUnit, if data-set list is empty, it will 
 * try and fetch data-sets from the server.
 *
 * @param {String} [ou] Organisation unit to fetch data-sets for
 * @returns {$.Deferred}
 */
dhis2.de.getOrFetchDataSetList = function( ou ) {
    var def = $.Deferred();

    var dataSets = getSortedDataSetList(ou);
    ou = ou || dhis2.de.getCurrentOrganisationUnit();

    if (dataSets.length > 0) {
        def.resolve(dataSets);
    } 
    else {
        dhis2.de.fetchDataSets(ou).then(function() {
            def.resolve(getSortedDataSetList(ou));
        });
    }

    /* TODO check if data sets are accessible for current user */
    
    return def.promise();
};

/**
 * Returns an array containing associative array elements with id and name
 * properties. The array is sorted on the element name property.
 */
function getSortedDataSetList( orgUnit )
{
    var associationSet = orgUnit !== undefined ? dhis2.de.organisationUnitAssociationSetMap[orgUnit] : dhis2.de.organisationUnitAssociationSetMap[dhis2.de.getCurrentOrganisationUnit()];
    var orgUnitDataSets = dhis2.de.dataSetAssociationSets[associationSet];

    var dataSetList = [];

    $.safeEach( orgUnitDataSets, function( idx, item ) 
    {
        var dataSetId = orgUnitDataSets[idx];
        
        if ( dhis2.de.dataSets[dataSetId] )
        {
	        var dataSetName = dhis2.de.dataSets[dataSetId].name;
	
	        var row = [];
	        row['id'] = dataSetId;
	        row['name'] = dataSetName;
	        dataSetList[idx] = row;
        }
    } );

    dataSetList.sort( function( a, b )
    {
        return a.name > b.name ? 1 : a.name < b.name ? -1 : 0;
    } );

    return dataSetList;
}

/**
 * Gets list of data sets for selected organisation units.
 */
function getSortedDataSetListForOrgUnits( orgUnits )
{
    var dataSetList = [];

    $.safeEach( orgUnits, function( idx, item )
    {
        dataSetList.push.apply( dataSetList, getSortedDataSetList(item) )
    } );

    var filteredDataSetList = [];

    $.safeEach( dataSetList, function( idx, item ) 
    {
        var formType = dhis2.de.dataSets[item.id].type;
        var found = false;

        $.safeEach( filteredDataSetList, function( i, el ) 
        {
            if( item.name == el.name )
            {
                found = true;
            }
        } );

        if ( !found && ( formType == dhis2.de.cst.formTypeSection || formType == dhis2.de.cst.formTypeDefault ) )
        {
            filteredDataSetList.push(item);
        }
    } );

    return filteredDataSetList;
}

// -----------------------------------------------------------------------------
// DataSet Selection
// -----------------------------------------------------------------------------

/**
 * Callback for changes in data set list. For previous selection to be valid and
 * the period selection to remain, the period type of the previous data set must
 * equal the current data set, and the allow future periods property of the previous
 * data set must equal the current data set or the current period offset must not
 * be in the future.
 */
function dataSetSelected()
{
    var previousDataSetValid = ( dhis2.de.currentDataSetId && dhis2.de.currentDataSetId != -1 );
    var previousDataSet = !!previousDataSetValid ? dhis2.de.dataSets[dhis2.de.currentDataSetId] : undefined;
    var previousPeriodType = previousDataSet ? previousDataSet.periodType : undefined;
    var previousOpenFuturePeriods = previousDataSet ? previousDataSet.openFuturePeriods : 0;

    dhis2.de.currentDataSetId = $( '#selectedDataSetId' ).val();
    
    var serverTimeDelta = dhis2.de.storageManager.getServerTimeDelta() || 0;
    dhis2.de.blackListedPeriods = dhis2.de.dataSets[dhis2.de.currentDataSetId].dataInputPeriods
        .filter(function(dip) { return ( dip.openingDate != "" && new Date( dip.openingDate ) > (Date.now() + serverTimeDelta) ) || ( dip.closingDate != "" && new Date( dip.closingDate ) < (Date.now() + serverTimeDelta) ); })
        .map(function(dip) { return dip.period.isoPeriod; });
    
    if ( dhis2.de.currentDataSetId && dhis2.de.currentDataSetId !== -1 )
    {
        $( '#selectedPeriodId' ).removeAttr( 'disabled' );
        $( '#prevButton' ).removeAttr( 'disabled' );
        $( '#nextButton' ).removeAttr( 'disabled' );

        var periodType = dhis2.de.dataSets[dhis2.de.currentDataSetId].periodType;
        var openFuturePeriods = dhis2.de.dataSets[dhis2.de.currentDataSetId].openFuturePeriods;

        var previousSelectionValid = !!( periodType == previousPeriodType && openFuturePeriods == previousOpenFuturePeriods );
        
        dhis2.de.currentCategories = dhis2.de.getCategories( dhis2.de.currentDataSetId );

        dhis2.de.setAttributesMarkup();   

        dhis2.de.multiOrganisationUnit = !!$( '#selectedDataSetId :selected' ).data( 'multiorg' );

        if ( dhis2.de.inputSelected() && previousSelectionValid )
        {
            showLoader();
            dhis2.de.loadForm();
        }
        else
        {
        	dhis2.de.currentPeriodOffset = 0;
        	displayPeriods();
        	dhis2.de.clearSectionFilters();
            dhis2.de.clearEntryForm();
        }
    }
    else
    {
        $( '#selectedPeriodId').val( "" );
        $( '#selectedPeriodId' ).attr( 'disabled', 'disabled' );
        $( '#prevButton' ).attr( 'disabled', 'disabled' );
        $( '#nextButton' ).attr( 'disabled', 'disabled' );

        dhis2.de.clearEntryForm();
        dhis2.de.clearAttributes();
    }
}

// -----------------------------------------------------------------------------
// Period Selection
// -----------------------------------------------------------------------------

/**
 * Callback for changes in period select list.
 */
function periodSelected()
{
	/* add for show approval div based on dataset selection */
	//alert( $( '#selectedDataSetId' ).val());
	
	if( $( '#selectedDataSetId' ).val() == 'wwcxotLHZGY' || $( '#selectedDataSetId' ).val() == 'XV12eKZar28')
	{
		//alert("hshsh");
		
		$("#validateButton").hide();
		//$("#undoButton").hide();
		
		//$( '#approvalButtonDiv' ).show();
	    //$( '#infoDiv' ).hide();
	}
	else
	{
		$( '#validateButton' ).show();
		//$( '#undoButton' ).show();
		//$( '#approvalButtonDiv' ).hide();
		//$( '#approvalButtonDiv' ).hide();
	}
	
	
	/* add for show approval div based on dataset selection end*/
    var periodName = $( '#selectedPeriodId :selected' ).text();

    $( '#currentPeriod' ).html( periodName );
        
    dhis2.de.setAttributesMarkup();
    
    if ( dhis2.de.inputSelected() )
    {    	
        showLoader();

        if ( dhis2.de.dataEntryFormIsLoaded )
        {
            loadDataValues();
        }
        else
        {
            dhis2.de.loadForm();
        }
    }
    else
    {
        dhis2.de.clearEntryForm();
    }
}

/**
 * Handles the onClick event for the next period button.
 */
function nextPeriodsSelected()
{
	var openFuturePeriods = !!( dhis2.de.currentDataSetId && dhis2.de.dataSets[dhis2.de.currentDataSetId].openFuturePeriods );
	
    if ( dhis2.de.currentPeriodOffset < 0 || openFuturePeriods )
    {
    	dhis2.de.currentPeriodOffset++;
        displayPeriods();
    }
}

/**
 * Handles the onClick event for the previous period button.
 */
function previousPeriodsSelected()
{
	dhis2.de.currentPeriodOffset--;
    displayPeriods();
}

/**
 * Generates the period select list options.
 */
function displayPeriods()
{
    var dataSetId = $( '#selectedDataSetId' ).val();
    var periodType = dhis2.de.dataSets[dataSetId].periodType;
    var openFuturePeriods = dhis2.de.dataSets[dataSetId].openFuturePeriods;
    var dsStartDate = dhis2.de.dataSets[dataSetId].startDate;
    var dsEndDate = dhis2.de.dataSets[dataSetId].endDate;
    var periods = dhis2.period.generator.generateReversedPeriods( periodType, dhis2.de.currentPeriodOffset );

    periods = dhis2.period.generator.filterOpenPeriods( periodType, periods, openFuturePeriods, dsStartDate, dsEndDate );

    clearListById( 'selectedPeriodId' );

    if ( periods.length > 0 )
    {
    	addOptionById( 'selectedPeriodId', "", '[ ' + i18n_select_period + ' ]' );
    }
    else
    {
    	addOptionById( 'selectedPeriodId', "", i18n_no_periods_click_prev_year_button );
    }

    dhis2.de.periodChoices = [];

    $.safeEach( periods, function( idx, item )
    {
        addOptionById( 'selectedPeriodId', item.iso, item.name );
        dhis2.de.periodChoices[ item.iso ] = item;
    } );
}

//------------------------------------------------------------------------------
// Attributes / Categories Selection
//------------------------------------------------------------------------------

/**
* Returns an array of category objects for the given data set identifier. Categories
* are looked up using the category combo of the data set. Null is returned if
* the given data set has the default category combo.
*/
dhis2.de.getCategories = function( dataSetId )
{
	var dataSet = dhis2.de.dataSets[dataSetId];
	
	if ( !dataSet || !dataSet.categoryCombo || dhis2.de.defaultCategoryCombo === dataSet.categoryCombo ) {
		return null;
	}

	var categoryCombo = dhis2.de.categoryCombos[dataSet.categoryCombo];
	
	var categories = [];
	
	$.safeEach( categoryCombo.categories, function( idx, cat ) {
		var category = dhis2.de.categories[cat];
		categories.push( category );
	} );
	
	return categories;
};

/**
 * Indicates whether all present categories have been selected. True is returned
 * if no categories are present. False is returned if less selections have been
 * made thant here are categories present.
 */
dhis2.de.categoriesSelected = function()
{
	if ( !dhis2.de.currentCategories || dhis2.de.currentCategories.length == 0 ) {
		return true; // No categories present which can be selected
	}
	
	var options = dhis2.de.getCurrentCategoryOptions();
	
	if ( !options || options.length < dhis2.de.currentCategories.length ) {
		return false; // Less selected options than categories present
	}
	
	return true;
};

/**
* Returns attribute category combo identifier. Based on the dhis2.de.currentDataSetId 
* global variable. Returns null if there is no current data set or if current 
* data set has the default category combo.
*/
dhis2.de.getCurrentCategoryCombo = function()
{
	var dataSet = dhis2.de.dataSets[dhis2.de.currentDataSetId];
	
	if ( !dataSet || !dataSet.categoryCombo || dhis2.de.defaultCategoryCombo === dataSet.categoryCombo ) {
		return null;
	}
	
	return dataSet.categoryCombo;
};

/**
* Returns an array of the currently selected attribute category option identifiers. 
* Based on the dhis2.de.currentCategories global variable. Returns null if there 
* are no current categories.
*/
dhis2.de.getCurrentCategoryOptions = function()
{
	if ( !dhis2.de.currentCategories || dhis2.de.currentCategories.length == 0 ) {
		return null;
	}
	
	var options = [];
	
	$.safeEach( dhis2.de.currentCategories, function( idx, category ) {
		var option = $( '#category-' + category.id ).val();
		
		if ( option && option != -1 ) {
			options.push( option );
		}
	} );
	
	return options;
};

/**
 * Returns an object for the currently selected attribute category options
 * with properties for the identifiers of each category and matching values
 * for the identifier of the selected category option. Returns an empty
 * object if there are no current categories.
 */
dhis2.de.getCurrentCategorySelections = function()
{
	var selections = {};
	
	if ( !dhis2.de.currentCategories || dhis2.de.currentCategories.length == 0 ) {
		return selections;
	}
		
	$.safeEach( dhis2.de.currentCategories, function( idx, category ) {
		var option = $( '#category-' + category.id ).val();
		
		if ( option && option != -1 ) {
			selections[category.id] = option;
		}
	} );
	
	return selections;
}

/**
 * Returns a query param value for the currently selected category options where
 * each option is separated by the ; character.
 */
dhis2.de.getCurrentCategoryOptionsQueryValue = function()
{
	if ( !dhis2.de.getCurrentCategoryOptions() ) {
		return null;
	}
	
	var value = '';
	
	$.safeEach( dhis2.de.getCurrentCategoryOptions(), function( idx, option ) {
		value += option + ';';
	} );
	
	if ( value ) {
		value = value.slice( 0, -1 );
	}
	
	return value;
}

/**
 * Tests to see if a category option is valid during a period.
 */
dhis2.de.optionValidWithinPeriod = function( option, period )
{
    var optionStartDate, optionEndDate;

    if ( option.start ) {
        optionStartDate = dhis2.period.calendar.parseDate( dhis2.period.format, option.start );
    }

    if ( option.end ) {
        optionEndDate = dhis2.period.calendar.parseDate( dhis2.period.format, option.end );
        var ds = dhis2.de.dataSets[dhis2.de.currentDataSetId];
        if ( ds.openPeriodsAfterCoEndDate ) {
            optionEndDate = dhis2.period.generator.datePlusPeriods( ds.periodType, optionEndDate, parseInt( ds.openPeriodsAfterCoEndDate ) );
        }
    }

    var periodStartDate = dhis2.period.calendar.parseDate( dhis2.period.format, dhis2.de.periodChoices[ period ].startDate );
    var periodEndDate = dhis2.period.calendar.parseDate( dhis2.period.format, dhis2.de.periodChoices[ period ].endDate );

    return ( !optionStartDate || optionStartDate <= periodEndDate )
        && ( !optionEndDate || optionEndDate >= periodStartDate )
}

/**
 * Tests to see if attribute category option is valid for the selected org unit.
 */
dhis2.de.optionValidForSelectedOrgUnit = function( option )
{
    var isValid = true;

    if (option.ous && option.ous.length) {
        isValid = false;
        var path = organisationUnits[dhis2.de.getCurrentOrganisationUnit()].path;
        $.safeEach(option.ous, function (idx, uid) {
            if (path.indexOf(uid) >= 0) {
                isValid = true;
                return false;
            }
        });
    }

    return isValid;
}

/**
 * Sets the markup for attribute selections.
 */
dhis2.de.setAttributesMarkup = function()
{
    var attributeMarkup = dhis2.de.getAttributesMarkup();
    $( '#attributeComboDiv' ).html( attributeMarkup );
}

/**
* Returns markup for drop down boxes to be put in the selection box for the
* given categories. The empty string is returned if no categories are given.
*
* TODO check for category option validity for selected organisation unit.
*/
dhis2.de.getAttributesMarkup = function()
{
	var html = '';

    var period = $( '#selectedPeriodId' ).val();

    var options = dhis2.de.getCurrentCategoryOptions();

	if ( !dhis2.de.currentCategories || dhis2.de.currentCategories.length == 0 || !period ) {
		return html;
	}
	
	$.safeEach( dhis2.de.currentCategories, function( idx, category ) {
		html += '<div class="selectionBoxRow">';
		html += '<div class="selectionLabel">' + category.name + '</div>&nbsp;';
		html += '<select id="category-' + category.id + '" class="selectionBoxSelect" onchange="dhis2.de.attributeSelected(\'' + category.id + '\')">';
		html += '<option value="-1">[ ' + i18n_select_option + ' ]</option>';

		$.safeEach( category.options, function( idx, option ) {
			if ( dhis2.de.optionValidWithinPeriod( option, period ) && dhis2.de.optionValidForSelectedOrgUnit( option ) ) {				
                                var selected = ( $.inArray( option.id, options ) != -1 ) || category.options.length == 1 ? " selected" : "";
				html += '<option value="' + option.id + '"' + selected + '>' + option.name + '</option>';
			}
		} );
		
		html += '</select>';
		html += '</div>';
	} );

	return html;
};

/**
 * Clears the markup for attribute select lists.
 */
dhis2.de.clearAttributes = function()
{
	$( '#attributeComboDiv' ).html( '' );
};

/**
 * Callback for changes in attribute select lists.
 */
dhis2.de.attributeSelected = function( categoryId )
{
	if ( dhis2.de.inputSelected() ) {    	
        showLoader();

        if ( dhis2.de.dataEntryFormIsLoaded ) {
            loadDataValues();
        }
        else {
            dhis2.de.loadForm();
        }
    }
    else
    {
        dhis2.de.clearEntryForm();
    }
};

// -----------------------------------------------------------------------------
// Form
// -----------------------------------------------------------------------------

/**
 * Indicates whether all required inpout selections have been made.
 */
dhis2.de.inputSelected = function()
{
    var dataSetId = $( '#selectedDataSetId' ).val();
    var periodId = $( '#selectedPeriodId').val();

	if (
	    dhis2.de.currentOrganisationUnitId &&
	    dataSetId && dataSetId != -1 &&
	    periodId && periodId != "" &&
	    dhis2.de.categoriesSelected() ) {
		return true;
	}

	return false;
};

function loadDataValues()
{
    $( '#completeButton' ).removeAttr( 'disabled' );
    $( '#undoButton' ).attr( 'disabled', 'disabled' );
    $( '#infoDiv' ).css( 'display', 'none' );

    dhis2.de.currentOrganisationUnitId = selection.getSelected()[0];

    getAndInsertDataValues();
    displayEntryFormCompleted();
}

function clearFileEntryFields() {
    var $fields = $( '.entryfileresource' );
    $fields.find( '.upload-fileinfo-name' ).text( '' );
    $fields.find( '.upload-fileinfo-size' ).text( '' );

    $fields.find( '.upload-field' ).css( 'background-color', dhis2.de.cst.colorWhite );
    $fields.find( 'input' ).val( '' );
    
    $('.select2-container').select2("val", "");
}

function getAndInsertDataValues()
{
    var periodId = $( '#selectedPeriodId').val();
    var dataSetId = $( '#selectedDataSetId' ).val();

    // Clear existing values and colors, grey disabled fields

    $( '.entryfield' ).val( '' );
    $( '.entrytime' ).val( '' );
    $( '.entryselect' ).each( function()
    {
        $( this ).removeClass( 'checked' );
        $( this ).prop( 'checked', false );
        
    } );
    $( '.entrytrueonly' ).prop( 'checked', false );
    $( '.entrytrueonly' ).prop( 'onclick', null );
    $( '.entrytrueonly' ).prop( 'onkeydown', null );

    $( '.entryfield' ).css( 'background-color', dhis2.de.cst.colorWhite ).css( 'border', '1px solid ' + dhis2.de.cst.colorBorder );
    $( '.entryselect' ).css( 'background-color', dhis2.de.cst.colorWhite ).css( 'border', '1px solid ' + dhis2.de.cst.colorBorder );
    $( '.indicator' ).css( 'background-color', dhis2.de.cst.colorLightGrey  ).css( 'border', '1px solid ' + dhis2.de.cst.colorBorder );
    $( '.entrytrueonly' ).css( 'background-color', dhis2.de.cst.colorWhite );    

    clearFileEntryFields();


    $( '[name="min"]' ).html( '' );
    $( '[name="max"]' ).html( '' );

    $( '.entryfield' ).filter( ':disabled' ).css( 'background-color', dhis2.de.cst.colorGrey );

    var params = {
		periodId : periodId,
        dataSetId : dataSetId,
        organisationUnitId : dhis2.de.getCurrentOrganisationUnit(),
        multiOrganisationUnit: dhis2.de.multiOrganisationUnit
    };

    var cc = dhis2.de.getCurrentCategoryCombo();
    var cp = dhis2.de.getCurrentCategoryOptionsQueryValue();
    
    if ( cc && cp )
    {
    	params.cc = cc;
    	params.cp = cp;
    }
    
    $.ajax( {
    	url: 'getDataValues.action',
    	data: params,
	    dataType: 'json',
	    error: function() // offline
	    {
	    	$( '#completenessDiv' ).show();
	    	$( '#infoDiv' ).hide();
	    	
	    	var json = getOfflineDataValueJson( params );

	    	insertDataValues( json );
	    },
	    success: function( json ) // online
	    {
	    	insertDataValues( json );
        },
        complete: function()
        {
            $( '.indicator' ).attr( 'readonly', 'readonly' );
            $( '.dataelementtotal' ).attr( 'readonly', 'readonly' );
            $( document ).trigger( dhis2.de.event.dataValuesLoaded, dhis2.de.currentDataSetId );
                     
            //populate section row/column totals
            dhis2.de.populateRowTotals();
            dhis2.de.populateColumnTotals();
        }
	} );
}

function getOfflineDataValueJson( params )
{
	var dataValues = dhis2.de.storageManager.getDataValuesInForm( params );
	var complete = dhis2.de.storageManager.hasCompleteDataSet( params );
	
	var json = {};
	json.dataValues = new Array();
	json.locked = 'OPEN';
	json.complete = complete;
	json.date = "";
	json.storedBy = "";
    json.lastUpdatedBy = "";
		
	for ( var i = 0; i < dataValues.length; i++ )
	{
		var dataValue = dataValues[i];
		
		json.dataValues.push( { 
			'id': dataValue.de + '-' + dataValue.co,
			'val': dataValue.value
		} );
	}
	
	return json;
}

function insertDataValues( json )
{
    var dataValueMap = []; // Reset
    dhis2.de.currentMinMaxValueMap = []; // Reset
    
    var period = dhis2.de.getSelectedPeriod();
    
    var dataSet = dhis2.de.dataSets[dhis2.de.currentDataSetId];
    
    var periodLocked = false;
    
    if ( dataSet && dataSet.expiryDays > 0 )
    {
        var serverTimeDelta = dhis2.de.storageManager.getServerTimeDelta() || 0;
        var maxDate = moment( period.endDate, dhis2.period.format.toUpperCase() ).add( parseInt(dataSet.expiryDays), 'day' );
        periodLocked = moment().add( serverTimeDelta, 'ms' ).isAfter( maxDate );
    }

    var lockExceptionId = dhis2.de.currentOrganisationUnitId + "-" + dhis2.de.currentDataSetId + "-" + period.iso;

    periodLocked = periodLocked && dhis2.de.lockExceptions.indexOf( lockExceptionId ) == -1;

    if ( json.locked !== 'OPEN' || dhis2.de.blackListedPeriods.indexOf( period.iso ) > -1 || periodLocked )
	{
		dhis2.de.lockForm();

		if ( periodLocked ) {
			setHeaderDelayMessage( i18n_dataset_is_concluded );
		} else if ( dhis2.de.blackListedPeriods.indexOf( period.iso ) > -1 ) {
			setHeaderDelayMessage( i18n_dataset_is_closed );
		} else if ( json.locked === 'APPROVED' ) {
			setHeaderDelayMessage( i18n_dataset_is_approved );
		} else {
			setHeaderDelayMessage( i18n_dataset_is_locked );
		}

	}
	else
	{
        $( '#contentDiv input' ).removeAttr( 'readonly' );
        $( '#contentDiv textarea' ).removeAttr( 'readonly' );
		$( '#completenessDiv' ).show();
	}

    // Set the data-disabled attribute on any file upload fields
    $( '#contentDiv .entryfileresource' ).data( 'disabled', json.locked !== 'OPEN' );

    // Set data values, works for selects too as data value=select value    
    if ( !dhis2.de.multiOrganisationUnit  )
    {	
    	if ( period )
		{    
    		if ( dhis2.de.validateOrgUnitOpening( organisationUnits[dhis2.de.getCurrentOrganisationUnit()], period ) )
    		{
    			dhis2.de.lockForm();
            setHeaderDelayMessage( i18n_orgunit_is_closed );
    	        return;
    		}
    	}
    }
    
    else{
    	
    	var orgUnitClosed = false;
    	
    	$.each( organisationUnitList, function( idx, item )
        {    		
    		orgUnitClosed = dhis2.de.validateOrgUnitOpening( item, period ) ;
    		
    		if( orgUnitClosed )
    		{    			  	        
    			return false;
    		}            
        } );
    	
    	if ( orgUnitClosed )
		{
    		dhis2.de.lockForm();
	        setHeaderDelayMessage( i18n_orgunit_is_closed );
	        return;
		}

    }
    
    $.safeEach( json.dataValues, function( i, value )
    {
        var fieldId = '#' + value.id + '-val';
        var commentId = '#' + value.id + '-comment';
        if ( $( fieldId ).length > 0 ) // Set values
        {
            var entryField = $( fieldId );
            if ( 'true' == value.val && ( entryField.attr( 'name' ) == 'entrytrueonly' || entryField.hasClass( "entrytrueonly" ) ) )
            {
              $( fieldId ).prop( 'checked', true );
            }
            else if ( entryField.attr( 'name' ) == 'entryoptionset' || entryField.hasClass( "entryoptionset" ) )
            {
                dhis2.de.setOptionNameInField( fieldId, value );
            }
            else if ( entryField.hasClass( 'entryselect' ) )
            {                
                var fId = fieldId.substring(1, fieldId.length);
    
                if( value.val == 'true' )
                {
                  $('input[id=' + fId + ']:nth(0)').prop( 'checked', true );
                  $('input[id=' + fId + ']:nth(0)').addClass( 'checked' );
                }
                else if ( value.val == 'false')
                {
                  $('input[id=' + fId + ']:nth(1)').prop( 'checked', true );
                  $('input[id=' + fId + ']:nth(1)').addClass( 'checked' );
                }
                else{
                    $('input[id=' + fId + ']:nth(0)').prop( 'checked', false );
                    $('input[id=' + fId + ']:nth(1)').prop( 'checked', false );
                }
            }
            else if ( entryField.attr( 'class' ) == 'entryfileresource' )
            {
                var $field = $( fieldId );

                $field.find( 'input[class="entryfileresource-input"]' ).val( value.val );

                var split = dhis2.de.splitFieldId( value.id );

                var dvParams = {
                    'de': split.dataElementId,
                    'co': split.optionComboId,
                    'ou': split.organisationUnitId,
                    'pe': $( '#selectedPeriodId' ).val(),
                    'ds': $( '#selectedDataSetId' ).val()
                };

                var cc = dhis2.de.getCurrentCategoryCombo();
                var cp = dhis2.de.getCurrentCategoryOptionsQueryValue();

                if( cc && cp )
                {
                    dvParams.cc = cc;
                    dvParams.cp = cp;
                }

                var name = "", size = "";

                if ( value.fileMeta )
                {
                    name = value.fileMeta.name;
                    size = '(' + filesize( value.fileMeta.size ) + ')';
                }
                else
                {
                    name = i18n_loading_file_info_failed;
                }

                var $filename = $field.find( '.upload-fileinfo-name' );

                $( '<a>', {
                    text: name,
                    title: name,
                    target: '_blank',
                    href: "../api/dataValues/files?" + $.param( dvParams )
                } ).appendTo( $filename );

                $field.find( '.upload-fileinfo-size' ).text( size );
            }
            else if ( $( fieldId.replace('val', 'time') ).length > 0 )
            {
                $( fieldId ).val( value.val );
                $( fieldId.replace('val', 'time') ).val( value.val.split('T')[1] );
            }
            else 
            {                
                $( fieldId ).val( value.val );
            }
        }
        
        if ( 'true' == value.com ) // Set active comments
        {
            if ( $( commentId ).length > 0 )
            {
                $( commentId ).attr( 'src', '../images/comment_active.png' );
            }
            else if ( $( fieldId ).length > 0 )
            {
                $( fieldId ).css( 'border-color', dhis2.de.cst.colorBorderActive )
            }
        }
        
        dataValueMap[value.id] = value.val;

        dhis2.period.picker.updateDate(fieldId);
        
    } );

    // Set min-max values and colorize violation fields

    if ( json.locked === 'OPEN' )
    {
        $.safeEach( json.minMaxDataElements, function( i, value )
        {
            var minId = value.id + '-min';
            var maxId = value.id + '-max';

            var valFieldId = '#' + value.id + '-val';

            var dataValue = dataValueMap[value.id];

            if ( dataValue && ( ( value.min && new Number( dataValue ) < new Number(
                value.min ) ) || ( value.max && new Number( dataValue ) > new Number( value.max ) ) ) )
            {
                $( valFieldId ).css( 'background-color', dhis2.de.cst.colorOrange );
            }

            dhis2.de.currentMinMaxValueMap[minId] = value.min;
            dhis2.de.currentMinMaxValueMap[maxId] = value.max;
        } );
    }

    // Update indicator values in form

    dhis2.de.updateIndicators();
    dhis2.de.updateDataElementTotals();

    // Set completeness button

    if ( json.complete && json.locked === 'OPEN' )
    {
        $( '#completeButton' ).attr( 'disabled', 'disabled' );
        $( '#undoButton' ).removeAttr( 'disabled' );

		$('#contentDiv input').attr('disabled','disabled');
		
		$( '#dHTApprovedButton' ).removeAttr( 'disabled' );
		$( '#sHTApprovedButton' ).removeAttr( 'disabled' );
		$( '#pMUAccountApprovedButton' ).removeAttr( 'disabled' );
		
		//$( '#sHTApprovedButton' ).attr( 'disabled', 'disabled' );
		//$( '#pMUAccountApprovedButton' ).attr( 'disabled', 'disabled' );

        if ( json.lastUpdatedBy )
        {
            $( '#infoDiv' ).show();
            $( '#completedBy' ).html( json.lastUpdatedBy );
            $( '#completedDate' ).html( json.date );

            dhis2.de.currentCompletedByUser = json.lastUpdatedBy;
        }
    }
    else
    {
        $( '#completeButton' ).removeAttr( 'disabled' );
        $( '#undoButton' ).attr( 'disabled', 'disabled' );
        $( '#infoDiv' ).hide();
		
		$( '#contentDiv input' ).removeAttr( 'disabled' );
		
		$( '#dHTApprovedButton' ).attr( 'disabled', 'disabled' );
		$( '#sHTApprovedButton' ).attr( 'disabled', 'disabled' );
		$( '#pMUAccountApprovedButton' ).attr( 'disabled', 'disabled' );
    }
}

function displayEntryFormCompleted()
{
    dhis2.de.addEventListeners();

    $( '#validationButton' ).removeAttr( 'disabled' );
    $( '#validateButton' ).removeAttr( 'disabled' );

    dhis2.de.dataEntryFormIsLoaded = true;
    hideLoader();
    
    $( document ).trigger( dhis2.de.event.formReady, dhis2.de.currentDataSetId );
}

function valueFocus( e )
{
    var id = e.target.id;
    var value = e.target.value;

    var split = dhis2.de.splitFieldId( id );
    var dataElementId = split.dataElementId;
    var optionComboId = split.optionComboId;
    dhis2.de.currentOrganisationUnitId = split.organisationUnitId;
    dhis2.de.currentExistingValue = value;

    var dataElementName = getDataElementName( dataElementId );
    var optionComboName = getOptionComboName( optionComboId );
    var organisationUnitName;
    if ( dhis2.de.multiOrganisationUnit ) {
        organisationUnitName = organisationUnitList.filter( ou=>ou.uid === dhis2.de.getCurrentOrganisationUnit() )[0].n;
    } else {
        organisationUnitName = organisationUnits[dhis2.de.getCurrentOrganisationUnit()].n;
    }

    $( '#currentOrganisationUnit' ).html( organisationUnitName );
    $( '#currentDataElement' ).html( dataElementName + ' ' + optionComboName );

    $( '#' + dataElementId + '-cell' ).addClass( 'currentRow' );
}

function valueBlur( e )
{
    var id = e.target.id;

    var split = dhis2.de.splitFieldId( id );
    var dataElementId = split.dataElementId;

    $( '#' + dataElementId + '-cell' ).removeClass( 'currentRow' );
}

function keyPress( event, field )
{
    var key = event.keyCode || event.charCode || event.which;

    var focusField = ( key == 13 || key == 40 ) ? getNextEntryField( field )
            : ( key == 38 ) ? getPreviousEntryField( field ) : false;

    if ( focusField )
    {
        focusField.focus();
    }
}

function getNextEntryField( field )
{
    var index = field.getAttribute( 'tabindex' );

    field = $( 'input[name="entryfield"][tabindex="' + ( ++index ) + '"]' );

    while ( field )
    {
        if ( field.is( ':disabled' ) || field.is( ':hidden' ) )
        {
            field = $( 'input[name="entryfield"][tabindex="' + ( ++index ) + '"]' );
        }
        else
        {
            return field;
        }
    }
}

function getPreviousEntryField( field )
{
    var index = field.getAttribute( 'tabindex' );

    field = $( 'input[name="entryfield"][tabindex="' + ( --index ) + '"]' );

    while ( field )
    {
        if ( field.is( ':disabled' ) || field.is( ':hidden' ) )
        {
            field = $( 'input[name="entryfield"][tabindex="' + ( --index ) + '"]' );
        }
        else
        {
            return field;
        }
    }
}

// -----------------------------------------------------------------------------
// Data completeness
// -----------------------------------------------------------------------------

function registerCompleteDataSet( completedStatus )
{
	
    if (completedStatus && $("#selectedDataSetId").val() == "XV12eKZar28") {
        const promptValue = validateIds();
        if (promptValue) {
          alert(`Please fill compulsary data elements!${promptValue}`);
          return false;
        }
        pushDEValues();
    }

	if ( !confirm( completedStatus ? i18n_confirm_complete : i18n_confirm_undo ) )
	{
		return false;
    }
	
	//alert( completedStatus );
	
	if ( completedStatus )
	{
		//alert( "complete");
		$('#contentDiv input').attr('disabled','disabled');
		$( '#contentDiv textarea').attr( 'disabled', 'disabled' );
	}
	else if( !completedStatus )
	{
		//alert( "incomplete");
		$('#contentDiv input').removeAttr( 'disabled' );
		$( '#contentDiv textarea').removeAttr( 'disabled' );
	}
	

	
	dhis2.de.validate( completedStatus, true, function() 
    {
        var params = dhis2.de.storageManager.getCurrentCompleteDataSetParams();

        var cc = dhis2.de.getCurrentCategoryCombo();
        var cp = dhis2.de.getCurrentCategoryOptionsQueryValue();

        params.isCompleted = completedStatus;
        
        if ( cc && cp )
        {
        	params.cc = cc;
        	params.cp = cp;
        }
        
        dhis2.de.storageManager.saveCompleteDataSet( params );
        
        var cdsr = {completeDataSetRegistrations: []};
        
        if( params.multiOu )
        {
            $.each( organisationUnitList, function( idx, item )
            {
                if( item.uid )
                {
                    cdsr.completeDataSetRegistrations.push( {cc: params.cc, cp: params.cp, dataSet: params.ds,period: params.pe, organisationUnit: item.uid, completed: params.isCompleted} );
                }            
            } );
        }
        else
        {
            cdsr.completeDataSetRegistrations.push( {cc: params.cc, cp: params.cp, dataSet: params.ds,period: params.pe, organisationUnit: params.ou, completed: params.isCompleted} );
        }
	
	    $.ajax( {
	    	url: '../api/completeDataSetRegistrations',
	    	data: JSON.stringify( cdsr ),
            contentType: "application/json; charset=utf-8",
	        dataType: 'json',
	        type: 'post',
	    	success: function( data, textStatus, xhr )
	        {
                dhis2.de.storageManager.clearCompleteDataSet( params );
                if( data && data.response && data.response.status == 'SUCCESS' )
                {
                    $( document ).trigger( dhis2.de.event.completed, [ dhis2.de.currentDataSetId, params ] );
                    disableCompleteButton( params.isCompleted );
                }
                else if( data && data.response && data.response.status == 'ERROR' )
                {
                    handleDataSetCompletenessResponse( data );
                }
	        },
		    error:  function( xhr, textStatus, errorThrown )
		    {
		    	if ( 403 == xhr.status || 409 == xhr.status || 500 == xhr.status ) // Invalid value or locked
	        	{
	        		setHeaderMessage( xhr.responseText );
	        	}
	        	else // Offline, keep local value
	        	{
                    $( document ).trigger( dhis2.de.event.completed, [ dhis2.de.currentDataSetId, params ] );
	        		disableCompleteButton( params.isCompleted );
	        		setHeaderMessage( i18n_offline_notification );
	        	}
		    }
	    } );
	} );
}
// -----------------------------------------------------------------------------
// Validation With data element
// -----------------------------------------------------------------------------

function validateIds() {
  var emptyValues = [];
  var result = "";
  var hasValue = true;
  var ids = {
    indicatorhZP3yMW6DrW: "Current quarter payments (utilised)",
    "pruqL3yxefO-HllvX50cXC0-val": "Opening balance project bank A/c",
    "PerpmFri8RS-HllvX50cXC0-val": "Opening balance cash (in hand)",
    "f0a2dQQiU1s-HllvX50cXC0-val": "Activity-wise IPA fund received - Bank",
    "sdoMh3e7puX-HllvX50cXC0-val": "Activity wise IPA fund utilized - Bank",
    "HSkhJ8tcjM7-HllvX50cXC0-val": "Activity wise IPA fund utilized - Cash",
    "ul7BDdSk6TL-HllvX50cXC0-val": "Other receipts - Fund Utilised - Bank",
    "xOgrzAWA7n5-HllvX50cXC0-val": "Other receipts - Fund Utilised - Cash",
  };
  var balanceIds = `PXHOGLuZzFk;spakGSjnoJz;Pl3oBvJtbkF;YDfRZ3VSW7o;txsnETYuLQu;GRFIQXGw4z9;QR9jYaPsO7D;qjzZgvOSilI;P1Hcm32Olaw;oGaYG2sfAJY`;
  for (let id in ids) {
    let de = document.getElementById(id);
    if (de && !de.value) {
      emptyValues.push(ids[id]);
    }
  }
  balanceIds.split(";").forEach((id) => {
    let de = document.getElementById(`${id}-HllvX50cXC0-val`);
    if (hasValue && de && !de.value) {
      hasValue = false;
    }
  });
  if (!hasValue)
    emptyValues.push("Funds received under Different Schemes table: Reciept/Payments column");
  emptyValues.forEach((val, index) => {
    result += `\n${index + 1}: ${val}`;
  });
  return result;
}

// -----------------------------------------------------------------------------
// pushed values to specific ids.
// -----------------------------------------------------------------------------

function pushDEValues() {
  var activityExpenditure = [
    ["vG8QKhAyRhT", "ukiRq2gleA9", "iDZNTrtvLFJ"],
    ["YKFEKXk4Aig", "mQBEQiXgCUX", "IxbYVGZ1ERo"],
    ["zQV9DTso2bN", "WAVUVCtgQkg", "V6pn1XiwWmZ"],
    ["ps4OBkuYE47", "yfrbWCdH02Z", "t2Z6dXifJ6N"],
    ["lbQsVwe61tI", "AOAqXrjm8sI", "fD8feI290wX"],
    ["ONADcVTlZtj", "vSGHAtD4np0", "dKhRKgdJ4qK"],
    ["xcBtxPUYQAN", "wdhNBKRp928", "GmOHVjX5RyD"],
    ["sNBdm2p4dko", "V0sDvcWZchC", "KcOepDJ2I6P"],
    ["jacGn6QIDyu", "mry3YXmsu04", "VmMx8RLlBbL"],
    ["gyCvqVkcyuf", "jDbWR4imXOJ", "B494KumJqJy"],
    ["G6l6F1Hjrig", "BJQ3sDnzqJk", "TAZeubDI3wq"],
    ["S7DtErytP5n", "f3XiosaJdGb", "PWQ2YifF82d"],
    ["X4JrI7jEj7X", "tk3BP3q9xUo", "wxhlz7x6mQN"],
    ["U8W8FZzOkcv", "yyx4AADIMlk", "n0ntNNHQxQN"],
    ["v4ZPSG5eIhu", "aDxy5n0hHKa", "jPc2i2OYq28"],
    ["U4SBmJsURjp", "OCkzjuz1U1b", "JiQFAUodWcb"],
    ["cZTxcz3qVLv", "MgVXfCweZDf", "loW0eBKEpQR"],
    ["NCvv7N3nJw2", "LqYgQd08nDi", "himz7qbGy7x"],
    ["TuOZ6Xd0ccY", "UNp4wMVeeZe", "bhcB0DhF1uu"],
    ["go8w36rvyzT", "p1SEMifOaiZ", "QRup2BDgZ8s"],
    ["c9aSocO2A5k", "UkDT3plut99", "obEv73ewbr5"],
    ["GKGo7J8OdLo", "vL5YhuF8tDI", "WZ5MJg1mxAg"],
    ["gv7ypAMglHn", "oOZl0EgyS5U", "vqn7yxUFA06"],
    ["eaNZpI7sdgh", "fFmADEj0xil", "YjLf2Ama5A0"],
    ["ocbVzbFPJaU", "b5Em3xwExJh", "FCCuxVdiuK5"],
    ["K0QTe3FbZQW", "BBVHK38qD0m", "b4fLSxZeWmS"],
    ["vK5dhkpYtFZ", "F9k85AXbT8Y", "bPWrMqoIZTL"],
    ["gTlDMAxLrVe", "L9whtHr0jof", "uDpAWx5NtEG"],
    ["zrYNrRnHUJ9", "NCsmmPDj9IB", "pysR1rHYLIs"],
    ["PQKG80KWWQ1", "tkLu3RVQK2O", "eZyAFioXcK0"],
    ["qxT7OdXpezK", "JJj2NIo4RaU", "U18vROet3YU"],
    ["ORc3lnnrri0", "hKAN3h5JYyB", "BzoAsbJ1LCU"],
    ["J7y3Gdgi5OI", "cymlOzmo39B", "D7qGWnLxw7g"],
    ["wReWf7GZlVG", "Re5VTzeapjL", "hbAWWo5PkwW"],
    ["w0QcN9uqxVh", "YoYX8Qy6aVK", "ovfwqkf92vm"],
    ["YDy3J1C58JA", "w4QZZmOlM2h", "BJxJKG5rOLG"],
    ["KgCofHptkMz", "gjaPDkL87vD", "btDndI3ObMv"],
    ["liPwyy6bwkV", "sJ43C2eJdIi", "fZlYQeJpgru"],
    ["eHEQG8WybwC", "fbG5Fs8PIBe", "iwpSYLXsZCZ"],
    ["cDkodEeb96U", "tgVRUjRej4g", "olNNhEzCLMj"],
    ["iSiUxRZPtZC", "fQwILCxgbAr", "fSGwtbWEUva"],
    ["N1RrNNkbees", "jdCQcGaavDR", "bowWm1K3EvE"],
    ["yDMkzwDkVsu", "OIimnDslZJE", "a1zSkTFtPdD"],
    ["ECTkJxBzdqt", "XIuQt690cvw", "VDPVx9Zrdpn"],
    ["Nyvg2Cp1jwe", "X9Igb1uSwzh", "M7ZwNaCmaBx"],
    ["uc8RwdtR8pC", "SdK8WgD57RE", "rUYjJZlpLF3"],
    ["MT3n06zYSnc", "EiAQQwCuP30", "p3xIOj5y3PL"],
    ["fUIo7poz1Ha", "niqK6viUgGF", "FPIu0E8w1xk"],
    ["Hz4hc2oFInO", "UD8X8XKmWJs", "EiGYY8OXIjY"],
    ["kaK9Bya4Vs3", "EH0zDYpDdVc", "XCs39Wx4p3Q"],
  ];
  var fundsReceived = {
    "Goods - E": "",
    "Goods - DC": "",
    "Goods - ME": "",
    "Goods - F": "",
    "Goods - BPS": "",
    Works: "",
    "Works - RC": "",
    Services: "",
    "Services - C": "",
    "Services - M": "",
  };
  var fundsUtilized = {
    "Goods - E": "",
    "Goods - DC": "",
    "Goods - ME": "",
    "Goods - F": "",
    "Goods - BPS": "",
    Works: "",
    "Works - RC": "",
    Services: "",
    "Services - C": "",
    "Services - M": "",
  };
  var fundsrec = {
    "Goods - E": "w8wv57QPWww-XnHmxw4QWAR",
    "Goods - ME": "w8wv57QPWww-XDqTSxjtTRU",
    "Goods - DC": "w8wv57QPWww-YrT6udvuOUU",
    "Goods - BPS": "w8wv57QPWww-huR6MbBAtO4",
    "Goods - F": "w8wv57QPWww-AWpgyF3XivS",
    Works: "GiI9u9gcAZW-QnWan7CJDeF",
    "Works - RC": "GiI9u9gcAZW-eWeOuVZm3dh",
    Services: "e5Crg54YYNb-Mdyc7ZWpxvN",
    "Services - C": "e5Crg54YYNb-jjXxNmzLTNi",
    "Services - M": "e5Crg54YYNb-IEi4W3S4GcG",
  };
  var fundsUtil = {
    "Goods - E": "ZK742qo5ZQj-XnHmxw4QWAR",
    "Goods - ME": "ZK742qo5ZQj-XDqTSxjtTRU",
    "Goods - DC": "ZK742qo5ZQj-YrT6udvuOUU",
    "Goods - BPS": "ZK742qo5ZQj-huR6MbBAtO4",
    "Goods - F": "ZK742qo5ZQj-AWpgyF3XivS",
    Works: "WPxpVXGts6v-QnWan7CJDeF",
    "Works - RC": "WPxpVXGts6v-eWeOuVZm3dh",
    Services: "ZN54RXxSYjl-Mdyc7ZWpxvN",
    "Services - C": "ZN54RXxSYjl-jjXxNmzLTNi",
    "Services - M": "ZN54RXxSYjl-IEi4W3S4GcG",
  };

  activityExpenditure.forEach((ids) => {
    let activityDetails = document.getElementById(
      `${ids[0]}-HllvX50cXC0-val`
    ).value;
    let receiptReceived = document.getElementById(
      `${ids[1]}-HllvX50cXC0-val`
    ).value;
    let receiptPaid = document.getElementById(
      `${ids[2]}-HllvX50cXC0-val`
    ).value;
    if (receiptReceived !== "")
      fundsReceived[activityDetails] =
        Number(receiptReceived) + Number(fundsReceived[activityDetails]);
    if (receiptPaid !== "")
      fundsUtilized[activityDetails] =
        Number(receiptPaid) + Number(fundsUtilized[activityDetails]);
  });

  for (let id in fundsrec) {
    for (let activityDetails in fundsReceived) {
      if (activityDetails == id && fundsReceived[activityDetails] !== "") {
        let dataValue = {
          de: fundsrec[id].split("-")[0],
          co: fundsrec[id].split("-")[1],
          ou: dhis2.de.getCurrentOrganisationUnit(),
          pe: $("#selectedPeriodId").val(),
          value: fundsReceived[activityDetails],
        };
        pushValue(dataValue);
        debugger;
        document.getElementById(`${fundsrec[id]}-val`).value =
          fundsReceived[activityDetails];
      }
    }
  }

  for (let id in fundsUtil) {
    for (let activityDetails in fundsUtilized) {
      if (activityDetails == id && fundsUtilized[activityDetails] !== "") {
        let dataValue = {
          de: fundsUtil[id].split("-")[0],
          co: fundsUtil[id].split("-")[1],
          ou: dhis2.de.getCurrentOrganisationUnit(),
          pe: $("#selectedPeriodId").val(),
          value: fundsUtilized[activityDetails],
        };
        pushValue(dataValue);
        document.getElementById(`${fundsUtil[id]}-val`).value =
          fundsUtilized[activityDetails];
      }
    }
  }

  function pushValue(dataValue) {
    $.ajax({
      url: "../api/dataValues",
      data: dataValue,
      type: "post",
      error: handleError,
    });
  }

  function handleError(xhr, textStatus, errorThrown) {
    if (409 == xhr.status || 500 == xhr.status) {
      // Invalid value or locked
      alert(" error to save");
    }
  }
}

function handleDataSetCompletenessResponse( data ){
    var html = '<h3>' + i18n_dataset_completeness_error + ' &nbsp;<img src="../images/warning_small.png"></h3>';
                    
    if( data && data.conflicts && data.conflicts.length > 0 )
    {
        html += '<table class="listTable" style="width:300px;">';
        var alternate = false;
        data.conflicts.forEach(function( conflict ) {
            var style = alternate ? 'class="listAlternateRow"' : '';                            
            html += '<tr><td ' + style + '>' + conflict.value + '</td></tr>';
            alternate = !alternate;
        });                        
        html +='</table>';                        
    }

    dhis2.de.displayValidationDialog( html, 400 );
}

function undoCompleteDataSet()
{
	if ( !confirm( i18n_confirm_undo ) )
	{
		return false;
	}

	
    var params = dhis2.de.storageManager.getCurrentCompleteDataSetParams();

    var cc = dhis2.de.getCurrentCategoryCombo();
    var cp = dhis2.de.getCurrentCategoryOptionsQueryValue();

    var params = 
    	'?ds=' + params.ds +
    	'&pe=' + params.pe +
    	'&ou=' + params.ou + 
    	'&multiOu=' + params.multiOu;

    if ( cc && cp )
    {
    	params += '&cc=' + cc;
    	params += '&cp=' + cp;
    }
        
    $.ajax( {
    	url: '../api/completeDataSetRegistrations' + params,
    	dataType: 'json',
    	type: 'delete',
    	success: function( data, textStatus, xhr )
        {
            dhis2.de.storageManager.clearCompleteDataSet( params );
            $( document ).trigger( dhis2.de.event.completed, [ dhis2.de.currentDataSetId, params ] );
            disableCompleteButton( params );
        },
        error: function( xhr, textStatus, errorThrown )
        {
        	if ( 403 == xhr.status || 409 == xhr.status || 500 == xhr.status ) // Invalid value or locked
        	{
        		setHeaderMessage( xhr.responseText );
        	}
        	else // Offline, keep local value
        	{
                $( document ).trigger( dhis2.de.event.uncompleted, dhis2.de.currentDataSetId );
        		disableUndoButton();
        		setHeaderMessage( i18n_offline_notification );
        	}

    		dhis2.de.storageManager.clearCompleteDataSet( params );
        }
    } );
}

function disableUndoButton()
{
    $( '#completeButton' ).removeAttr( 'disabled' );
    $( '#undoButton' ).attr( 'disabled', 'disabled' );
	
	$( '#dHTApprovedButton' ).attr( 'disabled', 'disabled' );
	$( '#sHTApprovedButton' ).attr( 'disabled', 'disabled' );
	$( '#pMUAccountApprovedButton' ).attr( 'disabled', 'disabled' );
	
}

function disableCompleteButton( status )
{
    if( status == true )
    {
        $( '#completeButton' ).attr( 'disabled', 'disabled' );
        $( '#undoButton' ).removeAttr( 'disabled' );
		
		$( '#dHTApprovedButton' ).removeAttr( 'disabled' );
		$( '#sHTApprovedButton' ).removeAttr( 'disabled' );
		$( '#pMUAccountApprovedButton' ).removeAttr( 'disabled' );
    }
    else
	{
        disableUndoButton();
    }
}

function displayUserDetails()
{
	if ( dhis2.de.currentCompletedByUser )
	{
		var url = '../api/35/userLookup';

		$.getJSON( url, { query: dhis2.de.currentCompletedByUser }, function( json )
		{
			$( '#userFullName' ).html( json.users[0].displayName );
			$( '#userUsername' ).html( dhis2.de.currentCompletedByUser );
			$( '#firstName' ).html( json.users[0].firstName );
			$( '#surname' ).html( json.users[0].surname );

			$( '#completedByDiv' ).dialog( {
	        	modal : true,
	        	width : 350,
	        	height : 350,
	        	title : 'User'
	    	} );
		} );
	}
}

// -----------------------------------------------------------------------------
// Validation
// -----------------------------------------------------------------------------

dhis2.de.validateCompulsoryDataElements = function ()
{
  var compulsoryValid = true;

  $('[required=required]').each( function() {

    if ( $(this).prop("disabled") )
    {
        return;
    }

    if ( $(this).hasClass("entryselect") )
    {
      var entrySelectName =  $(this).attr("name");
      var value  = $("[name="+entrySelectName+"]:checked").val();

      if( value == undefined )
      {
        $(this).parents("td").addClass("required");
        compulsoryValid = false;
      }
    }
    else if( $.trim( $( this ).val() ).length == 0 )
    {
      if( $(this).hasClass("entryoptionset") )
      {
        $(this).siblings("div.entryoptionset").css("border", "1px solid red");
      }
      else
      {
        $(this).css( 'background-color', dhis2.de.cst.colorRed );
      }

      compulsoryValid = false;
    }
  }) ;
  return compulsoryValid;
}

/**
 * Executes all validation checks.
 * 
 * @param ignoreValidationSuccess indicates whether no dialog should be display
 *        if validation is successful.
 * @param successCallback the function to execute if validation is successful.                                  
 */
dhis2.de.validate = function( completeUncomplete, ignoreValidationSuccess, successCallback )
{
	var compulsoryCombinationsValid = dhis2.de.validateCompulsoryCombinations();

    var compulsoryDataElementsValid = dhis2.de.validateCompulsoryDataElements();
    
    var compulsoryFieldsCompleteOnly = dhis2.de.dataSets[dhis2.de.currentDataSetId].compulsoryFieldsCompleteOnly;
	
	// Check for compulsory combinations and return false if violated
	
	if ( !compulsoryCombinationsValid || !compulsoryDataElementsValid )
	{
        if( !compulsoryDataElementsValid && !compulsoryFieldsCompleteOnly )
        {
            if( completeUncomplete )
            {
                setHeaderDelayMessage( i18n_complete_compulsory_notification );
            }
            else
            {
                setHeaderDelayMessage( i18n_uncomplete_notification );
            }
        }
        else
        {
            var html = '<h3>' + i18n_validation_result + ' &nbsp;<img src="../images/warning_small.png"></h3>' +
        	'<p class="bold">' + i18n_missing_compulsory_dataelements + '</p>';
		
            dhis2.de.displayValidationDialog( html, 300 );

            return false;            
        }    	
	}    

	// Check for validation rules and whether complete is only allowed if valid
	
	var successHtml = '<h3>' + i18n_validation_result + ' &nbsp;<img src="../images/success_small.png"></h3>' +
		'<p class="bold">' + i18n_successful_validation + '</p>';

	var validCompleteOnly = dhis2.de.dataSets[dhis2.de.currentDataSetId].validCompleteOnly;

    var cc = dhis2.de.getCurrentCategoryCombo();
    var cp = dhis2.de.getCurrentCategoryOptionsQueryValue();

    var params = dhis2.de.storageManager.getCurrentCompleteDataSetParams();

    if ( cc && cp )
    {
        params.cc = dhis2.de.getCurrentCategoryCombo();
        params.cp = dhis2.de.getCurrentCategoryOptionsQueryValue();
    }

    $( '#validationDiv' ).load( 'validate.action', params, function( response, status, xhr ) {
    	var success = null;
    	
        if ( status == 'error' && !ignoreValidationSuccess )
        {
            window.alert( i18n_operation_not_available_offline );
            success = true;  // Accept if offline
        }
        else
        {
        	var hasViolations = isDefined( response ) && $.trim( response ).length > 0;
        	var success = !( hasViolations && validCompleteOnly );
        	
        	if ( hasViolations )
        	{
        		dhis2.de.displayValidationDialog( response, 500 );
        	}
        	else if ( !ignoreValidationSuccess )
        	{
        		dhis2.de.displayValidationDialog( successHtml, 200 );
        	}        	
        }
        
        if ( success && $.isFunction( successCallback ) )
        {
        	successCallback.call();
        }
        
        if ( success )
        {
        	$( document ).trigger( dhis2.de.event.validationSucces, dhis2.de.currentDataSetId );
        }
        else
    	{
        	$( document ).trigger( dhis2.de.event.validationError, dhis2.de.currentDataSetId );
    	}
    } );
}

/**
 * Displays the validation dialog.
 * 
 * @param html the html content to display in the dialog.
 * @param height the height of the dialog.
 */
dhis2.de.displayValidationDialog = function( html, height )
{
	height = isDefined( height ) ? height : 500;
	
	$( '#validationDiv' ).html( html );
	
    $( '#validationDiv' ).dialog( {
        modal: true,
        title: 'Validation',
        width: 920,
        height: height
    } );
}

/**
 * Validates that all category option combinations have all values or no values
 * per data element given that the fieldCombinationRequired is true for the 
 * current data set.
 */
dhis2.de.validateCompulsoryCombinations = function()
{
	var fieldCombinationRequired = dhis2.de.dataSets[dhis2.de.currentDataSetId].fieldCombinationRequired;
	
    if ( fieldCombinationRequired )
    {
        var violations = false;

        $( '.entryfield' ).add( '[name="entryselect"]' ).each( function( i )
        {
            if ( $(this).prop("disabled") )
            {
                return;
            }
	    
            var id = $( this ).attr( 'id' );

            var split = dhis2.de.splitFieldId( id );
            var dataElementId = split.dataElementId;
            var hasValue = $.trim( $( this ).val() ).length > 0;
            
            if ( hasValue )
            {
            	$selector = $( '[name="entryfield"][id^="' + dataElementId + '-"]' ).
            		add( '[name="entryselect"][id^="' + dataElementId + '-"]' );
				
                $selector.each( function( i )
                {
                    if ( $.trim( $( this ).val() ).length == 0 )
                    {
                        violations = true;						
                        $selector.css( 'background-color', dhis2.de.cst.colorRed );						
                        return false;
                    }
                } );
            }
        } );
		
        if ( violations )
        {
            return false;
        }
    }
	
	return true;
};

dhis2.de.validateOrgUnitOpening = function(organisationUnit, period)
{
  var iso8601 = $.calendars.instance( 'gregorian' );
  var odate, cdate;

  if ( organisationUnit.odate ) {
    odate = dhis2.period.calendar.fromJD( iso8601.parseDate( "yyyy-mm-dd", organisationUnit.odate ).toJD() );
  }

  if ( organisationUnit.cdate ) {
    cdate = dhis2.period.calendar.fromJD( iso8601.parseDate( "yyyy-mm-dd", organisationUnit.cdate ).toJD() );
  }

  var startDate = dhis2.period.calendar.parseDate( dhis2.period.format, period.startDate );
  var endDate = dhis2.period.calendar.parseDate( dhis2.period.format, period.endDate );

  if( ( cdate && cdate < startDate ) || odate > endDate )
  {
    $( '#contentDiv input' ).attr( 'readonly', 'readonly' );
    $( '#contentDiv textarea' ).attr( 'readonly', 'readonly' );
    $( '.entrytrueonly' ).attr( 'onclick', 'return false;');
    $( '.entrytrueonly' ).attr( 'onkeydown', 'return false;');
    return true;
  }

  return false;
};

// -----------------------------------------------------------------------------
// History
// -----------------------------------------------------------------------------

function displayHistoryDialog( operandName )
{
    $( '#historyDiv' ).dialog( {
        modal: true,
        title: operandName,
        width: 580,
        height: 620
    } );
}

function viewHist( dataElementId, optionComboId )
{
    var periodId = $( '#selectedPeriodId').val();

	if ( dataElementId && optionComboId && periodId && periodId != -1 )
	{
	    var dataElementName = getDataElementName( dataElementId );
	    var optionComboName = getOptionComboName( optionComboId );
	    var operandName = dataElementName + ' ' + optionComboName;
	
	    var params = {
    		dataElementId : dataElementId,
	        optionComboId : optionComboId,
	        periodId : periodId,
	        organisationUnitId : dhis2.de.getCurrentOrganisationUnit()
	    };

	    var cc = dhis2.de.getCurrentCategoryCombo();
	    var cp = dhis2.de.getCurrentCategoryOptionsQueryValue();
	    
	    if ( cc && cp )
	    {
	    	params.cc = cc;
	    	params.cp = cp;
	    }
	    
	    $( '#historyDiv' ).load( 'viewHistory.action', params, 
	    function( response, status, xhr )
	    {
	        if ( status == 'error' )
	        {
	            window.alert( i18n_operation_not_available_offline );
	        }
	        else
	        {
	            displayHistoryDialog( operandName );
	        }
	    } );
	}
}

function closeCurrentSelection()
{
    $( '#currentSelection' ).fadeOut();
}

// -----------------------------------------------------------------------------
// Local storage of forms
// -----------------------------------------------------------------------------

function updateForms()
{
    DAO.store.open()
        .then(purgeLocalForms)
        .then(getLocalFormsToUpdate)
        .then(downloadForms)
        .then(getUserSetting)
        .then(getTimeDelta)
        .then(getRemoteFormsToDownload)
        .then(downloadForms)
        .then(dhis2.de.loadOptionSets)
        .done( function() {
            setDisplayNamePreferences();

            selection.responseReceived();
        } );
}

function setDisplayNamePreferences() {
    var settings = dhis2.de.storageManager.getUserSettings();
    var useShortNames = true;

    if ( settings !== null ) {
        useShortNames = settings.keyAnalysisDisplayProperty === "shortName";
    }

    selection.setDisplayShortNames(useShortNames);
}

function purgeLocalForms()
{
    var def = $.Deferred();

    dhis2.de.storageManager.getAllForms().done(function( formIds ) {
        var keys = [];

        $.safeEach( formIds, function( idx, item )
        {
            if ( dhis2.de.dataSets[item] == null )
            {
                keys.push(item);
            	dhis2.de.storageManager.deleteFormVersion( item );
                console.log( 'Deleted locally stored form: ' + item );
            }
        } );

        def.resolve();

        console.log( 'Purged local forms' );
    });

    return def.promise();
}

function getLocalFormsToUpdate()
{
    var def = $.Deferred();
    var formsToDownload = [];

    dhis2.de.storageManager.getAllForms().done(function( formIds ) {
        var formVersions = dhis2.de.storageManager.getAllFormVersions();

        $.safeEach( formIds, function( idx, item )
        {
            var ds = dhis2.de.dataSets[item];
            var remoteVersion = ds ? ds.version : null;
            var localVersion = formVersions[item];

            if ( remoteVersion == null || localVersion == null || remoteVersion != localVersion )
            {
            	formsToDownload.push({id: item, version: remoteVersion});
            }
        } );

        def.resolve( formsToDownload );
    });

    return def.promise();
}

function getRemoteFormsToDownload()
{
    var def = $.Deferred();
    var formsToDownload = [];

    $.safeEach( dhis2.de.dataSets, function( idx, item )
    {
        var remoteVersion = item.version;

        if ( !item.skipOffline )
        {
            dhis2.de.storageManager.formExists( idx ).done(function( value ) {
                if( !value ) {
                	formsToDownload.push({id: idx, version: remoteVersion})
                }
            });
        }
    } );

    $.when.apply($, formsToDownload).then(function() {
        def.resolve( formsToDownload );
    });

    return def.promise();
}

function downloadForms( forms )
{
    if ( !forms || !forms.length || forms.length < 1 )
    {
        return;
    }

    var batches = arrayChunk( forms, dhis2.de.cst.downloadBatchSize );

    var mainDef = $.Deferred();
    var mainPromise = mainDef.promise();

    var batchDef = $.Deferred();
    var batchPromise = batchDef.promise();

    var builder = $.Deferred();
    var build = builder.promise();

    $.safeEach( batches, function ( idx, batch ) {
        batchPromise = batchPromise.then(function(){
            return downloadFormsInBatch( batch );
        });
    });

    build.done(function() {
        batchDef.resolve();
        batchPromise = batchPromise.done( function () {
            mainDef.resolve();
        } );

    }).fail(function(){
        mainDef.resolve();
    });

    builder.resolve();

    return mainPromise;
}

function downloadFormsInBatch( batch )
{
    var def = $.Deferred();
    var chain = [];

    $.safeEach( batch, function ( idx, item ) {
        if ( item && item.id && item.version )
        {
            chain.push( dhis2.de.storageManager.downloadForm( item.id, item.version ) );
        }
    });

    $.when.apply($, chain).then(function() {
    	def.resolve( chain );
    });

    return def.promise();
}
// -----------------------------------------------------------------------------
// StorageManager
// -----------------------------------------------------------------------------

/**
 * This object provides utility methods for localStorage and manages data entry
 * forms and data values.
 */
function StorageManager()
{
    var KEY_FORM_VERSIONS = 'formversions';
    var KEY_DATAVALUES = 'datavalues';
    var KEY_COMPLETEDATASETS = 'completedatasets';
    var KEY_USER_SETTINGS = 'usersettings';
    var KEY_SERVER_TIME_DELTA = 'servertimedelta';
    var KEY_SERVER_TIME_RETRIEVED = 'servertimeretrieved';

    /**
     * Gets the content of a data entry form.
     *
     * @param dataSetId the identifier of the data set of the form.
     * @return the content of a data entry form.
     */
    this.getForm = function( dataSetId )
    {
        var def = $.Deferred();

        DAO.store.get( "forms", dataSetId ).done( function( form ) {
            if( typeof form !== 'undefined' ) {
                def.resolve( form.data );
            } else {
                dhis2.de.storageManager.loadForm( dataSetId ).done(function( data ) {
                    def.resolve( data );
                }).fail(function() {
                    def.resolve( "A form with that ID is not available. Please clear browser cache and try again." );
                });
            }
        });

        return def.promise();
    };

    /**
     * Returns an array of the identifiers of all forms.
     *
     * @return array with form identifiers.
     */
    this.getAllForms = function()
    {
        var def = $.Deferred();

        DAO.store.getKeys( "forms" ).done( function( keys ) {
            def.resolve( keys );
        });

        return def.promise();
    };

    /**
     * Indicates whether a form exists.
     *
     * @param dataSetId the identifier of the data set of the form.
     * @return true if a form exists, false otherwise.
     */
    this.formExists = function( dataSetId )
    {
        var def = $.Deferred();

        DAO.store.contains( "forms", dataSetId ).done( function( found ) {
            def.resolve( found );
        });

        return def.promise();
    };

    /**
     * Indicates whether a form exists remotely.
     *
     * @param dataSetId the identifier of the data set of the form.
     * @return true if a form exists, false otherwise.
     */
    this.formExistsRemotely = function( dataSetId )
    {
        var def = $.Deferred();

        $.ajax({
            url: '../api/dataSets/' + dataSetId,
            accept: 'application/json',
            type: 'GET'
        }).done(function() {
            def.resolve( true );
        }).fail(function() {
            def.resolve( false );
        });

        return def.promise();
    };

    /**
     * Loads a form directly from the server, does not try to save it in the
     * browser (so that it doesn't interfere with any current downloads).
     *
     * @param dataSetId
     * @returns {*}
     */
    this.loadForm = function( dataSetId )
    {
        return $.ajax({
            url: 'loadForm.action',
            data: {
                dataSetId: dataSetId
            },
            dataType: 'text'
        });
    };

    /**
     * Downloads the form for the data set with the given identifier from the
     * remote server and saves the form locally. Potential existing forms with
     * the same identifier will be overwritten. Updates the form version.
     *
     * @param dataSetId the identifier of the data set of the form.
     * @param formVersion the version of the form of the remote data set.
     */
    this.downloadForm = function( dataSetId, formVersion )
    {
        var def = $.Deferred();
        
        console.log( 'Starting download of form: ' + dataSetId );

        $.ajax( {
            url: 'loadForm.action',
            data:
            {
                dataSetId : dataSetId
            },
            dataSetId: dataSetId,
            formVersion: formVersion,
            dataType: 'text',
            success: function( data )
            {
                var dataSet = {
                    id: dataSetId,
                    version: formVersion,
                    data: data
                };

                DAO.store.set( "forms", dataSet ).done(function() {
                    console.log( 'Successfully stored form: ' + dataSetId );
                    def.resolve();
                });

            	dhis2.de.storageManager.saveFormVersion( this.dataSetId, this.formVersion );
            }
        } );

        return def.promise();
    };

    /**
     * Saves a version for a form.
     *
     * @param dataSetId the identifier of the data set of the form.
     * @param formVersion the version of the form.
     */
    this.saveFormVersion = function( dataSetId, formVersion )
    {
        var formVersions = {};

        if ( localStorage[KEY_FORM_VERSIONS] != null )
        {
            formVersions = JSON.parse( localStorage[KEY_FORM_VERSIONS] );
        }

        formVersions[dataSetId] = formVersion;

        try
        {
            localStorage[KEY_FORM_VERSIONS] = JSON.stringify( formVersions );

          console.log( 'Successfully stored form version: ' + dataSetId );
        } 
        catch ( e )
        {
          console.log( 'Max local storage quota reached, ignored form version: ' + dataSetId );
        }
    };

    /**
     * Returns the version of the form of the data set with the given
     * identifier.
     *
     * @param dataSetId the identifier of the data set of the form.
     * @return the form version.
     */
    this.getFormVersion = function( dataSetId )
    {
        if ( localStorage[KEY_FORM_VERSIONS] != null )
        {
            var formVersions = JSON.parse( localStorage[KEY_FORM_VERSIONS] );

            return formVersions[dataSetId];
        }

        return null;
    };

    /**
     * Deletes the form version of the data set with the given identifier.
     *
     * @param dataSetId the identifier of the data set of the form.
     */
    this.deleteFormVersion = function( dataSetId )
    {
    	if ( localStorage[KEY_FORM_VERSIONS] != null )
        {
            var formVersions = JSON.parse( localStorage[KEY_FORM_VERSIONS] );

            if ( formVersions[dataSetId] != null )
            {
                delete formVersions[dataSetId];
                localStorage[KEY_FORM_VERSIONS] = JSON.stringify( formVersions );
            }
        }
    };

    this.getAllFormVersions = function()
    {
        return localStorage[KEY_FORM_VERSIONS] != null ? JSON.parse( localStorage[KEY_FORM_VERSIONS] ) : null;
    };

    /**
     * Saves a data value.
     *
     * @param dataValue The datavalue and identifiers in json format.
     */
    this.saveDataValue = function( dataValue )
    {
        var id = this.getDataValueIdentifier( dataValue.de, 
        		dataValue.co, dataValue.pe, dataValue.ou );

        var dataValues = {};

        if ( localStorage[KEY_DATAVALUES] != null )
        {
            dataValues = JSON.parse( localStorage[KEY_DATAVALUES] );
        }

        dataValues[id] = dataValue;

        try
        {
            localStorage[KEY_DATAVALUES] = JSON.stringify( dataValues );

          console.log( 'Successfully stored data value' );
        } 
        catch ( e )
        {
          console.log( 'Max local storage quota reached, not storing data value locally' );
        }
    };

    /**
     * Gets the value for the data value with the given arguments, or null if it
     * does not exist.
     *
     * @param de the data element identifier.
     * @param co the category option combo identifier.
     * @param pe the period identifier.
     * @param ou the organisation unit identifier.
     * @return the value for the data value with the given arguments, null if
     *         non-existing.
     */
    this.getDataValue = function( de, co, pe, ou )
    {
        var id = this.getDataValueIdentifier( de, co, pe, ou );

        if ( localStorage[KEY_DATAVALUES] != null )
        {
            var dataValues = JSON.parse( localStorage[KEY_DATAVALUES] );

            return dataValues[id];
        }

        return null;
    };
    
    /**
     * Returns the data values for the given period and organisation unit 
     * identifiers as an array.
     * 
     * @param json object with periodId and organisationUnitId properties.
     */
    this.getDataValuesInForm = function( json )
    {
    	var dataValues = this.getDataValuesAsArray();
    	var valuesInForm = new Array();
    	
		for ( var i = 0; i < dataValues.length; i++ )
		{
			var val = dataValues[i];
			
			if ( val.pe == json.periodId && val.ou == json.organisationUnitId )
			{
				valuesInForm.push( val );
			}
		}
    	
    	return valuesInForm;
    }

    /**
     * Removes the given dataValue from localStorage.
     *
     * @param dataValue The datavalue and identifiers in json format.
     */
    this.clearDataValueJSON = function( dataValue )
    {
        this.clearDataValue( dataValue.de, dataValue.co, dataValue.pe,
                dataValue.ou );
    };

    /**
     * Removes the given dataValue from localStorage.
     *
     * @param de the data element identifier.
     * @param co the category option combo identifier.
     * @param pe the period identifier.
     * @param ou the organisation unit identifier.
     */
    this.clearDataValue = function( de, co, pe, ou )
    {
        var id = this.getDataValueIdentifier( de, co, pe, ou );
        var dataValues = this.getAllDataValues();

        if ( dataValues != null && dataValues[id] != null )
        {
            delete dataValues[id];
            localStorage[KEY_DATAVALUES] = JSON.stringify( dataValues );
        }
    };

    /**
     * Returns a JSON associative array where the keys are on the form <data
     * element id>-<category option combo id>-<period id>-<organisation unit
     * id> and the data values are the values.
     *
     * @return a JSON associative array.
     */
    this.getAllDataValues = function()
    {
        return localStorage[KEY_DATAVALUES] != null ? JSON.parse( localStorage[KEY_DATAVALUES] ) : null;
    };

    this.clearAllDataValues = function()
    {
        localStorage[KEY_DATAVALUES] = "";
    };

    /**
     * Returns all data value objects in an array. Returns an empty array if no
     * data values exist. Items in array are guaranteed not to be undefined.
     */
    this.getDataValuesAsArray = function()
    {
    	var values = new Array();
    	var dataValues = this.getAllDataValues();
    	
    	if ( undefined === dataValues )
    	{
    		return values;
    	}
    	
    	for ( i in dataValues )
    	{
    		if ( dataValues.hasOwnProperty( i ) && undefined !== dataValues[i] )
    		{
    			values.push( dataValues[i] );
    		}
    	}
    	
    	return values;
    }

    /**
     * Generates an identifier.
     */
    this.getDataValueIdentifier = function( de, co, pe, ou )
    {
        return de + '-' + co + '-' + pe + '-' + ou;
    };

    /**
     * Generates an identifier.
     */
    this.getCompleteDataSetId = function( json )
    {
        return json.ds + '-' + json.pe + '-' + json.ou;
    };

    /**
     * Returns current state in data entry form as associative array.
     *
     * @return an associative array.
     */
    this.getCurrentCompleteDataSetParams = function()
    {
        var params = {
            'ds': $( '#selectedDataSetId' ).val(),
            'pe': $( '#selectedPeriodId').val(),
            'ou': dhis2.de.getCurrentOrganisationUnit(),
            'multiOu': dhis2.de.multiOrganisationUnit
        };

        return params;
    };

    /**
     * Gets all complete data set registrations as JSON.
     *
     * @return all complete data set registrations as JSON.
     */
    this.getCompleteDataSets = function()
    {
        if ( localStorage[KEY_COMPLETEDATASETS] != null )
        {
            return JSON.parse( localStorage[KEY_COMPLETEDATASETS] );
        }

        return null;
    };

    /**
     * Saves a complete data set registration.
     *
     * @param json the complete data set registration as JSON.
     */
    this.saveCompleteDataSet = function( json )
    {
        var completeDataSets = this.getCompleteDataSets();
        var completeDataSetId = this.getCompleteDataSetId( json );

        if ( completeDataSets != null )
        {
            completeDataSets[completeDataSetId] = json;
        }
        else
        {
            completeDataSets = {};
            completeDataSets[completeDataSetId] = json;
        }

        try
        {
        	localStorage[KEY_COMPLETEDATASETS] = JSON.stringify( completeDataSets );
        }
        catch ( e )
        {
        	log( 'Max local storage quota reached, not storing complete registration locally' );
        }
    };
    
    /**
     * Indicates whether a complete data set registration exists for the given
     * argument.
     * 
     * @param json object with periodId, dataSetId, organisationUnitId properties.
     */
    this.hasCompleteDataSet = function( json )
    {
    	var id = this.getCompleteDataSetId( json );
    	var registrations = this.getCompleteDataSets();
    	
        if ( null != registrations && undefined !== registrations && undefined !== registrations[id] )
        {
            return true;
        }
    	
    	return false;
    }

    /**
     * Removes the given complete data set registration.
     *
     * @param json the complete data set registration as JSON.
     */
    this.clearCompleteDataSet = function( json )
    {
        var completeDataSets = this.getCompleteDataSets();
        var completeDataSetId = this.getCompleteDataSetId( json );

        if ( completeDataSets != null )
        {
            delete completeDataSets[completeDataSetId];

            if ( completeDataSets.length > 0 )
            {
                localStorage.removeItem( KEY_COMPLETEDATASETS );
            }
            else
            {
                localStorage[KEY_COMPLETEDATASETS] = JSON.stringify( completeDataSets );
            }
        }
    };

    /**
     * Returns the cached user settings
     */
    this.getUserSettings = function()
    {
        return localStorage[ KEY_USER_SETTINGS ] !== null 
            ? JSON.parse(localStorage[ KEY_USER_SETTINGS ])
            : null;
    }

    /**
     * Caches the user settings
     * @param settings The user settings object (JSON) to serialize into cache
     */
    this.setUserSettings = function(settings)
    {
        localStorage[ KEY_USER_SETTINGS ] = JSON.stringify(settings);
    }

    /**
     * Returns the cached server time delta
     */
    this.getServerTimeDelta = function()
    {
        // if it has been more than 1 hour since last update, pull server time again
        var lastRetrieved = this.getServerTimeRetrieved();
        if (lastRetrieved === null || (new Date() - lastRetrieved > 3600000)) {
            getTimeDelta();
        }
        return localStorage[ KEY_SERVER_TIME_DELTA ]
            ? JSON.parse(localStorage[ KEY_SERVER_TIME_DELTA ])
            : null;
    }

    /**
     * Caches the time difference between server time and browser time
     * @param timeDelta The time difference (server - client) in milliseconds (integer)
     */
    this.setServerTimeDelta = function(timeDelta)
    {
        localStorage[ KEY_SERVER_TIME_DELTA ] = timeDelta;
    }

    /**
     * Returns the cached time when server time delta was retrieved
     */
    this.getServerTimeRetrieved = function()
    {
        return localStorage[ KEY_SERVER_TIME_RETRIEVED ]
        ? parseInt(localStorage[ KEY_SERVER_TIME_RETRIEVED ])
        : null;
    }

    /**
     * Caches the time that server time delta was last retrieved
     * @param retrievalTime javascript date
     */
    this.setServerTimeRetrieved = function(retrievalTime)
    {
        localStorage[ KEY_SERVER_TIME_RETRIEVED ] = retrievalTime.getTime();
    }

    /**
     * Indicates whether there exists data values or complete data set
     * registrations in the local storage.
     *
     * @return true if local data exists, false otherwise.
     */
    this.hasLocalData = function()
    {
        var dataValues = this.getAllDataValues();
        var completeDataSets = this.getCompleteDataSets();

        if ( dataValues == null && completeDataSets == null )
        {
            return false;
        }
        else if ( dataValues != null )
        {
            if ( Object.keys( dataValues ).length < 1 )
            {
                return false;
            }
        }
        else if ( completeDataSets != null )
        {
            if ( Object.keys( completeDataSets ).length < 1 )
            {
                return false;
            }
        }

        return true;
    };
}

// -----------------------------------------------------------------------------
// Option set
// -----------------------------------------------------------------------------

/**
 * Inserts the name of the option set in the input field with the given identifier.
 * The option set input fields should use the name as label and code as value to
 * be saved.
 * 
 * @fieldId the identifier of the field on the form #deuid-cocuid-val.
 * @value the value with properties id (deuid-cocuid) and val (option name).
 */
dhis2.de.setOptionNameInField = function( fieldId, value )
{
  var id = value.id;

  if(value.id.split("-").length == 3)
  {
    id = id.substr(12);
  }

	var optionSetUid = dhis2.de.optionSets[id].uid;

	DAO.store.get( 'optionSets', optionSetUid ).done( function( obj ) {
		if ( obj && obj.optionSet && obj.optionSet.options ) {			
			$.each( obj.optionSet.options, function( inx, option ) {
				if ( option && option.code == value.val ) {
			          option.id = option.code;
			          option.text = option.displayName;
			          $( fieldId ).select2('data', option);
			          return false;
				}
			} );
		}		
	} );
};

/**
 * Performs a search for options for the option set with the given identifier based
 * on the given query. If query is null, the first MAX options for the option set
 * is used. Checks and uses option set from local store, if not fetches option
 * set from server.
 */
dhis2.de.searchOptionSet = function( uid, query, success ) 
{
	var noneVal = '[No value]';
	
    if ( window.DAO !== undefined && window.DAO.store !== undefined ) {
        DAO.store.get( 'optionSets', uid ).done( function ( obj ) {
            if ( obj && obj.optionSet ) {
                var options = [];

                if ( query == null || query == '' || query == noneVal ) {
                    options = obj.optionSet.options.slice( 0, dhis2.de.cst.dropDownMaxItems - 1 );
                } 
                else {
                    query = query.toLowerCase();

                    for ( var idx=0, len = obj.optionSet.options.length; idx < len; idx++ ) {
                        var item = obj.optionSet.options[idx];

                        if ( options.length >= dhis2.de.cst.dropDownMaxItems ) {
                            break;
                        }

                        if ( item.name.toLowerCase().indexOf( query ) != -1 ) {
                            options.push( item );
                        }
                    }
                }
                
                if ( options && options.length > 0 ) {
                	options.push( { name: noneVal, code: '' } );
                }

                success( $.map( options, function ( item ) {
                    return {
                        label: item.displayName,
                        id: item.code
                    };
                } ) );
            }
            else {
                dhis2.de.getOptions( uid, query, success );
            }
        } );
    } 
    else {
        dhis2.de.getOptions( uid, query, success );
    }
};

/**
 * Retrieves options from server. Provides result as jquery ui structure to the
 * given jquery ui success callback.
 */
dhis2.de.getOptions = function( uid, query, success ) 
{
    var encodedQuery = encodeURIComponent(query);
    var encodedFields = encodeURIComponent(':all,options[:all]');
    var encodedUrl =
      "../api/optionSets/" +
      uid +
      ".json?fields=" +
      encodedFields +
      "&links=false&q=" +
      encodedQuery;
  
    return $.ajax( {
        url: encodedUrl,
        dataType: "json",
        cache: false,
        type: 'GET',
        success: function ( data ) {
            success( $.map( data.options, function ( item ) {
                return {
                    label: item.displayName,
                    id: item.code
                };
            } ) );
        }
    } );
};

/**
 * Loads option sets from server into local store.
 */
dhis2.de.loadOptionSets = function() 
{
    var options = _.uniq( _.values( dhis2.de.optionSets ), function( item ) {
        return item.uid;
    }); // Array of objects with uid and v

    var uids = [];

    var deferred = $.Deferred();
    var promise = deferred.promise();

    _.each( options, function ( item, idx ) {
        if ( uids.indexOf( item.uid ) == -1 ) {
            DAO.store.get( 'optionSets', item.uid ).done( function( obj ) {
                if( !obj || !obj.optionSet || !obj.optionSet.version || !item.v || obj.optionSet.version !== item.v ) {
                    promise = promise.then( function () {
                        var encodedFields = encodeURIComponent(':all,options[:all]');
                      
                        return $.ajax( {
                            url: '../api/optionSets/' + item.uid + '.json?fields=' + encodedFields,
                            type: 'GET',
                            cache: false
                        } ).done( function ( data ) {
                            console.log( 'Successfully stored optionSet: ' + item.uid );

                            var obj = {};
                            obj.id = item.uid;
                            obj.optionSet = data;
                            DAO.store.set( 'optionSets', obj );
                        } );
                    } );

                    uids.push( item.uid );
                }
            });
        }
    } );

    promise = promise.then( function () {
    } );

    deferred.resolve();
};

/**
 * Enable event for showing DataElement description when click on
 * a DataElement label
 */
dhis2.de.enableDEDescriptionEvent = function()
{
    $('.dataelement-label, .indicator-label').on({
        "click": function () {
            var description = $('#' + $(this).attr('id') + '-description' ).val();
            $(this).tooltip({ items: '#' + $(this).attr('id'), content: description });
            $(this).tooltip("open");
        },
        "mouseout" : function() {
            if ( $(this).is(":ui-tooltip") ) {
                $(this).tooltip("disable");
            }
        }
    });
}

/**
 * Inserts option sets in the appropriate input fields.
 */
dhis2.de.insertOptionSets = function() 
{
    $( '.entryoptionset').each( function( idx, item ) {
        
        var fieldId = item.id;
        
        var split = dhis2.de.splitFieldId( fieldId );

        var dataElementId = split.dataElementId;
        var optionComboId = split.optionComboId;
        
    	var optionSetKey = dhis2.de.splitFieldId( item.id );
        var s2prefix = 's2id_';        
        optionSetKey.dataElementId = optionSetKey.dataElementId.indexOf(s2prefix) != -1 ? optionSetKey.dataElementId.substring(s2prefix.length, optionSetKey.dataElementId.length) : optionSetKey.dataElementId;
        
        if ( dhis2.de.multiOrganisationUnit ) {
        	item = optionSetKey.organisationUnitId + '-' + optionSetKey.dataElementId + '-' + optionSetKey.optionComboId;
        } 
        else {
        	item = optionSetKey.dataElementId + '-' + optionSetKey.optionComboId;
        }
        
        item = item + '-val';
        optionSetKey = optionSetKey.dataElementId + '-' + optionSetKey.optionComboId;
        var optionSetUid = dhis2.de.optionSets[optionSetKey].uid;
        
        DAO.store.get( 'optionSets', optionSetUid ).done( function( obj ) {
		if ( obj && obj.optionSet && obj.optionSet.options ) {

                    $.each( obj.optionSet.options, function( inx, option ) {
                        option.text = option.displayName;
                        option.id = option.code;
                    } );
                    
                    $("#" + item).select2({
                        placeholder: i18n_select_option ,
                        allowClear: true,
                        dataType: 'json',
                        data: obj.optionSet.options
                    }).on("change", function(e){
                        saveVal( dataElementId, optionComboId, fieldId );
                    });
		}		
	} );        
    } );
};

/**
 * Applies the autocomplete widget on the given input field using the option set
 * with the given identifier.
 */
dhis2.de.autocompleteOptionSetField = function( idField, optionSetUid ) 
{
    var input = jQuery( '#' + idField );

    if ( !input ) {
        return;
    }

    input.autocomplete( {
        delay: 0,
        minLength: 0,
        source: function ( request, response ) {
            dhis2.de.searchOptionSet( optionSetUid, input.val(), response );
        },
        select: function ( event, ui ) {
            input.val( ui.item.id );
            input.autocomplete( 'close' );
            input.change();
        },
        change: function( event, ui ) {
            if( ui.item == null ) {
                $( this ).val("");
                $( this ).focus();
            }
        }
    } ).addClass( 'ui-widget' );

    input.data( 'ui-autocomplete' )._renderItem = function ( ul, item ) {
        return $( '<li></li>' )
            .data( 'item.autocomplete', item )
            .append( '<a>' + item.label + '</a>' )
            .appendTo( ul );
    };

    var wrapper = this.wrapper = $( '<span style="width:200px">' )
        .addClass( 'ui-combobox' )
        .insertAfter( input );

    var button = $( '<a style="width:20px; margin-bottom:1px; height:20px;">' )
        .attr( 'tabIndex', -1 )
        .attr( 'title', i18n_show_all_items )
        .appendTo( wrapper )
        .button( {
            icons: {
                primary: 'ui-icon-triangle-1-s'
            },
            text: false
        } )
        .addClass( 'small-button' )
        .click( function () {
            if ( input.autocomplete( 'widget' ).is( ':visible' ) ) {
                input.autocomplete( 'close' );
                return;
            }
            $( this ).blur();
            input.autocomplete( 'search', '' );
            input.focus();
        } );
};

/*
 * get selected period - full object - with start and end dates
 */
dhis2.de.getSelectedPeriod = function()
{
    
    var periodId = $( '#selectedPeriodId').val();
    
    var period = null;
    
    if( periodId && periodId != "" )
    {
        period = dhis2.de.periodChoices[ periodId ];        
    }
    
    return period;
}

/*
 * lock all input filed in data entry form
 */
dhis2.de.lockForm = function()
{
    $( '#contentDiv input').attr( 'readonly', 'readonly' );
    $( '#contentDiv textarea').attr( 'readonly', 'readonly' );
    $( '.sectionFilter').removeAttr( 'disabled' );
    $( '#completenessDiv' ).hide();
    
    $( '#contentDiv input' ).css( 'backgroundColor', '#eee' );
    $( '.sectionFilter' ).css( 'backgroundColor', '#fff' );
}

/*
 * populate section row totals
 */
dhis2.de.populateRowTotals = function(){
    
    if( !dhis2.de.multiOrganisationUnit )
    {
        $("input[id^='row-']").each(function(i, el){
            var ids = this.id.split('-');
            if( ids.length > 2 )
            {
                var de = ids[1], total = new Number();
                for( var i=2; i<ids.length; i++ )
                {
                    var val = $( '#' + de + "-" + ids[i] + "-val" ).val();
                    if( dhis2.validation.isNumber( val ) )
                    {                        
                        total += new Number( val );
                    }                    
                }
                $(this).val( total );
            }            
        });
    }
};

/*
 * populate section column totals
 */
dhis2.de.populateColumnTotals = function(){
    
    if( !dhis2.de.multiOrganisationUnit )
    {
        $("input[id^='col-']").each(function(i, el){            
            
            var $tbody = $(this).closest('.sectionTable').find("tbody");
            var $trTarget = $tbody.find( 'tr');
            
            var ids = this.id.split('-');
            
            if( ids.length > 1 )
            {
                var total = new Number();
                for( var i=1; i<ids.length; i++ )
                {                    
                    $trTarget.each( function( idx, item ) 
                    {
                        var inputs = $( item ).find( '.entryfield' );                        
                        inputs.each( function(k, e){
                            if( this.id.indexOf( ids[i] ) !== -1 && $(this).is(':visible') )
                            {
                                var val = $( this ).val();
                                if( dhis2.validation.isNumber( val ) )
                                {                        
                                    total += new Number( val );
                                }
                            }
                        });
                    } );
                }                
                $(this).val( total );
            }            
        });
    }
};

// -----------------------------------------------------------------------------
// Various
// -----------------------------------------------------------------------------

function printBlankForm()
{
	$( '#contentDiv input, select' ).css( 'display', 'none' );
	window.print();
	$( '#contentDiv input, select' ).css( 'display', '' );	
}


function getUserSetting()
{   
    if ( dhis2.de.isOffline && settings !== null ) {
        return;
    }

    var def = $.Deferred();

    var url = '../api/userSettings.json?key=keyAnalysisDisplayProperty';

    //Gets the user setting for whether it should display short names for org units or not.
    $.getJSON(url, function( data ) {
            console.log("User settings loaded: ", data);
            dhis2.de.storageManager.setUserSettings(data);
            def.resolve();
        }
    );

    return def;
}

function getTimeDelta()
{
    if (dhis2.de.isOffline) {
        return;
    }

    var def = $.Deferred();

    var url = '../api/system/info';

    //Gets the server time delta
    $.getJSON(url, function( data ) {
            serverTimeDelta = new Date(data.serverDate.substring(0,24)) - new Date();
            dhis2.de.storageManager.setServerTimeDelta(serverTimeDelta);
            // if successful, record time of update
            dhis2.de.storageManager.setServerTimeRetrieved(new Date());
            console.log("stored server time delta of " + serverTimeDelta + " ms");
            def.resolve();
        }
    );

    return def;
}

/*add 3 dialog for approval and save comment dataset-wise in dataElement in dataEntry for mizoram-ipa */

function dHTApproved()
{
	//sel["ds"] = $( '#selectedDataSetId' ).val(),
	//sel["pe"] = $( '#selectedPeriodId').val(),
	//sel["ou"] = dhis2.de.currentOrganisationUnitId;	
	$( '#dhtApprovalCommentText' ).html( '' );
	var url = "";
	
	if( $( '#selectedDataSetId' ).val() == 'XV12eKZar28')
	{
		url = '../api/dataValues.json?de=wLKGbYJXIKt&pe=' + $( '#selectedPeriodId').val() + '&ou=' + dhis2.de.currentOrganisationUnitId;
	}
	else if( $( '#selectedDataSetId' ).val() == 'wwcxotLHZGY')
	{
		url = '../api/dataValues.json?de=kxPDBQIDg9e&pe=' + $( '#selectedPeriodId').val() + '&ou=' + dhis2.de.currentOrganisationUnitId;
	}
	//ipa/api/dataValues.json?de=wLKGbYJXIKt&pe=2023Q1&ou=s00pgmqXHcB
	$.getJSON( url, function( json )
	{
		
		if(json[0] != "")
		{
			$( '#dhtApprovalCommentText' ).html( json[0] );
			
			$( '#dhtApprovalCommentText' ).attr( 'disabled', 'disabled' );
			$( '#dhtApprovalComment' ).attr( 'disabled', 'disabled' );
		}
		else
		{
			$( '#dhtApprovalComment' ).removeAttr( 'disabled' );
			$( '#dhtApprovalCommentText' ).removeAttr( 'disabled' );
			//$( '#sHTApprovedButton' ).attr( 'disabled', 'disabled' );
		}

	} );	
		
	$( '#dHTApprovedDiv' ).dialog( {
		modal : true,
		width : 350,
		height : 300,
		title : 'DHT Approval'
	} );
	/*
	if ( dhis2.de.currentCompletedByUser )
	{
		var url = '../api/35/userLookup';

		$.getJSON( url, { query: dhis2.de.currentCompletedByUser }, function( json )
		{
			$( '#userFullName' ).html( json.users[0].displayName );
			$( '#userUsername' ).html( dhis2.de.currentCompletedByUser );
			$( '#firstName' ).html( json.users[0].firstName );
			$( '#surname' ).html( json.users[0].surname );

			$( '#completedByDiv' ).dialog( {
	        	modal : true,
	        	width : 350,
	        	height : 350,
	        	title : 'User'
	    	} );
		} );
	}
*/
	
}

function sHTApproved()
{
	//sel["ds"] = $( '#selectedDataSetId' ).val(),
	//sel["pe"] = $( '#selectedPeriodId').val(),
	//sel["ou"] = dhis2.de.currentOrganisationUnitId;	
	
	$( '#shtApprovalCommentText' ).html( '' );
	var url = "";
	
	if( $( '#selectedDataSetId' ).val() == 'XV12eKZar28')
	{
		url = '../api/dataValues?de=UjFfCQfJuti&pe=' + $( '#selectedPeriodId').val() + '&ou=' + dhis2.de.currentOrganisationUnitId;
	}
	else if( $( '#selectedDataSetId' ).val() == 'wwcxotLHZGY')
	{
		url = '../api/dataValues.json?de=xvkraRAS9AE&pe=' + $( '#selectedPeriodId').val() + '&ou=' + dhis2.de.currentOrganisationUnitId;
	}
	
	//var url = '../api/dataValues?de=UjFfCQfJuti&pe=' + $( '#selectedPeriodId').val() + '&ou=' + dhis2.de.currentOrganisationUnitId;
	//ipa/api/dataValues.json?de=wLKGbYJXIKt&pe=2023Q1&ou=s00pgmqXHcB
	$.getJSON( url, function( json )
	{
		if(json[0] != "")
		{
			$( '#shtApprovalCommentText' ).html( json[0] );
			$( '#shtApprovalComment' ).attr( 'disabled', 'disabled' );
			$( '#shtApprovalCommentText' ).attr( 'disabled', 'disabled' );
		}
		else
		{
			$( '#shtApprovalComment' ).removeAttr( 'disabled' );
			$( '#shtApprovalCommentText' ).removeAttr( 'disabled' );
			//$( '#sHTApprovedButton' ).attr( 'disabled', 'disabled' );
		}
		
	} );	
	
	$( '#sHTApprovedDiv' ).dialog( {
		modal : true,
		width : 350,
		height : 300,
		title : 'SHT Approval'
	} );
	
}


function pMUAccountApproved()
{
	//sel["ds"] = $( '#selectedDataSetId' ).val(),
	//sel["pe"] = $( '#selectedPeriodId').val(),
	//sel["ou"] = dhis2.de.currentOrganisationUnitId;	
	
	$( '#pMUAccountApprovalCommentText' ).html( '' );
	var url = "";
	
	if( $( '#selectedDataSetId' ).val() == 'XV12eKZar28')
	{
		url = '../api/dataValues?de=gi6F0xlOeeX&pe=' + $( '#selectedPeriodId').val() + '&ou=' + dhis2.de.currentOrganisationUnitId;
	}
	else if( $( '#selectedDataSetId' ).val() == 'wwcxotLHZGY')
	{
		url = '../api/dataValues.json?de=fByaGToLvi9&pe=' + $( '#selectedPeriodId').val() + '&ou=' + dhis2.de.currentOrganisationUnitId;
	}
	
	//var url = '../api/dataValues.json?de=gi6F0xlOeeX&pe=' + $( '#selectedPeriodId').val() + '&ou=' + dhis2.de.currentOrganisationUnitId;
	//ipa/api/dataValues.json?de=wLKGbYJXIKt&pe=2023Q1&ou=s00pgmqXHcB
	$.getJSON( url, function( json )
	{
		
		if(json[0] != "")
		{
			$( '#pMUAccountApprovalCommentText' ).html( json[0] );
			$( '#pMUAccountApprovalComment' ).attr( 'disabled', 'disabled' );
			$( '#pMUAccountApprovalCommentText' ).attr( 'disabled', 'disabled' );
		}
		else
		{
			$( '#pMUAccountApprovalComment' ).removeAttr( 'disabled' );
			$( '#pMUAccountApprovalCommentText' ).removeAttr( 'disabled' );
			//$( '#sHTApprovedButton' ).attr( 'disabled', 'disabled' );
		}

	} );
	
	$( '#pMUAccountApprovedDiv' ).dialog( {
		modal : true,
		width : 350,
		height : 300,
		title : 'PMU/Accounts Approval'
	} );	
	
	
}


function closeDialog()
{
	$('#dHTApprovedDiv').dialog('close');
}



function submitApprovalComment( selectedInputId,dialogDivID )
{
	var divID = '#' +dialogDivID;
	
	if($("#"+selectedInputId).val() != "")
	{
		$( "#"+selectedInputId ).attr( 'disabled', 'disabled' );
		
		var de = "";
		if( $( '#selectedDataSetId' ).val() == 'XV12eKZar28')
		{
			if( selectedInputId == 'dhtApprovalCommentText')
			{
				de = "wLKGbYJXIKt";
			}
			else if( selectedInputId == 'shtApprovalCommentText')
			{
				de = "UjFfCQfJuti";
			}
			else if( selectedInputId == 'pMUAccountApprovalCommentText')
			{
				de = "gi6F0xlOeeX";
			}
			
			//url = '../api/dataValues?de=gi6F0xlOeeX&pe=' + $( '#selectedPeriodId').val() + '&ou=' + dhis2.de.currentOrganisationUnitId;
		}
		else if( $( '#selectedDataSetId' ).val() == 'wwcxotLHZGY')
		{
			if( selectedInputId == 'dhtApprovalCommentText')
			{
				de = "kxPDBQIDg9e";
			}
			else if( selectedInputId == 'shtApprovalCommentText')
			{
				de = "xvkraRAS9AE";
			}
			else if( selectedInputId == 'pMUAccountApprovalCommentText')
			{
				de = "fByaGToLvi9";
			}
		}		
		
		var co = "HllvX50cXC0";
		
		//var de = selectedInputId.split("-")[0];
		//var co = selectedInputId.split("-")[1];
		var co = "HllvX50cXC0";
		var ou = dhis2.de.getCurrentOrganisationUnit();
		var pe = $( '#selectedPeriodId').val();
			
		var dataValue = {
			'de' : de,
			'co' : co,
			'ou' : ou,
			'pe' : pe,
			'value' : $("#"+selectedInputId).val()
		};

		var cc = dhis2.de.getCurrentCategoryCombo();
		var cp = dhis2.de.getCurrentCategoryOptionsQueryValue();
		
		if ( cc && cp )
		{
			dataValue.cc = cc;
			dataValue.cp = cp;
		}
		//console.log( " dataValue - " + dataValue );
		
		   $.ajax( {
			url: '../api/dataValues',
			data: dataValue,
			type: 'post',
			success: handleSuccess,
			error: handleError
		} );
		
		
	   function handleSuccess()
		{
			console.log( " SUCESS - " + $("#"+selectedInputId).val() );
			alert( " Data Approved " );
			//$('#form-dialog').dialog('close');
			//$('dHTApprovedDiv').dialog({modal : false});
			//$( 'dHTApprovedDiv' ).hide();
			
			$(divID).dialog('close');
			//$('#dHTApprovedDiv').dialog('close');
			//$('#sHTApprovedDiv').dialog('close');
			//$('#pMUAccountApprovedDiv').dialog('close');
		}

		function handleError( xhr, textStatus, errorThrown )
		{
			
			if ( 409 == xhr.status || 500 == xhr.status ) // Invalid value or locked
			{
				console.log( " ERROR - " + $("#"+selectedInputId).val() );
				alert( " error to save" );
				$(divID).dialog('close');
			}
			else // Offline, keep local value
			{
				console.log( " ERROR - " + $("#"+selectedInputId).val() );
				alert( " error to save" );
			}
		}	
	}
	else
	{
		alert( " Please enter comment " );
	}

}


function submitReturnComment( selectedInputId,dialogDivID )
{
	var divID = '#' +dialogDivID;
	
	if($("#"+selectedInputId).val() != "")
	{
		$( "#"+selectedInputId ).attr( 'disabled', 'disabled' );
		
		var de = "";
		if( $( '#selectedDataSetId' ).val() == 'XV12eKZar28')
		{
			if( selectedInputId == 'dhtApprovalCommentText')
			{
				de = "wLKGbYJXIKt";
			}
			else if( selectedInputId == 'shtApprovalCommentText')
			{
				de = "UjFfCQfJuti";
			}
			else if( selectedInputId == 'pMUAccountApprovalCommentText')
			{
				de = "gi6F0xlOeeX";
			}
			
			//url = '../api/dataValues?de=gi6F0xlOeeX&pe=' + $( '#selectedPeriodId').val() + '&ou=' + dhis2.de.currentOrganisationUnitId;
		}
		else if( $( '#selectedDataSetId' ).val() == 'wwcxotLHZGY')
		{
			if( selectedInputId == 'dhtApprovalCommentText')
			{
				de = "kxPDBQIDg9e";
			}
			else if( selectedInputId == 'shtApprovalCommentText')
			{
				de = "xvkraRAS9AE";
			}
			else if( selectedInputId == 'pMUAccountApprovalCommentText')
			{
				de = "fByaGToLvi9";
			}
		}		
		
		var co = "HllvX50cXC0";
		
		//var de = selectedInputId.split("-")[0];
		//var co = selectedInputId.split("-")[1];
		var co = "HllvX50cXC0";
		var ou = dhis2.de.getCurrentOrganisationUnit();
		var pe = $( '#selectedPeriodId').val();
			
		var dataValue = {
			'de' : de,
			'co' : co,
			'ou' : ou,
			'pe' : pe,
			'value' : $("#"+selectedInputId).val()
		};

		var cc = dhis2.de.getCurrentCategoryCombo();
		var cp = dhis2.de.getCurrentCategoryOptionsQueryValue();
		
		if ( cc && cp )
		{
			dataValue.cc = cc;
			dataValue.cp = cp;
		}
		//console.log( " dataValue - " + dataValue );
		
		   $.ajax( {
			url: '../api/dataValues',
			data: dataValue,
			type: 'post',
			success: handleSuccess,
			error: handleError
		} );
		
		
	   function handleSuccess()
		{
			console.log( " SUCESS - " + $("#"+selectedInputId).val() );
			//alert( " Data Return " );
			
			registerCompleteDataSet(false);
			//var dataSetId = $( '#selectedDataSetId' );
			//var periodId = $( '#selectedPeriodId');
			//alert( $( "#selectedDataSetId option:selected" ).text() + " -- " + $( "#selectedPeriodId option:selected" ).text() );
			
			//$( "#selectedDataSetId option:selected" ).text();
			
			sendEmailForReturn(  $("#"+selectedInputId).val() )
			
			//$('#form-dialog').dialog('close');
			//$('dHTApprovedDiv').dialog({modal : false});
			//$( 'dHTApprovedDiv' ).hide();
			
			$(divID).dialog('close');
			//$('#dHTApprovedDiv').dialog('close');
			//$('#sHTApprovedDiv').dialog('close');
			//$('#pMUAccountApprovedDiv').dialog('close');
		}

		function handleError( xhr, textStatus, errorThrown )
		{
			
			if ( 409 == xhr.status || 500 == xhr.status ) // Invalid value or locked
			{
				console.log( " ERROR - " + $("#"+selectedInputId).val() );
				registerCompleteDataSet(true)
				alert( " error to save" );
				$(divID).dialog('close');
			}
			else // Offline, keep local value
			{
				console.log( " ERROR - " + $("#"+selectedInputId).val() );
				alert( " error to save" );
			}
		}	
	}
	else
	{
		alert( " Please enter comment " );
	}

}


function sendEmailForReturn(  emailText ){

	// cmoaizawleast@gmail.com -- Aizawl East
	// dhtazlwest@gmail.com -- Aizawl West
	//alert(" inside send e-mail ");
	//var tempARVDrugStock = document.getElementById("SGdBfj0GEMJ-kdsirVNKdhm-val").value;
	var ou = dhis2.de.getCurrentOrganisationUnit();
	//alert(tempARVDrugStock + " -- " + ou);
	//console.log(" ou " + ou.name);
	//if(tempARVDrugStock === "true"){
		$.ajax({
			type: "GET",
			async: false,
			dataType: "json",
			contentType: "application/json",
			url: '../api/organisationUnits/' +ou + '.json?fields=id,name,parent[id,name,parent[id,name,email]]',
	
			success: function (orgUnitResponse) {

				var tempOrgUnitParentParentEmail = orgUnitResponse.parent.parent.email;
				//alert( " email -- " + tempOrgUnitParentParentEmail );
				
				var orgUnitName = document.getElementById("selectedOrganisationUnit").value;
				var completeEmailText = emailText + " for OrganisationUnit " + orgUnitName + " and Data Set " + $( "#selectedDataSetId option:selected" ).text() + " and period " + $( "#selectedPeriodId option:selected" ).text()
				
				//alert( $( "#selectedDataSetId option:selected" ).text() + " -- " + $( "#selectedPeriodId option:selected" ).text() );
				
				var tempURL = "email/notification?recipients=" + tempOrgUnitParentParentEmail + "&message=" + completeEmailText + "&subject=" + orgUnitName;
				
						
				$.ajax({
					url: '../api/' + tempURL,
					type: 'POST',
					success: handleSuccess,
					error: handleError
				});
	
			   function handleSuccess()
			   {
					console.log(" email send to " + tempOrgUnitParentParentEmail );
			   }

			   function handleError( xhr, textStatus, errorThrown )
			   {
					if ( 409 == xhr.status || 500 == xhr.status ) // Invalid value or locked
					{
						console.log( " ERROR to email send " );
					}
					else // Offline, keep local value
					{
						console.log( " ERROR - to email send" );
					}
				}
				
			},
			error: function (orgUnitResponse) {
				console.log(  " response: " + JSON.stringify(orgUnitResponse) );
				deferred.resolve(orgUnitResponse);
			},
			warning: function (orgUnitResponse) {
				console.log(  " response: " + JSON.stringify(orgUnitResponse) );
				deferred.resolve(orgUnitResponse);
			}
		});
}

/*
    $( '#contentDiv input').attr( 'readonly', 'readonly' );
    $( '#contentDiv textarea').attr( 'readonly', 'readonly' );
    $( '.sectionFilter').removeAttr( 'disabled' );
    $( '#completenessDiv' ).hide();
    
    $( '#contentDiv input' ).css( 'backgroundColor', '#eee' );
    $( '.sectionFilter' ).css( 'backgroundColor', '#fff' );

$('#contentDiv input').attr('disabled','disabled');


        $( '#contentDiv input' ).removeAttr( 'readonly' );
        $( '#contentDiv textarea' ).removeAttr( 'readonly' );
		$( '#completenessDiv' ).show();
		
		 "#selectedDataSetId option:selected" ).text() + " -- " + $( "#selectedPeriodId option:selected" ).text()
*/		