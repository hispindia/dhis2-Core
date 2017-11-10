package org.hisp.dhis.dataadmin.action.schedulecustomesms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

/**
 * @author Mithilesh Kumar Thakur
 */
public class ScheduleSMSAction implements Runnable
{

    private final static int  SMS_CONSENT_ATTRIBUTE_ID = 2618;
    private final static int  MOBILE_NUMBER_ATTRIBUTE_ID = 2617;
    
    @Autowired
    private TaskScheduler scheduler;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    
    private ScheduledFuture<?> future;
    
    
    @PostConstruct
    public void run()
    {
        try
        {
            scheduler.scheduleWithFixedDelay( scheduledMonthlySMS(), new Date(), 2000 );
            //scheduler.schedule(scheduledMonthlySMS(), new CronTrigger("* 15 9-17 * * MON-FRI"));
            scheduler.schedule(scheduledMonthlySMS(), new CronTrigger("0 0/2 * * * ?"));
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        //log.info( "Scheduled monitoring push service" );
    }
    
    /*
    public void run()
    {
        System.out.println(" Monthly SMS Scheduler Started at --1 : " + new Date() );
        try
        {
            future = scheduler.scheduleWithFixedDelay(scheduledMonthlySMS(), new Date(), 2000 );
            scheduler.schedule(scheduledMonthlySMS(), new CronTrigger("0 0/2 * * * ?"));
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //future = scheduler.schedule(scheduledMonthlySMS(), new CronTrigger("0 0/2 * * * ?"));
       
    }
    
    public void stop()
    {
        future.cancel( true );
    }
    */
    
    // -------------------------------------------------------------------------
    // Support methods 
    // -------------------------------------------------------------------------
    
    public Runnable scheduledMonthlySMS( ) throws IOException
    {
        System.out.println(" Monthly SMS Scheduler Started at : " + new Date() );
        
        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();
        String trackedEntityInstanceIds  = getTrackedEntityInstanceIdsByAttributeId( SMS_CONSENT_ATTRIBUTE_ID );
        List<String> mobileNumbers = new ArrayList<String>();
        if( trackedEntityInstanceIds != null && trackedEntityInstanceIds.length() > 0)
        {
            mobileNumbers = new ArrayList<>( getTrackedEntityInstanceAttributeValueByAttributeIdAndTrackedEntityInstanceIds( MOBILE_NUMBER_ATTRIBUTE_ID, trackedEntityInstanceIds ) );
        }
        
        String message = "Test";
        
        if( mobileNumbers != null && mobileNumbers.size() > 0 )
        {
            for( String mobileNumber : mobileNumbers )
            { 
                //bulkSMSHTTPInterface.sendMessage( message, mobileNumber );
                System.out.println( mobileNumber +  " -------- > " + message );
            }
        }
               
        System.out.println(" Monthly SMS Scheduler End at : " + new Date() );
        return (Runnable) mobileNumbers;
        //return null;
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
