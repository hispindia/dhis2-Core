package org.hisp.dhis.excelimportmultiplefacilities.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.hisp.dhis.excelimport.util.ReportService;

import com.opensymphony.xwork2.Action;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;

public class ExcelImportMultipleFacilitiesFormResultAction  implements Action
{
    /*
    private final String PHCFILE = "phc";

    private final String SC = "sc";
    
    private final String SOPPHC = "sopphc";
    
    private final String SOPSC = "sopsc";
    
    private final String PVT = "pvt";
	
    private final String PVTMONTHLY = "private";
    */
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ReportService reportService;

    public void setReportService( ReportService reportService )
    {
        this.reportService = reportService;
    }
    
    // -------------------------------------------------------------------------
    // Input and Output Parameters
    // -------------------------------------------------------------------------
    
    private InputStream inputStream;

    public InputStream getInputStream()
    {
        return inputStream;
    }
    
    private String fileName;

    public String getFileName()
    {
        return fileName;
    }
    
    private String fileType;
    
    public void setFileType( String fileType )
    {
        this.fileType = fileType;
    }

    private String raFolderName;
    
    private String exportFileName;
    
    
    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    public OrganisationUnitService getOrganisationUnitService()
    {
        return organisationUnitService;
    }

    private List<String> orgUnitListCB;

    public void setOrgUnitListCB( List<String> orgUnitListCB )
    {
        this.orgUnitListCB = orgUnitListCB;
    }

    private String ouIDTB;
    
    public void setOuIDTB( String ouIDTB )
    {
        this.ouIDTB = ouIDTB;
    }
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    
    public String execute() throws Exception
    {
        
        raFolderName = reportService.getRAFolderName();
        
        /*
        if ( fileType.equalsIgnoreCase( PHCFILE ))
        {
            exportFileName = "PHC_CHC_DH_SDH Format.xls";
        }
        
        else if ( fileType.equalsIgnoreCase( SC ) )
        {
            exportFileName = "SC Format.xls";
        }
        
	else if ( fileType.equalsIgnoreCase( PVTMONTHLY ) )
        {
            exportFileName = "Private_Monthly.xls";
        }
		
        else if ( fileType.equalsIgnoreCase( SOPPHC ) )
        {
            exportFileName = "SOP PHC 22-10-2012.doc";
        }
        
        else if ( fileType.equalsIgnoreCase( SOPSC ) )
        {
            exportFileName = "SOP Subcentre 22-10-2012.doc";
        }
        
        else if ( fileType.equalsIgnoreCase( PVT ) )
        {
            exportFileName = "Private_Consolidated.xls";
        }
        */
	
        //System.out.println( fileType );
        
        
       OrganisationUnit selectedOrgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );
        
        System.out.println( " selectedPeriod id is : " + " selectedOrgUnit id is " + selectedOrgUnit.getId() );
        
        
        exportFileName = fileType;
        
        String excelImportFolderName = "excelimport";
        
       // String inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName + File.separator + "template" + File.separator + excelImportFolderName;
        
        String inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName  + File.separator  + excelImportFolderName + File.separator  + "template" + File.separator  + exportFileName ;
        
        //String inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName  + File.separator  + excelImportFolderName + File.separator + "template" + File.separator + exportFileName; ;
        
        //String outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator +  Configuration_IN.DEFAULT_TEMPFOLDER;
        
        //outputReportPath += File.separator + UUID.randomUUID().toString() + ".xls";
        
        //fileName = exportFileName.replace( ".xls", "" );
        //fileName += "_Export" + ".xls";
        
        fileName = exportFileName;
        
        inputStream = new BufferedInputStream( new FileInputStream( inputTemplatePath ),1024 );
        
        return SUCCESS;
    }
}





