package org.hisp.dhis.reports.kayakalp.action;

//import static org.hisp.dhis.system.util.ConversionUtils.getIdentifiers;
//import static org.hisp.dhis.system.util.TextUtils.getCommaDelimitedString;

import static org.hisp.dhis.util.ConversionUtils.getIdentifiers;
import static org.hisp.dhis.util.TextUtils.getCommaDelimitedString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hisp.dhis.config.Configuration_IN;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
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
public class GenerateKayakalpBaseLineReportAnalyserResultAction implements Action
{

    private final String GENERATEAGGDATA = "generateaggdata";

    private final String USEEXISTINGAGGDATA = "useexistingaggdata";

    private final String USECAPTUREDDATA = "usecaptureddata";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

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
    private OrganisationUnitGroupService organisationUnitGroupService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
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
     * private String aggCB;
     * 
     * public void setAggCB( String aggCB ) { this.aggCB = aggCB; }
     */
    private String reportFileNameTB;

    private String reportModelTB;

    private List<OrganisationUnit> orgUnitList;

    private Period selectedPeriod;

    private SimpleDateFormat simpleDateFormat;

    private SimpleDateFormat monthFormat;

    private SimpleDateFormat simpleMonthFormat;

    private SimpleDateFormat yearFormat;

    private Date sDate;

    private Date eDate;

    private String raFolderName;

    private String aggData;

    public void setAggData( String aggData )
    {
        this.aggData = aggData;
    }

    private OrganisationUnit selectedOrgUnit;
    private Report_in selReportObj;
    private String inputTemplatePath;
    private String outputReportPath;
    private String dataElmentIdsByComma;
    private List<Report_inDesign> reportDesignList = new ArrayList<Report_inDesign>();
    private String periodIdsByComma;
    
    private Map<Integer, String> orgUnitAttributeValueMap = new HashMap<Integer, String>();
    private Map<Integer, String> orgUnitGroupNameMap = new HashMap<Integer, String>();
    
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
    {
        // statementManager.initialise();

        // Initialization
        raFolderName = reportService.getRAFolderName();
        String deCodesXMLFileName = "";
        simpleDateFormat = new SimpleDateFormat( "MMM-yyyy" );
        monthFormat = new SimpleDateFormat( "MMMM" );
        simpleMonthFormat = new SimpleDateFormat( "MMM" );
        yearFormat = new SimpleDateFormat( "yyyy" );

        //orgUnitAttributeValueMap.putAll( getOrgUnitAttributeValueMap() );
        //getOrgUnitAttributeValueMap();
        getOrgUnitGroupNameMap();
        
        //orgUnitGroupNameMap = new HashMap<Integer, String>( getOrgUnitGroupNameMap() );
        
        //orgUnitAttributeValueMap = new HashMap<Integer, String>( getOrgUnitAttributeValueMap() );
        
        //System.out.println( " Map Size : " + orgUnitGroupNameMap.size() );
        
        selReportObj = reportService.getReport( Integer.parseInt( reportList ) );

        deCodesXMLFileName = selReportObj.getXmlTemplateName();

        reportModelTB = selReportObj.getModel();
        reportFileNameTB = selReportObj.getExcelTemplateName();

        // Period Info
        selectedPeriod = periodService.getPeriod( availablePeriods );
        sDate = format.parseDate( String.valueOf( selectedPeriod.getStartDate() ) );
        eDate = format.parseDate( String.valueOf( selectedPeriod.getEndDate() ) );

        List<Period> periodList = new ArrayList<Period>();
        periodList = new ArrayList<Period>( periodService.getIntersectingPeriods( sDate, eDate ) );
        Collection<Integer> periodIds = new ArrayList<Integer>( getIdentifiers(Period.class, periodList ) );        
        periodIdsByComma = getCommaDelimitedString( periodIds );
        
        // Getting DataValues
        reportDesignList = new ArrayList<Report_inDesign>( reportService.getReportDesign( deCodesXMLFileName ));
        
        dataElmentIdsByComma = reportService.getDataelementIds( reportDesignList );
        
        String dataSetIdsByComma = "82,83,84,85,86,87,88,97,98,99,100,101,102,103,90,91,92,93,94,95,96,104,105,106,107,108,109,110";
        //List<String> kayaKalpDataSetList =  new ArrayList<String>( );
        
        String[] elements = dataSetIdsByComma.split(",");
        List<String> fixedLenghtList = Arrays.asList(elements);
        ArrayList<String> kayaKalpDataSetList = new ArrayList<String>(fixedLenghtList);
        
        //kayaKalpDataSetList = Arrays.asList( dataElmentIdsByComma.split("\\s*,\\s*") );
        
        //System.out.println(  " dataElmentIdsByComma : " +dataElmentIdsByComma );
        
        // OrgUnit Related Info
        selectedOrgUnit = new OrganisationUnit();
        selectedOrgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );
        //int selectedOrgUnitLevel = organisationUnitService.getLevelOfOrganisationUnit( ouIDTB );
        int selectedOrgUnitLevel = selectedOrgUnit.getLevel();
        
        System.out.println( selectedOrgUnit.getName() + " : " + selReportObj.getName()  + " : Report Generation Start Time is : " + new Date() );
        
        orgUnitList = new ArrayList<OrganisationUnit>();
        
        if( selectedOrgUnitLevel == 2 )
        {
            System.out.println( selectedOrgUnit.getName() + " : " + selectedOrgUnitLevel );
            //orgUnitList.add( selectedOrgUnit );
            //getOrgUnitAttributeValueMap();
            orgUnitList = new ArrayList<OrganisationUnit>( selectedOrgUnit.getChildren() );
            //Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );
            Collections.sort( orgUnitList );
        }

        else if( selectedOrgUnitLevel == 3 )
        {
            List<OrganisationUnit> childOrgUnitTree = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitWithChildren( selectedOrgUnit.getId() ) );
            List<OrganisationUnit> orgUnitGroupMembers = new ArrayList<OrganisationUnit>();
            if( selReportObj.getOrgunitGroup() != null )
            {
                orgUnitGroupMembers.addAll( selReportObj.getOrgunitGroup().getMembers() );
                orgUnitGroupMembers.retainAll( childOrgUnitTree );
                orgUnitList.addAll( orgUnitGroupMembers );
            }
            else
            {
                List<String> kayaKalpGroups = new ArrayList<String>();
                kayaKalpGroups.add("L5CSAKMrPiW"); // for KayaKalp DH
                kayaKalpGroups.add("qQHuUHiI9ak"); // for KayaKalp SDH
                kayaKalpGroups.add("H5Xrf4fu3un"); // for KayaKalp CH
                kayaKalpGroups.add("fkBGGFKg01E"); // for KayaKalp CHC
                //kayaKalpGroups.add("25"); // for KayaKalp PHC
                //kayaKalpGroups.add("26"); // for KayaKalp sub-center
                //getOrgUnitAttributeValueMap();
                
                //System.out.println( selectedOrgUnit.getName() + " Level : " + selectedOrgUnitLevel + " child Tree Count " + childOrgUnitTree.size());
                //Set<OrganisationUnit> dataSetSource = new HashSet<OrganisationUnit>();
                
                
                /*
                OrganisationUnitGroupSet  organisationUnitGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSetByName( "Kayakalp" );
                if( organisationUnitGroupSet != null )
                {
                    List<OrganisationUnitGroup> orgUnitGroupSetGroups = new ArrayList<OrganisationUnitGroup>( organisationUnitGroupSet.getSortedGroups() );
                    if( orgUnitGroupSetGroups != null && orgUnitGroupSetGroups.size() > 0 )
                    {
                        for( OrganisationUnitGroup orgUnitGroup : orgUnitGroupSetGroups)
                        {
                            System.out.println( organisationUnitGroupSet.getName() + " : " + orgUnitGroup.getName() + " : grpMem Count " + orgUnitGroup.getMembers().size());
                            orgUnitGroupMembers.addAll( orgUnitGroup.getMembers() );
                        }
                    }
                }
                */
                
                for( String orgGrp : kayaKalpGroups)
                {
                    //OrganisationUnitGroup orgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( Integer.parseInt( orgGrp ) );
                    OrganisationUnitGroup orgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( orgGrp );
                    //System.out.println( " : " + orgUnitGroup.getName() + " : grpMem Count " + orgUnitGroup.getMembers().size());
                    if( orgUnitGroup != null && orgUnitGroup.getMembers().size() > 0 )
                    {
                        orgUnitGroupMembers.addAll( orgUnitGroup.getMembers() );
                    }
                }
                
                /*
                for( String dsId : kayaKalpDataSetList )
                {
                    DataSet ds = dataSetService.getDataSet( Integer.parseInt( dsId ) );
                    //System.out.println(  " data set : " + ds.getName() + " ds source count Count " + ds.getSources().size());
                    if( ds != null && ds.getSources().size() > 0 )
                    {
                        dataSetSource.addAll( ds.getSources() );
                    }
                }
                */
                
                //orgUnitList = new ArrayList<OrganisationUnit>( getDataSetSources( dataSetIdsByComma ));
                //System.out.println( selectedOrgUnit.getName() + " Level : " + selectedOrgUnitLevel + " dsSource Count " + dataSetSource.size());
                //dataSetSource.retainAll( childOrgUnitTree );
                //orgUnitList.addAll( dataSetSource );
                orgUnitGroupMembers.retainAll( childOrgUnitTree );
                orgUnitList.addAll( orgUnitGroupMembers );
                
                //orgUnitList = new ArrayList<OrganisationUnit>(new HashSet<>( orgUnitList ));
                
                //System.out.println( selectedOrgUnit.getName() + " Level : " + selectedOrgUnitLevel + " final ou Count " + orgUnitList.size());
                //orgUnitList.add( selectedOrgUnit );
                //Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );
            }
        }
        /*
        else
        {
            System.out.println( selectedOrgUnit.getName() + " : " + selectedOrgUnitLevel );
            orgUnitList.add( selectedOrgUnit );
        }
        */
        
        System.out.println( selectedOrgUnit.getName() + " Level : " + selectedOrgUnitLevel + " final ou Count " + orgUnitList.size() );
        
        inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName + File.separator
            + "template" + File.separator + reportFileNameTB;
        // String outputReportPath = System.getenv( "DHIS2_HOME" ) +
        // File.separator + raFolderName + File.separator + "output" +
        // File.separator + UUID.randomUUID().toString() + ".xls";

        outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator + Configuration_IN.DEFAULT_TEMPFOLDER;
        File newdir = new File( outputReportPath );
        if ( !newdir.exists() )
        {
            newdir.mkdirs();
        }
        outputReportPath += File.separator + UUID.randomUUID().toString() + ".xlsx";
        
        FileInputStream tempFile = new FileInputStream( new File( inputTemplatePath ) );
        XSSFWorkbook apachePOIWorkbook = new XSSFWorkbook( tempFile );

        int orgUnitCount = 0;
        Iterator<OrganisationUnit> it = orgUnitList.iterator();
        while ( it.hasNext() )
        {
            OrganisationUnit currentOrgUnit = (OrganisationUnit) it.next();
            List<OrganisationUnit> childOrgUnitTree = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitWithChildren( currentOrgUnit.getId() ) );
            
            if( selReportObj.getOrgunitGroup() != null )
            {
                childOrgUnitTree.retainAll( selReportObj.getOrgunitGroup().getMembers() );
            }
            
            List<Integer> childOrgUnitTreeIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, childOrgUnitTree ) );
            String childOrgUnitsByComma = getCommaDelimitedString( childOrgUnitTreeIds );
            
            Map<String, String> aggDeMap = new HashMap<String, String>();
            if( aggData.equalsIgnoreCase( GENERATEAGGDATA ) )
            {
                //System.out.println( " Inside Report Service " + " facilitr : " + currentOrgUnit  + "---" + new Date() );
                
                aggDeMap.putAll( reportService.getAggDataFromDataValueTable( childOrgUnitsByComma, dataElmentIdsByComma, periodIdsByComma ) );
                
                //System.out.println( " Outside Report Service " + " facilitr : " + currentOrgUnit  + "---" + childOrgUnitsByComma );
            }
            else if( aggData.equalsIgnoreCase( USECAPTUREDDATA ) )
            {
                aggDeMap.putAll( reportService.getAggDataFromDataValueTable( ""+currentOrgUnit.getId(), dataElmentIdsByComma, periodIdsByComma ) );
            }

            else if( aggData.equalsIgnoreCase( USEEXISTINGAGGDATA ) )
            {
                aggDeMap.putAll( reportService.getResultDataValueFromAggregateTable( (int)currentOrgUnit.getId(), dataElmentIdsByComma, periodIdsByComma ) );
            }
            
            
            int count1 = 0;
            Iterator<Report_inDesign> reportDesignIterator = reportDesignList.iterator();
            while ( reportDesignIterator.hasNext() )
            {
                Report_inDesign report_inDesign = (Report_inDesign) reportDesignIterator.next();

                String sType = report_inDesign.getStype();
                String deCodeString = report_inDesign.getExpression();
                String tempStr = "";
                
                if( deCodeString.equalsIgnoreCase( "FACILITY" ) )
                {
                    tempStr = selectedOrgUnit.getName();
                } 
                else if( deCodeString.equalsIgnoreCase( "FACILITYP" ) )
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
                
                else if ( deCodeString.equalsIgnoreCase( "PERIOD" ) )
                {
                    tempStr = simpleDateFormat.format( sDate );
                }
                else if ( deCodeString.equalsIgnoreCase( "PERIOD-MONTH" ) )
                {
                    tempStr = monthFormat.format( sDate );
                }
                else if ( deCodeString.equalsIgnoreCase( "PERIOD-QUARTER" ) )
                {
                    String startMonth = "";
                    String tempYear = yearFormat.format( sDate );
                    int nextYear = Integer.parseInt( tempYear ) + 1;
                    startMonth = monthFormat.format( sDate );

                    if ( startMonth.equalsIgnoreCase( "January" ) )
                    {
                        tempStr = "January - March" + " " + tempYear;
                    }
                    
                    else if ( startMonth.equalsIgnoreCase( "April" ) )
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
                }

                else if ( deCodeString.equalsIgnoreCase( "SLNO" ) )
                {
                    tempStr = "" + (orgUnitCount + 1);
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITY-NAME" ) )
                {
                    tempStr = currentOrgUnit.getName();
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITY-TYPE" ) && selectedOrgUnitLevel == 2 )
                {
                    tempStr = orgUnitGroupNameMap.get( (int)currentOrgUnit.getId() );
                    
                    tempStr = "District";
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITY-TYPE" ) && selectedOrgUnitLevel == 3 )
                {
                    tempStr = orgUnitGroupNameMap.get( (int)currentOrgUnit.getId() );
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITY-TYPE" ) )
                {
                    tempStr = orgUnitGroupNameMap.get( (int)currentOrgUnit.getId() );
                }
                else if ( deCodeString.equalsIgnoreCase( "NA" ) )
                {
                    tempStr = " ";
                }
                
                else
                {
                    if ( sType.equalsIgnoreCase( "dataelement" ) )
                    {
                        if( aggData.equalsIgnoreCase( USECAPTUREDDATA ) ) 
                        {
                            tempStr = getAggVal( deCodeString, aggDeMap );
                        }
                        else if( aggData.equalsIgnoreCase( GENERATEAGGDATA ) )
                        {
                            tempStr = getAggVal( deCodeString, aggDeMap );
                        }
                        else if( aggData.equalsIgnoreCase( USEEXISTINGAGGDATA ) )
                        {
                            tempStr = getAggVal( deCodeString, aggDeMap );
                        }
                    }
                    
                }
                    
                //System.out.println( " deCodeString " + deCodeString  + " -tempSt " + tempStr + "-" + currentOrgUnit.getName()  );
                        
                int tempRowNo = report_inDesign.getRowno();
                int tempColNo = report_inDesign.getColno();
                int sheetNo = report_inDesign.getSheetno();
                Sheet sheet0 = apachePOIWorkbook.getSheetAt( sheetNo );
                
                if ( sType.equalsIgnoreCase( "dataelement" ) )
                {
                    if( deCodeString.equalsIgnoreCase( "FACILITY" ) || deCodeString.equalsIgnoreCase( "PERIOD-QUARTER" )  )
                    {
                    }
                    else
                    {
                        tempRowNo += orgUnitCount;
                    }
                    try
                    {
                        Row row = (Row) sheet0.getRow( tempRowNo );
                        Cell cell = (Cell) row.getCell( tempColNo );
                        cell.setCellValue( Double.parseDouble( tempStr ) );

                    }
                    catch ( Exception e )
                    {
                        Row row = (Row) sheet0.getRow( tempRowNo );
                        Cell cell = (Cell) row.getCell( tempColNo );
                        cell.setCellValue( tempStr );
                    }
                    count1++;
                }
            }// inner while loop end
            orgUnitCount++;
        }// outer while loop end    
        
        try
        {
            fileName = reportFileNameTB.replace( ".xlsx", "" );
            fileName += "_" + selectedOrgUnit.getShortName() + "_";
            fileName += "_" + simpleDateFormat.format( selectedPeriod.getStartDate() ) + ".xlsx";
            tempFile.close(); // Close the InputStream

            FileOutputStream output_file = new FileOutputStream( new File( outputReportPath ) );

            // private String outputReportPath;
            apachePOIWorkbook.setForceFormulaRecalculation( true );
            apachePOIWorkbook.write( output_file ); // write changes

            output_file.close(); // close the stream
            System.out.println(  selectedOrgUnit.getName()  + " : " + selReportObj.getName()  + " : Report Generation End Time is : " + new Date() );

            File outputReportFile = new File( outputReportPath );
            inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );

            outputReportFile.deleteOnExit();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        
        return SUCCESS;
    }    
    
    // get map value
    private String getAggVal( String expression, Map<String, String> aggDeMap )
    {
        //System.out.println( " childOrgUnitsByComma : " + aggDeMap.size() );
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
                //System.out.println( " replaceString : " + replaceString );
                
                if( replaceString == null )
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
            
            resultValue = "" + (double) d;

            return resultValue;
        }
        catch ( NumberFormatException ex )
        {
            throw new RuntimeException( "Illegal DataElement id", ex );
        }
    }
    
    // get dataset Sources
    public List<OrganisationUnit> getDataSetSources( String dataSetIdsByComma )
    {
        List<OrganisationUnit> dataSetSources = new ArrayList<OrganisationUnit>();
        try
        {
            String query = "SELECT sourceid  from datasetsource WHERE  datasetid IN (" + dataSetIdsByComma + ") ";
              
            //System.out.println( "data-set-Query - " + query );
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            while ( rs.next() )
            {
                Integer sourceId = rs.getInt( 1 );
                
                if ( sourceId != null )
                {
                    OrganisationUnit dsSource = organisationUnitService.getOrganisationUnit( sourceId );
                    if( dsSource != null  )
                    {
                        dataSetSources.add( dsSource );
                    }
                }
            }
            return dataSetSources;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal DataSetIds and PeriodIds", e );
        }
    }
    
    // get OrgUnit Attribute Value Map
    public void getOrgUnitAttributeValueMap()
    {
        orgUnitAttributeValueMap = new HashMap<Integer, String>();
        try
        {
            String query = "SELECT org.organisationunitid, attvalue.value from attributevalue attvalue " +
                           "INNER JOIN organisationunitattributevalues  org ON org.attributevalueid = attvalue.attributevalueid " + 
                           "Where attvalue.attributeid = 1";
              
            //System.out.println( "data-set-Query - " + query );
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            while ( rs.next() )
            {
                Integer orgUnitId = rs.getInt( 1 );
                String orgUnitAttribueValue = rs.getString( 2 );
                
                //System.out.println( orgUnitId + " : " + orgUnitAttribueValue );
                
                if ( orgUnitId != null && orgUnitAttribueValue != null  )
                {
                    orgUnitAttributeValueMap.put( orgUnitId, orgUnitAttribueValue );
                }
            }
            //return orgUnitAttributeValueMap;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal OrgUnitIds ", e );
        }
    }
    
    // get OrgUnit Attribute Value Map
    public void getOrgUnitGroupNameMap()
    {
        orgUnitGroupNameMap = new HashMap<Integer, String>();
        try
        {
            String query = "SELECT orgGrpMem.organisationunitid,orgGrp.name from orgunitgroupsetmembers orgGrpsetMem " +
                           "INNER JOIN orgunitgroupmembers orgGrpMem ON orgGrpMem.orgunitgroupid = orgGrpsetMem.orgunitgroupid " + 
                           "INNER JOIN orgunitgroupset orgGrpSet ON orgGrpSet.orgunitgroupsetid = orgGrpsetMem.orgunitgroupsetid " +
                           "INNER JOIN orgunitgroup orgGrp ON orgGrp.orgunitgroupid = orgGrpMem.orgunitgroupid " + 
                           "WHERE orgGrpSet.name IN ( 'Kayakalp CHC/CH/DH', 'Kayakalp PHC Bedded', 'Kayakalp PHC Non Bedded' )";
              
            //System.out.println( "data-set-Query - " + query );
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            while ( rs.next() )
            {
                Integer orgUnitId = rs.getInt( 1 );
                String orgUnitGroupName = rs.getString( 2 );
                
                //System.out.println( orgUnitId + " : " + orgUnitGroupName );
                
                if ( orgUnitId != null && orgUnitGroupName != null  )
                {
                    orgUnitGroupNameMap.put( orgUnitId, orgUnitGroupName );
                }
            }
            //return orgUnitAttributeValueMap;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal OrgUnitGroupIds ", e );
        }
    }    
    
    
}