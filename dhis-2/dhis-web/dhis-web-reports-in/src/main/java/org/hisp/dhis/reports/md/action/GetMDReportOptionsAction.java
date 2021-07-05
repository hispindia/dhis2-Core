package org.hisp.dhis.reports.md.action;
/**
 * @author Mithilesh Kumar Thakur
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hisp.dhis.reports.ReportOption;
import org.hisp.dhis.reports.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.opensymphony.xwork2.Action;

public class GetMDReportOptionsAction implements Action
{
    //--------------------------------------------------------------------------
    //  Dependencies
    //--------------------------------------------------------------------------

    @Autowired
    private ReportService reportService;

    /*
    public void setReportService( ReportService reportService )
    {
        this.reportService = reportService;
    }
    */
    
    //--------------------------------------------------------------------------
    //Input/Output
    //--------------------------------------------------------------------------
    
    private String mdReportFileName;
    
    public void setMdReportFileName( String mdReportFileName )
    {
        this.mdReportFileName = mdReportFileName;
    }


    private List<ReportOption> reportOptionList;
    
    public List<ReportOption> getReportOptionList()
    {
        return reportOptionList;
    }
    
    private String raFolderName;
    
    //--------------------------------------------------------------------------
    //  Action Implementation
    //--------------------------------------------------------------------------
    
    public String execute() throws Exception
    {
        raFolderName = reportService.getRAFolderName();
        
        reportOptionList = new ArrayList<ReportOption>();
        if( mdReportFileName != null && !mdReportFileName.equalsIgnoreCase( "" ) )
        {
            reportOptionList = new ArrayList<ReportOption>( getReportOptions() );
        }
        
        return SUCCESS;
    }
 
    // Supportive Methods
    
    private List<ReportOption> getReportOptions( )
    {
        List<ReportOption> reportOptionList = new ArrayList<ReportOption>();
        
        String newpath = "";
        try
        {
            newpath = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName + File.separator + mdReportFileName;
        }
        catch ( NullPointerException npe )
        {
            System.out.println("DHIS_HOME is not set");
            return null;
        }
        
        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse( new File( newpath ) );
            if ( doc == null )
            {
                System.out.println( "There is no MAP XML file in the DHIS2 Home" );
                return null;
            }

            NodeList listOfOption = doc.getElementsByTagName( "option" );
            int totalOptions = listOfOption.getLength();

            for( int s = 0; s < totalOptions; s++ )
            {
                Element element = (Element) listOfOption.item( s );
                String optiontext = element.getAttribute( "optiontext" );
                String optionvalue = element.getAttribute( "optionvalue" );

                optionvalue += "#@#" + optiontext;
                if( optiontext != null && optionvalue != null )
                {
                    ReportOption reportOption = new ReportOption( optiontext, optionvalue );
                    reportOptionList.add( reportOption );
                }
            }// end of for loop with s var
        }// try block end
        catch ( SAXParseException err )
        {
            System.out.println( "** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId() );
            System.out.println( " " + err.getMessage() );
            return null;
        }
        catch ( SAXException e )
        {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
            return null;
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
            return null;
        }
        
        return reportOptionList;
    }
}
