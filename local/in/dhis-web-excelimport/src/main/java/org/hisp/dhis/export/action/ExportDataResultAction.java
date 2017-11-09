package org.hisp.dhis.export.action;

import static org.hisp.dhis.util.ConversionUtils.getIdentifiers;
import static org.hisp.dhis.util.TextUtils.getCommaDelimitedString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hisp.dhis.config.Configuration_IN;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.excelimport.util.ExcelImport;
import org.hisp.dhis.excelimport.util.ExcelImportService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSetStore;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.WeeklyPeriodType;
import org.hisp.dhis.reports.ReportService;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.opensymphony.xwork2.Action;

public class ExportDataResultAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    private static final String FILE_HEADER = "dataelement,period,orgunit,categoryoptioncombo,attributeoptioncombo,value,storedby,lastupdated,comment,followup";

    private static final String COMMA_DELIMITER = ",";

    private static final String NEW_LINE_SEPARATOR = "\n";
    
    @Autowired
    private OrganisationUnitGroupSetStore organisationUnitGroupSetStore;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PeriodService periodService;

    private ExcelImportService excelImportService;

    public void setExcelImportService( ExcelImportService excelImportService )
    {
        this.excelImportService = excelImportService;
    }

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private OrganisationUnitGroupService organisationUnitGroupService;
    
    @Autowired
    private DataElementService dataElementService;
    
    @Autowired
    private DataElementCategoryService dataElementCategoryService;

    // private DatabaseInfoProvider databaseInfoProvider;
    //
    // public void setDatabaseInfoProvider( DatabaseInfoProvider
    // databaseInfoProvider )
    // {
    // this.databaseInfoProvider = databaseInfoProvider;
    // }

    // -------------------------------------------------------------------------
    // Input/Output
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

    private Integer year;

    private String org;

    public void setorg( String org )
    {
        this.org = org;
    }

    public void setYear( Integer year )
    {
        this.year = year;
    }

    private String orgUnitGroupId;

    public void setOrgUnitGroupId( String orgUnitGroupId )
    {
        this.orgUnitGroupId = orgUnitGroupId;
    }

    private String weeklyPeriodTypeName;

    private String deCodesXMLFileName = "";

    private SimpleDateFormat simpleDateFormat;

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    OrganisationUnitGroup orgUnitGroup;
    OrganisationUnitGroup orgUnitGroup1;

    String orgGroup ,orgGroupUid;

    private ArrayList<OrganisationUnitGroup> organisationUnitGroups;
 
    @SuppressWarnings( "unchecked" )
    public String execute()
        throws Exception
    {
       // System.out.println( "orgunit --" + org );
        OrganisationUnitGroupSet OrganisationUnitGroupSet = organisationUnitGroupSetStore.getByCode( "ExcelExportGroupSet" );
        organisationUnitGroups = new ArrayList<OrganisationUnitGroup>( OrganisationUnitGroupSet.getOrganisationUnitGroups());
        
        System.out.println( "Start Time : " + new Date() );
        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );

        String startDate = "01-01-" + year;
        String endDate = "31-12-" + year;

        String userName = currentUserService.getCurrentUsername();
        List<ExcelImport> excelExportDesignList = new ArrayList<ExcelImport>();
        // excelExportDesignList = new ArrayList<ExcelImport>(
        // excelImportService.getExcelImportDesignDesign( deCodesXMLFileName )
        // );

        // collect dataElementIDs by commaSepareted

        // period information
        weeklyPeriodTypeName = WeeklyPeriodType.NAME;
        PeriodType periodType = periodService.getPeriodTypeByName( weeklyPeriodTypeName );

        List<Period> periods = new ArrayList<Period>();

        String periodIdsByComma = "-1";
        if ( year != null )
        {
            String isoPeriodString = year.toString();

            Period period = periodService.reloadIsoPeriod( isoPeriodString );

            if ( period != null )
            {
                periods = new ArrayList<Period>( periodService.getPeriodsBetweenDates( periodType,
                    period.getStartDate(), period.getEndDate() ) );
            }

            Collection<Integer> periodIds = new ArrayList<Integer>( getIdentifiers( Period.class, periods ) );
            periodIdsByComma = getCommaDelimitedString( periodIds );
        }

      //System.out.println("outside value"+orgUnitGroupId);

        Map<String, String> aggDeMap = new HashMap<String, String>();
        
        
        List<Map<String,String>> list = new ArrayList<Map<String,String>>();
        
        
        List<String> getUID = new ArrayList<String>();
 
        
        List<Map<String,String>> listUid = new ArrayList<Map<String,String>>();
        
        
        if ( orgUnitGroupId != null && !orgUnitGroupId.equalsIgnoreCase( "ALL" )
            && !orgUnitGroupId.equalsIgnoreCase( "NA" ) )
        {

            deCodesXMLFileName = "exportData.xml";
            excelExportDesignList = new ArrayList<ExcelImport>(
            excelImportService.getExcelImportDesignDesign( deCodesXMLFileName ) );
            String dataElmentIdsByComma = excelImportService.getDataelementIds( excelExportDesignList );
            System.out.println( " ALL dataElmentIdsByComma  --  " + dataElmentIdsByComma );
            
            orgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( orgUnitGroupId );
            
            
            
            List<OrganisationUnit> groupMember = new ArrayList<OrganisationUnit>( orgUnitGroup.getMembers() );
            List<Integer> orgaUnitIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, groupMember ) );
            String orgaUnitIdsByComma = getCommaDelimitedString( orgaUnitIds );
            aggDeMap.putAll( excelImportService.getAggDataFromDataValueTable( orgaUnitIdsByComma, dataElmentIdsByComma,
                periodIdsByComma ) );
          
            
            orgGroup = orgUnitGroup.getUid();

            // System.out.println( "neworgGroup value --" +neworgGroup );

        }
        else
        {
            deCodesXMLFileName = "exportData.xml";
            excelExportDesignList = new ArrayList<ExcelImport>( excelImportService.getExcelImportDesignDesign( deCodesXMLFileName ) );
            String dataElmentIdsByComma = excelImportService.getDataelementIds( excelExportDesignList );
            System.out.println( " Selected dataElmentIdsByComma  --  " + dataElmentIdsByComma );
            
            
            // orgUnit Details

            
              
                for( OrganisationUnitGroup x: organisationUnitGroups)
                {
                orgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( x.getUid() );
               
                List<OrganisationUnit> groupMember = new ArrayList<OrganisationUnit>( orgUnitGroup.getMembers() );
                List<Integer> orgaUnitIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, groupMember ) );
                String orgaUnitIdsByComma = getCommaDelimitedString( orgaUnitIds );
                list.add( ( excelImportService.getAggDataFromDataValueTable( orgaUnitIdsByComma, dataElmentIdsByComma,
                    periodIdsByComma ) ) );
               
               
                //System.out.println( "x.getUid()--"+x.getUid() );
              
               }
                
            
            

            
        }

        // deCodesXMLFileName = "exportData.xml";

        String outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator + Configuration_IN.DEFAULT_TEMPFOLDER;
        File file = new File( outputReportPath );
        if ( !file.exists() )
        {
            file.mkdirs();
        }
        outputReportPath += File.separator + UUID.randomUUID().toString() + ".csv";

        // System.out.println( "periodIdsByComma is " + "  " + periodIdsByComma
        // + " --" );

        FileWriter fileWriter = null;
        fileWriter = new FileWriter( outputReportPath );
        // for header
        fileWriter.append( FILE_HEADER.toString() );

        // Add a new line separator after the header
        fileWriter.append( NEW_LINE_SEPARATOR );

        Iterator<ExcelImport> excelExportDesignIterator = excelExportDesignList.iterator();
        while ( excelExportDesignIterator.hasNext() )
        {
            ExcelImport exceEmportDesign = (ExcelImport) excelExportDesignIterator.next();

            String deCodeString = exceEmportDesign.getExpression();
            String tempStr = "",neworgGroup="";
           
            // int tempRowNo = report_inDesign.getRowno();
            // int tempColNo = report_inDesign.getColno();

            try
            {

                
                if ( orgUnitGroupId != null && !orgUnitGroupId.equalsIgnoreCase( "ALL" )
            && !orgUnitGroupId.equalsIgnoreCase( "NA" ) ){
                    tempStr = getAggVal( deCodeString, aggDeMap );

                    //System.out.println("deCodeString----"+deCodeString);
                    //System.out.println("getOrgunitgroup---"+exceEmportDesign.getOrgunitgroup());
                    if ( !tempStr.equalsIgnoreCase( "0" ) )
                    {
                        fileWriter.append( exceEmportDesign.getDataelement() );
                        fileWriter.append( COMMA_DELIMITER );
                        fileWriter.append( year.toString() );
                        fileWriter.append( COMMA_DELIMITER );
                        
                        if ( orgUnitGroupId.equalsIgnoreCase( "VnGNfO08w38" )   )
                        {
                            // System.out.println("in the if");
                            neworgGroup = "CL205-0000";
                        }
                        else if ( orgUnitGroupId.equalsIgnoreCase( "oPJQbzZ20Ff" ) )
                        {
                            neworgGroup = "OU205-0000";
                        }
                        else if ( orgUnitGroupId.equalsIgnoreCase( "FrKiTIjDUxU" ) )
                        {
                            neworgGroup = "AS205-0000";
                        }
                        else if ( orgUnitGroupId.equalsIgnoreCase( "GhuHmwRnPBs" ) )
                        {
                            neworgGroup = "CB205-0000";
                        }
                       
                        fileWriter.append( neworgGroup );
                        fileWriter.append( COMMA_DELIMITER );

                        fileWriter.append( exceEmportDesign.getCategoryoptioncombo() );
                        fileWriter.append( COMMA_DELIMITER );
                        fileWriter.append( exceEmportDesign.getAttributeoptioncombo() );
                        fileWriter.append( COMMA_DELIMITER );

                        fileWriter.append( tempStr );
                        fileWriter.append( COMMA_DELIMITER );

                        fileWriter.append( userName );
                        fileWriter.append( COMMA_DELIMITER );
                        fileWriter.append( simpleDateFormat.format( new Date() ) );
                        fileWriter.append( COMMA_DELIMITER );
                        fileWriter.append( exceEmportDesign.getComment() );
                        fileWriter.append( COMMA_DELIMITER );
                        fileWriter.append( "" );
                        fileWriter.append( COMMA_DELIMITER );

                        fileWriter.append( NEW_LINE_SEPARATOR );
                }
                    
                    else
                    {
                       
                    }
                }
                    else{
                        
                                              
                         int i=0;
                        for(Map<String, String> li :list)
                        {
                        
                           tempStr = getAggVal( deCodeString, li );
                           String val=exceEmportDesign.getOrgunitgroup();
                           
                           
                            if ( !tempStr.equalsIgnoreCase( "0" ) )
                            {
                                fileWriter.append( exceEmportDesign.getDataelement() );
                                fileWriter.append( COMMA_DELIMITER );
                                fileWriter.append( year.toString() );
                                fileWriter.append( COMMA_DELIMITER );
                              
                             
                                    // System.out.println("in the if");
                               

                                    if ( i==0   )
                                    {
                                        // System.out.println("in the if");
                                        neworgGroup = "CL205-0000";
                                    }
                                    else if ( i==1 )
                                    {
                                        neworgGroup = "AS205-0000";
                                        
                                    }
                                    else if ( i==2 )
                                    {
                                        neworgGroup = "OU205-0000";
                                    }
                                    else if ( i==3 )
                                    {
                                        neworgGroup = "CB205-0000";
                                    }
                                    
                                    fileWriter.append( neworgGroup );                                  
                                    fileWriter.append( COMMA_DELIMITER );
                                 
                                fileWriter.append( exceEmportDesign.getCategoryoptioncombo() );
                                fileWriter.append( COMMA_DELIMITER );
                                fileWriter.append( exceEmportDesign.getAttributeoptioncombo() );
                                fileWriter.append( COMMA_DELIMITER );

                                fileWriter.append( tempStr );
                                fileWriter.append( COMMA_DELIMITER );

                                fileWriter.append( userName );
                                fileWriter.append( COMMA_DELIMITER );
                                fileWriter.append( simpleDateFormat.format( new Date() ) );
                                fileWriter.append( COMMA_DELIMITER );
                                fileWriter.append( exceEmportDesign.getComment() );
                                fileWriter.append( COMMA_DELIMITER );
                                fileWriter.append( "" );
                                fileWriter.append( COMMA_DELIMITER );

                                fileWriter.append( NEW_LINE_SEPARATOR );
                            
                            
                           
                            
                            }
                            
                            else
                            {
                                
                            }
                        
                        
                            i++;
                    }
                        
                    }
                }
                        
                        catch ( Exception e )
                        {
                           
                            System.out.println( "Error in CsvFileWriter !!!" );
                            e.printStackTrace();
                        } 
                /*
                 * Map<String, String> aggDeMap = new HashMap<String, String>();
                 * String orgUnitGroupUid = exceEmportDesign.getOrgunitgroup();
                 * 
                 * OrganisationUnitGroup orgUnitGroup =
                 * organisationUnitGroupService.getOrganisationUnitGroup(
                 * orgUnitGroupUid );
                 * 
                 * List<OrganisationUnit> groupMember = new
                 * ArrayList<OrganisationUnit>( orgUnitGroup.getMembers() );
                 * List<Integer> orgaUnitIds = new ArrayList<Integer>(
                 * getIdentifiers( OrganisationUnit.class, groupMember ) );
                 * String orgaUnitIdsByComma = getCommaDelimitedString(
                 * orgaUnitIds );
                 * 
                 * aggDeMap.putAll(
                 * excelImportService.getAggDataFromDataValueTable(
                 * orgaUnitIdsByComma, dataElmentIdsByComma, periodIdsByComma )
                 * );
                 */
                
                
               
                
               

                // fileWriter.append( NEW_LINE_SEPARATOR );
            
           
        }

        try
        {
            fileWriter.flush();
            fileWriter.close();

            fileName = "ExportResult.csv";
            File outputReportFile = new File( outputReportPath );
            inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );

            outputReportFile.deleteOnExit();

        }
        catch ( IOException e )
        {
            System.out.println( "Error while flushing/closing fileWriter !!!" );
            e.printStackTrace();
        }

        System.out.println( "End Time : " + new Date() );

        return SUCCESS;
    }

    // getting data value using Map
    private String getAggVal( String expression, Map<String, String> aggDeMap )
    {
        try
        {
            //Pattern pattern = Pattern.compile( "(\\[\\d+\\.\\d+\\])" );
            Pattern pattern = Pattern.compile( "(\\[\\w+\\.\\w+\\])" );	
            Matcher matcher = pattern.matcher( expression );
            StringBuffer buffer = new StringBuffer();

            String resultValue = "";

            while ( matcher.find() )
            {
                String replaceString = matcher.group();

                replaceString = replaceString.replaceAll( "[\\[\\]]", "" );
                
                //System.out.println( " replaceString in side fetch value --" + replaceString );
                //String deUID = replaceString.split(".")[0];
                //String categoryComboUID = replaceString.split(".")[1];
                
                String categoryComboUID = replaceString.substring( replaceString.indexOf( '.' ) + 1, replaceString
                        .length() );

                    //System.out.println( "** In side DeAndCombo 1 Replacing String" + replaceString  );
                    
                    replaceString = replaceString.substring( 0, replaceString.indexOf( '.' ) );
                    
                    //System.out.println( "** In side DeAndCombo 2 Replacing String" + replaceString  );
                    
                    //int dataElementId = Integer.parseInt( replaceString );
                    //int optionComboId = Integer.parseInt( optionComboIdStr );
                
                
                
                
                
                //int dataElementId = Integer.parseInt( deUID );
                //int categoryOptionComboId = Integer.parseInt( categoryComboUID );
                
                //System.out.println( "** In side DeAndCombo 1 " + dataElementId + ":" + optionComboId );
                
                DataElement dataElement = dataElementService.getDataElement( replaceString );
                DataElementCategoryOptionCombo categoryOptionCombo = dataElementCategoryService.getDataElementCategoryOptionCombo(categoryComboUID);
                
                replaceString = dataElement.getId() + "." + categoryOptionCombo.getId();
                
                //System.out.println( " replaceString in side fetch value --" + replaceString );
                
                replaceString = aggDeMap.get( replaceString );

                if ( replaceString == null )
                {
                    replaceString = "0";
                }

                matcher.appendReplacement( buffer, replaceString );

                resultValue = replaceString;
            }

            matcher.appendTail( buffer );

            double d = 0.0;
            try
            {
                d = MathUtils.calculateExpression( buffer.toString() );
            }
            catch ( Exception e )
            {
                d = 0.0;
                resultValue = "";
            }

            resultValue = "" + (int) d;

            return resultValue;
        }
        catch ( NumberFormatException ex )
        {
            throw new RuntimeException( "Illegal DataElement id", ex );
        }
    }

}