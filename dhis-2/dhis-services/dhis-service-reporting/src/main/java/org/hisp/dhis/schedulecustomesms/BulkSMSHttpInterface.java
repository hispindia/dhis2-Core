package org.hisp.dhis.schedulecustomesms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserGroupService;



/**
 * @author Mithilesh Kumar Thakur
 */
public class BulkSMSHttpInterface
{
    public static final String SMS_USER_GROUP_ID = "SMS_USER_GROUP_ID";//
    
    private String username, password, phoneNo, senderName;

    private URL url;

    private String url_string, data, response = "";

    Properties properties;
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    
    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }
    
    private UserGroupService userGroupService;

    public void setUserGroupService( UserGroupService userGroupService )
    {
        this.userGroupService = userGroupService;
    }

    // sending message to single mobile no
    public String sendMessage( String message, String phoneNo ) throws MalformedURLException, IOException
    {
        if (message==null || phoneNo==null)
        {
            return "either message or phone no null";
        }
        
        else if (message.equalsIgnoreCase( "") || phoneNo.equalsIgnoreCase( "") )
        {
            return "either message or phone no empty";
        }
        
        
        username = "hispindia";
        password = "hisp1234";
        senderName = "HSSPIN";
        
        //String token = "2Ca4N06OnVfBBl0BMemc";
        String token="fzuvg0cm8J6DJdFTP2kr";
        String from = "infoSMS";
        
        //Populating the data according to the api link
        
        //data = "username=" + username + "&password=" + password + "&sendername=" + senderName + "&mobileno=" + phoneNo + "&message=" + message;
        
        data = "token=" + token + "&from=" + from + "&to=" + phoneNo + "&text=" + message;
        
        //data = "username=" + username + "&password=" + password +  "&to=" + phoneNo + "&from=" + senderName + "&text=" + message;
        
        //http://myvaluefirst.com/smpp/sendsms?username=nrhmhttp&password=nrhm1234&to=9643208387&from=NRHMHR&text=hi

        //this link is used for sending sms(there are different links for different functions.refer to the api for more details)
        //url_string = "http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?";
        url_string = "http://api.sparrowsms.com/v2/sms/?";
        
        url = new URL( url_string );
        URLConnection conn = url.openConnection();
        conn.setDoOutput( true );
        
        //sending data:
        OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream() );
        out.write( data );
        out.flush();

        //recieving response:
        InputStreamReader in = new InputStreamReader( conn.getInputStream() );
        BufferedReader buff_in = new BufferedReader( in );
        while ( buff_in.ready() )
        {
            response += buff_in.readLine() + "   ";
            //System.out.println( response + " " + data );
        }

        buff_in.close();
        out.close();

        return response;
    }
    
    // sending message to single mobile no
    public String sendSMS( String message, String phoneNo )
        throws UnsupportedEncodingException
    {
        String resopnseString = "";
        System.out.println( phoneNo + " -- 1 -- " + message );
        // System.out.println(encodeMessage(new String(message.getBytes())));
        try
        {
            String token = "fzuvg0cm8J6DJdFTP2kr";
            //String token = "2Ca4N06OnVfBBl0BMemc";
            String from = "infoSMS";
            data = "token=" + token + "&from=" + from + "&to=" + phoneNo + "&text=" + message;
            
            //http://api.sparrowsms.com/v2/sms/?token=fzuvg0cm8J6DJdFTP2kr&from=infoSMS&to=9898989898&text=testMsg
                
            // Send data
            //HttpURLConnection conn = (HttpURLConnection) new URL( url_string ).openConnection();
            
            HttpURLConnection conn = (HttpURLConnection) new URL( "http://api.sparrowsms.com/v2/sms/?" ).openConnection();
            
            //Populating the data according to the api link
            
            //data = "username=" + username + "&password=" + password + "&sendername=" + senderName + "&mobileno=" + phoneNo + "&message=" + message;
            
            conn.setDoOutput( true );
            conn.setRequestMethod( "POST" );
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
        
    // sending message to multiple mobile no
    public String sendMessages( String message, List<String> phonenos ) throws MalformedURLException, IOException
    {

        Iterator<String> it = phonenos.iterator();

        while ( it.hasNext() )
        {
            if ( phoneNo == null )
            {
                phoneNo = (String) it.next();
            } 
            
            else
            {
                phoneNo += "," + it.next();
            }
        }
        
        //System.out.println(" Mobile No -------------------->"+ phoneNo );

        data = "username=" + username + "&password=" + password + "&sendername=" + senderName + "&mobileno=" + phoneNo + "&message=" + message;

        //for sending multiple sms (same as single sms)
        //url_string = "http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?";
        
        url_string = "http://myvaluefirst.com/smpp/sendsms?";
        
        url = new URL( url_string );
        URLConnection conn = url.openConnection();
        conn.setDoOutput( true );
        
        //System.out.println(" URL -------------------->"+ url_string );
        
        //System.out.println(" Data -------------------->"+ data );
        
        OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream() );
        out.write( data );
        out.flush();

        InputStreamReader in = new InputStreamReader( conn.getInputStream() );
        BufferedReader buff_in = new BufferedReader( in );

        while ( buff_in.ready() )
        {
            response += buff_in.readLine() + "   ";
            //System.out.println( response + " " + data );

        }

        buff_in.close();
        out.close();

        return response;

    }   
    
    // get phoneNo of users
    public List<String> getUsersMobileNumber( Integer organisationUnitId )
    {
        List<String> mobileNumbers = new ArrayList<String>();
        
        System.out.println(" OrgUnit Id in SMS Service " + organisationUnitId );
        
        System.out.println(" Organisation Unit Service " + organisationUnitService );
        
        List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();
        
        //List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitBranch( organisationUnitId ) );
        
        List<User> orgUnitUserList = new ArrayList<User>();
        for( OrganisationUnit orgUnit : orgUnitList )
        {
            if( orgUnit.getUsers() != null && orgUnit.getUsers().size() > 0 )
            {
                orgUnitUserList.addAll( orgUnit.getUsers() );
            }
        }
        
        // SMS user Details
        //Constant smsUserGroupConstant = constantService.getConstantByName( SMS_USER_GROUP_ID );
        Constant smsUserGroupConstant = constantService.getConstant( SMS_USER_GROUP_ID );
        
        UserGroup userGroup = userGroupService.getUserGroup( (int) smsUserGroupConstant.getValue() );
        List<User> smsUsers = new ArrayList<User>( userGroup.getMembers() );
        
        smsUsers.retainAll( orgUnitUserList );
        
        try
        {
            for( User user : smsUsers )
            {
                if( user.getPhoneNumber() != null && user.getPhoneNumber().equalsIgnoreCase( "" ) )
                {
                    mobileNumbers.add( user.getPhoneNumber()  );
                }
            }
            
            System.out.println("-------------------- > " + mobileNumbers );
            
            return mobileNumbers;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal OrganisationUnit id", e );
        }
        
    }
}
