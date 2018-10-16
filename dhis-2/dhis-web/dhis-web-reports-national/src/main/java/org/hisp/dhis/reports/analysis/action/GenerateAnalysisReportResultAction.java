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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hisp.dhis.config.Configuration_IN;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.reports.ReportService;
import org.hisp.dhis.reports.Report_in;
import org.hisp.dhis.reports.Report_inDesign;
import org.hisp.dhis.system.util.MathUtils;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class GenerateAnalysisReportResultAction implements Action
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

    private OrganisationUnitGroupService organisationUnitGroupService;

    public void setOrganisationUnitGroupService( OrganisationUnitGroupService organisationUnitGroupService )
    {
        this.organisationUnitGroupService = organisationUnitGroupService;
    }
    
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

    private String startDate;

    public void setStartDate( String startDate )
    {
        this.startDate = startDate;
    }

    private String endDate;

    public void setEndDate( String endDate )
    {
        this.endDate = endDate;
    }

    private String periodTypeId;

    public void setPeriodTypeId( String periodTypeId )
    {
        this.periodTypeId = periodTypeId;
    }

    private String aggData;
    
    public void setAggData( String aggData )
    {
        this.aggData = aggData;
    }

    private Integer orgUnitGroup;
    
    public void setOrgUnitGroup( Integer orgUnitGroup )
    {
        this.orgUnitGroup = orgUnitGroup;
    }
    
    private String reportModelTB;

    private List<OrganisationUnit> orgUnitList;

    private Date sDate;

    private Date eDate;

    private Date sDateTemp;

    private Date eDateTemp;

    private PeriodType periodType;

    private String raFolderName;

    private Integer monthCount;
    
    private int availablePeriods;

    public void setAvailablePeriods( int availablePeriods )
    {
        this.availablePeriods = availablePeriods;
    }

    private Period selectedPeriod;
    
    private OrganisationUnit selectedOrgUnit;
    
    private Map<String, String> orgUnitWiseAggDeMap = new HashMap<String, String>();
    
    private Map<String, String> orgUnitGroupWiseAggDeMap = new HashMap<String, String>();
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    @SuppressWarnings( "deprecation" )
    public String execute() throws Exception
    {
        // Initialization
        raFolderName = reportService.getRAFolderName();
        String deCodesXMLFileName = "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "MMM-yyyy" );
        SimpleDateFormat dayFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        SimpleDateFormat monthFormat = new SimpleDateFormat( "MMMM" );
        SimpleDateFormat simpleMonthFormat = new SimpleDateFormat( "MMM" );
        SimpleDateFormat yearFormat = new SimpleDateFormat( "yyyy" );
        SimpleDateFormat simpleYearFormat = new SimpleDateFormat( "yy" );

        Report_in selReportObj = reportService.getReport( Integer.parseInt( reportList ) );

        deCodesXMLFileName = selReportObj.getXmlTemplateName();
        reportModelTB = selReportObj.getModel();
        reportFileNameTB = selReportObj.getExcelTemplateName();
        
        System.out.println( "Analysis Report Generation Start Time is : \t" + new Date() );
        
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
        
        sDate = format.parseDate( startDate );
        eDate = format.parseDate( endDate );
        
        List<Period> periodList = new ArrayList<Period>( periodService.getIntersectingPeriods( sDate, eDate ) );
        Collection<Integer> periodIds = new ArrayList<Integer>( getIdentifiers(Period.class, periodList ) );
        String periodIdsByComma = getCommaDelimitedString( periodIds );
        
        Calendar tempStartDate = Calendar.getInstance();
        Calendar tempEndDate = Calendar.getInstance();
        tempStartDate.setTime( sDate );
        tempEndDate.setTime( eDate );
        
        // dataElements Related Info
        List<Report_inDesign> reportDesignList = reportService.getReportDesign( deCodesXMLFileName );
        String dataElmentIdsByComma = reportService.getDataelementIds( reportDesignList );
        
        
        // OrgUnit Related Info
        selectedOrgUnit = new OrganisationUnit();
        selectedOrgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );
        int selectedOrgUnitLevel = selectedOrgUnit.getLevel();

        List<OrganisationUnit> childOrgUnitTree = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitWithChildren( selectedOrgUnit.getId() ) );
        List<Integer> childOrgUnitTreeIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, childOrgUnitTree ) );
        String childOrgUnitsByComma = getCommaDelimitedString( childOrgUnitTreeIds );
        
        orgUnitWiseAggDeMap = new HashMap<String, String>();
        orgUnitGroupWiseAggDeMap = new HashMap<String, String>();
        
        orgUnitWiseAggDeMap.putAll( reportService.getAggDataFromDataValueTable( childOrgUnitsByComma, dataElmentIdsByComma, periodIdsByComma ) );
        
        if( orgUnitGroup != 0 )
        {
            orgUnitList = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitWithChildren( selectedOrgUnit.getId() ) );
            OrganisationUnitGroup ouGroup = organisationUnitGroupService.getOrganisationUnitGroup( orgUnitGroup );
        
            if( ouGroup != null )
            {
                orgUnitList.retainAll( ouGroup.getMembers() );
                
                List<Integer> orgUbnitGroupMembersIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, orgUnitList ) );
                String orgUnitGroupMembersIdsByComma = getCommaDelimitedString( orgUbnitGroupMembersIds );
                orgUnitGroupWiseAggDeMap.putAll( reportService.getAggDataFromDataValueTable( orgUnitGroupMembersIdsByComma, dataElmentIdsByComma, periodIdsByComma ) );
            }
           
            else
            {
                orgUnitGroupWiseAggDeMap.putAll( reportService.getAggDataFromDataValueTable( childOrgUnitsByComma, dataElmentIdsByComma, periodIdsByComma ) );
            }
        }
        else
        {
            orgUnitGroupWiseAggDeMap.putAll( reportService.getAggDataFromDataValueTable( childOrgUnitsByComma, dataElmentIdsByComma, periodIdsByComma ) );
        }

        
        FileInputStream tempFile = new FileInputStream( new File( inputTemplatePath ) );
        XSSFWorkbook apachePOIWorkbook = new XSSFWorkbook( tempFile );
        
        //XSSFFormulaEvaluator.evaluateAllFormulaCells( apachePOIWorkbook );
        
        System.out.println( selectedOrgUnit.getName()+ " : " + selReportObj.getName()+" - Generation Start Time is : " + new Date() + " -- " + apachePOIWorkbook.getNumberOfSheets() );
        
        //System.out.println( " dataElmentIdsByComma " + dataElmentIdsByComma +" - periodIdsByComma : " + periodIdsByComma + " -- " + childOrgUnitsByComma );
        
        for (int i = 0; i < apachePOIWorkbook.getNumberOfSheets(); i++) 
        {

            System.out.println("Sheet name: " + apachePOIWorkbook.getSheetName(i));
        }
        
        
        int orgUnitCount = 0;
        Iterator<Report_inDesign> reportDesignIterator = reportDesignList.iterator();
        while ( reportDesignIterator.hasNext() )
        {
            Report_inDesign report_inDesign = (Report_inDesign) reportDesignIterator.next();

            String deType = report_inDesign.getPtype();
            String sType = report_inDesign.getStype();
            String deCodeString = report_inDesign.getExpression();
            String tempStr = "";
            String tempaGrpStr = "";

            if ( deCodeString.equalsIgnoreCase( "FACILITY" ) )
            {
                tempStr = selectedOrgUnit.getName();
            }
           
            else if ( deCodeString.equalsIgnoreCase( "FACILITYP" ) )
            {
                if( selectedOrgUnit.getParent() != null )
                {
                    tempStr = selectedOrgUnit.getParent().getName();
                }
                else
                {
                    tempStr = "";
                }
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
            else if( deCodeString.equalsIgnoreCase( "PERIODSDED" ) )
            {
                tempStr = simpleDateFormat.format( sDate ) + " To " + simpleDateFormat.format( eDate );
            }
            
            else if( deCodeString.equalsIgnoreCase( "MONTHCOUNT" ) )
            {
                int endYear = tempEndDate.get( Calendar.YEAR );
                int startYear = tempStartDate.get( Calendar.YEAR );
                int endMonth = tempEndDate.get( Calendar.MONTH );
                int startMonth = tempStartDate.get( Calendar.MONTH );

                monthCount = ( (endYear - startYear) * 12) - startMonth + endMonth + 1;
                tempStr = monthCount.toString();
            }
            
            else if( deCodeString.equalsIgnoreCase( "FINANCIAL-YEAR" ) )
            {
                int endYear = tempEndDate.get( Calendar.YEAR );
                int startYear = tempStartDate.get( Calendar.YEAR );
                
                tempStr = startYear +"-" + endYear;
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
                        System.out.println( "inside GENERATEAGGDATA" );
                        
                        tempStr = getAggVal( deCodeString, orgUnitWiseAggDeMap );
                        //tempaGrpStr = getAggVal( deCodeString, orgUnitGroupWiseAggDeMap );
                        //System.out.println( aggData + " 1 SType : " + sType + " DECode : " + deCodeString + "   TempStr : " + tempStr + " -- " + tempaGrpStr );
                    }
                }
            }
            
            int tempRowNo = report_inDesign.getRowno();
            int tempColNo = report_inDesign.getColno();
            int sheetNo = report_inDesign.getSheetno();
            
            Sheet sheet0 = apachePOIWorkbook.getSheetAt( sheetNo );
            
            //System.out.println( aggData + " 2 SType : " + sType + " DECode : " + deCodeString + "   TempStr : " + tempStr + " " + tempRowNo + " " + tempColNo + " " + sheetNo );
            
            if ( tempStr == null || tempStr.equals( " " ) )
            {
                tempColNo += orgUnitCount;
            } 
            else
            {
                if ( reportModelTB.equalsIgnoreCase( "DYNAMIC-ORGUNIT" ) )
                {
                    if ( deCodeString.equalsIgnoreCase( "FACILITY" ) || deCodeString.equalsIgnoreCase( "FACILITYP" ) || deCodeString.equalsIgnoreCase( "FACILITYPP" ) )
                    {
                        
                    }
                    else if ( deCodeString.equalsIgnoreCase( "PERIOD" ) || deCodeString.equalsIgnoreCase( "PERIODSDED" ) )
                    {
                        
                    }
                    else
                    {
                        tempColNo += orgUnitCount;
                    }
                }
                else if ( reportModelTB.equalsIgnoreCase( "dynamicwithrootfacility" ) )
                {
                    if ( deCodeString.equalsIgnoreCase( "FACILITY" ) || deCodeString.equalsIgnoreCase( "FACILITYP" ) || deCodeString.equalsIgnoreCase( "FACILITYPP" ) )
                    {
                        
                    }
                    else if ( deCodeString.equalsIgnoreCase( "PERIOD" ) || deCodeString.equalsIgnoreCase( "PERIODSDED" ) )
                    {
                        
                    }
                    else
                    {
                        tempRowNo += orgUnitCount;
                    }
                }
                if ( sType.equalsIgnoreCase( "dataelement" )  )
                {
                    try
                    {
                        Row row = sheet0.getRow( tempRowNo );
                        Cell cell = row.getCell( tempColNo );
                        cell.setCellValue( Double.parseDouble( tempStr ) );
                        
                        /*
                        Cell cell_2 = row.getCell( tempColNo + 1 );
                        cell_2.setCellValue( Double.parseDouble( tempaGrpStr ) );
                        */
                        
                    }
                    catch ( Exception e )
                    {
                        Row row = sheet0.getRow( tempRowNo );
                        Cell cell = row.getCell( tempColNo );
                        cell.setCellValue( tempStr );
                        
                    }
                }
                /*
                else if ( sType.equalsIgnoreCase( "dataelement_popultion" )  )
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
                         
                else if ( sType.equalsIgnoreCase( "dataelement-string" ) )
                {
                    try
                    {
                        Row row = sheet0.getRow( tempRowNo );
                        Cell cell = row.getCell( tempColNo );
                        cell.setCellValue( tempStr );
                        
                    }
                    catch ( Exception e )
                    {
                        Row row = sheet0.getRow( tempRowNo );
                        Cell cell = row.getCell( tempColNo );
                        cell.setCellValue( tempStr );
                    }
                }
                */
            }
        }
        
        // code for apachePOI Workbook
        fileName = reportFileNameTB.replace( ".xlsx", "" );
        fileName += "_" + selectedOrgUnit.getName() + ".xlsx";
        //fileName += "_" + simpleDateFormat.format( selectedPeriod.getStartDate() ) + ".xlsx";
        
        tempFile.close(); //Close the InputStream
        
        FileOutputStream output_file = new FileOutputStream( new File(  outputReportPath ) );  //Open FileOutputStream to write updates
        
        XSSFFormulaEvaluator.evaluateAllFormulaCells( apachePOIWorkbook );
        apachePOIWorkbook.write( output_file ); //write changes
        
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
        output_file.close();  //close the stream   
        
        File outputReportFile = new File( outputReportPath );
        inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );
        
        outputReportFile.deleteOnExit();
        
        System.out.println( selectedOrgUnit.getName()+ " : " + selReportObj.getName()+" Report Generation End Time is : " + new Date() );
        
        
        return SUCCESS;
    }
    
    // supportive methods
    public String getAggVal( String expression, Map<String, String> aggDeMap )
    {
        System.out.println( " expression -- " + expression + " aggDeMap " + aggDeMap.size() );
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
            
            System.out.println( " expression -- " + expression +" -- resultValue " + resultValue);
            return resultValue;
        }
        catch ( NumberFormatException ex )
        {
            throw new RuntimeException( "Illegal DataElement id", ex );
        }
    }   
}

