package org.hisp.dhis.reports.analysis.action;

import static org.hisp.dhis.util.ConversionUtils.getIdentifiers;
import static org.hisp.dhis.util.TextUtils.getCommaDelimitedString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hisp.dhis.config.ConfigurationService_IN;
import org.hisp.dhis.config.Configuration_IN;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.periods.comparator.PeriodsComparator;
import org.hisp.dhis.reports.ReportService;
import org.hisp.dhis.reports.Report_in;
import org.hisp.dhis.reports.Report_inDesign;
import org.hisp.dhis.system.util.MathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class GenerateAdvanceAnalysisReportResultAction implements Action
{
   
    //private final String GENERATEAGGDATA = "generateaggdata";

    //private final String USEEXISTINGAGGDATA = "useexistingaggdata";

    //private final String USECAPTUREDDATA = "usecaptureddata";
    
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
    private OrganisationUnitGroupService organisationUnitGroupService;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ConfigurationService_IN configurationService_IN;
    
    @Autowired
    private DataSetService dataSetService;
    
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

    private String reportFileNameTB;

    public void setReportFileNameTB( String reportFileNameTB )
    {
        this.reportFileNameTB = reportFileNameTB;
    }
    
    private String ouIDTB;
    
    public void setOuIDTB( String ouIDTB )
    {
        this.ouIDTB = ouIDTB;
    }
    
    private String reportList;

    public void setReportList( String reportList )
    {
        this.reportList = reportList;
    }

    private String periodTypeId;

    public void setPeriodTypeId( String periodTypeId )
    {
        this.periodTypeId = periodTypeId;
    }

    //private String reportModelTB;

    private Date sDate;

    private Date eDate;

    private PeriodType periodType;

    private String raFolderName;

    private Integer monthCount;
    
    private int availablePeriods;

    public void setAvailablePeriods( int availablePeriods )
    {
        this.availablePeriods = availablePeriods;
    }
    
    private int availablePeriodsto;
    
    public void setAvailablePeriodsto( int availablePeriodsto )
    {
        this.availablePeriodsto = availablePeriodsto;
    }

    private Period selectedEndPeriod;
    
    private List<Period> selectedPeriodList = new ArrayList<Period>();
    
    private Period selectedPeriod;
    
    private OrganisationUnit selectedOrgUnit;
    
    private SimpleDateFormat tempSimpleDateFormat;
    
    private Map<String, String> dataSetReportingRateMap = new HashMap<String, String>();
    private Map<String, String> facilityTypDataValuMap = new HashMap<String, String>();
    private Map<String, String> allChildOrgUnitWiseAggDeMap =  new HashMap<String, String>();
    /*
    private Map<Integer, Map<String, String>> orgUnitWiseAggDeMap = new HashMap<Integer, Map<String, String>>();
    private Map<String, Map<String, String>> orgUnitAndOrgGroupWiseAggDeMap = new HashMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> orgUnitAndOrgGroupMemberWiseAggDeMap = new HashMap<String, Map<String, String>>();
    */
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    @SuppressWarnings( "resource" )
    public String execute() throws Exception
    {
        // Initialization
        raFolderName = reportService.getRAFolderName();
        String deCodesXMLFileName = "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "MMM-yyyy" );
        SimpleDateFormat dayFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd-MM-yyyy" );

        Report_in selReportObj = reportService.getReport( Integer.parseInt( reportList ) );

        deCodesXMLFileName = selReportObj.getXmlTemplateName();
        //reportModelTB = selReportObj.getModel();
        reportFileNameTB = selReportObj.getExcelTemplateName();
        
        // OrgUnit Related Info
        selectedOrgUnit = new OrganisationUnit();
        selectedOrgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );
        
        //int selectedOrgUnitLevel = selectedOrgUnit.getLevel();
        //System.out.println( "Analysis Report Generation Start Time is : \t" + new Date() );
        System.out.println( selectedOrgUnit.getName() + " : " + selReportObj.getName() + " - Generation Start Time is : " + new Date() );
        
        List<OrganisationUnit> allChildOrgUnitTree = new ArrayList<OrganisationUnit>( getOrganisationUnitWithChildren( (int)selectedOrgUnit.getId() ) );
        List<Integer> allChildOrgUnitIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, allChildOrgUnitTree ) );
        String allChildOrgUnitIdsByComma = getCommaDelimitedString( allChildOrgUnitIds );
        
        
        String inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName + File.separator + "template" + File.separator + reportFileNameTB;
        //String outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName + File.separator + "output" + File.separator + UUID.randomUUID().toString() + ".xls";
        
        String outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator +  Configuration_IN.DEFAULT_TEMPFOLDER;
        File newdir = new File( outputReportPath );
        if( !newdir.exists() )
        {
            newdir.mkdirs();
        }
        outputReportPath += File.separator + UUID.randomUUID().toString() + ".xlsx";
        
        // period Related Info
        periodType = periodService.getPeriodTypeByName( periodTypeId );
        
        selectedPeriod = periodService.getPeriod( availablePeriods );
        selectedEndPeriod = periodService.getPeriod( availablePeriodsto );

        sDate = format.parseDate( String.valueOf( selectedPeriod.getStartDate() ) );
        eDate = format.parseDate( String.valueOf( selectedEndPeriod.getEndDate() ) );
        
        // one year before startDate
        Calendar oneYearBeforeStartDate = Calendar.getInstance();
        oneYearBeforeStartDate.setTime( sDate );
        oneYearBeforeStartDate.add( Calendar.YEAR, -1 );
        Date oneYearBeforeSDate = oneYearBeforeStartDate.getTime();
        String oneYearBeforeSDateString = dayFormat.format( oneYearBeforeSDate );
        Date oneYearBeforeStDate = format.parseDate( String.valueOf( oneYearBeforeSDateString ) );

        // one year before endDate
        Calendar oneYearBeforeEndDate = Calendar.getInstance();
        oneYearBeforeEndDate.setTime( eDate );
        oneYearBeforeEndDate.add( Calendar.YEAR, -1 );
        Date oneYearBeforeEDate = oneYearBeforeEndDate.getTime();
        String oneYearBeforeEDateString = dayFormat.format( oneYearBeforeEDate );
        Date oneYearBeforeEnDate = format.parseDate( String.valueOf( oneYearBeforeEDateString ) );
        
        selectedPeriodList = new ArrayList<Period>( periodService.getPeriodsBetweenDates( periodType, sDate, eDate ) );
        Collections.sort( selectedPeriodList, new PeriodsComparator() );
        Collections.reverse( selectedPeriodList );
        Collection<Integer> selectedPeriodIds = new ArrayList<Integer>( getIdentifiers(Period.class, selectedPeriodList ) );
        String selectedPeriodIdsByComma = getCommaDelimitedString( selectedPeriodIds );
        
        
        List<Period> interSectingPeriodList = new ArrayList<Period>();
        interSectingPeriodList = new ArrayList<Period>( periodService.getIntersectingPeriods( sDate, eDate ) );
        Collection<Integer> periodIds = new ArrayList<Integer>( getIdentifiers(Period.class, interSectingPeriodList ) );
        String periodIdsByComma = getCommaDelimitedString( periodIds );
        
        List<Period> oneYearBeforePeriodList = new ArrayList<Period>();
        //oneYearBeforePeriodList = new ArrayList<Period>( periodService.getPeriodsBetweenDates( periodType, oneYearBeforeStDate, oneYearBeforeEtDate ) );
        oneYearBeforePeriodList = new ArrayList<Period>( periodService.getIntersectingPeriods( oneYearBeforeStDate, oneYearBeforeEnDate ) );
        Collection<Integer> oneYearBeforePeriodIds = new ArrayList<Integer>( getIdentifiers( Period.class, oneYearBeforePeriodList ) );
        String oneYearBeforePeriodIdsByComma = getCommaDelimitedString( oneYearBeforePeriodIds );
        
        //System.out.println( " sDate : " + sDate + " - eDate -- " + eDate );
        //System.out.println( " oneYearBeforeStDate : " + oneYearBeforeStDate + " - oneYearBeforeEtDate -- " + oneYearBeforeEtDate );
        
        //System.out.println( " periodIdsByComma : " + periodIdsByComma + " - oneYearBeforePeriodIdsByComma -- " + oneYearBeforePeriodIdsByComma );
        
        Calendar tempStartDate = Calendar.getInstance();
        Calendar tempEndDate = Calendar.getInstance();
        tempStartDate.setTime( sDate );
        tempEndDate.setTime( eDate );
        
        int endYear = tempEndDate.get( Calendar.YEAR );
        int startYear = tempStartDate.get( Calendar.YEAR );
        int endMonth = tempEndDate.get( Calendar.MONTH );
        int startMonth = tempStartDate.get( Calendar.MONTH );

        monthCount = ( (endYear - startYear) * 12) - startMonth + endMonth + 1;
        
        if( periodTypeId.equalsIgnoreCase( "monthly" ) )
        {
            tempSimpleDateFormat = new SimpleDateFormat( "MMM-yyyy" ); 
        }
        else if( periodTypeId.equalsIgnoreCase( "yearly" ) )
        {
            tempSimpleDateFormat = new SimpleDateFormat( "yyyy" );
        }
        else
        {
            tempSimpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        }
        
        // dataElements/dataSets Related Info
        List<Report_inDesign> reportDesignListDataElement = getReportDesign( deCodesXMLFileName, "de-code" );
        List<Report_inDesign> reportDesignListDataSet = getReportDesign( deCodesXMLFileName, "data-set" );
        List<Report_inDesign> reportDesignListFacilityType = getReportDesign( deCodesXMLFileName, "facility-type" );
        
        String dataElmentIdsByComma = getExpressionIds( reportDesignListDataElement );
        String dataSetIdsByComma = getExpressionIds( reportDesignListDataSet );
        String facilityTypeElmentIdsByComma = getExpressionIds( reportDesignListFacilityType );
        
        dataSetReportingRateMap.putAll( getDataSetReportingRate( dataSetIdsByComma, selectedPeriodIdsByComma ) );
        allChildOrgUnitWiseAggDeMap =  new HashMap<String, String>();
        allChildOrgUnitWiseAggDeMap.putAll( reportService.getAggDataFromDataValueTable( allChildOrgUnitIdsByComma, dataElmentIdsByComma, oneYearBeforePeriodIdsByComma ) );
        facilityTypDataValuMap.putAll( reportService.getAggDataFromDataValueTable( allChildOrgUnitIdsByComma, facilityTypeElmentIdsByComma, periodIdsByComma ) );
        
        // orgUnit details
        List<OrganisationUnitGroup> orgUnitGroupList = new ArrayList<OrganisationUnitGroup>();
        List<OrganisationUnit> districtOrgUnitList = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitsAtLevel( 4 )  );
        OrganisationUnitGroupSet organisationUnitGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( "tunEPeJmZ4o" );
        if( organisationUnitGroupSet != null && districtOrgUnitList != null && districtOrgUnitList.size() > 0 )
        {
            orgUnitGroupList = new ArrayList<OrganisationUnitGroup>( organisationUnitGroupSet.getOrganisationUnitGroups() );
            /*
            for ( OrganisationUnit organisationUnit : districtOrgUnitList )
            {
                List<OrganisationUnit> childOrgUnitTree = new ArrayList<OrganisationUnit>( getOrganisationUnitWithChildren( organisationUnit.getId() ) );
                List<Integer> districtOrgUnitIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, childOrgUnitTree ) );
                String districtOrgUnitIdsByComma = getCommaDelimitedString( districtOrgUnitIds );
                Map<String, String> districtOrgUnitWiseAggDeMap =  new HashMap<String, String>();
                districtOrgUnitWiseAggDeMap.putAll( reportService.getAggDataFromDataValueTable( districtOrgUnitIdsByComma, dataElmentIdsByComma, periodIdsByComma ) );
                orgUnitWiseAggDeMap.put( organisationUnit.getId() , districtOrgUnitWiseAggDeMap );
                
                if( orgUnitGroupList != null && orgUnitGroupList.size() > 0 )
                {
                    for ( OrganisationUnitGroup ouGroup : orgUnitGroupList )
                    {
                        //List<OrganisationUnit> childOrgUnitTree = new ArrayList<OrganisationUnit>( getOrganisationUnitWithChildren( organisationUnit.getId() ) );
                        if( ouGroup != null && ouGroup.getMembers().size() > 0 )
                        {
                            List<Integer> orgGroupMemberIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, ouGroup.getMembers() ) );
                            String orgGroupMemberIdsByComma = getCommaDelimitedString( orgGroupMemberIds );
                            Map<String, String> orgUnitGroupMemberWiseAggDeMap =  new HashMap<String, String>();
                            orgUnitGroupMemberWiseAggDeMap.putAll( reportService.getAggDataFromDataValueTable( orgGroupMemberIdsByComma, dataElmentIdsByComma, oneYearBeforePeriodIdsByComma ) );
                            orgUnitAndOrgGroupMemberWiseAggDeMap.put( ouGroup.getUid() , orgUnitGroupMemberWiseAggDeMap );
                            
                            //System.out.println( organisationUnit.getName()+ " before filter  : " + ouGroup.getName() + " - -- " + ouGroup.getMembers().size()  + " - -- " + childOrgUnitTree.size() );
                            childOrgUnitTree.retainAll( ouGroup.getMembers() );
                            //System.out.println( organisationUnit.getName()+ " after filter : " + ouGroup.getName()+ " - -- " + ouGroup.getMembers().size()  + " - -- " + childOrgUnitTree.size() );
                            String childOrgUnitsByComma = "-1";
                            if( childOrgUnitTree.size() > 0 )
                            {
                                List<Integer> childOrgUnitTreeIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, childOrgUnitTree ) );
                                childOrgUnitsByComma = getCommaDelimitedString( childOrgUnitTreeIds );
                            }
                            //System.out.println( organisationUnit.getName()+ " final org leanth : " + ouGroup.getName()+ " - -- " + childOrgUnitsByComma.length() );
                            Map<String, String> orgUnitWiseAggDeMap =  new HashMap<String, String>();
                            orgUnitWiseAggDeMap.putAll( reportService.getAggDataFromDataValueTable( childOrgUnitsByComma, dataElmentIdsByComma, periodIdsByComma ) );
                            orgUnitAndOrgGroupWiseAggDeMap.put( organisationUnit.getId()+":" + ouGroup.getUid() , orgUnitWiseAggDeMap );
                            //System.out.println( organisationUnit.getName()+ " Final Map : " + ouGroup.getName()+ " - -- " + orgUnitWiseAggDeMap.size() );
                        }
                    }
                }
            }
            */
            
        }

        FileInputStream tempFile = new FileInputStream( new File( inputTemplatePath ) );
        XSSFWorkbook apachePOIWorkbook = new XSSFWorkbook( tempFile );
        
        //XSSFFormulaEvaluator.evaluateAllFormulaCells( apachePOIWorkbook );
        
        //System.out.println( selectedOrgUnit.getName()+ " : " + selReportObj.getName()+" - Generation Start Time is : " + new Date() + " -- " + apachePOIWorkbook.getNumberOfSheets() );
        
        //System.out.println( " dataElmentIdsByComma " + dataElmentIdsByComma +" - periodIdsByComma : " + periodIdsByComma + " -- " + childOrgUnitsByComma );
        
        /*
        for (int i = 0; i < apachePOIWorkbook.getNumberOfSheets(); i++) 
        {
            System.out.println("Sheet name: " + apachePOIWorkbook.getSheetName(i));
        }
        */
        
        
        // for printing facilityType and fixed column value
        //int facilityTypeCount = 0;
        Iterator<Report_inDesign> reportDesignIteratorFacilityType = reportDesignListFacilityType.iterator();
        while ( reportDesignIteratorFacilityType.hasNext() )
        {
            Report_inDesign reportDesign =  reportDesignIteratorFacilityType.next();
            String deCodeString = reportDesign.getExpression();

            String sType = reportDesign.getStype();
            String tempStr = "";

            if ( deCodeString.equalsIgnoreCase( "FACILITY" ) )
            {
                tempStr = selectedOrgUnit.getName();
            }
            else if ( deCodeString.equalsIgnoreCase( "FACILITYPP" ) )
            {
                if( selectedOrgUnit.getParent().getParent() != null )
                {
                    tempStr = selectedOrgUnit.getParent().getParent().getName();
                }
                else
                {
                    tempStr = "";
                }
            }
            else if( deCodeString.equalsIgnoreCase( "MONTHCOUNT" ) )
            {
                tempStr = monthCount.toString();
            }
            else if( deCodeString.equalsIgnoreCase( "FINANCIAL-YEAR" ) )
            {
                endYear = tempEndDate.get( Calendar.YEAR );
                startYear = tempStartDate.get( Calendar.YEAR );
                if( endYear == startYear )
                {
                    endYear = endYear + 1;
                }
                tempStr = startYear +"-" + endYear;
            }
            else if( deCodeString.equalsIgnoreCase( "REPORT-DATE" ) )
            {
                tempStr = dateFormat.format( new Date() );
            }
            else if( deCodeString.equalsIgnoreCase( "PERIODSDED" ) )
            {
                tempStr = simpleDateFormat.format( sDate ) + " To " + simpleDateFormat.format( eDate );
            }
            else if( deCodeString.equalsIgnoreCase( "PREVIOUS-PERIODSDED" ) )
            {
                tempStr = simpleDateFormat.format( oneYearBeforeStDate ) + " To " + simpleDateFormat.format( oneYearBeforeEnDate );
            }
            else if ( deCodeString.equalsIgnoreCase( "NA" ) )
            {
                tempStr = " ";
            }
            
            else
            {
                if ( sType.equalsIgnoreCase( "dataelement" ) )
                {
                    tempStr = getAggVal( deCodeString, facilityTypDataValuMap );
                    //System.out.println( aggData + " 1 SType : " + sType + " DECode : " + deCodeString + "   TempStr : " + tempStr + " -- " + tempaGrpStr );
                }
                
            }
            int tempRowNo = reportDesign.getRowno();
            int tempColNo = reportDesign.getColno();
            int sheetNo = reportDesign.getSheetno();
            
            Sheet sheet0 = apachePOIWorkbook.getSheetAt( sheetNo );
            if ( sType.equalsIgnoreCase( "dataelement" )  )
            {
                //System.out.println( " DECode  : " + deCodeString + "   TempStr : " + tempStr + " -- " + " rowNo : " + tempRowNo + " colNo : " + tempColNo );
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
            //facilityTypeCount++;
        }
        
        // for printing dataSet reporting Rate
        int periodCount = 0;
        for( Period period : selectedPeriodList )
        {
            Iterator<Report_inDesign> reportDesignIteratorDataSet = reportDesignListDataSet.iterator();
            while (  reportDesignIteratorDataSet.hasNext() )
            {
                Report_inDesign reportDesign =  reportDesignIteratorDataSet.next();
                String dsCodeString = reportDesign.getExpression();

                String sType = reportDesign.getStype();
                String tempStr = "";

                
                if ( dsCodeString.equalsIgnoreCase( "PROGRESSIVE-PERIOD" ) )
                {
                    tempStr = tempSimpleDateFormat.format( period.getStartDate() );
                }
                else
                {
                    if ( sType.equalsIgnoreCase( "reporting-rate" ) )
                    {
                        tempStr = getReportingRate( dsCodeString, ""+period.getId(), dataSetReportingRateMap );
                        //System.out.println( aggData + " 1 SType : " + sType + " DECode : " + deCodeString + "   TempStr : " + tempStr + " -- " + tempaGrpStr );
                    }
                }
            
                int tempRowNo = reportDesign.getRowno();
                int tempColNo = reportDesign.getColno();
                int sheetNo = reportDesign.getSheetno();
                Sheet sheet0 = apachePOIWorkbook.getSheetAt( sheetNo );
                
                if ( sType.equalsIgnoreCase( "reporting-rate" ) )
                {
                    if( dsCodeString.equalsIgnoreCase( "PROGRESSIVE-PERIOD" ) )
                    {
                        tempColNo += periodCount;
                        //System.out.println( " DECode : " + dsCodeString + "   TempStr : " + tempStr + " -- " + " rowNo : " + tempRowNo + " colNo : " + tempColNo );
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
                    else
                    {
                        tempColNo += periodCount;
                        //System.out.println( " DECode 1 : " + dsCodeString + "   TempStr : " + tempStr + " -- " + " rowNo : " + tempRowNo + " colNo : " + tempColNo );
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
                }
            }// inner while loop end

            periodCount++;
           
        }// outer while loop end
        
        // dataElement value printing
        int orgUnitCount = 0;
        int mregeColCount = 0;
        int tempColCount = 0;
        Iterator<OrganisationUnit> orgIt = districtOrgUnitList.iterator();
        while ( orgIt.hasNext() )
        {
            OrganisationUnit currentOrgUnit = (OrganisationUnit) orgIt.next();
            //Map<String, String> districtWiseAggDeMap = new HashMap<String, String>();
            //districtWiseAggDeMap.putAll( orgUnitWiseAggDeMap.get( currentOrgUnit.getId() ));
            List<OrganisationUnit> childOrgUnitTree = new ArrayList<OrganisationUnit>( getOrganisationUnitWithChildren( (int)currentOrgUnit.getId() ) );
            List<Integer> districtOrgUnitIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, childOrgUnitTree ) );
            String districtOrgUnitIdsByComma = getCommaDelimitedString( districtOrgUnitIds );
            Map<String, String> districtOrgUnitWiseAggDeMap =  new HashMap<String, String>();
            districtOrgUnitWiseAggDeMap.putAll( reportService.getAggDataFromDataValueTable( districtOrgUnitIdsByComma, dataElmentIdsByComma, periodIdsByComma ) );
            
            //int orgUnitGroupCount = 0;
            Iterator<OrganisationUnitGroup> orgGroupIt = orgUnitGroupList.iterator();
            while ( orgGroupIt.hasNext() )
            {
                OrganisationUnitGroup currentOrgUnitGroup = (OrganisationUnitGroup) orgGroupIt.next();
                Map<String, String> aggDeMap = new HashMap<String, String>();
                //Map<String, String> orgUnitWiseAggDeMap =  new HashMap<String, String>();
                List<OrganisationUnit> orgUnitGrpMember = new ArrayList<OrganisationUnit>( currentOrgUnitGroup.getMembers() );
                orgUnitGrpMember.retainAll( childOrgUnitTree );
                //System.out.println( currentOrgUnit.getName()+ " after filter : " + currentOrgUnitGroup.getName()+ " - -- " + currentOrgUnitGroup.getMembers().size()  + " - -- " + orgUnitGrpMember.size() );
                String childOrgUnitsByComma = "-1";
                if( orgUnitGrpMember.size() > 0 )
                {
                    List<Integer> childOrgUnitTreeIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, orgUnitGrpMember ) );
                    childOrgUnitsByComma = getCommaDelimitedString( childOrgUnitTreeIds );
                }
                aggDeMap.putAll( reportService.getAggDataFromDataValueTable( childOrgUnitsByComma, dataElmentIdsByComma, periodIdsByComma ) );
                
                //int count1 = 0;
                Iterator<Report_inDesign> reportDesignIterator = reportDesignListDataElement.iterator();
                while ( reportDesignIterator.hasNext() )
                {
                    Report_inDesign report_inDesign = (Report_inDesign) reportDesignIterator.next();

                    //String deType = report_inDesign.getPtype();
                    String sType = report_inDesign.getStype();
                    String deCodeString = report_inDesign.getExpression();
                    String tempStr = "";
                    String districtTotalStr = "";

                    if ( deCodeString.equalsIgnoreCase( "PROGRESSIVE-ORGUNIT" ) )
                    {
                        tempStr = currentOrgUnit.getName();
                    }
                    else
                    {
                        if ( sType.equalsIgnoreCase( "dataelement" ) )
                        {
                            tempStr = getAggVal( deCodeString, aggDeMap );
                            districtTotalStr = getAggVal( deCodeString, districtOrgUnitWiseAggDeMap );
                            //System.out.println( aggData + " 1 SType : " + sType + " DECode : " + deCodeString + "   TempStr : " + tempStr + " -- " + tempaGrpStr );
                        }
                    }
                    int tempRowNo = report_inDesign.getRowno();
                    int tempColNo = report_inDesign.getColno();
                    int sheetNo = report_inDesign.getSheetno();
                    
                    Sheet sheet0 = apachePOIWorkbook.getSheetAt( sheetNo );
                    
                    
                    if ( sType.equalsIgnoreCase( "dataelement" ) )
                    {
                        if( deCodeString.equalsIgnoreCase( "PROGRESSIVE-ORGUNIT" ) )
                        {
                            //tempRowNo += orgUnitCount + orgUnitGroupMemberCount;
                            //tempRowNo = tempRowNo + orgUnitGroupMemberCount;
                            tempColNo = tempColNo + orgUnitCount + mregeColCount;
                            
                            //System.out.println( " DECode : " + deCodeString + "   TempStr : " + tempStr + " -- " + tempaGrpStr + " rowNo : " + tempRowNo + " colNo : " + tempColNo );
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( tempStr );
                                
                                //sheet0.addMergedRegion( new CellRangeAddress( tempRowNo, tempRowNo, tempColNo, tempColNo + 7 ) );
                                //cell.setCellStyle( getCellFormatPOIExtended( apachePOIWorkbook ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( tempStr );
                                
                                //sheet0.addMergedRegion( new CellRangeAddress( tempRowNo, tempRowNo, tempColNo, tempColNo + 7 ) );
                                //cell.setCellStyle( getCellFormatPOIExtended( apachePOIWorkbook ) );
                            }
                            
                        }
                        else
                        {
                            //tempRowNo += orgUnitCount + orgUnitGroupMemberCount;
                            //tempRowNo = tempRowNo + orgUnitGroupMemberCount;
                            //tempRowNo = tempRowNo + orgUnitCount + orgUnitGroupMemberCount;
                            
                            tempColNo = tempColNo + tempColCount;
                            //System.out.println( " DECode : " + deCodeString + " deType : " + deType + "   TempStr : " + tempStr + " rowNo : " + tempRowNo + " colNo : " + tempColNo );
                            //tempRowNo = reportDesign.getRowno();
                            
                            // for printing total
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo - 1 );
                                cell.setCellValue( Double.parseDouble( districtTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo - 1 );
                                cell.setCellValue( districtTotalStr );
                                
                            }
                            if ( currentOrgUnitGroup.getUid().equalsIgnoreCase( "xYvEtLYNPKx" )  )
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
                            else if ( currentOrgUnitGroup.getUid().equalsIgnoreCase( "wR42WpA0VHp" )  )
                            {
                                try
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 1 );
                                    cell.setCellValue( Double.parseDouble( tempStr ) );
                                    
                                }
                                catch ( Exception e )
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 1 );
                                    cell.setCellValue( tempStr );
                                    
                                }
                            }
                            else if ( currentOrgUnitGroup.getUid().equalsIgnoreCase( "NP6zRkPiA4S" )  )
                            {
                                try
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 2 );
                                    cell.setCellValue( Double.parseDouble( tempStr ) );
                                    
                                }
                                catch ( Exception e )
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 2 );
                                    cell.setCellValue( tempStr );
                                    
                                }
                            }
                            else if ( currentOrgUnitGroup.getUid().equalsIgnoreCase( "K3UhUR7OIm0" )  )
                            {
                                try
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 3 );
                                    cell.setCellValue( Double.parseDouble( tempStr ) );
                                    
                                }
                                catch ( Exception e )
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 3 );
                                    cell.setCellValue( tempStr );
                                    
                                }
                            }
                            else if ( currentOrgUnitGroup.getUid().equalsIgnoreCase( "R9BqNOdb28Q" )  )
                            {
                                try
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 4 );
                                    cell.setCellValue( Double.parseDouble( tempStr ) );
                                    
                                }
                                catch ( Exception e )
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 4 );
                                    cell.setCellValue( tempStr );
                                    
                                }
                            }
                            else if ( currentOrgUnitGroup.getUid().equalsIgnoreCase( "LzDGwjcCNbD" )  )
                            {
                                try
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 5 );
                                    cell.setCellValue( Double.parseDouble( tempStr ) );
                                    
                                }
                                catch ( Exception e )
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 5 );
                                    cell.setCellValue( tempStr );
                                }
                            }
                            else if ( currentOrgUnitGroup.getUid().equalsIgnoreCase( "bYeMmLxh8Xs" )  )
                            {
                                try
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 6 );
                                    cell.setCellValue( Double.parseDouble( tempStr ) );
                                    
                                }
                                catch ( Exception e )
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 6 );
                                    cell.setCellValue( tempStr );
                                }
                            }
                        }
                    }
                    
                    //count1++;
                }
                //orgUnitGroupCount++;
            }
            mregeColCount+=7;
            tempColCount+=8;
            orgUnitCount++;
        }
            
        //System.out.println( " col Count before total : " + tempColCount );
        
        // for previous period dataElement value printing    
        
        //int orgUnitGroupCount = 0;
        tempColCount = tempColCount + 8;
        Iterator<OrganisationUnitGroup> orgGroupIt = orgUnitGroupList.iterator();
        while ( orgGroupIt.hasNext() )
        {
            OrganisationUnitGroup currentOrgUnitGroup = (OrganisationUnitGroup) orgGroupIt.next();
            
            List<Integer> orgGroupMemberIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, currentOrgUnitGroup.getMembers() ) );
            String orgGroupMemberIdsByComma = getCommaDelimitedString( orgGroupMemberIds );
            Map<String, String> aggDeMap = new HashMap<String, String>();
            aggDeMap.putAll( reportService.getAggDataFromDataValueTable( orgGroupMemberIdsByComma, dataElmentIdsByComma, oneYearBeforePeriodIdsByComma ) );
            
            //int count1 = 0;
            Iterator<Report_inDesign> reportDesignIterator = reportDesignListDataElement.iterator();
            while ( reportDesignIterator.hasNext() )
            {
                Report_inDesign report_inDesign = (Report_inDesign) reportDesignIterator.next();

                //String deType = report_inDesign.getPtype();
                String sType = report_inDesign.getStype();
                String deCodeString = report_inDesign.getExpression();
                String tempStr = "";
                String previousYearAllTotalTempStr = "";

                int tempRowNo = report_inDesign.getRowno();
                int tempColNo = report_inDesign.getColno();
                int sheetNo = report_inDesign.getSheetno();
                
                Sheet sheet0 = apachePOIWorkbook.getSheetAt( sheetNo );
                
                if ( sType.equalsIgnoreCase( "dataelement" ) )
                {
                    tempStr = getAggVal( deCodeString, aggDeMap );
                    previousYearAllTotalTempStr = getAggVal( deCodeString, allChildOrgUnitWiseAggDeMap );
                    
                    
                    if( deCodeString.equalsIgnoreCase( "FACILITY" ) || deCodeString.equalsIgnoreCase( "FACILITYPP" ) )
                    {
                        continue;
                        //tempRowNo = reportDesign.getRowno();
                        //tempColNo = reportDesign.getColno();
                    }
                    else
                    {
                        if( deCodeString.equalsIgnoreCase( "PROGRESSIVE-ORGUNIT" ) )
                        {
                            continue;
                        }
                        else
                        {
                            tempColNo = tempColNo + tempColCount;
                            //System.out.println( " DECode : " + deCodeString + " deType : " + deType + "   TempStr : " + tempStr +  " rowNo : " + tempRowNo + " colNo : " + tempColNo );
                            //tempRowNo = reportDesign.getRowno();
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo - 1 );
                                cell.setCellValue( Double.parseDouble( previousYearAllTotalTempStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo - 1  );
                                cell.setCellValue( previousYearAllTotalTempStr );
                                
                            }
                            
                            if ( currentOrgUnitGroup.getUid().equalsIgnoreCase( "xYvEtLYNPKx" )  )
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
                            else if ( currentOrgUnitGroup.getUid().equalsIgnoreCase( "wR42WpA0VHp" )  )
                            {
                                try
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 1 );
                                    cell.setCellValue( Double.parseDouble( tempStr ) );
                                    
                                }
                                catch ( Exception e )
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 1 );
                                    cell.setCellValue( tempStr );
                                    
                                }
                            }
                            else if ( currentOrgUnitGroup.getUid().equalsIgnoreCase( "NP6zRkPiA4S" )  )
                            {
                                try
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 2 );
                                    cell.setCellValue( Double.parseDouble( tempStr ) );
                                    
                                }
                                catch ( Exception e )
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 2 );
                                    cell.setCellValue( tempStr );
                                    
                                }
                            }
                            else if ( currentOrgUnitGroup.getUid().equalsIgnoreCase( "K3UhUR7OIm0" )  )
                            {
                                try
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 3 );
                                    cell.setCellValue( Double.parseDouble( tempStr ) );
                                    
                                }
                                catch ( Exception e )
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 3 );
                                    cell.setCellValue( tempStr );
                                    
                                }
                            }
                            else if ( currentOrgUnitGroup.getUid().equalsIgnoreCase( "R9BqNOdb28Q" )  )
                            {
                                try
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 4 );
                                    cell.setCellValue( Double.parseDouble( tempStr ) );
                                    
                                }
                                catch ( Exception e )
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 4 );
                                    cell.setCellValue( tempStr );
                                    
                                }
                            }
                            else if ( currentOrgUnitGroup.getUid().equalsIgnoreCase( "LzDGwjcCNbD" )  )
                            {
                                try
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 5 );
                                    cell.setCellValue( Double.parseDouble( tempStr ) );
                                    
                                }
                                catch ( Exception e )
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 5 );
                                    cell.setCellValue( tempStr );
                                    
                                }
                            }
                            else if ( currentOrgUnitGroup.getUid().equalsIgnoreCase( "bYeMmLxh8Xs" )  )
                            {
                                try
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 6 );
                                    cell.setCellValue( Double.parseDouble( tempStr ) );
                                    
                                }
                                catch ( Exception e )
                                {
                                    Row row = sheet0.getRow( tempRowNo );
                                    Cell cell = row.getCell( tempColNo + 6 );
                                    cell.setCellValue( tempStr );
                                }
                            }
                        }
                    }
                }
                //count1++;
            }
            //orgUnitGroupCount++;
            //tempColCount++;
        }
        
        //System.out.println( " col Count after total : " + tempColCount );
        
        // code for apachePOI Workbook
        fileName = reportFileNameTB.replace( ".xlsx", "" );
        fileName += "_" + selectedOrgUnit.getName() + ".xlsx";
        //fileName += "_" + simpleDateFormat.format( selectedPeriod.getStartDate() ) + ".xlsx";
        
        tempFile.close(); //Close the InputStream
        
        FileOutputStream output_file = new FileOutputStream( new File(  outputReportPath ) );  //Open FileOutputStream to write updates
        
        //XSSFFormulaEvaluator.evaluateAllFormulaCells( apachePOIWorkbook );
        apachePOIWorkbook.setForceFormulaRecalculation(true);
        /*
        FormulaEvaluator evaluator = apachePOIWorkbook.getCreationHelper().createFormulaEvaluator();
        for (Sheet sheet : apachePOIWorkbook ) 
        {
            for (Row r : sheet) 
            {
                for (Cell c : r) 
                {
                    System.out.println(  " Row : " + r.getRowNum() +" Coloum  : " + c.getColumnIndex() + " cell Type -- " + c.getCellType() );
                    
                    if (c.getCellType() == Cell.CELL_TYPE_FORMULA) 
                    {
                        evaluator.evaluateFormulaCell(c);
                    }
                }
            }
        }
        */
        apachePOIWorkbook.write( output_file ); //write changes

        output_file.close();  //close the stream   
        
        File outputReportFile = new File( outputReportPath );
        inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );
        
        outputReportFile.deleteOnExit();
        
        System.out.println( selectedOrgUnit.getName()+ " : " + selReportObj.getName() + " - Generation End Time is : " + new Date() );
        
        return SUCCESS;
    }
    
    // supportive methods
    public String getAggVal( String expression, Map<String, String> aggDeMap )
    {
        //System.out.println( " expression -- " + expression + " aggDeMap " + aggDeMap.size() );
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

                d = Math.round( d );

            }
            catch ( Exception e )
            {
                d = 0.0;
                resultValue = "";
            }

            resultValue = "" + (double) d;
            
            //System.out.println( " expression -- " + expression +" -- resultValue " + resultValue);
            return resultValue;
        }
        catch ( NumberFormatException ex )
        {
            throw new RuntimeException( "Illegal DataElement id", ex );
        }
    }
    
    public XSSFCellStyle getCellFormatPOIExtended( XSSFWorkbook apachePOIWorkbook )
        throws Exception
    {
 
        XSSFCellStyle my_style = apachePOIWorkbook.createCellStyle();

        my_style.setAlignment( HorizontalAlignment.CENTER );
        my_style.setVerticalAlignment( org.apache.poi.ss.usermodel.VerticalAlignment.CENTER );
        //my_style.setFillForegroundColor( IndexedColors.LIGHT_GREEN.getIndex() );
        my_style.setFillPattern( FillPatternType.SOLID_FOREGROUND );
        my_style.setWrapText( true );

        XSSFFont my_font = apachePOIWorkbook.createFont();
        my_style.setFont( my_font );

        return my_style;

    }
    
    
    /**
     * Support method for getOrganisationUnitWithChildren(). Adds all
     * OrganisationUnit children to a result collection.
     */
    public Collection<OrganisationUnit> getOrganisationUnitWithChildren( int id )
    {
        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( id );

        if ( organisationUnit == null )
        {
            return Collections.emptySet();
        }

        List<OrganisationUnit> result = new ArrayList<OrganisationUnit>();

        int rootLevel = 1;

        organisationUnit.setHierarchyLevel( rootLevel );

        result.add( organisationUnit );

        addOrganisationUnitChildren( organisationUnit, result, rootLevel );

        return result;
    }


    private void addOrganisationUnitChildren( OrganisationUnit parent, List<OrganisationUnit> result, int level )
    {
        if ( parent.getChildren() != null && parent.getChildren().size() > 0 )
        {
            level++;
        }

        List<OrganisationUnit> childList = parent.getSortedChildren();

        for ( OrganisationUnit child : childList )
        {
            child.setHierarchyLevel( level );

            result.add( child );

            addOrganisationUnitChildren( child, result, level );
        }

        level--;
    }
        
 
//
    
    public String getExpressionIds( List<Report_inDesign> reportDesignList )
    {
        String expressionIdsByComma = "-1";
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
                    expressionIdsByComma += "," + dataElementId;
                    replaceString = "";
                    matcher.appendReplacement( buffer, replaceString );
                }
            }
            catch ( Exception e )
            {

            }
        }

        return expressionIdsByComma;
    }

    // -------------------------------------------------------------------------
    // Get Aggregated Result for dataelement expression
    // -------------------------------------------------------------------------

    public List<Report_inDesign> getReportDesign( String fileName, String tagName )
    {
        List<Report_inDesign> reportDesignList = new ArrayList<Report_inDesign>();

        String path = System.getProperty( "user.home" ) + File.separator + "dhis" + File.separator
            + configurationService_IN.getConfigurationByKey( Configuration_IN.KEY_REPORTFOLDER ).getValue()
            + File.separator + fileName;
        try
        {
            String newpath = System.getenv( "DHIS2_HOME" );
            if ( newpath != null )
            {
                path = newpath + File.separator
                    + configurationService_IN.getConfigurationByKey( Configuration_IN.KEY_REPORTFOLDER ).getValue()
                    + File.separator + fileName;
            }
        }
        catch ( NullPointerException npe )
        {
            System.out.println( "DHIS2_HOME not set" );
        }

        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse( new File( path ) );
            if ( doc == null )
            {
                System.out.println( "There is no DECodes related XML file in the ra folder" );
                return null;
            }

            NodeList listOfDECodes = doc.getElementsByTagName( tagName );
            int totalDEcodes = listOfDECodes.getLength();

            for ( int s = 0; s < totalDEcodes; s++ )
            {
                Element deCodeElement = (Element) listOfDECodes.item( s );
                NodeList textDECodeList = deCodeElement.getChildNodes();

                String expression = ((Node) textDECodeList.item( 0 )).getNodeValue().trim();
                String stype = deCodeElement.getAttribute( "stype" );
                String ptype = deCodeElement.getAttribute( "type" );
                int sheetno = new Integer( deCodeElement.getAttribute( "sheetno" ) );
                int rowno = new Integer( deCodeElement.getAttribute( "rowno" ) );
                int colno = new Integer( deCodeElement.getAttribute( "colno" ) );

                Report_inDesign report_inDesign = new Report_inDesign( stype, ptype, sheetno, rowno, colno, expression );
                reportDesignList.add( report_inDesign );
            }// end of for loop with s var
        }// try block end
        catch ( SAXParseException err )
        {
            System.out.println( "** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId() );
            System.out.println( " " + err.getMessage() );
        }
        catch ( SAXException e )
        {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
        return reportDesignList;
    }
    
    
    
    // get dataSet reporting rate
    public Map<String, String> getDataSetReportingRate( String dataSetIdsByComma, String periodIdsByComma )
    {
        Map<String, String> dataSetReportingRateMap = new HashMap<String, String>();
        double reportingRate;
        try
        {
            String query = "SELECT datasetid, periodid, attributeoptioncomboid, count( sourceid ) from completedatasetregistration " +
                           "WHERE  datasetid IN (" + dataSetIdsByComma + ") AND  periodid IN (" + periodIdsByComma + ") " + 
                           "GROUP BY datasetid, periodid, attributeoptioncomboid";
              
            //System.out.println( "data-set-Query - " + query );
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            while ( rs.next() )
            {
                Integer datasetId = rs.getInt( 1 );
                Integer periodId = rs.getInt( 2 );
                Integer attComId = rs.getInt( 3 );
                Integer completeCount = rs.getInt( 4 );
                
                if ( datasetId != null && periodId != null && attComId != null && completeCount != null )
                {
                    DataSet dataSet = dataSetService.getDataSet( datasetId );
                    if( dataSet != null && dataSet.getSources().size() > 0 )
                    {
                        //reportingRate = (double) (completeCount/dataSet.getSources().size())*100;
                        try
                        {
                            //System.out.println( "Result is : \t" + sqlResultSet.getLong( 1 ) );
                            reportingRate = ((double) completeCount / (double) ( dataSet.getSources().size() )) * 100.0;
                            
                        }
                        catch ( Exception e )
                        {
                            reportingRate = 0.0;
                        }
                        String tempReportingRate = String.format("%.2f", reportingRate );
                        dataSetReportingRateMap.put( datasetId + ":" + periodId, tempReportingRate );
                        //System.out.println( "dataSetId - " + dataSet.getId() + " periodId  - " + datasetId + " completeCount  - " + completeCount + " sourceCount  - " + dataSet.getSources().size() + " reportingRate  - " + reportingRate  + " tempReportingRate  - " + tempReportingRate  );
                    }
                }
            }
            return dataSetReportingRateMap;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal DataSetIds and PeriodIds", e );
        }
    }        
    
    public String getReportingRate( String expression, String periodId, Map<String, String> reportingRateMap )
    {
        //System.out.println( " expression -- " + expression + " aggDeMap " + aggDeMap.size() );
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
                replaceString = replaceString.substring( 0, replaceString.indexOf( '.' ) );

                int dataSetId = Integer.parseInt( replaceString );

                replaceString = reportingRateMap.get( dataSetId + ":" + periodId );

                if ( replaceString == null )
                {
                    replaceString = "0";
                }

                matcher.appendReplacement( buffer, replaceString );

                resultValue = replaceString;
            }
            
            matcher.appendTail( buffer );
            
            return resultValue;
        }
        catch ( NumberFormatException ex )
        {
            throw new RuntimeException( "Illegal DataSet id", ex );
        }
    }
}

