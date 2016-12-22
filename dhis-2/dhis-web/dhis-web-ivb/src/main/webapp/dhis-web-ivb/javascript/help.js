
$( document ).ready( function()
{	
	var icons = {
			header: "ui-icon-circle-arrow-e",
			headerSelected: "ui-icon-circle-arrow-s"
		};
    $.get( 
       'getHelpItems.action',
       function( data )
       {
           $( "div#helpMenu" ).html( data );
           $( "div#helpMenu" ).accordion({
   			icons: icons,
   			autoHeight: false,
   			autoWeight: false,
   		});
       } );
} );

function getHelpItemContent( id )
{
	$.get( 
       'getHelpContent.action',
       { "id": id },
       function( data )
       {
           $( "div#helpContent" ).html( data );
       } );
}
