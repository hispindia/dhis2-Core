package org.hisp.dhis.googlesheet;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.scheduling.AbstractJob;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.setting.SettingKey;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

/**
 * @author Mithilesh Kumar Thakur
 */
public class ScheduleGoogleSheetTask  extends AbstractJob
{
    private static final Log log = LogFactory.getLog( ScheduleGoogleSheetTask.class );
    
    public static final String KEY_TASK = "scheduleGoogleSheetTask";

    private static final String CREDENTIALS_FILE_PATH = "DHIS2 CHND PHC 21-461d051b611f.p12";
    //private static final String CREDENTIALS_FILE_PATH = "DHIS2CHNDPHC21-461d051b611f.p12";
    
    private String APPLICATION_NAME = "DHIS2 CHND PHC 21" ;

    private String SERVICE_ACCOUNT = "ward-21@dhis2-chnd-phc-21.iam.gserviceaccount.com";

    private String SPREAD_SHEET_ID = "1-qsHZjYJWxswKKbsTuiGTImYZSXR7eYzw_7XgzSkBvE";
    
    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    
    private final static int MOBILE_NUMBER_ATTRIBUTE_ID = 142;

    private final static int NPCDCS_FOLLOW_UP_PROGRAM_STAGE_ID = 133470;
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private SystemSettingManager systemSettingManager;
    
    @Autowired
    private TrackedEntityInstanceService trackedEntityInstanceService;

    @Autowired
    private TrackedEntityAttributeValueService trackedEntityAttributeValueService;

    @Autowired
    private TrackedEntityAttributeService trackedEntityAttributeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
    public String getAPPLICATION_NAME()
    {
        return APPLICATION_NAME;
    }

    public String getSPREAD_SHEET_ID()
    {
        return SPREAD_SHEET_ID;
    }

    public JsonFactory getJsonFactory()
    {
        return JSON_FACTORY;
    }

    
    GoogleSheetConfig googleSheetConfig;
    
    static String inputTemplatePath = "";
    
    private SimpleDateFormat simpleDateFormat;
    
    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    @Override
    public JobType getJobType()
    {
        return JobType.SCHEDULE_PUSH_IN_GOOGLE_SHEET;
    }

    @Override
    public void execute( JobConfiguration jobConfiguration )
    {
        System.out.println( "INFO: scheduler Push Data in Google Sheet job has started at : " + new Date() + " -- " + JobType.SCHEDULE_PUSH_IN_GOOGLE_SHEET );
        boolean isSchedulePushDataInGoogleSheetJobEnabled = (Boolean) systemSettingManager.getSystemSetting( SettingKey.SCHEDULE_PUSH_IN_GOOGLE_SHEET );
        System.out.println( "isScheduleCustomeSMSJobEnabled -- " + isSchedulePushDataInGoogleSheetJobEnabled );

        if ( !isSchedulePushDataInGoogleSheetJobEnabled )
        {
            log.info( String.format( "%s aborted. Schedule Push Data in Google Sheet Job are disabled", KEY_TASK ) );

            return;
        }

        log.info( String.format( "%s has started", KEY_TASK ) );

        inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + CREDENTIALS_FILE_PATH;
        
        try
        {
            //testDhis2SampleSheet();
            pushTeiDataInGoogleSheet();

        }
        catch ( IOException e1 )
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            System.out.println( "Error SMS " + e1.getMessage() );
        }
        catch ( GeneralSecurityException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    // -------------------------------------------------------------------------
    // Support methods
    // -------------------------------------------------------------------------
    
    public void pushTeiDataInGoogleSheet()
        throws Exception
    {
        System.out.println( "In Side pushTeiDataInGoogleSheet " );
        
        //String inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + CREDENTIALS_FILE_PATH;
        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        googleSheetConfig = new GoogleSheetConfig();
        googleSheetConfig.setSPREAD_SHEET_ID( SPREAD_SHEET_ID );
        googleSheetConfig.setAPPLICATION_NAME(  APPLICATION_NAME );
        googleSheetConfig.setSERVICE_ACCOUNT( SERVICE_ACCOUNT );
        googleSheetConfig.setCREDENTIALS_FILE_PATH( inputTemplatePath);
        
        googleSheetConfig.clear();
        System.out.println( "clear sheet  complete " );
        addDataInSheet();
       
    }
    
    public void addDataInSheet()
        throws IOException
    {
        List<String> customString = new ArrayList<String>( getTrackedEntityInstanceAttributeValueByAttributeId( 5762 ) );
        System.out.println( "List Size --  " + customString.size() );
        
        List<List<Object>> fullData = new ArrayList<>();
        
        for( String customStr : customString )
        {
            List<Object> data = new ArrayList<>();
            
            data.add( customStr.split( ":" )[0] );
            data.add( customStr.split( ":" )[1] );
            data.add( customStr.split( ":" )[2] );
            data.add( customStr.split( ":" )[3] );
            fullData.add( data );
            
            /*
            if( teiName != null && teiSex != null && teiAge != null )
            {
                System.out.println( "data" + " : " + mctsNumber.split( ":" )[1] + " : " + teiName + " : " + teiSex + " : " + teiAge);
                
                data.add( mctsNumber.split( ":" )[1] );
                data.add( teiName );
                data.add( teiSex );
                data.add( teiAge );
                fullData.add( data );
            }
            */
            
        }
 
        ValueRange valueRange = new ValueRange();
        valueRange.setValues( fullData );
        
        System.out.println( "fullData --  " + fullData.size() );

        Sheets service = googleSheetConfig.getService();
        //Sheets service = getService();
        System.out.println( "service --  " + service.getApplicationName() );
        
        if ( service != null )
        {
            service.spreadsheets().values()
                .update( getSPREAD_SHEET_ID(), "Sheet1!A2:L10000000", valueRange )
                .setValueInputOption( "RAW" ).execute();
            
            /*
            service.spreadsheets().values()
                .update( googleSheetConfig.getSPREAD_SHEET_ID(), "Sheet1!A2:L10000000", valueRange )
                .setValueInputOption( "RAW" );
            */
            
            Sheets.Spreadsheets.Values.Update updateRequest = service.spreadsheets().values().update(  googleSheetConfig.getSPREAD_SHEET_ID(), "Sheet1!A2:L10000000", valueRange );
            updateRequest.setValueInputOption( "RAW" );
            
            UpdateValuesResponse updateResponse = updateRequest.execute();
            System.out.println( "Update Response -- " + updateResponse );
        }
    }
    // --------------------------------------------------------------------------------
    // Get TrackedEntityInstance Ids from tracked entity attribute value
    // --------------------------------------------------------------------------------
    
    public List<String> getTrackedEntityInstanceAttributeValueByAttributeId( Integer attributeId )
    {
        List<String> customeString = new ArrayList<String>();
        
        try
        {
            String query = "SELECT pi.trackedentityinstanceid, psi.organisationunitid, psi.duedate::date,teav.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentityattributevalue teav ON teav.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE psi.programstageid = "  + NPCDCS_FOLLOW_UP_PROGRAM_STAGE_ID  + " AND psi.status = 'SCHEDULE' and  "
                + "teav.trackedentityattributeid =  " + MOBILE_NUMBER_ATTRIBUTE_ID;

            //System.out.println( "query: " + query );
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiID = rs.getInt( 1 );
                Integer orgUnitID = rs.getInt( 2 );
                String dueDate = rs.getString( 3 );
                String mobileNo = rs.getString( 4 );
                
                if ( teiID != null && orgUnitID != null && dueDate != null && mobileNo != null
                    && mobileNo.length() == 10 )
                {
                    Date dueDateObject = simpleDateFormat.parse( dueDate );
                    String dueDateString = simpleDateFormat.format( dueDateObject );

                    customeString.add( mobileNo + ":" + teiID + ":" + dueDateString + ":" + orgUnitID );
                }
            }

            return customeString;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }    
}
