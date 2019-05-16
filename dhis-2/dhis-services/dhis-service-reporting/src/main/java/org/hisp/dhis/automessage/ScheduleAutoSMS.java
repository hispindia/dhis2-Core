package org.hisp.dhis.automessage;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.scheduling.AbstractJob;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.setting.SettingKey;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Samta Pandey
 */
public class ScheduleAutoSMS extends AbstractJob
{
    private static final Log log = LogFactory.getLog( ScheduleAutoEmailMessage.class );

    private static final String KEY_TASK = "scheduleAutoSMSData";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private SystemSettingManager systemSettingManager;
    
    @Autowired
    private TrackedEntityDataValueService trackedEntityDataValueService;
    
    
    @Autowired
    private DataElementService dataElementService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    @Override
    public JobType getJobType()
    {
        return JobType.AUTO_SMS_MESSAGE;
    }

    @Override
    public void execute( JobConfiguration jobConfiguration )
    {
       
        boolean isAutoSMSEnabled = (Boolean) systemSettingManager.getSystemSetting( SettingKey.AUTO_SMS_MESSAGE );
        System.out.println( "IS Auto SMS Enabled \n" + isAutoSMSEnabled );
        
        if ( !isAutoSMSEnabled )
        {
            log.info( String.format( "%s aborted. Auto SMS  are disabled", KEY_TASK ) );

            return;
        }

        log.info( String.format( "%s has started", KEY_TASK ) );
        
        String importStatus = "";
        
        try {
        	 String recipient = "+918826288599";
             String message = " Greetings from Mr. ABC Have a nice day!";
             String username = "admin";
             String password = "abc123";
             String originator = "+440987654321";

             String requestUrl  = "http://127.0.0.1:9501/api?action=sendmessage&" +
             					 "username=" + URLEncoder.encode(username, "UTF-8") +
             					 "&password=" + URLEncoder.encode(password, "UTF-8") +
             					 "&recipient=" + URLEncoder.encode(recipient, "UTF-8") +
             					 "&messagetype=SMS:TEXT" +
             					 "&messagedata=" + URLEncoder.encode(message, "UTF-8") +
             					 "&originator=" + URLEncoder.encode(originator, "UTF-8") +
             					 "&serviceprovider=GSMModem1" +
             					 "&responseformat=html";



             URL url = new URL(requestUrl);
             HttpURLConnection uc = (HttpURLConnection)url.openConnection();

             System.out.println(uc.getResponseMessage());

             uc.disconnect();

            }
            catch ( Exception e )
            {
                importStatus = "Exception occured while import, please check log for more details" + e.getMessage();
            }
        
        
        System.out.println("ImportStatus : " + importStatus );
        System.out.println("INFO: Scheduler job has ended at : " + new Date() );
             
    }

}