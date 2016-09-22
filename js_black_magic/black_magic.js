function black_magic(){
	var filename = "developer-livepost-export.json";
	window.alert("Swag ");
	$.getJSON( filename, function( data ) {
		  var items = [];
		  $.each( data, function( key, val ) {
			items.push( "<li id='" + key + "'>" + val + "</li>" );
		  });
		 
		  $( "<ul/>", {
			"class": "my-new-list",
			html: items.join( "" )
		  }).appendTo( "body" );
		});
}