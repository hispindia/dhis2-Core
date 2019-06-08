package org.hisp.dhis.automessage;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.scheduling.AbstractJob;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.setting.SettingKey;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserInfo;
import org.hisp.dhis.user.UserService;
import org.hisp.dhis.user.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

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
    private OrganisationUnitService organisationUnitService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private UserService userService;
    
   
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
        
        // Declare the security credentials to use
        String username = "Indianhealthaction";
        String password = "12345678";

        // Set the attributes of the message to send
        String message  = getContentMessage();
        String senderid = "LBWKMC";
        String to       = "8826288599,9818438691,8285561428,9582310459,9644655521";
        String channel = "TRANS";

        try {
          // Build URL encoded query string
          String encoding = "UTF-8";
          String queryString = "user=" + URLEncoder.encode(username, encoding)
            + "&password=" + URLEncoder.encode(password, encoding)
            + "&senderid=" + URLEncoder.encode(senderid, encoding)
            + "&channel="+URLEncoder.encode(channel, encoding)
            + "&DCS="+URLEncoder.encode("0", encoding)
            + "&flashsms="+URLEncoder.encode("0", encoding)
            + "&number=" + URLEncoder.encode(to, encoding)
            + "&text=" + URLEncoder.encode(message, encoding)
            + "&route="+URLEncoder.encode("02", encoding);

          // Send request to the API servers over HTTPS
          URL url = new URL("http://aanviit.com/api/mt/SendSMS?"+queryString);
          
          HttpURLConnection uc = (HttpURLConnection)url.openConnection();
          System.out.println(uc.getResponseMessage());
          uc.disconnect();
        } 
        catch (Exception e) {
          System.out.println("Error - " + e);
        }
        
        System.out.println("INFO: Scheduler job has ended at : " + new Date() );
             
    }
    public String getUserContactNumber()
    {
    	String userContact = "";
    	
    	List<OrganisationUnit> orgUnits= new ArrayList<OrganisationUnit>();
    	orgUnits =	organisationUnitService.getOrganisationUnitsAtLevel(5);
    	userContact = "8826288599,9644655521,9582310459,9643274071,8285561428";
    	/*for(OrganisationUnit ou: orgUnits)
    	{
    		String userInfo = "SELECT userinfoid FROM usermembership WHERE organisationunitid = "+ou.getId();
        	
   	 		SqlRowSet sql2ResultSet = jdbcTemplate.queryForRowSet( userInfo );
   	 		while ( sql2ResultSet != null && sql2ResultSet.next() )
	 		{
	        	String uid = sql2ResultSet.getString("userinfoid");
	        	User user = userService.getUser(Integer.parseInt(uid));
	        	if(user.getPhoneNumber() != null)
	        	{
		        	//System.out.println("user.name : "+user.getName());
		        	userContact = userContact+","+user.getPhoneNumber();
	        	}
	 		}
        	
    	}*/
    	//userContact = userContact+","+"9582310459";
    	//userContact = userContact+","+"9643274071";
    	
    	
    	return userContact;
    }
    public String getContentMessage()
    {
    	String content = "";
    	Calendar now = Calendar.getInstance();
    	String dataEntryDate = "16/"+(now.get(Calendar.MONTH)+1)+"/"+now.get(Calendar.YEAR);
    	
    	content = "Dear UPHMIS User,\n Please fill your data till "+dataEntryDate+" , if you already fill please ignore this\n Thanks";
    	
    	return content;
    }

}