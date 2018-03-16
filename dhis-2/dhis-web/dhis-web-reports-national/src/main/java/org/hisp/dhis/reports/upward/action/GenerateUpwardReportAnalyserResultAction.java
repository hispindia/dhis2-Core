package org.hisp.dhis.reports.upward.action;

import static org.hisp.dhis.util.ConversionUtils.getIdentifiers;
import static org.hisp.dhis.util.TextUtils.getCommaDelimitedString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

public class GenerateUpwardReportAnalyserResultAction
    implements Action
{

    private final String GENERATEAGGDATA = "generateaggdata";

    private final String USEEXISTINGAGGDATA = "useexistingaggdata";

    private final String USECAPTUREDDATA = "usecaptureddata";
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    /*private StatementManager statementManager;

    public void setStatementManager( StatementManager statementManager )
    {
        this.statementManager = statementManager;
    }
        */

    private ReportService reportService;

    public void setReportService( ReportService reportService )
    {
        this.reportService = reportService;
    }

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

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
    
    /*
    private int ouIDTB;

    public void setOuIDTB( int ouIDTB )
    {
        this.ouIDTB = ouIDTB;
    }
    */
    
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
    
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
    {
        //statementManager.initialise();

        // Initialization
        raFolderName = reportService.getRAFolderName();
        String deCodesXMLFileName = "";
        simpleDateFormat = new SimpleDateFormat( "MMM-yyyy" );
        monthFormat = new SimpleDateFormat( "MMMM" );
        simpleMonthFormat = new SimpleDateFormat( "MMM" );
        yearFormat = new SimpleDateFormat( "yyyy" );
        simpleDateMonthYearFormat = new SimpleDateFormat( "dd/MM/yyyy" );
        String parentUnit = "";
        
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
        
        //FileInputStream tempFile = new FileInputStream( new File( inputTemplatePath ) );
        //HSSFWorkbook apachePOIWorkbook = new HSSFWorkbook( tempFile );
        
        if ( reportModelTB.equalsIgnoreCase( "DYNAMIC-ORGUNIT" ) )
        {
            OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );
            orgUnitList = new ArrayList<OrganisationUnit>( orgUnit.getChildren() );
            //Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );
            Collections.sort( orgUnitList );
        }
        else if ( reportModelTB.equalsIgnoreCase( "STATIC" ) || reportModelTB.equalsIgnoreCase( "STATIC-DATAELEMENTS" ) || reportModelTB.equalsIgnoreCase( "STATIC-FINANCIAL" ) )
        {
            orgUnitList = new ArrayList<OrganisationUnit>();
            OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );
            orgUnitList.add( orgUnit );
        }
        else if ( reportModelTB.equalsIgnoreCase( "dynamicwithrootfacility" ) )
        {
            OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );
            orgUnitList = new ArrayList<OrganisationUnit>( orgUnit.getChildren() );
            //Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );
            Collections.sort( orgUnitList );
            orgUnitList.add( orgUnit );

            parentUnit = orgUnit.getName();
        }

        System.out.println( orgUnitList.get( 0 ).getName()+ " : " + selReportObj.getName()+" : Report Generation Start Time is : " + new Date() );

        // Period Info
        selectedPeriod = periodService.getPeriod( availablePeriods );
        sDate = format.parseDate( String.valueOf( selectedPeriod.getStartDate() ) );
        eDate = format.parseDate( String.valueOf( selectedPeriod.getEndDate() ) );
        simpleDateFormat = new SimpleDateFormat( "MMM-yyyy" );
        
        // collect periodId by commaSepareted
        List<Period> tempPeriodList = new ArrayList<Period>( periodService.getIntersectingPeriods( sDate, eDate ) );
        
        Collection<Integer> tempPeriodIds = new ArrayList<Integer>( getIdentifiers(Period.class, tempPeriodList ) );
        
        String periodIdsByComma = getCommaDelimitedString( tempPeriodIds );

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
            
            if( aggData.equalsIgnoreCase( USEEXISTINGAGGDATA ) )
            {
                aggDeMap.putAll( reportService.getResultDataValueFromAggregateTable( currentOrgUnit.getId(), dataElmentIdsByComma, periodIdsByComma ) );
            }
            else if( aggData.equalsIgnoreCase( GENERATEAGGDATA ) )
            {
                List<OrganisationUnit> childOrgUnitTree = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitWithChildren( currentOrgUnit.getId() ) );
                List<Integer> childOrgUnitTreeIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, childOrgUnitTree ) );
                String childOrgUnitsByComma = getCommaDelimitedString( childOrgUnitTreeIds );

                aggDeMap.putAll( reportService.getAggDataFromDataValueTable( childOrgUnitsByComma, dataElmentIdsByComma, periodIdsByComma ) );
            }
            else if( aggData.equalsIgnoreCase( USECAPTUREDDATA ) )
            {
                aggDeMap.putAll( reportService.getAggDataFromDataValueTable( ""+currentOrgUnit.getId(), dataElmentIdsByComma, periodIdsByComma ) );
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

                Calendar tempStartDate = Calendar.getInstance();
                Calendar tempEndDate = Calendar.getInstance();
                List<Calendar> calendarList = new ArrayList<Calendar>( reportService.getStartingEndingPeriods( deType, selectedPeriod ) );
                if( calendarList == null || calendarList.isEmpty() )
                {
                    tempStartDate.setTime( selectedPeriod.getStartDate() );
                    tempEndDate.setTime( selectedPeriod.getEndDate() );
                    return SUCCESS;
                } 
                else
                {
                    tempStartDate = calendarList.get( 0 );
                    tempEndDate = calendarList.get( 1 );
                }

                if ( deCodeString.equalsIgnoreCase( "FACILITY" ) )
                {
                    tempStr = currentOrgUnit.getName();
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITY-NOREPEAT" ) )
                {
                    tempStr = parentUnit;
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITYP" ) )
                {
                    tempStr = currentOrgUnit.getParent().getName();
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITYPP" ) )
                {
                    tempStr = currentOrgUnit.getParent().getParent().getName();
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITYPPP" ) )
                {
                    tempStr = currentOrgUnit.getParent().getParent().getParent().getName();
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITYPPPP" ) )
                {
                    tempStr = currentOrgUnit.getParent().getParent().getParent().getParent().getName();
                }
                else if ( deCodeString.equalsIgnoreCase( "PERIOD" )
                    || deCodeString.equalsIgnoreCase( "PERIOD-NOREPEAT" ) )
                {
                    tempStr = simpleDateFormat.format( sDate );
                }
                else if ( deCodeString.equalsIgnoreCase( "PERIOD-MONTH" ) )
                {
                    tempStr = monthFormat.format( sDate );
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
                        if( aggData.equalsIgnoreCase( USEEXISTINGAGGDATA ) )
                        {
                            tempStr = getAggVal( deCodeString, aggDeMap );
                            
                            if ( deCodeString.equalsIgnoreCase( "[1.1]" ) || deCodeString.equalsIgnoreCase( "[2.1]" ) || deCodeString.equalsIgnoreCase( "[153.1]" ) 
                                || deCodeString.equalsIgnoreCase( "[157.1]" ) || deCodeString.equalsIgnoreCase( "[158.1]" )
                                || deCodeString.equalsIgnoreCase( "[160.1]" ) || deCodeString.equalsIgnoreCase( "[5990.1]" ) )
                            {
                                //System.out.println( " USEEXISTINGAGGDATA Before Converting : SType : " + sType + " DECode : " + deCodeString + "   TempStr : " + tempStr );
                                
                                if( tempStr.equalsIgnoreCase( "0.0" ) )
                                {
                                    tempStr = ""+ 1.0;
                                }
                                else if ( tempStr.equalsIgnoreCase( "1.0" ) )
                                {
                                    tempStr = ""+ 0.0;
                                }
                                else
                                {
                                }
                                //System.out.println( "  USEEXISTINGAGGDATA After Converting : SType : " + sType + " DECode : " + deCodeString + "   TempStr : " + tempStr );
                            }
                        }
                        else if( aggData.equalsIgnoreCase( GENERATEAGGDATA ) )
                        {
                            tempStr = getAggVal( deCodeString, aggDeMap );
                            
                            if ( deCodeString.equalsIgnoreCase( "[1.1]" ) || deCodeString.equalsIgnoreCase( "[2.1]" ) || deCodeString.equalsIgnoreCase( "[153.1]" ) 
                                || deCodeString.equalsIgnoreCase( "[157.1]" ) || deCodeString.equalsIgnoreCase( "[158.1]" )
                                || deCodeString.equalsIgnoreCase( "[160.1]" ) || deCodeString.equalsIgnoreCase( "[5990.1]" ) )
                            {
                                //System.out.println( " GENERATEAGGDATA Before Converting : SType : " + sType + " DECode : " + deCodeString + "   TempStr : " + tempStr );
                                
                                if( tempStr.equalsIgnoreCase( "0.0" ) )
                                {
                                    tempStr = ""+ 1.0;
                                }
                                else if ( tempStr.equalsIgnoreCase( "1.0" ) )
                                {
                                    tempStr = ""+ 0.0;
                                }
                                else
                                {
                                }
                                //System.out.println( " GENERATEAGGDATA After Converting : SType : " + sType + " DECode : " + deCodeString + "   TempStr : " + tempStr );
                            }
                        }
                        
                        else if( aggData.equalsIgnoreCase( USECAPTUREDDATA ) ) 
                        {
                            tempStr = getAggVal( deCodeString, aggDeMap );
                            
                            //System.out.println( " USECAPTUREDDATA Before Converting : SType : " + sType + " DECode : " + deCodeString + "   TempStr : " + tempStr );
                            
                            if ( deCodeString.equalsIgnoreCase( "[1.1]" ) || deCodeString.equalsIgnoreCase( "[2.1]" ) || deCodeString.equalsIgnoreCase( "[153.1]" ) 
                                || deCodeString.equalsIgnoreCase( "[157.1]" ) || deCodeString.equalsIgnoreCase( "[158.1]" )
                                || deCodeString.equalsIgnoreCase( "[160.1]" ) || deCodeString.equalsIgnoreCase( "[5990.1]" ))
                            {
                                //System.out.println( " USECAPTUREDDATA Before Converting : SType : " + sType + " DECode : " + deCodeString + "   TempStr : " + tempStr );
                                
                                if( tempStr.equalsIgnoreCase( "0.0" ) )
                                {
                                    tempStr = ""+ 1.0;
                                }
                                else if ( tempStr.equalsIgnoreCase( "1.0" ) )
                                {
                                    tempStr = ""+ 0.0;
                                }
                                else
                                {
                                }
                                //System.out.println( " USECAPTUREDDATA After Converting : SType : " + sType + " DECode : " + deCodeString + "   TempStr : " + tempStr );
                            }
                        }
                     
                    }
                    else if ( sType.equalsIgnoreCase( "dataelement_institution" ) )
                    {
                        if( aggData.equalsIgnoreCase( USEEXISTINGAGGDATA ) )
                        {
                            tempStr = getAggVal( deCodeString, aggDeMap );
                            
                            if( tempStr.equalsIgnoreCase( "0.0" ) )
                            {
                                tempStr = ""+ 1.0;
                            }
                            else if ( tempStr.equalsIgnoreCase( "1.0" ) )
                            {
                                tempStr = ""+ 0.0;
                            }
                            else
                            {
                            }
                        }
                        else if( aggData.equalsIgnoreCase( GENERATEAGGDATA ) )
                        {
                            tempStr = getAggVal( deCodeString, aggDeMap );
                            
                            if( tempStr.equalsIgnoreCase( "0.0" ) )
                            {
                                tempStr = ""+ 1.0;
                            }
                            else if ( tempStr.equalsIgnoreCase( "1.0" ) )
                            {
                                tempStr = ""+ 0.0;
                            }
                            else
                            {
                            }
                        }
                        
                        else if( aggData.equalsIgnoreCase( USECAPTUREDDATA ) ) 
                        {
                            tempStr = getAggVal( deCodeString, aggDeMap );
                            
                            if( tempStr.equalsIgnoreCase( "0.0" ) )
                            {
                                tempStr = ""+ 1.0;
                            }
                            else if ( tempStr.equalsIgnoreCase( "1.0" ) )
                            {
                                tempStr = ""+ 0.0;
                            }
                            else
                            {
                            }
                        }
                        //tempStr = reportService.getResultDataValue( deCodeString, tempStartDate.getTime(), tempEndDate.getTime(), currentOrgUnit, reportModelTB );
                    }
                    else if ( sType.equalsIgnoreCase( "dataelement-boolean" ) )
                    {
                        //tempStr = reportService.getBooleanDataValue( deCodeString, tempStartDate.getTime(), tempEndDate.getTime(), currentOrgUnit, reportModelTB );
                    }
                    // for added new dataElement in GOI Report
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
                        //System.out.println( " SType : " + sType + " DECode : " + deCodeString  );
                        //tempStr = reportService.getResultIndicatorValue( deCodeString, tempStartDate.getTime(),tempEndDate.getTime(), currentOrgUnit );
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
                	if ( reportModelTB.equalsIgnoreCase( "DYNAMIC-ORGUNIT" ) )
                    {
                        if ( deCodeString.equalsIgnoreCase( "FACILITYP" )
                            || deCodeString.equalsIgnoreCase( "FACILITYPP" )
                            || deCodeString.equalsIgnoreCase( "FACILITYPPP" )
                            || deCodeString.equalsIgnoreCase( "FACILITYPPPP" ) )
                        {
                        }
                        else if ( deCodeString.equalsIgnoreCase( "PERIOD" )
                            || deCodeString.equalsIgnoreCase( "PERIOD-NOREPEAT" )
                            || deCodeString.equalsIgnoreCase( "PERIOD-WEEK" )
                            || deCodeString.equalsIgnoreCase( "PERIOD-MONTH" )
                            || deCodeString.equalsIgnoreCase( "PERIOD-QUARTER" )
                            || deCodeString.equalsIgnoreCase( "PERIOD-YEAR" )
                            || deCodeString.equalsIgnoreCase( "MONTH-START" )
                            || deCodeString.equalsIgnoreCase( "MONTH-END" )
                            || deCodeString.equalsIgnoreCase( "MONTH-START-SHORT" )
                            || deCodeString.equalsIgnoreCase( "MONTH-END-SHORT" )
                            || deCodeString.equalsIgnoreCase( "SIMPLE-QUARTER" )
                            || deCodeString.equalsIgnoreCase( "QUARTER-MONTHS-SHORT" )
                            || deCodeString.equalsIgnoreCase( "QUARTER-MONTHS" )
                            || deCodeString.equalsIgnoreCase( "QUARTER-START-SHORT" )
                            || deCodeString.equalsIgnoreCase( "QUARTER-END-SHORT" )
                            || deCodeString.equalsIgnoreCase( "QUARTER-START" )
                            || deCodeString.equalsIgnoreCase( "QUARTER-END" )
                            || deCodeString.equalsIgnoreCase( "SIMPLE-YEAR" )
                            || deCodeString.equalsIgnoreCase( "YEAR-END" )
                            || deCodeString.equalsIgnoreCase( "YEAR-FROMTO" ) )
                        {
                        }
                        else
                        {
                            tempColNo += orgUnitCount;
                        }
                    }
                    else if ( reportModelTB.equalsIgnoreCase( "dynamicwithrootfacility" ) )
                    {
                        if ( deCodeString.equalsIgnoreCase( "FACILITYP" )
                            || deCodeString.equalsIgnoreCase( "FACILITY-NOREPEAT" )
                            || deCodeString.equalsIgnoreCase( "FACILITYPP" )
                            || deCodeString.equalsIgnoreCase( "FACILITYPPP" )
                            || deCodeString.equalsIgnoreCase( "FACILITYPPPP" ) )
                        {
                        }
                        else if ( deCodeString.equalsIgnoreCase( "PERIOD" )
                            || deCodeString.equalsIgnoreCase( "PERIOD-NOREPEAT" )
                            || deCodeString.equalsIgnoreCase( "PERIOD-WEEK" )
                            || deCodeString.equalsIgnoreCase( "PERIOD-QUARTER" )
                            || deCodeString.equalsIgnoreCase( "PERIOD-YEAR" )
                            || deCodeString.equalsIgnoreCase( "MONTH-START" )
                            || deCodeString.equalsIgnoreCase( "MONTH-END" )
                            || deCodeString.equalsIgnoreCase( "MONTH-START-SHORT" )
                            || deCodeString.equalsIgnoreCase( "MONTH-END-SHORT" )
                            || deCodeString.equalsIgnoreCase( "SIMPLE-QUARTER" )
                            || deCodeString.equalsIgnoreCase( "QUARTER-MONTHS-SHORT" )
                            || deCodeString.equalsIgnoreCase( "QUARTER-MONTHS" )
                            || deCodeString.equalsIgnoreCase( "QUARTER-START-SHORT" )
                            || deCodeString.equalsIgnoreCase( "QUARTER-END-SHORT" )
                            || deCodeString.equalsIgnoreCase( "QUARTER-START" )
                            || deCodeString.equalsIgnoreCase( "QUARTER-END" )
                            || deCodeString.equalsIgnoreCase( "SIMPLE-YEAR" )
                            || deCodeString.equalsIgnoreCase( "YEAR-END" )
                            || deCodeString.equalsIgnoreCase( "YEAR-FROMTO" ) )
                        {
                        }
                        else
                        {
                            tempRowNo += orgUnitCount;
                        }
                    }
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
        
        /*
        outputReportWorkbook.write();
        outputReportWorkbook.close();

        fileName = reportFileNameTB.replace( ".xls", "" );
        fileName += "_" + orgUnitList.get( 0 ).getShortName() + "_";
        fileName += "_" + simpleDateFormat.format( selectedPeriod.getStartDate() ) + ".xls";
        File outputReportFile = new File( outputReportPath );
        inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );

        System.out.println( orgUnitList.get( 0 ).getName()+ " : " + selReportObj.getName()+" Report Generation End Time is : " + new Date() );

        outputReportFile.deleteOnExit();
        */
        
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
        //statementManager.destroy();

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
        //Statement st1 = null;
        //ResultSet rs1 = null;
        // System.out.println( "Inside LL Data Value Method" );
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
                
                /*
                if ( sqlResultSet.next() )
                {
                	resultValue = sqlResultSet.getString( 1 );
                }
                */
                
                while ( sqlResultSet.next() )
                {
                	String stringDataValue = sqlResultSet.getString( 1 );
                    if ( stringDataValue != null )
                    {
                    	resultValue = stringDataValue;
                    }
                }
    
            }
    
            //System.out.println( "resultValue - " + resultValue);
            
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
