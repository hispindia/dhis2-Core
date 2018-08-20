package org.hisp.dhis.userimport.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.security.PasswordManager;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;
/**
 * @author Mithilesh Kumar Thakur
 */
public class AddMultipleUserAction implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

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

        System.out.println( "File name : " + fileName  );
        String fileType = fileName.substring( fileName.indexOf( '.' ) + 1, fileName.length() );

        
        if ( !fileType.equalsIgnoreCase( "xlsx" ) )
        {
            message = "The file you are trying to import is not an excel file";

            return SUCCESS;
        }

        //WritableSheet sheet0 = outputReportWorkbook.getSheet( sheetNo );
        //Workbook excelImportFile = Workbook.getWorkbook( file );
        //int sheetNo = 0 ;
        
        //Sheet sheet0 = excelImportFile.getSheet( sheetNo );
       
        /*
        Integer rowStart = Integer.parseInt( sheet0.getCell( 6, 0 ).getContents() );
        Integer rowEnd = Integer.parseInt( sheet0.getCell( 6, 1 ).getContents() );
        System.out.println( "User  Creation Start Time is : " + new Date() );
        System.out.println( "Row Start : " + rowStart + " ,Row End : "  + rowEnd );
        */
        // Finds the workbook instance for XLSX file
        XSSFWorkbook workBook = new XSSFWorkbook (file);
        // Return first sheet from the XLSX workbook
        XSSFSheet sheet = workBook.getSheetAt(0);
        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();
        
        // Get iterator to all the rows in current sheet
        /*
        Iterator<Row> rowIterator = sheet.iterator();
        // Traversing over each row of XLSX file
        while (rowIterator.hasNext())
        {
            Row row = rowIterator.next();
            // For each row, iterate through each columns
              
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext())
            {
                Cell cell = cellIterator.next();
                String cellValue = dataFormatter.formatCellValue(cell);
                System.out.print(cellValue + "\t");
            }
        }
        */
        
        for( int tempRowStart = 1 ; tempRowStart < sheet.getLastRowNum() + 1 ; tempRowStart++ )
        {
            Row row = sheet.getRow( tempRowStart );
            
            Cell cellFirstName = row.getCell( 0 );
            String userFirstName = dataFormatter.formatCellValue( cellFirstName );
            
            Cell cellSurName = row.getCell( 1 );
            String userSurName = dataFormatter.formatCellValue( cellSurName );
            
            Cell cellUserName = row.getCell( 2 );
            String userName = dataFormatter.formatCellValue( cellUserName );
            
            Cell cellrawPassword = row.getCell( 3 );
            String rawPassword = dataFormatter.formatCellValue( cellrawPassword );
            
            Cell celluserRolesUid = row.getCell( 4 );
            String userRolesUid = dataFormatter.formatCellValue( celluserRolesUid );
            
            
            Cell cellorgUnitUid = row.getCell( 5 );
            String orgUnitUid = dataFormatter.formatCellValue( cellorgUnitUid );
            
            OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitUid );
            Set<OrganisationUnit> orgUnits = new HashSet<OrganisationUnit>();
            orgUnits.add( orgUnit );
            
            System.out.println( userFirstName + " " + userSurName + " "  + userName + " "  + rawPassword + " "  + userRolesUid + " "  + orgUnitUid );
            System.out.println( " "  + orgUnit.getName() );
            
            Collection<User> tempUserList = new ArrayList<User>( orgUnit.getUsers() ) ;
            int flag = 0;
            if ( tempUserList != null && tempUserList.size() > 0 )
            {
                for ( User u : tempUserList )
                {
                    UserCredentials uc = userService.getUserCredentialsByUsername( u.getName() );
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
                continue;
            }
            
            // ---------------------------------------------------------------------
            // User credentials and user
            // ---------------------------------------------------------------------
            
            User user = new User();
            user.setSurname( StringUtils.trimToNull( userSurName ) );
            user.setFirstName( StringUtils.trimToNull( userFirstName ) );
            UserCredentials userCredentials = new UserCredentials();
            userCredentials.setUserInfo( user );
            user.setUserCredentials( userCredentials );
            //userCredentials.setUser( user );
            userCredentials.setUsername( StringUtils.trimToNull( userName ) );
           
            // ---------------------------------------------------------------------
            // Organisation units
            // ---------------------------------------------------------------------
            
            user.setOrganisationUnits( orgUnits );
            user.setDataViewOrganisationUnits( orgUnits );

            //userCredentials.setPassword( passwordManager.encode( passWord ) );
            userService.encodeAndSetPassword( userCredentials, StringUtils.trimToNull( rawPassword ) );
            
            // ---------------------------------------------------------------------
            // User roles
            // ---------------------------------------------------------------------

            Set<UserAuthorityGroup> userAuthorityGroups = new HashSet<>();

            for ( String uid : userRolesUid.split( "," ) )
            {
                userAuthorityGroups.add( userService.getUserAuthorityGroup( uid ) );
            }

            userService.canIssueFilter( userAuthorityGroups );

            userCredentials.setUserAuthorityGroups( userAuthorityGroups );
            
            userService.addUser( user );
            userService.addUserCredentials( userCredentials );
            System.out.println( userName + " Created" );
            

            importStatusMsgList.add( "User Name -- " + userName + " Created" );
            
        }
        
        
        
        /*
        int orgunitcount = 0;
        for( int i = rowStart ; i <= rowEnd ; i++ )
        {
            Integer orgUnitId = Integer.parseInt( sheet0.getCell( 0, i ).getContents() );
            
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
                    UserCredentials uc = userService.getUserCredentials( u );
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
                continue;
            }
            
            
            User user = new User();
            user.setSurname( userName );
            
            user.setFirstName( userName );
            user.setOrganisationUnits( orgUnits );
            user.setDataViewOrganisationUnits( orgUnits );


            UserCredentials userCredentials = new UserCredentials();
            userCredentials.setUser( user );
            userCredentials.setUsername( userName );
           
            userCredentials.setPassword( passwordManager.encode( passWord ) );
            
            UserAuthorityGroup group = userService.getUserAuthorityGroup( userRoleId );
            
           
            userCredentials.getUserAuthorityGroups().add( group );

         
            
            userService.addUser( user );
            userService.addUserCredentials( userCredentials );
            System.out.println( userName + " Created" );
            orgunitcount++;

            importStatusMsgList.add( "User Name -- " + userName + " Created" );
        }
        */
        
        //excelImportFile.close();
        workBook.close();
        
        System.out.println( "**********************************************" );
        System.out.println( "MULTIPLE USER CREATION IS FINISHED" );
        //System.out.println( "Total No of User Created : -- " + orgunitcount );
        System.out.println( "**********************************************" );
        System.out.println( "User  Creation End Time is : " + new Date() );

        //message += "<font color=red><strong>" + orgunitcount +  " : User Created .<br></font></strong>";

        //importStatusMsgList.add( "Total No of User Created -- " + orgunitcount );

        return SUCCESS;
    }
}
