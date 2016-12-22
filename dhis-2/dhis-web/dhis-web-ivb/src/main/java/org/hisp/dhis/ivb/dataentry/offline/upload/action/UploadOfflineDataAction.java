package org.hisp.dhis.ivb.dataentry.offline.upload.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.WritableWorkbook;

import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.CSVImportSatus;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroup;

import au.com.bytecode.opencsv.CSVReader;

import com.opensymphony.xwork2.Action;

/**
 * @author BHARATH
 */

public class UploadOfflineDataAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private IVBUtil ivbUtil;

    public void setIvbUtil( IVBUtil ivbUtil )
    {
        this.ivbUtil = ivbUtil;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
    }

    private MessageService messageService;

    public void setMessageService( MessageService messageService )
    {
        this.messageService = messageService;
    }

    private ConfigurationService configurationService;

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    // -------------------------------------------------------------------------
    // Getter & Setter
    // -------------------------------------------------------------------------

    private File upload;

    public void setUpload( File upload )
    {
        this.upload = upload;
    }

    private String fileName;

    public void setUploadFileName( String fileName )
    {
        this.fileName = fileName;
    }

    private String fileFormat;

    public void setFileFormat( String fileFormat )
    {
        this.fileFormat = fileFormat;
    }

    private String message = "";

    public String getMessage()
    {
        return message;
    }

    private CSVImportSatus csvImportStatus;

    public CSVImportSatus getCsvImportStatus()
    {
        return csvImportStatus;
    }

    private String userName;

    public String getUserName()
    {
        return userName;
    }

    private String language;

    public String getLanguage()
    {
        return language;
    }

    private int messageCount;

    public int getMessageCount()
    {
        return messageCount;
    }

    private String adminStatus;

    public String getAdminStatus()
    {
        return adminStatus;
    }

    private String conflict;

    public void setConflict( String conflict )
    {
        this.conflict = conflict;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
    {
        User currentUser = currentUserService.getCurrentUser();

        userName = currentUser.getUsername();

        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }
        messageCount = (int) messageService.getUnreadMessageConversationCount();
        List<UserGroup> userGrps = new ArrayList<UserGroup>( currentUserService.getCurrentUser().getGroups() );
        if ( userGrps.contains( configurationService.getConfiguration().getFeedbackRecipients() ) )
        {
            adminStatus = "Yes";
        }
        else
        {
            adminStatus = "No";
        }

        message += "<br><font color=blue>Importing StartTime : " + new Date() + "  - By "
            + currentUserService.getCurrentUsername() + "</font><br>";

        try
        {

            if ( fileFormat.equalsIgnoreCase( IVBUtil.CSV_FORMAT ) )
            {
                InputStream in = new FileInputStream( upload );
                CSVReader csvReader = new CSVReader( new FileReader( upload ), ',', '\'' );

                List allRows = new ArrayList<String>();
                String[] row = null;
                int count = 0;
                while ( (row = csvReader.readNext()) != null )
                {
                    System.out.println( count++ + " : " + row[0] + " : " + row[1] + " : " + row[2]);
                    allRows.add( row );
                }
                csvReader.close();

                csvImportStatus = ivbUtil.importCSVData( allRows, conflict );

                if ( csvImportStatus != null )
                {
                    message += "<br>Uploaded file name is : " + fileName;
                    message += "<br>Total number of Facilities for Importing : " + csvImportStatus.getFacilityCount();
                    message += "<br>Total number of Facilities that are Imported : "
                        + csvImportStatus.getImportFacilityCount();
                    message += "<br>Total number of Records for Importing : " + csvImportStatus.getTotalCount();
                    message += "<br>Total new records that are imported : " + csvImportStatus.getInsertCount();
                    message += "<br>Total records that are updated : " + csvImportStatus.getUpdateCount();
                    if ( csvImportStatus.getMissingFacilities() != null
                        && !csvImportStatus.getMissingFacilities().trim().equals( "" ) )
                        message += "<br><font color=red>Missing Facilities in DHIS : "
                            + csvImportStatus.getMissingFacilities() + "</font>";
                }
            }            
            else if ( fileFormat.equalsIgnoreCase( IVBUtil.XLS_FORMAT ) )
            {
                String outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator + "temp";
                File newdir = new File( outputReportPath );
                if ( !newdir.exists() )
                {
                    newdir.mkdirs();
                }
                outputReportPath += File.separator + UUID.randomUUID().toString() + ".xls";

                Workbook excelImportFile = Workbook.getWorkbook( upload );
                WritableWorkbook writableExcelImportFile = Workbook.createWorkbook( new File( outputReportPath ),
                    excelImportFile );

                Sheet sheet0 = writableExcelImportFile.getSheet( 0 );
                Sheet sheet1 = writableExcelImportFile.getSheet( 1 );

                String cellStartRange = sheet1.getCell( 0, 0 ).getContents();
                String cellEndRange = sheet1.getCell( 1, 0 ).getContents();

                int colStart = Integer.parseInt( cellStartRange.split( ":" )[0] );
                int rowStart = Integer.parseInt( cellStartRange.split( ":" )[1] );
                int colEnd = Integer.parseInt( cellEndRange.split( ":" )[0] );
                int rowEnd = Integer.parseInt( cellEndRange.split( ":" )[1] );

                Map<String, List<String>> excelCellDataMap = new HashMap<String, List<String>>();
                for ( int rowCount = rowStart; rowCount <= rowEnd; rowCount++ )
                {
                    for ( int colCount = colStart; colCount <= colEnd; colCount++ )
                    {
                        String cellKey = sheet1.getCell( colCount, rowCount ).getContents();

                        if ( cellKey != null && !cellKey.trim().equals( "" ) )
                        {
                            if ( cellKey.split( "#" )[0].equals( "DV" ) )
                            {
                                List<String> cellValues = new ArrayList<String>();
                                String cellValue = sheet0.getCell( colCount, rowCount ).getContents();
                                cellValues.add( cellValue );
                                cellValue = sheet0.getCell( colCount + 1, rowCount ).getContents();
                                cellValues.add( cellValue );
                                cellValue = sheet0.getCell( colCount + 2, rowCount ).getContents();
                                cellValues.add( cellValue );
                                excelCellDataMap.put( cellKey, cellValues );
                            }
                        }
                    }
                }

                writableExcelImportFile.close();

                csvImportStatus = ivbUtil.importXLSData( excelCellDataMap, conflict );

                if ( csvImportStatus != null )
                {
                    message += "<br>Uploaded file name is : " + fileName;
                    message += "<br>Total number of Facilities for Importing : " + csvImportStatus.getFacilityCount();
                    message += "<br>Total number of Facilities that are Imported : "
                        + csvImportStatus.getImportFacilityCount();
                    message += "<br>Total number of Records for Importing : " + csvImportStatus.getTotalCount();
                    message += "<br>Total new records that are imported : " + csvImportStatus.getInsertCount();
                    message += "<br>Total records that are updated : " + csvImportStatus.getUpdateCount();
                    if ( csvImportStatus.getMissingFacilities() != null
                        && !csvImportStatus.getMissingFacilities().trim().equals( "" ) )
                        message += "<br><font color=red>Missing Facilities in DHIS : "
                            + csvImportStatus.getMissingFacilities() + "</font>";
                }

            }
            else if ( fileFormat.equalsIgnoreCase( IVBUtil.XLS_FORMAT_COUNTRY_AS_ROWS ) )
            {
                String outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator + "temp";
                File newdir = new File( outputReportPath );
                if ( !newdir.exists() )
                {
                    newdir.mkdirs();
                }
                outputReportPath += File.separator + UUID.randomUUID().toString() + ".xls";

                Workbook excelImportFile = Workbook.getWorkbook( upload );
                WritableWorkbook writableExcelImportFile = Workbook.createWorkbook( new File( outputReportPath ), excelImportFile );

                Sheet sheet0 = writableExcelImportFile.getSheet( 0 );
                Sheet sheet1 = writableExcelImportFile.getSheet( 1 );

                String cellStartRange = sheet1.getCell( 0, 0 ).getContents();
                String cellEndRange = sheet1.getCell( 1, 0 ).getContents();

                int colStart = Integer.parseInt( cellStartRange.split( ":" )[0] );
                int rowStart = Integer.parseInt( cellStartRange.split( ":" )[1] );
                int colEnd = Integer.parseInt( cellEndRange.split( ":" )[0] );
                int rowEnd = Integer.parseInt( cellEndRange.split( ":" )[1] );

                try
                {
	                if( !sheet0.getCell( 4, rowStart-1 ).getContents().trim().equalsIgnoreCase( "CountryCode" ) || !sheet0.getCell( 6, rowStart-1 ).getContents().trim().equalsIgnoreCase( "IndicatorCode" ) || !sheet0.getCell( 7, rowStart-1 ).getContents().trim().equalsIgnoreCase( "Period" ) )
	                {
	                	message += "<br><font color=red><strong>Importing file format is not supported for the file : " + fileName + "</font></strong>";
	                	message += "<br><br><font color=blue>Importing EndTime : " + new Date() + "  - By " + currentUserService.getCurrentUsername() + "</font>";
	                	return SUCCESS;
	                }
                }
                catch( Exception e )
                {
                	message += "<br><font color=red><strong>Importing file format is not supported for the file : " + fileName + "</font></strong>";
                	message += "<br><br><font color=blue>Importing EndTime : " + new Date() + "  - By " + currentUserService.getCurrentUsername() + "</font>";
                	return SUCCESS;
                }
                
                Map<String, List<String>> excelCellDataMap = new HashMap<String, List<String>>();
                for ( int rowCount = rowStart; rowCount <= rowEnd; rowCount++ )
                {
                	//int colCount = colStart;
                	String ouId = sheet0.getCell( 4, rowCount ).getContents();
                	String deId = sheet0.getCell( 6, rowCount ).getContents();
                	String period = sheet0.getCell( 7, rowCount ).getContents();
                	
                	String value = sheet0.getCell( 8, rowCount ).getContents();
                	String comment = sheet0.getCell( 9, rowCount ).getContents();
                	String ta = sheet0.getCell( 10, rowCount ).getContents();
                	
                	String cellKey = "DV#" + ouId +"#" + deId + "#" + period;
                	List<String> cellValues = new ArrayList<String>();
                	cellValues.add( value );
                	cellValues.add( comment );
                	cellValues.add( ta );
                	
                	excelCellDataMap.put( cellKey, cellValues );
                	/*
                    //for ( int colCount = colStart; colCount <= colEnd; colCount++ )
                    {
                        String cellKey = sheet1.getCell( colCount, rowCount ).getContents();

                        if ( cellKey != null && !cellKey.trim().equals( "" ) )
                        {
                            if ( cellKey.split( "#" )[0].equals( "DV" ) )
                            {
                                List<String> cellValues = new ArrayList<String>();
                                String cellValue = sheet0.getCell( colCount, rowCount ).getContents();
                                cellValues.add( cellValue );
                                cellValue = sheet0.getCell( colCount + 1, rowCount ).getContents();
                                cellValues.add( cellValue );
                                cellValue = sheet0.getCell( colCount + 2, rowCount ).getContents();
                                cellValues.add( cellValue );
                                excelCellDataMap.put( cellKey, cellValues );
                            }
                        }
                    }
                    */
                }

                writableExcelImportFile.close();

                csvImportStatus = ivbUtil.importXLSData( excelCellDataMap, conflict );
                
                /*
                for ( String cellKey : excelCellDataMap.keySet() )
                {
                	System.out.println( cellKey + " -- " + excelCellDataMap.get( cellKey ).get(0) + " -- " + excelCellDataMap.get( cellKey ).get(1) + " -- " + excelCellDataMap.get( cellKey ).get(2) );
                }
                */

                if ( csvImportStatus != null )
                {
                    message += "<br>Uploaded file name is : " + fileName;
                    message += "<br>Total number of Facilities for Importing : " + csvImportStatus.getFacilityCount();
                    message += "<br>Total number of Facilities that are Imported : " + csvImportStatus.getImportFacilityCount();
                    message += "<br>Total number of Records for Importing : " + csvImportStatus.getTotalCount();
                    message += "<br>Total new records that are imported : " + csvImportStatus.getInsertCount();
                    message += "<br>Total records that are updated : " + csvImportStatus.getUpdateCount();
                    if ( csvImportStatus.getMissingFacilities() != null
                        && !csvImportStatus.getMissingFacilities().trim().equals( "" ) )
                        message += "<br><font color=red>Missing Facilities in DHIS : "
                            + csvImportStatus.getMissingFacilities() + "</font>";
                }

            }
            else
            {
                message += "<br><font color=red><strong>Importing file format is not supported for the file : " + fileName + "</font></strong>";
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            message += "<br><font color=red><strong>Please check the file format : " + fileName + "<br>Detailed Log Message: " + e.getMessage() + "</font></strong>";
        }

        message += "<br><br><font color=blue>Importing EndTime : " + new Date() + "  - By " + currentUserService.getCurrentUsername() + "</font>";

        return SUCCESS;
    }
}