package org.hisp.dhis.dataadmin.action.sendemail;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class SendEmail
    implements Action
{
    // -------------------------------------------------------------------------
    //
    // -------------------------------------------------------------------------

    private String message;

    public String getMessage()
    {
        return message;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
    {
        System.out.println( "inside send email" );

        // HttpServletRequest request;
        // HttpServletResponse response;

        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        // ResourceBundle rb = ResourceBundle.getBundle( "LocalStrings",
        // request.getLocale() );

        // response.setContentType( "text/html" );

        String subject = "";
        String body = "";
        String attachmentLocation = "";
        String email_address_to = "";
        String email_address_cc = "";

        Map<String, String[]> params = new HashMap<String, String[]>( request.getParameterMap() );

        if ( params != null && params.size() > 0 )
        {
            for ( String key : params.keySet() )
            {
                if ( key.equalsIgnoreCase( "subject" ) )
                {
                    subject = ((String[]) params.get( key ))[0];
                }
                if ( key.equalsIgnoreCase( "body" ) )
                {
                    body = ((String[]) params.get( key ))[0];
                }
                if ( key.equalsIgnoreCase( "attachmentLocation" ) )
                {
                    attachmentLocation = ((String[]) params.get( key ))[0];
                }
                if ( key.equalsIgnoreCase( "email_address_to" ) )
                {
                    email_address_to = ((String[]) params.get( key ))[0];
                }
                if ( key.equalsIgnoreCase( "email_address_cc" ) )
                {
                    email_address_cc = ((String[]) params.get( key ))[0];
                }
            }

            /*
             * Iterator<String> it = params.keySet().iterator();
             * 
             * while ( it.hasNext() ) { String key = (String) it.next(); String
             * messageValue = ((String[]) params.get( key ))[ 0 ];
             * 
             * if( key == "mobileno") { mobileNo = messageValue; } if( key ==
             * "message") { smsMessage = messageValue; }
             * 
             * System.out.println( key + "---" + messageValue );
             * 
             * 
             * }
             */

            System.out.println( "subject -- " + subject );
            System.out.println( "body -- " + body );
            System.out.println( "attachmentLocation -- " + attachmentLocation );
            System.out.println( "email_address_to -- " + email_address_to );
            System.out.println( "email_address_cc -- " + email_address_cc );
            // location in url like - C:\DHIS2_HOME\dhis2_developer_manual.pdf
            /*
             * List<String> attachedFiles = new ArrayList<String>(); File file =
             * new File( attachmentLocation ); File[] files = file.listFiles();
             * System.out.println( "files count -- " + files.length ); if( files
             * != null && files.length > 0 ) { for ( File tempFile : files ) {
             * if( tempFile.isFile() ) { //System.out.println(
             * f.getAbsolutePath() ); System.out.println( tempFile.getName() );
             * attachedFiles.add( tempFile.getName() ); } } }
             */

            String host = "smtp.gmail.com";
            String port = "587";
            String mailFrom = "eoctiruvallur@gmail.com";
            String password = "Eoc@tiruvallur";

            // message info
            String mailTo = email_address_to;
            //String mailTo = "wasibtariq@gmail.com";
            String mailToCC = email_address_cc;
            String mailSubject = subject;
            String message = body;
            String attachedFile = attachmentLocation;

            try
            {
                // sendEmailWithAttachments( host, port, mailFrom, password,
                // mailTo, subject, message, attachedFiles );
                sendEmailWithSingleAttachment( host, port, mailFrom, password, mailTo, mailToCC, mailSubject, message,
                    attachedFile );
                System.out.println( "Email sent." );
            }
            catch ( Exception ex )
            {
                System.out.println( "Could not send email." );
                ex.printStackTrace();
            }

        }

        return SUCCESS;
    }

    // Supportive methods
    public static void sendEmailWithSingleAttachment( String host, String port, final String mailFrom,
        final String password, String toAddress, String toAddressCC, String subject, String message, String attachedFile )
        throws AddressException, MessagingException
    {
        // sets SMTP server properties
        System.out.println( "inside method" );
        Properties properties = new Properties();
        properties.put( "mail.smtp.host", host );
        properties.put( "mail.smtp.port", port );
        properties.put( "mail.smtp.auth", "true" );
        properties.put( "mail.smtp.starttls.enable", "true" );
        properties.put( "mail.user", mailFrom );
        properties.put( "mail.password", password );

        // creates a new session with an authenticator
        Authenticator auth = new Authenticator()
        {
            public PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication( mailFrom, password );
            }
        };
        
       
        Session session = Session.getInstance( properties, auth );
        
        // creates a new e-mail message
        Message msg = new MimeMessage( session );
        msg.setFrom( new InternetAddress( mailFrom ) );
        
        // can put multiple receivers in the array
        // for set multiple TO address seperated by semi-colon
        if( toAddress != null && toAddress.length() > 0)
        {
            String[] mailAddressTo = toAddress.split( ";" );
            InternetAddress[] mailAddress_TO = new InternetAddress[mailAddressTo.length];
            for ( int i = 0; i < mailAddressTo.length; i++ )
            {
                mailAddress_TO[i] = new InternetAddress( mailAddressTo[i] );
                System.out.println( mailAddress_TO[i] );
            }
            System.out.println( mailAddress_TO );
            
            msg.addRecipients( Message.RecipientType.TO, mailAddress_TO );
        }
        
        //InternetAddress[] toAddresses = { new InternetAddress( toAddress ) };
        //msg.setRecipients( Message.RecipientType.TO, toAddresses );
        // for set multiple CC address seperated by semi-colon
        if( toAddressCC != null && toAddressCC.length() > 0)
        {
            String[] mailAddressCC = toAddressCC.split( ";" );
            InternetAddress[] mailAddress_CC = new InternetAddress[mailAddressCC.length];
            for ( int i = 0; i < mailAddressCC.length; i++ )
            {
                mailAddress_CC[i] = new InternetAddress( mailAddressCC[i] );
                System.out.println( mailAddress_CC[i] );
            }
            System.out.println( mailAddress_CC );
            
            msg.addRecipients( Message.RecipientType.CC, mailAddress_CC );
        }

        //InternetAddress[] toAddressesCC = { new InternetAddress( toAddressCC ) };
        //msg.setRecipients( Message.RecipientType.CC, toAddressesCC );

        msg.setSubject( subject );
        msg.setSentDate( new Date() );

        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent( message, "text/html" );

        // location - C:\DHIS2_HOME\dhis2_developer_manual.pdf
        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart( messageBodyPart );

        // Part two is attachment
        messageBodyPart = new MimeBodyPart();
        String filename = attachedFile;
        DataSource source = new FileDataSource( filename );
        messageBodyPart.setDataHandler( new DataHandler( source ) );
        //messageBodyPart.setFileName( filename );
        messageBodyPart.setFileName(new File(filename).getName());
        multipart.addBodyPart( messageBodyPart );
       
        // sets the multi-part as e-mail's content
        msg.setContent( multipart );

        // sends the e-mail
        Transport.send( msg );
    }

    // for multiple attachments
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
