
// -----------------------------------------------------------------------------
// select data-elements
// -----------------------------------------------------------------------------

function selectDataElements()
{
	var selectedList = jQuery("#selectedList");
	jQuery("#availableList").children().each(function(i, item){
		if( item.selected ){
			html = "<tr class='selectedTr' title='" + item.title + "' data-id = '" + $(this).data("id") +"'  id='" + item.value + "' ondblclick='unSelectDataElement( this )'><td onmousedown='select(event,this)'>" + item.text + "</td>";
			html += "<td align='center'><input type='checkbox' name='value' checked></td>";
			html += "<td align='center'><input type='checkbox' name='comment' checked></td>";
			html += "</tr>";
			selectedList.append( html );
			jQuery( item ).remove();
		}
	});
}


function selectAllDataElements()
{
	var selectedList = jQuery("#selectedList");
	jQuery("#availableList").children().each(function(i, item){
		html = "<tr class='selectedTr' title='" + item.title + "' data-id = '" + $(this).data("id") +"' id='" + item.value + "' ondblclick='unSelectDataElement( this )'><td onmousedown='select(event,this)'>" + item.text + "</td>";
		html += "<td align='center'><input type='checkbox' name='value' checked></td>";
		html += "<td align='center'><input type='checkbox' name='comment' checked></td>";
		html += "</tr>";
		selectedList.append( html );
		jQuery( item ).remove();
	});
}

function unSelectDataElements()
{
	var availableList = jQuery("#availableList");
	jQuery("#selectedList").find("tr").each( function( i, item ){
		item = jQuery(item);
		if( item.hasClass("selectedTr") )
		{		
			availableList.append( "<option title='" + item.attr("title") + "' data-id='"+item.attr("data-id")+"' value='" + item.attr( "id" ) + "' selected='true'>" + item.find("td:first").text() + "</option>" );
			item.remove();
		}
	});
}


function unSelectAllDataElements()
{
	var availableList = jQuery("#availableList");
	jQuery("#selectedList").find("tr").each( function( i, item ){
		item = jQuery(item);
		availableList.append( "<option title='" + item.attr("title") + "' data-id='"+item.attr("data-id")+"' value='" + item.attr( "id" ) + "' selected='true'>" + item.find("td:first").text() + "</option>" );
		item.remove();
	});
}

//-----------------------------------------------------------------------------
//Move Table Row Up and Down
//-----------------------------------------------------------------------------

function moveUpDataElement()
{
	var selectedList = jQuery("#selectedList");

	jQuery("#selectedList").find("tr").each( function( i, item ){
		item = jQuery(item);
		if( item.hasClass("selectedTr") )
		{
			var prev = item.prev('#selectedList tr');
			if (prev.length == 1) 
			{ 
				prev.before(item);
			}
		}
	});
}

function moveDownDataElement()
{
	var selectedList = jQuery("#selectedList");
	var items = new Array();
	jQuery("#selectedList").find("tr").each( function( i, item ){
		items.push(jQuery(item));
	});
	
	for( var i=items.length-1;i>=0;i--)
	{	
		var item = items[i];
		if( item.hasClass("selectedTr") )
		{
			var next = item.next('#selectedList tr');
			if (next.length == 1) 
			{ 
				next.after(item);
			}
		}
	}
}

function unSelectDataElement( element )
{
	element = jQuery(element);	
	jQuery("#availableList").append( "<option title='" + element.attr("title") + "' data-id = '" + element.attr("data-id") +"' value='" + element.attr( "id" ) + "' selected='true'>" + element.find("td:first").text() + "</option>" );
	element.remove();
}

function select( event, element )
{
	if ( !getKeyCode( event ) )// Ctrl
	{
		jQuery("#selectedList .selectedTr").removeClass( 'selectedTr' );
	}
	
	element = jQuery( element ).parent();
	if( element.hasClass( 'selectedTr') ) element.removeClass( 'selectedTr' );
	else element.addClass( 'selectedTr' );
}

function getKeyCode(e)
{
	var ctrlPressed=0;

	if (parseInt(navigator.appVersion)>3) {

		var evt = e ? e:window.event;

		if (document.layers && navigator.appName=="Netscape"
		&& parseInt(navigator.appVersion)==4) {
			// NETSCAPE 4 CODE
			var mString =(e.modifiers+32).toString(2).substring(3,6);
			ctrlPressed =(mString.charAt(1)=="1");
		}
		else {
			// NEWER BROWSERS [CROSS-PLATFORM]
			ctrlPressed=evt.ctrlKey;
		}
	}
	return ctrlPressed;
}
