package org.hisp.dhis.dataadmin.action.schedulecustomesms;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.scheduling.TaskScheduler;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class ScheduleCustomeSMSAction implements Action
{
    private final static int  SMS_CONSENT_ATTRIBUTE_ID = 2618;
    private final static int  MOBILE_NUMBER_ATTRIBUTE_ID = 2617;
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private OrganisationUnitService organisationUnitService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private TaskScheduler taskScheduler;

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
    private SimpleDateFormat simpleDateFormat;
    private String complateDate = "";
    private Period currentperiod;
    private String trackedEntityInstanceIds = "";
    // -------------------------------------------------------------------------
    // Action
    // -------------------------------------------------------------------------
    
    public String execute() throws Exception
    {
        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        //get current date time with Date()
        Date date = new Date();
        System.out.println(timeFormat.format(date));
        
        String currentDate = simpleDateFormat.format( date ).split( "-" )[2];
        String currentMonth = simpleDateFormat.format( date ).split( "-" )[1];
        String currentYear = simpleDateFormat.format( date ).split( "-" )[0];
        String currentHour = timeFormat.format(date).split( ":" )[0];
        
        System.out.println( currentDate + " --- " + currentMonth + " --- " + currentYear + " --- " + currentHour);
        
        String trackedEntityInstanceIds  = getTrackedEntityInstanceIdsByAttributeId( SMS_CONSENT_ATTRIBUTE_ID );
        List<String> mobileNumbers = new ArrayList<String>();
        if( trackedEntityInstanceIds != null && trackedEntityInstanceIds.length() > 0)
        {
            mobileNumbers = new ArrayList<>( getTrackedEntityInstanceAttributeValueByAttributeIdAndTrackedEntityInstanceIds( MOBILE_NUMBER_ATTRIBUTE_ID, trackedEntityInstanceIds ) );
        }
        
        
        if ( currentDate.equalsIgnoreCase( "16" ) )
        {
            scheduledMonthlySMS( mobileNumbers );
        }
        
        if ( currentDate.equalsIgnoreCase( "01" ) || currentDate.equalsIgnoreCase( "15" ) )
        {
            //scheduledBiMonthlySMS();
        }
       
        if ( currentDate.equalsIgnoreCase( "01" ) || currentDate.equalsIgnoreCase( "08" ) || currentDate.equalsIgnoreCase( "15" ) || currentDate.equalsIgnoreCase( "22" ) || currentDate.equalsIgnoreCase( "29" ))
        {
            //scheduledWeeklySMS();
        }
        
        if ( currentDate.equalsIgnoreCase( "01" ) || currentDate.equalsIgnoreCase( "08" ) || currentDate.equalsIgnoreCase( "15" ) || currentDate.equalsIgnoreCase( "22" ) || currentDate.equalsIgnoreCase( "29" ))
        {
            //scheduledWeeklySMS();
        }
        
        
        return SUCCESS;
    }
    
    // -------------------------------------------------------------------------
    // Support methods 
    // -------------------------------------------------------------------------
    
    // Key Performance Indicators Scheduler
    //@Scheduled(cron="*/2 * * * * MON-FRI")
    public void scheduledMonthlySMS( List<String> mobileNumbers ) throws IOException
    {
        System.out.println(" Monthly SMS Scheduler Started at : " + new Date() );
        
        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();
        
       
        
        String message = "Test";
        
        if( mobileNumbers != null && mobileNumbers.size() > 0 )
        {
            for( String mobileNumber : mobileNumbers )
            { 
                //bulkSMSHTTPInterface.sendMessage( message, mobileNumber );
                System.out.println( mobileNumber +  " -------- > " + message );
            }
        }
               
        System.out.println(" Monthly SMS Scheduler Started at : " + new Date() );
    }
        
    
    
    
    
    
    
    
    
    
    
    
    
    
    //--------------------------------------------------------------------------------
    // Get TrackedEntityInstance Ids from tracked entity attribute value
    //--------------------------------------------------------------------------------
    public String getTrackedEntityInstanceIdsByAttributeId( Integer attributeId )
    {
        String trackedEntityInstanceIds = "-1";
        
        try
        {
            String query = "SELECT trackedentityinstanceid FROM trackedentityattributevalue " +
                "WHERE value = 'true' AND trackedentityattributeid =  "+ attributeId + " order by trackedentityinstanceid ASC ";                        

            //SELECT trackedentityinstanceid FROM trackedentityattributevalue where trackedentityattributeid = 2618 and value = 'true';
            
            System.out.println( "query = " + query );
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiId = rs.getInt( 1 );
                if ( teiId != null )
                {
                    trackedEntityInstanceIds += "," + teiId;
                }
            }

            return trackedEntityInstanceIds;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }
    
    //--------------------------------------------------------------------------------
    // Get TrackedEntityInstance Ids from tracked entity attribute value
    //--------------------------------------------------------------------------------
    public List<String> getTrackedEntityInstanceAttributeValueByAttributeIdAndTrackedEntityInstanceIds( Integer attributeId, String trackedEntityInstanceIdsByComma )
    {
        List<String> mobileNumbers = new ArrayList<String>();
        
        try
        {
            String query = "SELECT value FROM trackedentityattributevalue " +
                           "WHERE trackedentityattributeid =  "+ attributeId + " AND trackedentityinstanceid in ( " + trackedEntityInstanceIdsByComma + ")";                        

            System.out.println( "query = " + query );
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String mobileNo = rs.getString( 1 );
                if ( mobileNo != null )
                {
                    mobileNumbers.add( mobileNo );
                }
            }

            return mobileNumbers;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }
}
