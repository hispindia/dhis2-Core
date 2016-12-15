package org.hisp.dhis.reporting.scheduleemail.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.hisp.dhis.setting.SystemSettingManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class ScheduleEmailAction
    implements Action
{
    private static final String DHIS2_HOME = "DHIS2_HOME";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private SystemSettingManager systemSettingManager;

    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    private String outputReportPath;

    private String EMAIL_RECEIVER = null;

    private String EMAIL_SUBJECT = null;

    private String BODY = null;

    private String ATTACHMENT_PATH = null;

    private static String USER_NAME;

    private static String PASSWORD;

    private String HOST_NAME;

    public String getHOST_NAME()
    {
        return HOST_NAME;
    }

    public void setHOST_NAME( String HOST_NAME )
    {
        this.HOST_NAME = HOST_NAME;
    }

    private Integer SMTP_PORT;

    public Integer getSMTP_PORT()
    {
        return SMTP_PORT;
    }

    public void setSMTP_PORT( Integer SMTP_PORT )
    {
        this.SMTP_PORT = SMTP_PORT;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        System.out.println( " Email Schedule Start Time is : " + new Date() );

        outputReportPath = System.getenv( DHIS2_HOME ) + File.separator + "akriosh_report";

        File newdir = new File( outputReportPath );
        if ( !newdir.exists() )
        {
            newdir.mkdirs();
        }

        // outputReportPath = System.getenv( DHIS2_HOME ) + File.separator +
        // "akriosh_report" + File.separator + "ConveyanceClaim.xls";

        List<String> attachedFiles = new ArrayList<String>();
        
        File file = new File( outputReportPath );
        File[] files = file.listFiles();
        System.out.println( "files count -- " + files.length );
        if( files != null && files.length > 0 )
        {
            for ( File tempFile : files )
            {
                if( tempFile.isFile() )
                {
                    //System.out.println( f.getAbsolutePath() );
                    System.out.println( tempFile.getName() );
                    attachedFiles.add( System.getenv( DHIS2_HOME ) + File.separator + "akriosh_report" + File.separator
                        + tempFile.getName() );
                }
            }
        }
        
//        
//        File dir = new File("C:/Windows/System32/oobe/info/backgrounds/");
//        if(dir.listFiles() == null)
//            System.out.println("Empty");
//        for(File img : dir.listFiles())
//        {
//            if(img.getName().endsWith(".jpg") && img.getName() != "backgroundDefault.jpg")
//                wallpapers.add(img);
//        }
//        
        
        // HOST_NAME = systemSettingManager.getEmailHostName().trim();
        // SMTP_PORT = systemSettingManager.getEmailPort();
        // USER_NAME = systemSettingManager.getEmailUsername().trim();
        //
        // System.out.println( " HOST_NAME : " + HOST_NAME );
        // System.out.println( " SMTP_PORT : " + SMTP_PORT );
        // System.out.println( " USER_NAME : " + USER_NAME );

        // PASSWORD = systemSettingManager.getEmailPassword();

        EMAIL_SUBJECT = "Find Attachment Contain Report :  Report Generated At : " + new Date();
        BODY = "DHIS2 Generated Adverse Report. Please Do Not reply.";

        ATTACHMENT_PATH = outputReportPath;

        String host = "smtp.gmail.com";
        String port = "587";
        String mailFrom = "mithilesh.hisp@gmail.com";
        String password = "";

        // message info
        String mailTo = "neeraj.hisp@gmail.com";
        String subject = "Find Attachment Contain Report :  Report Generated At : " + new Date();
        String message = "Find Attachment Contain Report :  Report Generated At : " + new Date();

        // attachments
        String[] attachFiles = new String[3];
        attachFiles[0] = System.getenv( DHIS2_HOME ) + File.separator + "akriosh_report" + File.separator
            + "ConveyanceClaim.xls";
        attachFiles[1] = System.getenv( DHIS2_HOME ) + File.separator + "akriosh_report" + File.separator
            + "1-b6175f5fbd.jpg";
        attachFiles[2] = System.getenv( DHIS2_HOME ) + File.separator + "akriosh_report" + File.separator
            + "dhis_2_architecture .pdf";
        
        
        
       
        try
        {
            //sendEmailWithAttachments( host, port, mailFrom, password, mailTo, subject, message, attachedFiles );
            sendEmailWithAttachments( host, port, mailFrom, password, mailTo, subject, message, attachedFiles );
            System.out.println( "Email sent." );
        }
        catch ( Exception ex )
        {
            System.out.println( "Could not send email." );
            ex.printStackTrace();
        }
        /*
         * try { sendEmail( USER_NAME, PASSWORD, HOST_NAME,
         * SMTP_PORT.intValue(), EMAIL_RECEIVER, EMAIL_SUBJECT, BODY,
         * ATTACHMENT_PATH ); } catch ( Throwable e ) { e.printStackTrace(); }
         */

        System.out.println( " Email Schedule End Time is : " + new Date() );

        return SUCCESS;
    }

    //
    public void sendEmail( String EMAIL_USERNAME, String EMAIL_PASSWORD, String HOST_NAME, Integer SMTP_PORT,
        String EMAIL_RECEIVER, String EMAIL_SUBJECT, String BODY, String ATTACHMENT_PATH )
    {
        File attachFile = new File( ATTACHMENT_PATH );
        EmailAttachment attachment = new EmailAttachment();
        attachment.setPath( attachFile.getPath() );
        attachment.setDisposition( EmailAttachment.ATTACHMENT );
        attachment.setDescription( attachFile.getName() );
        attachment.setName( attachFile.getName() );
        MultiPartEmail email = new MultiPartEmail();
        email.setHostName( HOST_NAME );// "smtp.gmail.com"
        email.setSmtpPort( SMTP_PORT );// 587,465
        email.setAuthenticator( new DefaultAuthenticator( EMAIL_USERNAME, EMAIL_PASSWORD ) );
        email.setSSL( true );
        try
        {
            email.setFrom( EMAIL_USERNAME );
            email.addTo( EMAIL_RECEIVER );
            email.setMsg( EMAIL_SUBJECT );
            email.setSubject( BODY );
            email.attach( attachment );
            email.send();
        }
        catch ( EmailException e )
        {
            e.printStackTrace();
        }
    }

    public static void sendEmailWithAttachments( String host, String port, final String userName,
        final String password, String toAddress, String subject, String message, List<String> attachFiles )
        throws AddressException, MessagingException
    {
        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put( "mail.smtp.host", host );
        properties.put( "mail.smtp.port", port );
        properties.put( "mail.smtp.auth", "true" );
        properties.put( "mail.smtp.starttls.enable", "true" );
        properties.put( "mail.user", userName );
        properties.put( "mail.password", password );

        // creates a new session with an authenticator
        Authenticator auth = new Authenticator()
        {
            public PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication( userName, password );
            }
        };

        Session session = Session.getInstance( properties, auth );

        // creates a new e-mail message
        Message msg = new MimeMessage( session );

        msg.setFrom( new InternetAddress( userName ) );
        InternetAddress[] toAddresses = { new InternetAddress( toAddress ) };
        msg.setRecipients( Message.RecipientType.TO, toAddresses );
        msg.setSubject( subject );
        msg.setSentDate( new Date() );

        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent( message, "text/html" );

        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart( messageBodyPart );

        // adds attachments
        if ( attachFiles != null && attachFiles.size() > 0 )
        {
            for ( String filePath : attachFiles )
            {
                MimeBodyPart attachPart = new MimeBodyPart();

                try
                {
                    attachPart.attachFile( filePath );
                }
                catch ( IOException ex )
                {
                    ex.printStackTrace();
                }

                multipart.addBodyPart( attachPart );
            }
        }

        // sets the multi-part as e-mail's content
        msg.setContent( multipart );

        // sends the e-mail
        Transport.send( msg );

    }
}
