function moveUpDataElement( selectedListName )
{
	var selectedList = jQuery("#"+selectedListName);

	jQuery("#"+selectedListName +" option").each( function( i, item ){
		item = jQuery(item);
		if( item.is(':selected') )
		{
			var prev = item.prev('#'+selectedListName+' option');
			if (prev.length == 1) 
			{ 
				prev.before(item);
			}
		}
	});
}

function moveDownDataElement( selectedListName )
{
	var selectedList = jQuery("#"+selectedListName);
	var items = new Array();
	jQuery("#"+selectedListName +" option").each( function( i, item ){
		items.push(jQuery(item));
	});
	
	for( var i=items.length-1;i>=0;i--)
	{	
		var item = items[i];
		if( item.is(':selected'))
		{
			var next = item.next('#'+selectedListName+' option');
			if (next.length == 1) 
			{ 
				next.after(item);
			}
		}
	}
}