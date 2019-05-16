package org.hisp.dhis.automessage;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.category.CategoryCombo;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataentryform.DataEntryForm;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.scheduling.AbstractJob;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.setting.SettingKey;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.system.filter.PastAndCurrentPeriodFilter;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserService;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * @author Samta Pandey
 */
public class ScheduleAutoEmailMessage extends AbstractJob
{
    private static final Log log = LogFactory.getLog( ScheduleAutoEmailMessage.class );

    private static final String KEY_TASK = "scheduleAutoEmailData";
    
    private String[] ccEmail = {"sourabh.bhardwaj@hispindia.org ","yogesh.chand@hispindia.org","harsh.atal@gmail.com "};
    
    private List<String> emailList = new ArrayList<String>();
    
    private final static int   DATASET_ID = 1657803;
    
    private Map<String,Integer> emailMap = new HashMap<String,Integer>();
	
	private Set<OrganisationUnit> sources = new HashSet<OrganisationUnit>();
	
	List<OrganisationUnit> orgList = new ArrayList<OrganisationUnit>();

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private SystemSettingManager systemSettingManager;
   
    @Autowired
    private DataSetService dataSetService;
    
    @Autowired
    private DataValueService dataValueService;
    
    @Autowired
    private DataElementService dataElementService;
    
    @Autowired
    private PeriodService periodService;
    
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
        return JobType.AUTO_EMAIL_MESSAGE;
    }

    
    @Override
    public void execute( JobConfiguration jobConfiguration )
    {
       
        boolean isAutoEmailDataEnabled = (Boolean) systemSettingManager.getSystemSetting( SettingKey.AUTO_EMAIL_MESSAGE );
        //System.out.println( "isAutoEmailEnabled -- " + isAutoEmailDataEnabled );
        
        if ( !isAutoEmailDataEnabled )
        {
            log.info( String.format( "%s aborted. Auto Email are disabled", KEY_TASK ) );

            return;
        }

        log.info( String.format( "%s has started", KEY_TASK ) );
        String importStatus = "";
        
     // Recipient's email ID needs to be mentioned.
        String to = "samta.pandey@hispindia.org";
        
        String all= "";

        // Sender's email ID needs to be mentioned
        String from = "samta.bajpayee@gmail.com";
        String password = "**************";

        // Assuming you are sending email from localhost
        String host = "smtp.gmail.com";

        // Get system properties
        Properties props = new Properties();  
        props.setProperty("mail.transport.protocol", "smtp");     
        props.setProperty("mail.host", "smtp.gmail.com");  
        props.put("mail.smtp.auth", "true");  
        props.put("mail.smtp.port", "465");  
        props.put("mail.smtp.user", from);
        props.put("mail.debug", "true");  
        props.put("mail.smtp.socketFactory.port", "465");  
        props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");  
        props.put("mail.smtp.socketFactory.fallback", "false");  
        props.put("mail.smtp.starttls.enable", "true");
        
        

        // Get the default Session object.
        
        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator()
        {
          protected PasswordAuthentication getPasswordAuthentication()
          {
            return new PasswordAuthentication(from, password);
          }
        });
        
        try {
        
        	// Create a default MimeMessage object.
       	 	Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com" , 465 , from, password);
            //transport.send(msg); 
       	   	InternetAddress addressFrom = new InternetAddress(from); 
	       	InternetAddress[] ccAddress = new InternetAddress[ccEmail.length];
	        MimeMessage message = new MimeMessage(session);  
	       	message.setSender(addressFrom);
	        message.setSubject("Email Notification");  
	       	
	        // To get the array of ccaddresses
	        for( int i = 0; i < ccEmail.length; i++ ) {
	            ccAddress[i] = new InternetAddress(ccEmail[i]);
	        }
	        
	      Map<String,Integer> emailMap = getDataValueMessage();
	      if(emailList != null)
	      {
	    	  String content = "\n Hello User";
	    	  
	    	  System.out.println("email List: "+emailList);
		       for(String emailId : emailList)
		       {	
		    	   for(OrganisationUnit ou: orgList) 
		    	   {
		    		   content = content + "\n Organisation Unit:\t"+ou.getName();
		    	   }
		    	   content = content + "\n Mail Id Or Name of User is \t"+emailId+" and data count is "+emailMap.get(emailId)+" \n thanks";
		       }
		       message.setContent(content, "text/plain");
	      }
	      else
	      {
	    	  message.setContent("Data Not Found","text/plain");
	      }

       	  message.addRecipient(Message.RecipientType.TO, new InternetAddress(to)); 
       	   
       	// Set cc: header field of the header.
	        for( int i = 0; i < ccAddress.length; i++) {
	            message.addRecipient(Message.RecipientType.CC, ccAddress[i]);
	        }

       	   //transport.connect();  
       	   Transport.send(message)  ;
       	   transport.close();
       	   
           System.out.println("Sent message successfully....");
        
                
            }
        	catch (Exception mex)
            {
                importStatus = "Exception occured while import, please check log for more details" + mex.getMessage();
            }
       
       // System.out.println("ImportStatus : " + importStatus );
       System.out.println("INFO: Scheduler job has ended at : " + new Date() );
             
    }
    
    public Map<String,Integer> getDataValueMessage()
    {
    	DataSet d = new DataSet();
    	
    	d.setId(DATASET_ID);

    	DataSet dataSet = dataSetService.getDataSet(DATASET_ID);
    	
    	System.out.println("Data set name: \t"+dataSet.getName());
    	
    	String dataSetSources = "SELECT sourceid FROM datasetsource WHERE datasetid = "+DATASET_ID;
    	
    	 SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( dataSetSources );
         while ( sqlResultSet != null && sqlResultSet.next() )
         {
        	
        	String oid = sqlResultSet.getString("sourceid");        	 
        	OrganisationUnit orgunit = organisationUnitService.getOrganisationUnit(Integer.parseInt(oid));
        	if(!sources.contains(orgunit))
        	{
        		sources.add(orgunit);
        	}
        	 
         }
    	for(OrganisationUnit source : sources)
    	{
    		OrganisationUnit parent = source.getParent();
    		if(!orgList.contains(parent)) 
    		{
    			
    			orgList.add(parent);
    			String orgUnits = "SELECT userid FROM organisationunit WHERE organisationunitid = "+parent.getId();
        	
       	 		SqlRowSet sql2ResultSet = jdbcTemplate.queryForRowSet( orgUnits );
       	 		while ( sql2ResultSet != null && sql2ResultSet.next() )
       	 		{
	            	String uid = sql2ResultSet.getString("userid");
	            	User user = userService.getUser(Integer.parseInt(uid));
	            	
	            	System.out.println("user.name and mail id: "+user.getName()+"\t"+user.getEmail());
	            	if(!emailList.contains(user.getEmail()) && !user.getEmail().isEmpty())
	    			{
	    				emailList.add(user.getEmail());
	    				
	    			}
            	else {
            		emailList.add(user.getName());
            		//System.out.println("user.getName(): "+user.getName());
            	}

            }
    		}
    	}
        Object[] objDays = emailList.toArray();
		
		//Second Step: convert Object array to String array
        ccEmail = Arrays.copyOf(objDays, objDays.length, String[].class);
        
        Set<DataElement> dataElements = new HashSet<DataElement>();
    	String dataElementQuery = "SELECT dataelementid FROM datasetmembers WHERE datasetid = "+DATASET_ID;
    	
    	SqlRowSet sql3ResultSet = jdbcTemplate.queryForRowSet( dataElementQuery );
        while ( sql3ResultSet != null && sql3ResultSet.next() )
        {
        	String dataElementid = sql3ResultSet.getString("dataelementid");
        	DataElement dataElement = dataElementService.getDataElement(Integer.parseInt(dataElementid));
        	dataElements.add(dataElement);
        }


    	Period period = periodService.getPeriod(79030054);

    	
	    	for(OrganisationUnit ou : sources)
			{
	    		int count = 0;
		    	for(DataElement de : dataElements)
		    	{
		    		CategoryOptionCombo categoryCombo = new CategoryOptionCombo();
		    		categoryCombo.setId(15);
		    		DataValue dataValue = dataValueService.getDataValue(de, period, ou, categoryCombo, categoryCombo);
		    		   			
	    			//System.out.println("DataValue Count Is:\t"+dataValue.getValue());
	    			
	    			if(dataValue != null && dataValue.getValue() != "")
	    			{
	    				count++;
	    			}
	    		}
		    	if(!emailMap.containsKey(ou.getParent().getUser().getEmail())) {
			    	if(!ou.getParent().getUser().getEmail().isEmpty())
			    	{
				    	System.out.println("Email If:\t"+ou.getParent().getUser().getEmail());
				    	emailMap.put(ou.getParent().getUser().getEmail(), count);
			    	}
			    	else
			    	{
			    		System.out.println("Email Else:\t"+ou.getParent().getUser().getName());
			    		emailMap.put(ou.getParent().getUser().getName(), count);
			    	}
		     }
	    	}
    	
	    	return emailMap;
    }
    	
}