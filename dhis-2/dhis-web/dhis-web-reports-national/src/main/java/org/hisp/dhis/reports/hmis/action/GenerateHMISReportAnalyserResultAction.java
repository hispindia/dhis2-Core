package org.hisp.dhis.reports.hmis.action;

import static org.hisp.dhis.util.ConversionUtils.getIdentifiers;
import static org.hisp.dhis.util.TextUtils.getCommaDelimitedString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.hisp.dhis.config.Configuration_IN;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.reports.ReportService;
import org.hisp.dhis.reports.Report_in;
import org.hisp.dhis.reports.Report_inDesign;
import org.hisp.dhis.system.util.MathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class GenerateHMISReportAnalyserResultAction implements Action
{

    private final String GENERATEAGGDATA = "generateaggdata";

    private final String USECAPTUREDDATA = "usecaptureddata";
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private ReportService reportService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private DataElementService dataElementService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    // -------------------------------------------------------------------------
    // Properties
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

    private String reportList;

    public void setReportList( String reportList )
    {
        this.reportList = reportList;
    }
    

    private String ouIDTB;
    
    public void setOuIDTB( String ouIDTB )
    {
        this.ouIDTB = ouIDTB;
    }
    
    private int availablePeriods;

    public void setAvailablePeriods( int availablePeriods )
    {
        this.availablePeriods = availablePeriods;
    }
/*
    private String aggCB;

    public void setAggCB( String aggCB )
    {
        this.aggCB = aggCB;
    }
*/
    private String reportFileNameTB;

    private String reportModelTB;

    private List<OrganisationUnit> orgUnitList;

    private Period selectedPeriod;

    private SimpleDateFormat simpleDateFormat;

    private SimpleDateFormat monthFormat;

    private SimpleDateFormat simpleMonthFormat;

    private Date sDate;

    private Date eDate;

    private String raFolderName;
    
    private String aggData;
    
    public void setAggData( String aggData )
    {
        this.aggData = aggData;
    }

    private SimpleDateFormat yearFormat;
    private SimpleDateFormat simpleDateMonthYearFormat;
    //private SimpleDateFormat simpleMonthYearFormat;
    private  String deCodesXMLFileName = "";
    
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
    {
        
     // Initialization
        raFolderName = reportService.getRAFolderName();
       
        simpleDateFormat = new SimpleDateFormat( "MMM-yyyy" );
        //simpleMonthYearFormat = new SimpleDateFormat( "MMM-yyyy" );
        simpleDateMonthYearFormat = new SimpleDateFormat( "dd/MM/yyyy" );
        monthFormat = new SimpleDateFormat( "MMMM" );
        yearFormat = new SimpleDateFormat( "yyyy" );
        
        Report_in selReportObj =  reportService.getReport( Integer.parseInt( reportList ) );
        
        deCodesXMLFileName = selReportObj.getXmlTemplateName();
        reportModelTB = selReportObj.getModel();
        reportFileNameTB = selReportObj.getExcelTemplateName();
        
        String inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName + File.separator + "template" + File.separator + reportFileNameTB;
        //String outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName + File.separator + "output" + File.separator + UUID.randomUUID().toString() + ".xls";
        
        String outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator +  Configuration_IN.DEFAULT_TEMPFOLDER;
        File newdir = new File( outputReportPath );
        if( !newdir.exists() )
        {
            newdir.mkdirs();
        }
        outputReportPath += File.separator + UUID.randomUUID().toString() + ".xls";
        
        // OrgUnit Related Information        
        OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );
        
        System.out.println( orgUnit.getName()+ " : " + selReportObj.getName()+" Report Generation Start Time is : " + new Date() );
        
        if ( reportModelTB.equalsIgnoreCase( "DYNAMIC-ORGUNIT" ) )
        {
            orgUnitList = new ArrayList<>( orgUnit.getChildren() );
            Collections.sort( orgUnitList );
        }
        else if ( reportModelTB.equalsIgnoreCase( "STATIC" ) || reportModelTB.equalsIgnoreCase( "STATIC-DATAELEMENTS" ) )
        {
            orgUnitList = new ArrayList<OrganisationUnit>();
            orgUnitList.add( orgUnit );
        }
        
        // Period Info
        selectedPeriod = periodService.getPeriod( availablePeriods );
        sDate = format.parseDate( String.valueOf( selectedPeriod.getStartDate() ) );
        eDate = format.parseDate( String.valueOf( selectedPeriod.getEndDate() ) );
        
        // collect periodId by CommaSepareted
        List<Period> periodList = new ArrayList<Period>( periodService.getIntersectingPeriods( sDate, eDate ) );        
        Collection<Integer> periodIds = new ArrayList<Integer>( getIdentifiers( Period.class, periodList ) );        
        String periodIdsByComma = getCommaDelimitedString( periodIds );
        
        // collect dataElementIDs by commaSepareted
        List<Report_inDesign> reportDesignList = reportService.getReportDesign( deCodesXMLFileName );
        
        //String dataElmentIdsByComma = reportService.getDataelementIds( reportDesignList );
        String dataElmentIdsByComma = getDataelementIdsByComma( reportDesignList );
        
        FileInputStream tempFile = new FileInputStream( new File( inputTemplatePath ) );
        HSSFWorkbook apachePOIWorkbook = new HSSFWorkbook( tempFile );

        // Getting DataValues
        
        int orgUnitCount = 0;

        Iterator<OrganisationUnit> it = orgUnitList.iterator();
        while ( it.hasNext() )
        {
            OrganisationUnit currentOrgUnit = (OrganisationUnit) it.next();
            
            Map<String, String> aggDeMap = new HashMap<String, String>();
            
            if( aggData.equalsIgnoreCase( USECAPTUREDDATA ) )
            {
                aggDeMap.putAll( reportService.getAggDataFromDataValueTable( ""+currentOrgUnit.getId(), dataElmentIdsByComma, periodIdsByComma ) );
            }
            else if( aggData.equalsIgnoreCase( GENERATEAGGDATA ) )
            {
                List<OrganisationUnit> childOrgUnitTree = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitWithChildren( currentOrgUnit.getId() ) );
                List<Integer> childOrgUnitTreeIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, childOrgUnitTree ) );
                String childOrgUnitsByComma = getCommaDelimitedString( childOrgUnitTreeIds );

                aggDeMap.putAll( reportService.getAggDataFromDataValueTable( childOrgUnitsByComma, dataElmentIdsByComma, periodIdsByComma ) );
            }
            
            int count1 = 0;
            Iterator<Report_inDesign> reportDesignIterator = reportDesignList.iterator();
            while ( reportDesignIterator.hasNext() )
            {
                Report_inDesign report_inDesign = (Report_inDesign) reportDesignIterator.next();

                String deType = report_inDesign.getPtype();
                String sType = report_inDesign.getStype();
                String deCodeString = report_inDesign.getExpression();
                String tempStr = "";
                String tempadeInAdeStr = "";
                String tempStr1 = "";

                if ( deCodeString.equalsIgnoreCase( "FACILITY" ) )
                {
                    tempStr = currentOrgUnit.getName();
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITYP" ) )
                {
                    if( currentOrgUnit.getParent() != null )
                    {
                        tempStr = currentOrgUnit.getParent().getName();
                    }
                    else
                    {
                        tempStr = "";
                    }
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITYPP" ) )
                {
                    if( currentOrgUnit.getParent().getParent() != null )
                    {
                        tempStr = currentOrgUnit.getParent().getParent().getName();
                    }
                    else
                    {
                        tempStr = "";
                    }
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITYPPP" ) )
                {
                    if( currentOrgUnit.getParent().getParent().getParent() != null )
                    {
                        tempStr = currentOrgUnit.getParent().getParent().getParent().getName();
                    }
                    else
                    {
                        tempStr = "";
                    }
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITYPPPP" ) )
                {
                    if( currentOrgUnit.getParent().getParent().getParent().getParent() != null )
                    {
                        tempStr = currentOrgUnit.getParent().getParent().getParent().getParent().getName();
                    }
                    else
                    {
                        tempStr = "";
                    }
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITYPPPPP" ) )
                {
                    if( currentOrgUnit.getParent().getParent().getParent().getParent().getParent() != null )
                    {
                        tempStr = currentOrgUnit.getParent().getParent().getParent().getParent().getParent().getName();
                    }
                    else
                    {
                        tempStr = "";
                    }
                }
                else if ( deCodeString.equalsIgnoreCase( "PERIOD-MONTH" ) )
                {
                    tempStr = monthFormat.format( sDate );
                }
                else if ( deCodeString.equalsIgnoreCase( "PERIOD-YEAR" ) )
                {
                    tempStr = yearFormat.format( sDate );
                }
                else if ( deCodeString.equalsIgnoreCase( "YEAR-FROMTO" ) )
                {
                    tempStr = yearFormat.format( sDate );
                }
                else if ( deCodeString.equalsIgnoreCase( "MONTH-START-SHORT" ) )
                {
                    tempStr = simpleMonthFormat.format( sDate );
                }
                else if ( deCodeString.equalsIgnoreCase( "MONTH-END-SHORT" ) )
                {
                    tempStr = simpleMonthFormat.format( eDate );
                }
                else if ( deCodeString.equalsIgnoreCase( "MONTH-START" ) )
                {
                    tempStr = monthFormat.format( sDate );
                }
                else if ( deCodeString.equalsIgnoreCase( "MONTH-END" ) )
                {
                    tempStr = monthFormat.format( eDate );
                }
                else if ( deCodeString.equalsIgnoreCase( "PERIOD-QUARTER" ) )
                {
                    String startMonth = "";
                    String tempYear = yearFormat.format( sDate );
                    startMonth = monthFormat.format( sDate );

                    if ( startMonth.equalsIgnoreCase( "April" ) )
                    {
                        tempStr = "April - June" + " " + tempYear;
                    }
                    else if ( startMonth.equalsIgnoreCase( "July" ) )
                    {
                        tempStr = "July - September" + " " + tempYear;
                    }
                    else if ( startMonth.equalsIgnoreCase( "October" ) )
                    {
                        tempStr = "October - December" + " " + tempYear;
                    }
                    else
                    {
                        tempStr = "January - March" + " " + Integer.parseInt( tempYear ) + 1;
                    }
                }
                
                
                else if ( deCodeString.equalsIgnoreCase( "SLNO" ) )
                {
                    tempStr = "" + (orgUnitCount + 1);
                }
                else if ( deCodeString.equalsIgnoreCase( "NA" ) )
                {
                    tempStr = " ";
                }                
                else
                {
                    if ( sType.equalsIgnoreCase( "dataelement" ) )
                    {
                        if( aggData.equalsIgnoreCase( GENERATEAGGDATA ) )
                        {
                            tempStr = getAggVal( deCodeString, aggDeMap );
                        }
                        
                        else if( aggData.equalsIgnoreCase( USECAPTUREDDATA ) ) 
                        {
                            tempStr = getAggVal( deCodeString, aggDeMap );
                        }
                     
                    }
                    // for added new dataElement in HMIS Report
                    else if ( sType.equalsIgnoreCase( "dataelement-date" ) )
                    {
                        if( aggData.equalsIgnoreCase( USECAPTUREDDATA ) ) 
                        {
                            String tempDateString = getStringDataFromDataValue( deCodeString, selectedPeriod.getId(),currentOrgUnit.getId() );
                            if( tempDateString != null && !tempDateString.equalsIgnoreCase(""))
                            {
                                Date tempDate = format.parseDate( tempDateString );
                                tempStr = simpleDateMonthYearFormat.format(tempDate);
                            }
                            //System.out.println( " USECAPTUREDDATA  SType : " + sType + " DECode : " + deCodeString + "   TempStr : " + tempStr );
                        }
                    }
                    else if ( sType.equalsIgnoreCase( "dataelement-string" ) )
                    {
                        if( aggData.equalsIgnoreCase( USECAPTUREDDATA ) ) 
                        {
                            tempadeInAdeStr = getStringDataFromDataValue( deCodeString, selectedPeriod.getId(),currentOrgUnit.getId() );
                            //System.out.println( " USECAPTUREDDATA  SType : " + sType + " DECode : " + deCodeString + "   TempStr : " + tempadeInAdeStr );
                        }
                    }                    
                    else
                    {
                    }
                }
                int tempRowNo = report_inDesign.getRowno();
                int tempColNo = report_inDesign.getColno();
                int sheetNo = report_inDesign.getSheetno();
                
                Sheet sheet0 = apachePOIWorkbook.getSheetAt( sheetNo );
                
                if ( tempStr == null || tempStr.equals( " " ) )
                {
                    tempColNo += orgUnitCount;
                }
                else
                {
                    if ( sType.equalsIgnoreCase( "dataelement" ) )
                    {
                        try
                        {
                            Row row = sheet0.getRow( tempRowNo );
                            Cell cell = row.getCell( tempColNo );
                            cell.setCellValue( Double.parseDouble( tempStr ) );
                            
                        }
                        catch ( Exception e )
                        {
                            Row row = sheet0.getRow( tempRowNo );
                            Cell cell = row.getCell( tempColNo );
                            cell.setCellValue( tempStr );
                            
                        }
                    }   
                    else if ( sType.equalsIgnoreCase( "dataelement-date" ) )
                    {
                        try
                        {
                            Row row = sheet0.getRow( tempRowNo );
                            Cell cell = row.getCell( tempColNo );
                            cell.setCellValue( tempStr );
                            
                        }
                        catch ( Exception e )
                        {
                                //System.out.println( " Exception : " + e.getMessage() );
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( tempStr );
                        }
                    }
                    else if ( sType.equalsIgnoreCase( "dataelement-string" ) )
                    {
                        
                        if ( tempadeInAdeStr != null && tempadeInAdeStr.equalsIgnoreCase("Adequate") )
                        {
                            tempStr1 = "59";
                        }
                        else if ( tempadeInAdeStr != null && tempadeInAdeStr.equalsIgnoreCase("Inadequate") )
                        {
                            tempStr1 = "60";
                        }
                       
                        try
                        {
                            Row row = sheet0.getRow( tempRowNo );
                            
                            Cell cell_1 = row.getCell( tempColNo - 1 );
                            cell_1.setCellValue( tempStr1 );
                            
                            Cell cell_2 = row.getCell( tempColNo );
                            cell_2.setCellValue( tempadeInAdeStr );
                        }
                        catch ( Exception e )
                        {
                            Row row = sheet0.getRow( tempRowNo );
                            Cell cell = row.getCell( tempColNo );
                            cell.setCellValue( tempadeInAdeStr );
                        }
                    }              
                }                
                count1++;
            }// inner while loop end
            
            
            orgUnitCount++;
        }// outer while loop end
        
        
        // code for apachePOI Workbook
        fileName = reportFileNameTB.replace( ".xls", "" );
        fileName += "_" + orgUnitList.get( 0 ).getShortName() + "_";
        fileName += "_" + simpleDateFormat.format( selectedPeriod.getStartDate() ) + ".xls";
        
        tempFile.close(); //Close the InputStream
        
        FileOutputStream output_file = new FileOutputStream( new File(  outputReportPath ) );  //Open FileOutputStream to write updates
        
        apachePOIWorkbook.write( output_file ); //write changes
          
        output_file.close();  //close the stream   
        
        File outputReportFile = new File( outputReportPath );
        inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );
        
        outputReportFile.deleteOnExit();
        
        System.out.println( orgUnitList.get( 0 ).getName()+ " : " + selReportObj.getName()+" Report Generation End Time is : " + new Date() );
        
        
        return SUCCESS;
    }
    
    // getting data value using Map

    private String getAggVal( String expression, Map<String, String> aggDeMap )
    {
        //System.out.println( " expression -- " + expression + " aggDeMap " + aggDeMap );
        int flag = 0;
        try
        {
            Pattern pattern = Pattern.compile( "(\\[\\d+\\.\\d+\\])" );

            Matcher matcher = pattern.matcher( expression );
            StringBuffer buffer = new StringBuffer();

            String resultValue = "";

            while ( matcher.find() )
            {
                String replaceString = matcher.group();

                replaceString = replaceString.replaceAll( "[\\[\\]]", "" );

                replaceString = aggDeMap.get( replaceString );
                
                if( replaceString == null )
                {
                    replaceString = "0";                    
                }
                else
                {
                    flag = 1;
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
            
            resultValue = "" + (double) d;
            
            if( flag == 0 )
            {
                return "";
            }
                
            else
            {
                //System.out.println( " expression -- " + expression +" -- resultValue " + resultValue);
                return resultValue;
            }
            
            
            //return resultValue;
        }
        catch ( NumberFormatException ex )
        {
            throw new RuntimeException( "Illegal DataElement id", ex );
        }
    }

    // get capture data for which dataType String or date
    public String getStringDataFromDataValue( String formula, Integer periodId, Integer organisationUnitId )
    {
        String query = "";
        String resultValue = "";
        try
        {
            Pattern pattern = Pattern.compile( "(\\[\\d+\\.\\d+\\])" );
    
            Matcher matcher = pattern.matcher( formula );
            StringBuffer buffer = new StringBuffer();
    
            while ( matcher.find() )
            {
                String replaceString = matcher.group();
    
                replaceString = replaceString.replaceAll( "[\\[\\]]", "" );
                String optionComboIdStr = replaceString.substring( replaceString.indexOf( '.' ) + 1, replaceString
                    .length() );
    
                replaceString = replaceString.substring( 0, replaceString.indexOf( '.' ) );
    
                int dataElementId = Integer.parseInt( replaceString );
                int categoryOptionComboId = Integer.parseInt( optionComboIdStr );

                query = "SELECT value FROM datavalue WHERE sourceid = " + organisationUnitId
                        + " AND periodid = " + periodId + " AND dataelementid = " + dataElementId + " AND categoryoptioncomboid = " + categoryOptionComboId;
                
                SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( query );
                
                while ( sqlResultSet.next() )
                {
                        String stringDataValue = sqlResultSet.getString( 1 );
                    if ( stringDataValue != null )
                    {
                        resultValue = stringDataValue;
                    }
                }
    
            }
        }
        catch ( NumberFormatException ex )
        {
            throw new RuntimeException( "Illegal DataElement id", ex );
        }
        catch ( Exception e )
        {
            System.out.println( "SQL Exception : " + e.getMessage() );
            return null;
        }
        
        return resultValue;
    }
    //getDataelementIdsByComma
    public String getDataelementIdsByComma( List<Report_inDesign> reportDesignList )
    {
        String dataElmentIdsByComma = "-1";
        for ( Report_inDesign report_inDesign : reportDesignList )
        {
            String formula = report_inDesign.getExpression();
            try
            {
                Pattern pattern = Pattern.compile( "(\\[\\d+\\.\\d+\\])" );

                Matcher matcher = pattern.matcher( formula );
                StringBuffer buffer = new StringBuffer();

                while ( matcher.find() )
                {
                    String replaceString = matcher.group();

                    replaceString = replaceString.replaceAll( "[\\[\\]]", "" );
                    replaceString = replaceString.substring( 0, replaceString.indexOf( '.' ) );

                    int dataElementId = Integer.parseInt( replaceString );
                    
                    DataElement dataElement = dataElementService.getDataElement( dataElementId );
                    if( dataElement.getValueType().isInteger() || dataElement.getValueType().isNumeric() )
                    {
                        dataElmentIdsByComma += "," + dataElementId;
                        //System.out.println( " dataElmentIdsByComma - " + dataElmentIdsByComma );
                        replaceString = "";
                        matcher.appendReplacement( buffer, replaceString );
                    }
                }
            }
            catch ( Exception e )
            {

            }
        }

        return dataElmentIdsByComma;
    }

}