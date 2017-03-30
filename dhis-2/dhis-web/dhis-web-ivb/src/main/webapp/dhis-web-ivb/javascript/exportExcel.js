	
var tablesToExcel = (function() {
var uri ='data:application/vnd.ms-excel;base64,', tmplWorkbookXML ='<?xml version="1.0"?><?mso-application progid="Excel.Sheet"?><Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet" xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">'+'<DocumentProperties xmlns="urn:schemas-microsoft-com:office:office"><Author>ThaiND</Author><Created>{created}</Created></DocumentProperties>'+'<Styles><Style ss:ID="Default" ss:Name="Normal"><Alignment ss:Horizontal="Center" ss:WrapText="1"/><Borders><Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/><Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/><Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/><Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/></Borders><Font/><Interior /><NumberFormat /><Protection /></Style><Style ss:ID="s21"><Alignment ss:WrapText="1" ss:Horizontal="Center" ss:Vertical="Center"/><Font ss:Size="10" ss:Bold="1" /><Interior ss:Color="#999999" ss:Pattern="Solid" /><Borders><Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/><Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/><Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/><Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/></Borders></Style> <Style ss:ID="s22"><Alignment ss:WrapText="1" ss:Horizontal="Center" ss:Vertical="Center"/><Font ss:Size="10" ss:Bold="1" /><Interior ss:Color="#999999" ss:Pattern="Solid" /><Borders><Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/><Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/><Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/><Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/></Borders></Style><Style ss:ID="s64"> <Alignment ss:WrapText="1" ss:Horizontal="Center" ss:Vertical="Center" ss:Indent="0" ss:Rotate="90"/><Font ss:Size="10" ss:Bold="1" /><Interior ss:Color="#999999" ss:Pattern="Solid" /> </Style> </Styles>'+'{worksheets}</Workbook>', tmplWorksheetXML ='<Worksheet ss:Name="{nameWS}"><Table><Column ss:AutoFitWidth="0" ss:Width="100"/><Column ss:AutoFitWidth="0" ss:Width="150"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/><Column ss:AutoFitWidth="0" ss:Width="80"/>{rows}</Table></Worksheet>', tmplCellXML ='<Cell ss:StyleID="{sid}" ss:MergeAcross="{mrg}"><Data ss:Type="{dtype}">{data}</Data></Cell>', base64 = function(s) { return window.btoa(unescape(encodeURIComponent(s))) }
    , format = function(s, c) { return s.replace(/{(\w+)}/g, function(m, p) { return c[p]; }) }
	
    return function(tables, wsnames, wbname) {
		var ctx = "";
		var workbookXML = "";
		var worksheetsXML = "";
		var rowsXML = "";
		
		for (var i = 0; i < tables.length; i++) 
		{
		
			if (!tables[i].nodeType) 
			tables[i] = document.getElementById(tables[i]);
		
			for (var j = 0; j < tables[i].rows.length; j++) 
			{
				rowsXML +='<Row>';
				for (var k = 0; k < tables[i].rows[j].cells.length; k++) 
				{
					
					var cols=tables[i].rows[j].cells[k].getAttribute("colspan");
					var rws=tables[i].rows[j].cells[k].getAttribute("rowspan");
					var styl=tables[i].rows[j].cells[k].getAttribute("bgcolor");
					var styl2=tables[i].rows[j].getAttribute("bgcolor");
					var cls=tables[i].rows[j].cells[k].getAttribute("class");
					
						var dataType ='String';
						var dataValue = tables[i].rows[j].cells[k].innerHTML;
						var typeD="";
						var parsed=parseInt(dataValue);
						
						if (parsed==dataValue)
						{
							typeD="Number";
						}
						else
						{
							typeD="String";
						}
					
					
					
					if(styl2)
					{
						if(cols)
						{
							if(cls=="aso")
							{
								ctx = {data: dataValue, mrg: cols-1, sid:"s64",dtype:typeD};
								rowsXML += format(tmplCellXML, ctx);
							}
							else
							{
								ctx = {data: dataValue, mrg: cols-1, sid:"s22",dtype:typeD};
								rowsXML += format(tmplCellXML, ctx);
							}
						}

						else
						{
							if(cls=="aso")
							{
								ctx = {data: dataValue, mrg: 0, sid:"s64",dtype:typeD};
								rowsXML += format(tmplCellXML, ctx);
							}
							else
							{
								ctx = {data: dataValue, mrg: 0, sid: "s22",dtype:typeD};
								rowsXML += format(tmplCellXML, ctx);
							}
						}
					}
				
					else if(styl)
					{
						if(cols)
						{
							ctx = {data: dataValue, mrg: cols-1, sid:"s21",dtype:typeD};
							rowsXML += format(tmplCellXML, ctx);
						}

						else
						{
							ctx = {data: dataValue, mrg: 0, sid: "s21",dtype:typeD};
							rowsXML += format(tmplCellXML, ctx);
						}
					}
					
					else
					{
						if(cols)
						{
							ctx = {data: dataValue, mrg: cols-1, sid:"Default",dtype:typeD};
							rowsXML += format(tmplCellXML, ctx);
						}

						else
						{
							ctx = {data: dataValue, mrg: 0, sid: "Default",dtype:typeD};
							rowsXML += format(tmplCellXML, ctx);
						}
					
					}
					
				}
				  rowsXML +='</Row>'}
			ctx = {rows: rowsXML, nameWS: wsnames[i] ||'Sheet'+ i};
			worksheetsXML += format(tmplWorksheetXML, ctx);
			rowsXML = "";
      }

		ctx = {created: (new Date()).getTime(), worksheets: worksheetsXML};
		workbookXML = format(tmplWorkbookXML, ctx);

		//console.log(workbookXML);

		var link = document.createElement("A");
		link.href = uri + base64(workbookXML);
		link.download = wbname ||'Workbook.xls';
		//link.download = 'abc.xls';
		link.target ='_blank';
		document.body.appendChild(link);
		link.click();
		document.body.removeChild(link);
    }
  })();



jQuery( document ).ready( function() {	
	$("#btnExport").click(function(e) {	
		
		var htmlExcelContent = $('#htmlContent').html();
		$("#exportExcelDiv").html( htmlExcelContent );
		
		$("#exportExcelDiv").find('input').each(function(){
			 
			$(this).replaceWith(function(){
					return $(this).val(); 
			 });
			 
			//alert( $(this).val() );
		});
		
		$("#exportExcelDiv").find('img').remove();
		
		htmlExcelContent = $('#exportExcelDiv').html();
		/*
		$("#exportExcelDiv :input").replaceWith(function(){
			return this.val();
		});
		*/
		alert( htmlExcelContent );
		window.open('data:application/vnd.ms-excel,'+ encodeURIComponent( htmlExcelContent ) );
		e.preventDefault();
	});
	
});