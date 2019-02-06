package org.hisp.dhis.webapi.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.hisp.dhis.common.DhisApiVersion;
import org.hisp.dhis.dxf2.webmessage.WebMessageException;
import org.hisp.dhis.dxf2.webmessage.WebMessageUtils;
import org.hisp.dhis.googlesheet.GoogleSheetConfig;
import org.hisp.dhis.webapi.mvc.annotation.ApiVersion;
import org.hisp.dhis.webapi.service.WebMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
/**
 * @author Mithilesh Kumar Thakur
 */

@Controller
@RequestMapping( value = GoogleSheetController.RESOURCE_PATH )
@ApiVersion( { DhisApiVersion.DEFAULT, DhisApiVersion.ALL } )
public class GoogleSheetController
{
    public static final String RESOURCE_PATH = "/googleSheet";
    
    private static final String CREDENTIALS_FILE_PATH = "DHIS2 CHND PHC 21-461d051b611f.p12";
    
    //private static SpreadsheetService spreadsheetService = null;
    
    private String APPLICATION_NAME = "DHIS2 CHND PHC 21" ;

    private String SERVICE_ACCOUNT = "ward-21@dhis2-chnd-phc-21.iam.gserviceaccount.com";

    //private String CREDENTIALS_FILE_PATH;

    private String SPREAD_SHEET_ID = "1i2XnV-poR5s3jZ6lUzAKpx3ANX4HmhJE_5snNsV-YHc";

    // ---------------------------------------------------------------------
    // Dependencies
    // ---------------------------------------------------------------------
    
    @Autowired
    private WebMessageService messageService;
    
    private static String inputTemplatePath = "";
    private GoogleSheetConfig googleSheetConfig;
    
    // ---------------------------------------------------------------------
    // POST
    // ---------------------------------------------------------------------
    
    @PreAuthorize( "hasRole('ALL')" )
    @RequestMapping( method = RequestMethod.POST )
    @ResponseStatus( HttpStatus.CREATED )
    public void pushDataInGoogleSheet(
        @RequestParam( required = true ) String scriptId,
        @RequestParam( required = true ) String mobileNumbers, HttpServletResponse response )
        throws WebMessageException
    {

        // ---------------------------------------------------------------------
        // Input validation
        // ---------------------------------------------------------------------

        if ( scriptId == null )
        {
            throw new WebMessageException( WebMessageUtils.notFound( "The scriptId not found" ) );
        }
        
        if ( mobileNumbers == null )
        {
            throw new WebMessageException( WebMessageUtils.notFound( "The mobileNumbers not found" ) );
        }
        
        if( scriptId != null && mobileNumbers != null && mobileNumbers.length() > 0 )
        {
            
            List<String> mobileNumberList = new ArrayList<String>(Arrays.asList( mobileNumbers.split(",") ) );
            
            System.out.println( "In Side pushTeiDataInGoogleSheet " );
            
            inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + CREDENTIALS_FILE_PATH;
            
            try
            {
                UpdateValuesResponse updateResponse = pushTeiDataInGoogleSheet( scriptId, mobileNumberList );
                response.setStatus( HttpServletResponse.SC_CREATED );
                messageService.sendJson( WebMessageUtils.created( updateResponse + "Data pushed in google sheet successfully" ), response );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                System.out.println( "Error in pushing google sheet " + e.getMessage() );
            }
            
        }
        
    }

    
    // -------------------------------------------------------------------------
    // Support methods
    // -------------------------------------------------------------------------
    
    public UpdateValuesResponse pushTeiDataInGoogleSheet( String scriptId, List<String> mobileNumberList )
        throws Exception
    {
        System.out.println( "In Side pushTeiDataInGoogleSheet " );
        
        UpdateValuesResponse updateResponse = null;
        //String inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + CREDENTIALS_FILE_PATH;
        
        googleSheetConfig = new GoogleSheetConfig();
        googleSheetConfig.setSPREAD_SHEET_ID( SPREAD_SHEET_ID );
        googleSheetConfig.setAPPLICATION_NAME(  APPLICATION_NAME );
        googleSheetConfig.setSERVICE_ACCOUNT( SERVICE_ACCOUNT );
        googleSheetConfig.setCREDENTIALS_FILE_PATH( inputTemplatePath);
        
        googleSheetConfig.clear();
        System.out.println( "clear sheet  --  " );
        
        List<List<Object>> fullData = new ArrayList<>();
        
        /*
        fullData = new ArrayList<>( googleSheetConfig.read() );
        System.out.println( "read sheet  --  " + fullData );
        for( List<Object> test :  fullData )
        {
            System.out.println( "test  --  " + test );
            for( int i = 0; i < test.size(); i++ )
            {
                System.out.println( "obj"+i+ " - " + test.get( i ) );
            }
        }
        */
        
        if( mobileNumberList != null && mobileNumberList.size() > 0 )
        {
            for( String mobileNumber : mobileNumberList )
            {
                if( !mobileNumber.equalsIgnoreCase( "NA" ) && mobileNumber.length() == 10 )
                {
                    mobileNumber = "91"+mobileNumber;
                    List<Object> data = new ArrayList<>();
                    data.add( mobileNumber );
                    data.add( scriptId );
                    fullData.add( data );
                }
                
            }
            
            ValueRange valueRange = new ValueRange();
            valueRange.setValues( fullData );
            
            System.out.println( "fullData --  " + fullData.size() );

            Sheets service = googleSheetConfig.getService();
            //Sheets service = getService();
            System.out.println( "service --  " + service.getApplicationName() );
            
            if ( service != null )
            {
                /*
                service.spreadsheets().values()
                    .update( googleSheetConfig.getSPREAD_SHEET_ID(), "Sheet1!A2:L10000000", valueRange )
                    .setValueInputOption( "RAW" );
                */
                
                Sheets.Spreadsheets.Values.Update updateRequest = service.spreadsheets().values().update(  googleSheetConfig.getSPREAD_SHEET_ID(), "Sheet1!A2:L10000000", valueRange );
                updateRequest.setValueInputOption( "RAW" );
                
                updateResponse = updateRequest.execute();
                System.out.println( "Update Response -- " + updateResponse );
            }
        }
        
        return updateResponse;
        
    }
}