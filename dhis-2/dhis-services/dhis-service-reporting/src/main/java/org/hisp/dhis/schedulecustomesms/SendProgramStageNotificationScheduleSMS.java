package org.hisp.dhis.schedulecustomesms;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hisp.dhis.system.notification.NotificationLevel.INFO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.feedback.ErrorReport;
import org.hisp.dhis.message.MessageSender;
import org.hisp.dhis.scheduling.Job;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobProgress;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.scheduling.parameters.SmsJobParameters;
import org.hisp.dhis.sms.outbound.OutboundSms;
import org.hisp.dhis.sms.outbound.OutboundSmsService;
import org.hisp.dhis.sms.scheduling.SendScheduledMessageJob;
import org.hisp.dhis.system.notification.Notifier;
import org.hisp.dhis.system.util.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

/**
 * @author Mithilesh Kumar Thakur
 */
@Component("sendScheduledProgramNotificationMessageJob")
public class SendProgramStageNotificationScheduleSMS implements Job
{
    private static final Log log = LogFactory.getLog( SendProgramStageNotificationScheduleSMS.class );
    
    private static final String KEY_TASK = "sendScheduledProgramNotificationMessageJob";
    
    private final Notifier notifier;
    
    //@Qualifier("smsMessageSender")
    private MessageSender smsSender;
    
    private OutboundSmsService outboundSmsService;
    
    private JdbcTemplate jdbcTemplate;
    
    public SendProgramStageNotificationScheduleSMS( Notifier notifier, @Qualifier("smsMessageSender") MessageSender smsSender, OutboundSmsService outboundSmsService,
           JdbcTemplate jdbcTemplate )
    
    {

        checkNotNull( notifier );
        //checkNotNull( trackedEntityInstanceService );
        checkNotNull( jdbcTemplate );
        checkNotNull( smsSender );
        checkNotNull( outboundSmsService );
        //checkNotNull( dataElementService ); 
        //checkNotNull( programInstanceService );
        //checkNotNull( programStageInstanceService );
        //checkNotNull( categoryService );
        //checkNotNull( organisationUnitService );
        
        this.notifier = notifier;
        //this.trackedEntityInstanceService = trackedEntityInstanceService;
        this.jdbcTemplate = jdbcTemplate;
        this.smsSender = smsSender;
        this.outboundSmsService = outboundSmsService;
        
        //this.dataElementService = dataElementService;
        //this.programInstanceService = programInstanceService;
        //this.programStageInstanceService = programStageInstanceService;
        //this.programStageService = programStageService;
        //this.categoryService = categoryService;
        //this.organisationUnitService = organisationUnitService;
    
    }    
    
    private String url_string, data, response = "";
    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    @Override
    public JobType getJobType() {
      return JobType.SEND_PROGRAM_NOTIFICATION_SCHEDULED_MESSAGE;
    }

    @Override
    public void execute( JobConfiguration jobConfiguration, JobProgress progress )
    {
        /*
        SmsJobParameters params = (SmsJobParameters) jobConfiguration.getJobParameters();
        OutboundSms sms = new OutboundSms();
        sms.setSubject(params.getSmsSubject());
        sms.setMessage(params.getMessage());
        sms.setRecipients(new HashSet<>(params.getRecipientsList()));

        progress.startingProcess("Send SMS");
        smsSender.sendMessage(sms.getSubject(), sms.getMessage(), sms.getRecipients());
        */
        //System.out.println("INFO: PROGRAM NOTIFICATION SCHEDULED SMS job has started at : " + new Date() +" -- " + JobType.SEND_PROGRAM_NOTIFICATION_SCHEDULED_MESSAGE );
        
        Clock clock = new Clock().startClock();

        //clock.logTime( "Starting Create Missing ART Follow-up stage Event job " );
        notifier.notify( jobConfiguration, INFO, "PROGRAM NOTIFICATION SCHEDULED SMS job started ", true );

        //sendMessages();
        
        //notifier.notify( jobConfiguration, INFO, String.format( "%s has started", KEY_TASK ) );
        log.info( String.format( "%s has started", KEY_TASK ) );
        
        /*
        Set<String> teimMobileNumber = new HashSet<String>();
        
        //teimMobileNumber.add( "778807722" );
        teimMobileNumber.add( "775338133" );
        
        
        String tempCompleteMessage = "حان موعد تطعيم طفلكم بحسب جدول التطعيم الروتيني.وزارة الصحة- برنامج التحصين";
        
        //SmsJobParameters params = (SmsJobParameters) jobConfiguration.getJobParameters();
        OutboundSms sms = new OutboundSms();
        sms.setSubject("TEST SMS");
        sms.setMessage(tempCompleteMessage);
        sms.setRecipients(new HashSet<>(teimMobileNumber));
        */
        
        //progress.startingProcess("Send SMS");
        
        //smsSender.sendMessage(sms.getSubject(), sms.getMessage(), sms.getRecipients());
        
        /*
        try
        {
            sendCustomSMS( tempCompleteMessage, "775338133");
        }
        catch ( UnsupportedEncodingException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */
        
        sendSMSProgramStageNotificationToTEI();
        
        //clock.logTime( "Completed Create Missing ART Follow-up stage Event job" );
        notifier.notify( jobConfiguration, INFO, "PROGRAM NOTIFICATION SCHEDULED SMS job completed", true );
    }
    
    
    @Override
    public ErrorReport validate() {
      if (!smsSender.isConfigured()) {
        return new ErrorReport(
            SendScheduledMessageJob.class,
            ErrorCode.E7010,
            "SMS gateway configuration does not exist");
      }
      return Job.super.validate();
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Get TrackedEntityInstance list/name/phone-number for Electronic Immunization Registry before one day of due date
    // ----------------------------------------------------------------------------------------------------------------
    public void sendSMSProgramStageNotificationToTEI()
    {
        Set<String> teimMobileNumber = new HashSet<String>();
        String oneDayAfterTodayDate = "";
        String tempCompleteMessage = "";
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
            
            //create instance of the Calendar class and set the date to the given date  
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            // use add() method to add the days to the given date  
            cal.add(Calendar.DAY_OF_MONTH, 1);
            //String tempOneDayAfterTodayDate = sdf.format(cal.getTime());
            oneDayAfterTodayDate = sdf.format(cal.getTime()); 
            
            String query = "SELECT tei.uid AS teiUID, psi.uid AS eventUID, psi.duedate::date as due_date, " +
                " org.name as orgunit_name,teav1.value as mobile_number, teav2.value as first_name,  " +
                " teav3.value as last_name FROM programstageinstance psi " +
                " INNER JOIN programinstance pi ON pi.programinstanceid = psi.programinstanceid " +
                " INNER JOIN trackedentityinstance tei ON tei.trackedentityinstanceid = pi.trackedentityinstanceid " +
                " INNER JOIN organisationunit org ON org.organisationunitid = psi.organisationunitid " +
                " INNER JOIN trackedentityattributevalue teav1 ON tei.trackedentityinstanceid = teav1.trackedentityinstanceid " +
                
                " INNER JOIN ( SELECT trackedentityinstanceid, value FROM trackedentityattributevalue WHERE trackedentityattributeid = 3421643 ) teav2 " +
                " ON teav1.trackedentityinstanceid = teav2.trackedentityinstanceid " +
                
                " INNER JOIN ( SELECT trackedentityinstanceid,value FROM trackedentityattributevalue WHERE trackedentityattributeid = 3421645 ) teav3 " +
                " ON teav1.trackedentityinstanceid = teav3.trackedentityinstanceid " +
            
                " WHERE psi.programstageid in ( select programstageid from programstage WHERE uid = 's53RFfXA75f') " +
                " AND teav1.trackedentityattributeid = 3421642 " + 
                " AND psi.duedate::date = '" + oneDayAfterTodayDate + "' ";
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String tei_uid = rs.getString( 1 );
                String event_uid = rs.getString( 2 );
                String due_date = rs.getString( 3 );
                String orgunit_name = rs.getString( 4 );
                String mobile_number = rs.getString( 5 );
                String first_name = rs.getString( 6 );
                String last_name = rs.getString( 7 );
                
                if ( tei_uid != null && due_date != null && mobile_number != null )
                {
                    teimMobileNumber.add( mobile_number );
                    
                    //teimMobileNumber.add( "778807722" );
                    //teimMobileNumber.add( "775338133" );
                    
                    
                    //String tempCompleteMessage = "Welcome " + first_name + " " + last_name + ". Most parents of children attend immunization visits to help keep their baby safe. Your child will get immunizations to protect them and others against certain infectious diseases. As agreed, we will send you reminders about upcoming visits. You may let us know if you donot want to receive anymore.";
                    
                    
                    tempCompleteMessage = "حان موعد تطعيم طفلكم بحسب جدول التطعيم الروتيني.وزارة الصحة- برنامج التحصين حان موعد زيارتك في تاريخ "+ due_date + " وفي المرفق" + orgunit_name;
                    sendCustomSMS( tempCompleteMessage, mobile_number);
                    
                    //System.out.println( " tei_uid : " + tei_uid + " mobile_number : " + mobile_number + " tempCompleteMessage : " + tempCompleteMessage );
                    
                    //SmsJobParameters params = (SmsJobParameters) jobConfiguration.getJobParameters();
                    /*
                    OutboundSms sms = new OutboundSms();
                    sms.setSubject("TEST SMS");
                    sms.setMessage(tempCompleteMessage);
                    sms.setRecipients(new HashSet<>(teimMobileNumber));
                    smsSender.sendMessage(sms.getSubject(), sms.getMessage(), sms.getRecipients());
                    */
                    
                }
                
                
            }

            //sendCustomSMS( tempCompleteMessage, "775338133");
            
            //System.out.println( " 1 tei list size : " + teiUids.size() );
            //return teiUids;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Mobile Nubmer", e );
        }
    }
    
    
    // sending message to single mobile no
    public String sendCustomSMS( String message, String phoneNo )
        throws UnsupportedEncodingException
    {
        String resopnseString = "";
        System.out.println( phoneNo + " -- 1 -- " + message );
        // System.out.println(encodeMessage(new String(message.getBytes())));
        try
        {
            data = "orgName=MOHEPI&userName=EPI&password=MoH@@5423&mobileNo=" + phoneNo + "&text=" + message + "&coding=2";
            
            //http://52.36.50.145:8080/MainServlet?orgName=MOHEPI&userName=EPI&password=MoH@@5423&mobileNo=775338133&text=حان موعد تطعيم طفلكم بحسب جدول التطعيم الروتيني.وزارة الصحة- برنامج التحصين
            
            // Send data
            //HttpURLConnection conn = (HttpURLConnection) new URL( url_string ).openConnection();
            
            HttpURLConnection conn = (HttpURLConnection) new URL( "http://52.36.50.145:8080/MainServlet?" ).openConnection();
            
            //Populating the data according to the api link
            
            //data = "username=" + username + "&password=" + password + "&sendername=" + senderName + "&mobileno=" + phoneNo + "&message=" + message;
            
            conn.setDoOutput( true );
            conn.setRequestMethod( "GET" );
            conn.setRequestProperty( "Content-Length", Integer.toString( data.length() ) );
            conn.getOutputStream().write( data.getBytes( "UTF-8" ) );
            final BufferedReader rd = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
            final StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ( (line = rd.readLine()) != null )
            {
                stringBuffer.append( line );
            }

            rd.close();

            resopnseString = stringBuffer.toString();

            System.out.println( "SMS Response : --" + stringBuffer.toString() );
        }

        catch ( Exception e )
        {
            System.out.println( "Error SMS " + e );
        }

        return resopnseString;

    }
    
    
    
}
