package org.hisp.dhis.googlesheet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;

/**
 * @author Mithilesh Kumar Thakur
 */
public class GoogleSheetConfig
{

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Collections.singletonList( SheetsScopes.SPREADSHEETS );

    private static HttpTransport httpTransport;

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private String APPLICATION_NAME;

    public String getAPPLICATION_NAME()
    {
        return APPLICATION_NAME;
    }

    public void setAPPLICATION_NAME( String aPPLICATION_NAME )
    {
        APPLICATION_NAME = aPPLICATION_NAME;
    }

    private String SERVICE_ACCOUNT;

    public String getSERVICE_ACCOUNT()
    {
        return SERVICE_ACCOUNT;
    }

    public void setSERVICE_ACCOUNT( String sERVICE_ACCOUNT )
    {
        SERVICE_ACCOUNT = sERVICE_ACCOUNT;
    }

    private String CREDENTIALS_FILE_PATH;

    public String getCREDENTIALS_FILE_PATH()
    {
        return CREDENTIALS_FILE_PATH;
    }

    public void setCREDENTIALS_FILE_PATH( String cREDENTIALS_FILE_PATH )
    {
        CREDENTIALS_FILE_PATH = cREDENTIALS_FILE_PATH;
    }

    private String SPREAD_SHEET_ID;

    public String getSPREAD_SHEET_ID()
    {
        return SPREAD_SHEET_ID;
    }

    public void setSPREAD_SHEET_ID( String sPREAD_SHEET_ID )
    {
        SPREAD_SHEET_ID = sPREAD_SHEET_ID;
    }

    public JsonFactory getJsonFactory()
    {
        return JSON_FACTORY;
    }

    // -------------------------------------------------------------------------
    // Support methods
    // -------------------------------------------------------------------------

    public GoogleCredential getGoogleCredential()
    {
        System.out.println( "In Side config " + CREDENTIALS_FILE_PATH );

        // ServletContext context = null;
        // String absoluteDiskPath = context.getRealPath(CREDENTIALS_FILE_PATH);

        //File f = null;
        //f = new File( CREDENTIALS_FILE_PATH );

        InputStream in = null;
        try
        {
            in = new FileInputStream( CREDENTIALS_FILE_PATH );

        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        try
        {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        }
        catch ( GeneralSecurityException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        System.out.println( "in " + in );

        // Build a service account credential.
        GoogleCredential credential = null;
        try
        {
            credential = new GoogleCredential.Builder().setTransport( httpTransport ).setJsonFactory( JSON_FACTORY )
                .setServiceAccountId( SERVICE_ACCOUNT ).setServiceAccountScopes( SCOPES )
                .setServiceAccountPrivateKeyFromP12File( in ).build();
        }
        catch ( GeneralSecurityException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        System.out.println( "credential " + credential.toString() );
        return credential;

    }

    // service
    public Sheets getService()
    {

        NetHttpTransport HTTP_TRANSPORT = null;
        try
        {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        }
        catch ( GeneralSecurityException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        // HttpTransport httpTransport =
        // GoogleNetHttpTransport.newTrustedTransport();
        // JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        System.out.println( "inside sheet-service " + HTTP_TRANSPORT );

        Sheets service = new Sheets.Builder( HTTP_TRANSPORT, this.getJsonFactory(), getGoogleCredential() )
            .setApplicationName( this.getAPPLICATION_NAME() ).build();

        /*
         * return new Sheets.Builder( httpTransport, jsonFactory,
         * getGoogleCredential() ).setApplicationName(
         * this.getAPPLICATION_NAME() ).build();
         */

        return service;
    }

    // clear sheet data
    public void clear()
        throws IOException
    {
        System.out.println( "In Side clear() "  );
        Sheets service = getService();
        if ( service != null )
        {
            Spreadsheet spreadsheetResponse = service.spreadsheets().get( this.getSPREAD_SHEET_ID() ).setIncludeGridData( false ).execute();
            
            //System.out.println( "In Side clear() spreadsheetResponse -- " + spreadsheetResponse );
            
            for ( Sheet s : spreadsheetResponse.getSheets() )
            {
                String sheet = s.getProperties().getTitle();
                System.out.println( "In Side clear sheet title -- " + sheet );
                String range = sheet + "!A2:Z10000000";
                ClearValuesRequest clearValuesRequest = new ClearValuesRequest();
                service.spreadsheets().values().clear( this.getSPREAD_SHEET_ID(), range, clearValuesRequest ).execute();
            }
        }
    }

}
