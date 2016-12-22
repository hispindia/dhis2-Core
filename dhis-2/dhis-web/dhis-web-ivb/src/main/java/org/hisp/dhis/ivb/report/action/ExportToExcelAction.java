package org.hisp.dhis.ivb.report.action;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class ExportToExcelAction
    implements Action
{

	/*
	@Autowired
	private LookupService lookupService;
	*/
	
    // -------------------------------------------------------------------------
    // Input & output
    // -------------------------------------------------------------------------

    private InputStream inputStream;

    public InputStream getInputStream()
    {
        return inputStream;
    }

    private String fileName;

    public String getFileName()
    {
        return fileName;
    }

    private String htmlCode;

    public void setHtmlCode( String htmlCode )
    {
        this.htmlCode = htmlCode;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
    {     
        fileName = "Output.xls"; 
        String HTML_A_TAG_PATTERN = "<a\\b[^>]*href=\"[^>]*>";
        
        htmlCode = htmlCode.replaceAll( HTML_A_TAG_PATTERN, "" );
        
        htmlCode = htmlCode.replaceAll( "</a>", "" );
        
        String HTML_IMG_TAG_PATTERN = "(<img\\b[^>]*\\bsrc\\s*=\\s*)([\"\'])((?:(?!\\2)[^>])*)\\2(\\s*[^>]*>)";  
           
        htmlCode = htmlCode.replaceAll( HTML_IMG_TAG_PATTERN, "" ); 
        
        htmlCode = htmlCode.replaceAll( "<table", "<table border=\'1\'" );
        
        //System.out.println( "*************************************************************" );
        //System.out.println( htmlCode );
        //System.out.println( "*************************************************************" );
        
        
        String header = "<html><head><title>IVB Data Repository</title><meta http-equiv='Content-Type' content='text/html; charset=utf-8' /></head><body>";
        String footer = "</body></html>";
       
        htmlCode = header + htmlCode + footer;
        
        inputStream = new BufferedInputStream( new ByteArrayInputStream( htmlCode.getBytes("UTF-8") ) );
        
        /*
        Lookup lookup = lookupService.getLookupByName( "EXPORT_EXCEL" );
        lookup.setValue( htmlCode );
        lookupService.updateLookup( lookup );
        */
        
        return SUCCESS;
    }
}
