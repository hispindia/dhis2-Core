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
import org.hisp.dhis.config.ConfigurationService;
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
import org.hisp.dhis.period.comparator.PeriodComparator;
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
    private ConfigurationService configurationService;
    
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
    
    private Map<String, String> dhAggDeMap = new HashMap<String, String>();
    private Map<String, String> sdhAggDeMap = new HashMap<String, String>();
    private Map<String, String> chcAggDeMap = new HashMap<String, String>();
    private Map<String, String> phcAggDeMap = new HashMap<String, String>();
    private Map<String, String> scAggDeMap = new HashMap<String, String>();
    
    private List<OrganisationUnit> level7OrgList = new ArrayList<OrganisationUnit>();
    private List<OrganisationUnit> level8OrgList = new ArrayList<OrganisationUnit>();
    
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
        
        List<OrganisationUnit> allChildOrgUnitTree = new ArrayList<OrganisationUnit>( getOrganisationUnitWithChildren( selectedOrgUnit.getId() ) );
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
        Collections.sort( selectedPeriodList, new PeriodComparator() );
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
        }

        
        level7OrgList = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitsAtLevel( 7 ) );
        level8OrgList = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitsAtLevel( 8 ) );
        
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
        
        // dataElement data-value printing
        int orgUnitCount = 0;
        int mregeColCount = 0;
        int tempColCount = 0;
        Iterator<OrganisationUnit> orgIt = districtOrgUnitList.iterator();
        while ( orgIt.hasNext() )
        {
            OrganisationUnit currentOrgUnit = (OrganisationUnit) orgIt.next();
            //Map<String, String> districtWiseAggDeMap = new HashMap<String, String>();
            //districtWiseAggDeMap.putAll( orgUnitWiseAggDeMap.get( currentOrgUnit.getId() ));
            List<OrganisationUnit> childOrgUnitTree = new ArrayList<OrganisationUnit>( getOrganisationUnitWithChildren( currentOrgUnit.getId() ) );
            List<Integer> districtOrgUnitIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, childOrgUnitTree ) );
            String districtOrgUnitIdsByComma = getCommaDelimitedString( districtOrgUnitIds );
            Map<String, String> districtOrgUnitWiseAggDeMap =  new HashMap<String, String>();
            districtOrgUnitWiseAggDeMap.putAll( reportService.getAggDataFromDataValueTable( districtOrgUnitIdsByComma, dataElmentIdsByComma, periodIdsByComma ) );
            
            // dh
            
            OrganisationUnitGroup dhOrgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( "NP6zRkPiA4S" );
            
            if( dhOrgUnitGroup != null )
            {
                dhAggDeMap = new HashMap<String, String>();
                List<OrganisationUnit> childOrgUnitTreeForDH = new ArrayList<OrganisationUnit>( getOrganisationUnitWithChildren( currentOrgUnit.getId() ) );
                List<OrganisationUnit> dhOrgUnitGrpMember = new ArrayList<OrganisationUnit>( dhOrgUnitGroup.getMembers() );
                dhOrgUnitGrpMember.retainAll( childOrgUnitTreeForDH );
                String childOrgUnitsByComma = "-1";
                if( dhOrgUnitGrpMember.size() > 0 )
                {
                    List<Integer> childOrgUnitTreeIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, dhOrgUnitGrpMember ) );
                    childOrgUnitsByComma = getCommaDelimitedString( childOrgUnitTreeIds );
                }
                
                dhAggDeMap.putAll( reportService.getAggDataFromDataValueTable( childOrgUnitsByComma, dataElmentIdsByComma, periodIdsByComma ) );
            }
            
            // sdh
            OrganisationUnitGroup sdhOrgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( "K3UhUR7OIm0" );
            
            if( sdhOrgUnitGroup != null )
            {
                sdhAggDeMap = new HashMap<String, String>();
                List<OrganisationUnit> childOrgUnitTreeForSDH = new ArrayList<OrganisationUnit>( getOrganisationUnitWithChildren( currentOrgUnit.getId() ) );
                List<OrganisationUnit> sdhOrgUnitGrpMember = new ArrayList<OrganisationUnit>( sdhOrgUnitGroup.getMembers() );
                sdhOrgUnitGrpMember.retainAll( childOrgUnitTreeForSDH );
                String childOrgUnitsByComma = "-1";
                if( sdhOrgUnitGrpMember.size() > 0 )
                {
                    List<Integer> childOrgUnitTreeIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, sdhOrgUnitGrpMember ) );
                    childOrgUnitsByComma = getCommaDelimitedString( childOrgUnitTreeIds );
                }
                
                sdhAggDeMap.putAll( reportService.getAggDataFromDataValueTable( childOrgUnitsByComma, dataElmentIdsByComma, periodIdsByComma ) );
            }
            
            // chc
            OrganisationUnitGroup chcOrgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( "R9BqNOdb28Q" );
            
            if( chcOrgUnitGroup != null )
            {
                chcAggDeMap = new HashMap<String, String>();
                List<OrganisationUnit> childOrgUnitTreeForCHC = new ArrayList<OrganisationUnit>( getOrganisationUnitWithChildren( currentOrgUnit.getId() ) );
                List<OrganisationUnit> chcOrgUnitGrpMember = new ArrayList<OrganisationUnit>( chcOrgUnitGroup.getMembers() );
                chcOrgUnitGrpMember.retainAll( childOrgUnitTreeForCHC );
                String childOrgUnitsByComma = "-1";
                if( chcOrgUnitGrpMember.size() > 0 )
                {
                    List<Integer> childOrgUnitTreeIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, chcOrgUnitGrpMember ) );
                    childOrgUnitsByComma = getCommaDelimitedString( childOrgUnitTreeIds );
                }
                
                chcAggDeMap.putAll( reportService.getAggDataFromDataValueTable( childOrgUnitsByComma, dataElmentIdsByComma, periodIdsByComma ) );
            }            
            
            // phc
            OrganisationUnitGroup phcOrgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( "LzDGwjcCNbD" );
            
            if( phcOrgUnitGroup != null )
            {
                //System.out.println( currentOrgUnit.getName() +  " Inside phc -- " + phcOrgUnitGroup.getName() );
                
                phcAggDeMap = new HashMap<String, String>();
                List<OrganisationUnit> childOrgUnitTreeForPHC = new ArrayList<OrganisationUnit>( getOrganisationUnitWithChildren( currentOrgUnit.getId() ) );
                //System.out.println( " 1 childOrgUnitTree size -- " + childOrgUnitTreeForPHC.size() );
                //System.out.println( " 1 Level 7 size -- " + level7OrgList.size() );
                childOrgUnitTreeForPHC.retainAll( level7OrgList );
                
                //System.out.println( " 2 childOrgUnitTree -- " + childOrgUnitTreeForPHC.size() );
                
                List<OrganisationUnit> phcOrgUnitGrpMember = new ArrayList<OrganisationUnit>( phcOrgUnitGroup.getMembers() );
                //System.out.println( " 1 phcOrgUnitGrpMember -- " + phcOrgUnitGrpMember.size() );
                
                phcOrgUnitGrpMember.retainAll( childOrgUnitTreeForPHC );
                
                //System.out.println( " 2 phcOrgUnitGrpMember -- " + phcOrgUnitGrpMember.size() );
                
                String childOrgUnitsByComma = "-1";
                if( phcOrgUnitGrpMember.size() > 0 )
                {
                    List<Integer> childOrgUnitTreeIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, phcOrgUnitGrpMember ) );
                    childOrgUnitsByComma = getCommaDelimitedString( childOrgUnitTreeIds );
                }
                
                phcAggDeMap.putAll( reportService.getAggDataFromDataValueTable( childOrgUnitsByComma, dataElmentIdsByComma, periodIdsByComma ) );
            }            
            
            // sc
            OrganisationUnitGroup scOrgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( "bYeMmLxh8Xs" );
            
            if( scOrgUnitGroup != null )
            {
                //System.out.println( currentOrgUnit.getName() +  " Inside sc -- " + scOrgUnitGroup.getName() );
                scAggDeMap = new HashMap<String, String>();
                List<OrganisationUnit> childOrgUnitTreeForSC = new ArrayList<OrganisationUnit>( getOrganisationUnitWithChildren( currentOrgUnit.getId() ) );
                //System.out.println( " 1 childOrgUnitTree size -- " + childOrgUnitTreeForSC.size() );
                //System.out.println( " 1 Level 8 size -- " + level8OrgList.size() );
                
                childOrgUnitTreeForSC.retainAll( level8OrgList );
                //level8OrgList.retainAll( childOrgUnitTree );
                //System.out.println( " 2 childOrgUnitTree -- " + childOrgUnitTreeForSC.size() );
                
                List<OrganisationUnit> scOrgUnitGrpMember = new ArrayList<OrganisationUnit>( scOrgUnitGroup.getMembers() );
                //System.out.println( " 1 scOrgUnitGrpMember -- " + scOrgUnitGrpMember.size() );
                
                scOrgUnitGrpMember.retainAll( childOrgUnitTreeForSC );
                //scOrgUnitGrpMember.retainAll( level8OrgList );
                
                //System.out.println( " 2 scOrgUnitGrpMember -- " + scOrgUnitGrpMember.size() );
                
                String childOrgUnitsByComma = "-1";
                if( scOrgUnitGrpMember.size() > 0 )
                {
                    List<Integer> childOrgUnitTreeIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, scOrgUnitGrpMember ) );
                    childOrgUnitsByComma = getCommaDelimitedString( childOrgUnitTreeIds );
                }
                
                scAggDeMap.putAll( reportService.getAggDataFromDataValueTable( childOrgUnitsByComma, dataElmentIdsByComma, periodIdsByComma ) );
            }            
            
            Iterator<Report_inDesign> reportDesignIterator = reportDesignListDataElement.iterator();
            while ( reportDesignIterator.hasNext() )
            {
                Report_inDesign report_inDesign = (Report_inDesign) reportDesignIterator.next();

                //String deType = report_inDesign.getPtype();
                String sType = report_inDesign.getStype();
                String type = report_inDesign.getPtype();
                String deCodeString = report_inDesign.getExpression();
                String tempStr = "";
                String dhTotalStr = "";
                String sdhTotalStr = "";
                String chcTotalStr = "";
                String phcTotalStr = "";
                String scTotalStr = "";
                String districtTotalStr = "";
                String excludeSCTotalStr = "";

                if ( deCodeString.equalsIgnoreCase( "PROGRESSIVE-ORGUNIT" ) )
                {
                    tempStr = currentOrgUnit.getName();
                }
                else
                {
                    if ( sType.equalsIgnoreCase( "dataelement" ) )
                    {
                        dhTotalStr = getAggVal( deCodeString, dhAggDeMap );
                        sdhTotalStr = getAggVal( deCodeString, sdhAggDeMap );
                        chcTotalStr = getAggVal( deCodeString, chcAggDeMap );
                        phcTotalStr = getAggVal( deCodeString, phcAggDeMap );
                        scTotalStr = getAggVal( deCodeString, scAggDeMap );
                        
                        districtTotalStr = getAggVal( deCodeString, districtOrgUnitWiseAggDeMap );
                        double distTotal = 0.0;
                        double scTotal = 0.0;
                        if( !districtTotalStr.equalsIgnoreCase( "" ) )
                        {
                            distTotal = Double.parseDouble( districtTotalStr );
                        }
                        
                        if( !scTotalStr.equalsIgnoreCase( "" ) )
                        {
                            scTotal = Double.parseDouble( scTotalStr );
                        }
                        
                        
                        excludeSCTotalStr = ""+( distTotal - scTotal );
                        
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
                        tempColNo = tempColNo + tempColCount;
                        //System.out.println( " DECode : " + deCodeString + " deType : " + deType + "   TempStr : " + tempStr + " rowNo : " + tempRowNo + " colNo : " + tempColNo );
                        //tempRowNo = reportDesign.getRowno();
                        
                        // for printing total
                        
                        
                        if (  type.equalsIgnoreCase( "total" ) || type.equalsIgnoreCase( "public" ) || type.equalsIgnoreCase( "private" ) )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( Double.parseDouble( districtTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( districtTotalStr );
                                
                            }
                        }
                        
                        else if (  type.equalsIgnoreCase( "total-only-sc" ) || type.equalsIgnoreCase( "public-only-sc" ) || type.equalsIgnoreCase( "private-only-sc" ) )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( Double.parseDouble( scTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( scTotalStr );
                                
                            }
                        }
                        else if (  type.equalsIgnoreCase( "total-exclude-sc" ) || type.equalsIgnoreCase( "public-exclude-sc" ) || type.equalsIgnoreCase( "private-exclude-sc" ) )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( Double.parseDouble( excludeSCTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( excludeSCTotalStr );
                                
                            }
                        }
                        else if ( type.equalsIgnoreCase( "dh" )  )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( Double.parseDouble( dhTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( dhTotalStr );
                                
                            }
                        }
                        else if ( type.equalsIgnoreCase( "sdh" )  )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo  );
                                cell.setCellValue( Double.parseDouble( sdhTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( sdhTotalStr );
                                
                            }
                        }
                        else if ( type.equalsIgnoreCase( "chc" )  )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( Double.parseDouble( chcTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( chcTotalStr );
                                
                            }
                        }
                        else if (  type.equalsIgnoreCase( "phc" ) )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( Double.parseDouble( phcTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( phcTotalStr );
                                
                            }
                        }
                        else if (  type.equalsIgnoreCase( "sc" ) )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( Double.parseDouble( scTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( scTotalStr );
                                
                            }
                        }
                    }
                }
                
                //count1++;
            }
            //orgUnitGroupCount++;
            mregeColCount+=7;
            tempColCount+=8;
            orgUnitCount++;
        }
            
        //System.out.println( " col Count before total : " + tempColCount );

        // for previous year data populate
        // dh
        
        OrganisationUnitGroup dhOrgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( "NP6zRkPiA4S" );
        
        if( dhOrgUnitGroup != null )
        {
            List<Integer> orgGroupMemberIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, dhOrgUnitGroup.getMembers() ) );
            String orgGroupMemberIdsByComma = getCommaDelimitedString( orgGroupMemberIds );
            dhAggDeMap = new HashMap<String, String>();
            dhAggDeMap.putAll( reportService.getAggDataFromDataValueTable( orgGroupMemberIdsByComma, dataElmentIdsByComma, oneYearBeforePeriodIdsByComma ) );
        }
        
        // sdh
        OrganisationUnitGroup sdhOrgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( "K3UhUR7OIm0" );
        
        if( sdhOrgUnitGroup != null )
        {
            List<Integer> orgGroupMemberIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, sdhOrgUnitGroup.getMembers() ) );
            String orgGroupMemberIdsByComma = getCommaDelimitedString( orgGroupMemberIds );
            sdhAggDeMap = new HashMap<String, String>();
            sdhAggDeMap.putAll( reportService.getAggDataFromDataValueTable( orgGroupMemberIdsByComma, dataElmentIdsByComma, oneYearBeforePeriodIdsByComma ) );
        }
        
        // chc
        OrganisationUnitGroup chcOrgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( "R9BqNOdb28Q" );
        
        if( chcOrgUnitGroup != null )
        {
            List<Integer> orgGroupMemberIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, chcOrgUnitGroup.getMembers() ) );
            String orgGroupMemberIdsByComma = getCommaDelimitedString( orgGroupMemberIds );
            chcAggDeMap = new HashMap<String, String>();
            chcAggDeMap.putAll( reportService.getAggDataFromDataValueTable( orgGroupMemberIdsByComma, dataElmentIdsByComma, oneYearBeforePeriodIdsByComma ) );
        }
        
        // phc
        OrganisationUnitGroup phcOrgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( "LzDGwjcCNbD" );
        
        if( phcOrgUnitGroup != null )
        {
            List<Integer> orgGroupMemberIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, phcOrgUnitGroup.getMembers() ) );
            String orgGroupMemberIdsByComma = getCommaDelimitedString( orgGroupMemberIds );
            phcAggDeMap = new HashMap<String, String>();
            phcAggDeMap.putAll( reportService.getAggDataFromDataValueTable( orgGroupMemberIdsByComma, dataElmentIdsByComma, oneYearBeforePeriodIdsByComma ) );
        }
        
        // sc
        OrganisationUnitGroup scOrgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( "bYeMmLxh8Xs" );
        
        if( scOrgUnitGroup != null )
        {
            List<Integer> orgGroupMemberIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, scOrgUnitGroup.getMembers() ) );
            String orgGroupMemberIdsByComma = getCommaDelimitedString( orgGroupMemberIds );
            scAggDeMap = new HashMap<String, String>();
            scAggDeMap.putAll( reportService.getAggDataFromDataValueTable( orgGroupMemberIdsByComma, dataElmentIdsByComma, oneYearBeforePeriodIdsByComma ) );
        }
        
        tempColCount = tempColCount + 8;
        
        Iterator<Report_inDesign> reportDesignIterator = reportDesignListDataElement.iterator();
        while ( reportDesignIterator.hasNext() )
        {
            Report_inDesign report_inDesign = (Report_inDesign) reportDesignIterator.next();

            //String deType = report_inDesign.getPtype();
            String sType = report_inDesign.getStype();
            String type = report_inDesign.getPtype();
            String deCodeString = report_inDesign.getExpression();
            //String tempStr = "";
            String dhTotalStr = "";
            String sdhTotalStr = "";
            String chcTotalStr = "";
            String phcTotalStr = "";
            String scTotalStr = "";
            String excludeSCTotalStr = "";
            
            String previousYearAllTotalTempStr = "";

            int tempRowNo = report_inDesign.getRowno();
            int tempColNo = report_inDesign.getColno();
            int sheetNo = report_inDesign.getSheetno();
            
            Sheet sheet0 = apachePOIWorkbook.getSheetAt( sheetNo );
            
            if ( sType.equalsIgnoreCase( "dataelement" ) )
            {
                //tempStr = getAggVal( deCodeString, aggDeMap );
                dhTotalStr = getAggVal( deCodeString, dhAggDeMap );
                sdhTotalStr = getAggVal( deCodeString, sdhAggDeMap );
                chcTotalStr = getAggVal( deCodeString, chcAggDeMap );
                phcTotalStr = getAggVal( deCodeString, phcAggDeMap );
                scTotalStr = getAggVal( deCodeString, scAggDeMap );
                previousYearAllTotalTempStr = getAggVal( deCodeString, allChildOrgUnitWiseAggDeMap );
                
                double distTotal = 0.0;
                double scTotal = 0.0;
                if( !previousYearAllTotalTempStr.equalsIgnoreCase( "" ) )
                {
                    distTotal = Double.parseDouble( previousYearAllTotalTempStr );
                }
                
                if( !scTotalStr.equalsIgnoreCase( "" ) )
                {
                    scTotal = Double.parseDouble( scTotalStr );
                }
                
                
                excludeSCTotalStr = ""+( distTotal - scTotal );
                
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
                        if (  type.equalsIgnoreCase( "total" ) || type.equalsIgnoreCase( "public" ) || type.equalsIgnoreCase( "private" ) )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( Double.parseDouble( previousYearAllTotalTempStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( previousYearAllTotalTempStr );
                                
                            }
                        }
                        else if (  type.equalsIgnoreCase( "total-only-sc" ) || type.equalsIgnoreCase( "public-only-sc" ) || type.equalsIgnoreCase( "private-only-sc" ) )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( Double.parseDouble( scTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( scTotalStr );
                                
                            }
                        }
                        else if (  type.equalsIgnoreCase( "total-exclude-sc" ) || type.equalsIgnoreCase( "public-exclude-sc" ) || type.equalsIgnoreCase( "private-exclude-sc" ) )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( Double.parseDouble( excludeSCTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( excludeSCTotalStr );
                                
                            }
                        }                        
                        else if ( type.equalsIgnoreCase( "dh" )  )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( Double.parseDouble( dhTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( dhTotalStr );
                                
                            }
                        }
                        else if ( type.equalsIgnoreCase( "sdh" )  )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo  );
                                cell.setCellValue( Double.parseDouble( sdhTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( sdhTotalStr );
                                
                            }
                        }
                        else if ( type.equalsIgnoreCase( "chc" )  )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( Double.parseDouble( chcTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( chcTotalStr );
                                
                            }
                        }
                        else if (  type.equalsIgnoreCase( "phc" ) )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( Double.parseDouble( phcTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( phcTotalStr );
                                
                            }
                        }
                        else if (  type.equalsIgnoreCase( "sc" ) )
                        {
                            try
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( Double.parseDouble( scTotalStr ) );
                                
                            }
                            catch ( Exception e )
                            {
                                Row row = sheet0.getRow( tempRowNo );
                                Cell cell = row.getCell( tempColNo );
                                cell.setCellValue( scTotalStr );
                                
                            }
                        }
                    }
                }
            }
            //count1++;
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
            + configurationService.getConfigurationByKey( Configuration_IN.KEY_REPORTFOLDER ).getValue()
            + File.separator + fileName;
        try
        {
            String newpath = System.getenv( "DHIS2_HOME" );
            if ( newpath != null )
            {
                path = newpath + File.separator
                    + configurationService.getConfigurationByKey( Configuration_IN.KEY_REPORTFOLDER ).getValue()
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

