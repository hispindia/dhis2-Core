var tmpLookupId;
var tmpSource;

// -----------------------------------------------------------------------------
// DataSet details form
// -----------------------------------------------------------------------------

function showlookupDetails(context) {

	jQuery.getJSON('getLookup.action', {
		id : context.id
	}, function(json) {
		setInnerHTML('nameField', json.lookup.name);
		setInnerHTML('codeField', json.lookup.code);
		setInnerHTML('descriptionField', json.lookup.description);
		setInnerHTML('typeField', json.lookup.type);
		setInnerHTML('valueField', json.lookup.value);

		showDetails();
	});
}

// -----------------------------------------------------------------------------
// Delete DataSet
// -----------------------------------------------------------------------------

function removeLookup( id, name ) {

	//removeItem(id, name, 'Value Deleted','delLookup.action');
	location.href = 'delLookup.action?id=' + id;
}

// ----------------------------------------------------------------------
// DataEntryForm
// ----------------------------------------------------------------------

function editLookupForm(id) {
	location.href = 'editLookupForm.action?lookupId=' + id;
}