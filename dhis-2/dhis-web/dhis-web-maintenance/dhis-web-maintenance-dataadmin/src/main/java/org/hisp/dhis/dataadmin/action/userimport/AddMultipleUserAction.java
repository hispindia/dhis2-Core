package org.hisp.dhis.dataadmin.action.userimport;

import com.opensymphony.xwork2.Action;

import jxl.Sheet;
import jxl.Workbook;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.security.PasswordManager;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Mithilesh Kumar Thakur
 */
public class AddMultipleUserAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    /*
     * private UserStore userStore;
     * 
     * public void setUserStore( UserStore userStore ) { this.userStore =
     * userStore; }
     */
    @Autowired
    private PasswordManager passwordManager;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private UserService userService;

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private String message;

    public String getMessage()
    {
        return message;
    }

    private File file;

    public void setUpload( File file )
    {
        this.file = file;
    }

    private String fileName;

    public String getFileName()
    {
        return fileName;
    }

    public void setUploadFileName( String fileName )
    {
        this.fileName = fileName;
    }

    private List<String> importStatusMsgList = new ArrayList<String>();

    public List<String> getImportStatusMsgList()
    {
        return importStatusMsgList;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
    {
        message = "";
        importStatusMsgList = new ArrayList<String>();

        System.out.println( "File name : " + fileName );
        String fileType = fileName.substring( fileName.indexOf( '.' ) + 1, fileName.length() );

        if ( !fileType.equalsIgnoreCase( "xls" ) )
        {
            message = "The file you are trying to import is not an excel file";

            return SUCCESS;
        }

        Workbook excelImportFile = Workbook.getWorkbook( file );
        int sheetNo = 0;
        Sheet sheet0 = excelImportFile.getSheet( sheetNo );
        Integer rowStart = Integer.parseInt( sheet0.getCell( 6, 0 ).getContents() );
        Integer rowEnd = Integer.parseInt( sheet0.getCell( 6, 1 ).getContents() );
        System.out.println( "User  Creation Start Time is : " + new Date() );
        System.out.println( "Row Start : " + rowStart + " ,Row End : " + rowEnd );
        
        /*
        for ( int i = rowStart; i <= rowEnd; i++ )
        {
            String responseMessage = "";
            String mobileNo = sheet0.getCell( 1, i ).getContents();
            
            System.out.println( rowStart + " -- Mobiel No -- " + mobileNo );
            
            responseMessage = sendSMS( mobileNo );
            importStatusMsgList.add( responseMessage );
        }
        */

        
        int orgunitcount = 0;
        for( int i = rowStart ; i <= rowEnd ; i++ )
        {
            Integer orgUnitId = Integer.parseInt( sheet0.getCell( 0, i ).getContents() );
            //String orgUnitname = sheet0.getCell( 1, i ).getContents();
            //String orgUnitCode = sheet0.getCell( 2, i ).getContents();
            String userName = sheet0.getCell( 1, i ).getContents();
            String passWord = sheet0.getCell( 2, i ).getContents();
            Integer userRoleId = Integer.parseInt( sheet0.getCell( 3, i ).getContents() );
            
            OrganisationUnit orgUId = organisationUnitService.getOrganisationUnit( orgUnitId );
            Set<OrganisationUnit> orgUnits = new HashSet<OrganisationUnit>();
            orgUnits.add( orgUId );
            
            Collection<User> tempUserList = orgUId.getUsers();
            int flag = 0;
            if ( tempUserList != null )
            {
                for ( User u : tempUserList )
                {
                    //UserCredentials uc = userStore.getUserCredentials( u );
                    //UserCredentials uc = userService.getUserCredentials( u );
                    UserCredentials uc = userService.getUserCredentialsByUsername( u.getUsername() );
                    if ( uc != null && uc.getUsername().equalsIgnoreCase( userName ) )
                    {
                        System.out.println( uc.getUsername() + " ALREADY EXITS -- " + userName );
                        importStatusMsgList.add( "User Name -- " + userName + " ALREADY EXITS" );
                        flag = 1;
                    }
                }
            }
            
            if ( flag == 1 )
            {
                //System.out.println( userName + " ALREADY EXITS" );
                //message += "<font color=red><strong>"+ userName +  " ALREADY EXITS .<br></font></strong>";
                //importStatusMsgList.add( userName + " ALREADY EXITS" );
                continue;
            }
            
            User user = new User();
            user.setSurname( userName );
            //user.setFirstName( orgUnitCode );
            user.setFirstName( userName );
            user.setOrganisationUnits( orgUnits );
            user.setDataViewOrganisationUnits( orgUnits );


            UserCredentials userCredentials = new UserCredentials();
            userCredentials.setUser( user );
            userCredentials.setUsername( userName );
            //userCredentials.setPassword( passwordManager.encodePassword( userName, passWord ) );
            userCredentials.setPassword( passwordManager.encode( passWord ) );
            
            UserAuthorityGroup group = userService.getUserAuthorityGroup( userRoleId );
            
            //UserAuthorityGroup group = userStore.getUserAuthorityGroup( userRoleId );
            userCredentials.getUserAuthorityGroups().add( group );

            //userStore.addUser( user );
            //userStore.addUserCredentials( userCredentials );
            
            userService.addUser( user );
            userService.addUserCredentials( userCredentials );
            System.out.println( userName + " Created" );
            orgunitcount++;

            importStatusMsgList.add( "User Name -- " + userName + " Created" );
            /*
            if( flag != 1 )
            {
                //message += "<font color=red><strong>"+ userName +  " Created .<br></font></strong>";

                //importStatusMsgList.add( userName + " Created" );
            }
            */
        }

        excelImportFile.close();
        
        System.out.println( "**********************************************" );
        System.out.println( "MULTIPLE USER CREATION IS FINISHED" );
        System.out.println( "Total No of User Created : -- " + orgunitcount );
        System.out.println( "**********************************************" );
        System.out.println( "User  Creation End Time is : " + new Date() );

        //message += "<font color=red><strong>" + orgunitcount +  " : User Created .<br></font></strong>";

        importStatusMsgList.add( "Total No of User Created -- " + orgunitcount );
        
        return SUCCESS;
    }

    public String sendSMS( String mobileNo )
        throws UnsupportedEncodingException
    {
        String resopnseString = "";
        //System.out.println( mobileNo + " -- 1 -- " + message );
        // System.out.println(encodeMessage(new String(message.getBytes())));
        try
        {
            // Construct data
            
            message = "Your phone number has been successfully registered for MDA.You can send report. Format-10.20.30 10.20.30 10";
            String user = "username=" + "harsh.atal@gmail.com";
            String hash = "&hash=" + "04fa1b5546432e99162704a7025403879d589271";
            message = "&message=" + message;
            String sender = "&sender=" + "NVBDCP";
            //String numbers = "&numbers=" + "919654232779&test=1";
            //String numbers = "&numbers=" + mobileNo + "&test=1";
            //String numbers = "&numbers=" + mobileNo + "&unicode=1";
            String numbers = "&numbers=" + mobileNo;

            // Send data
            HttpURLConnection conn = (HttpURLConnection) new URL( "http://api.textlocal.in/send/?" ).openConnection();
            String data = user + hash + message + sender + numbers;
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

            System.out.println( stringBuffer.toString() );
        }

        catch ( Exception e )
        {
            System.out.println( "Error SMS " + e );
        }

        return resopnseString;

    }

}
