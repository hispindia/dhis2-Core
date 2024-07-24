package org.hisp.dhis.excelimport.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.dxf2.datavalue.DataValue;
import org.hisp.dhis.dxf2.datavalueset.DataValueSetService;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.dxf2.events.event.EventService;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.excelimport.api.XMLAttribute;
import org.hisp.dhis.excelimport.api.XMLRootAttribute;
import org.hisp.dhis.excelimport.util.ReportService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
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

public class ExcelImportEventDataValueAction implements Action
{
    //private static final Logger log = LoggerFactory.getLogger( ExcelImportDataValueSetAction.class );
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private ReportService reportService;

    public void setReportService( ReportService reportService )
    {
        this.reportService = reportService;
    }
    
    @Autowired
    private OrganisationUnitService organisationUnitService;
    
    @Autowired
    private CurrentUserService currentUserService;
    
    @Autowired
    private DataValueSetService dataValueSetService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private PeriodService periodService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private EventService eventService;
    
    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------
    
    private String message;

    public String getMessage()
    {
        return message;
    }
    
    private InputStream inputStream;

    public InputStream getInputStream()
    {
        return inputStream;
    }

    private String contentType;

    public String getContentType()
    {
        return contentType;
    }

    public void setUploadContentType( String contentType )
    {
        this.contentType = contentType;
    }

    private int bufferSize;

    public int getBufferSize()
    {
        return bufferSize;
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
    private String raFolderName;
    
    private String isoPeriod = "";
    
    private ImportSummary importSummary;
    
    public ImportSummary getImportSummary()
    {
        return importSummary;
    }
    
    private String attributeTypeCOCUid;
    
    public void setAttributeTypeCOCUid( String attributeTypeCOCUid )
    {
        this.attributeTypeCOCUid = attributeTypeCOCUid;
    }
    
    private String dataImportType;
    
    public void setDataImportType( String dataImportType )
    {
        this.dataImportType = dataImportType;
    }

    String attributeOptionCombo = "";
    String deCodesImportXMLFileName = "";
    Integer attributeCOCId;
    Integer periodId;
    private Map<String, Integer> categoryOptionComboIdUidMap = new HashMap<String, Integer>();
    private Map<String, Integer> dataElementIdUidMap = new HashMap<String, Integer>();
    
    List<String> tempDataValueList = new ArrayList<String>();
    
    private String importSummaryDescription;
    
    public String getImportSummaryDescription()
    {
        return importSummaryDescription;
    }
    
    private String totalCount;

    public String getTotalCount()
    {
        return totalCount;
    }

    private String insertCount;
    
    public String getInsertCount()
    {
        return insertCount;
    }

    private String updateCount;


    public String getUpdateCount()
    {
        return updateCount;
    }
    
    private String ouIDTB;
    
    public void setOuIDTB( String ouIDTB )
    {
        this.ouIDTB = ouIDTB;
    }
    
    /*
    private String availablePeriods;

    public void setAvailablePeriods( String availablePeriods )
    {
        this.availablePeriods = availablePeriods;
    }
    */
    
    private String reportingPeriod;
    public void setReportingPeriod( String reportingPeriod )
    {
        this.reportingPeriod = reportingPeriod;
    }

    private String organisationUnitName;
    private String defaultCategoryOptionComboUID;
    private OrganisationUnit selectedOrgUnit;
    
    private Map<Long, String> organisationUnitXMLFileMap = new HashMap<Long, String>();
    
    private String selectedReportingPeriod = "";
    private SimpleDateFormat simpleDateFormat;
    
    private Map<String, String> teiListByOrgUnitAndProgramMap = new HashMap<String, String>();
    
    private String importStatusMessage = "";
    
    private String reportingPeriodDe = "Cdxi6aNEkbf";
    private String reportingYearDe = "rpQi6D8L58H";
    
    private String year1,year2,year3;
    
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    
    public String execute()
        throws Exception
    {
        
        raFolderName = reportService.getRAFolderName();
        User user = currentUserService.getCurrentUser();
        
        message = "";
        importStatusMsgList = new ArrayList<String>();

        selectedOrgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );
        
        //organisationUnitName = selectedOrgUnit.getName();
        
        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        String eventDate = simpleDateFormat.format( new Date() );
        
        if( selectedOrgUnit != null )
        {
            
            organisationUnitName = selectedOrgUnit.getName();
        }
        else
        {
            message = "OrganisationUnit is not selected";
            //importStatusMsgList.add( message );
            return SUCCESS;
        }
        
        /*
        System.out.println( "availablePeriods " + availablePeriods  );
        
        if( availablePeriods != null )
        {
            
            isoPeriod = availablePeriods;
        }
        else
        {
            message = "Priod is not selected or ISO format";
            //importStatusMsgList.add( message );
            return SUCCESS;
        }
        */
        System.out.println( "selectedReportingPeriod " + reportingPeriod  );
        
        if( reportingPeriod != null )
        {
            
            selectedReportingPeriod = reportingPeriod;
            year1 = selectedReportingPeriod.split( "-" )[0].trim();
            year2 = ""+(Integer.parseInt( selectedReportingPeriod.split( "-" )[0].trim()) + 1);
            year3 = selectedReportingPeriod.split( "-" )[1].trim();
        }
        else
        {
            message = "Reporting Period not selected";
            //importStatusMsgList.add( message );
            return SUCCESS;
        }
        
        //periodId = (int) periodService.reloadIsoPeriod( isoPeriod ).getId();
        
        System.out.println( "File name : " + fileName +  " import Start " + new Date() );
        
        String fileType = fileName.substring( fileName.indexOf( '.' ) + 1, fileName.length() );
                
        System.out.println( "File type : " + fileType );
        
        
        if ( !fileType.equalsIgnoreCase( "xlsx" ) )
        {
            message = "The file you are trying to import is not an excel file";
            //importStatusMsgList.add( message );

            return SUCCESS;
        }
        
        //deCodesImportXMLFileName = "dataValueSetMappingPHCToolKit.xml";
        
        //organisationUnitXMLFileMap = new HashMap<Long, String>( getXMLFileNameList() );
        
        //deCodesImportXMLFileName = organisationUnitXMLFileMap.get( selectedOrgUnit.getId() );
        
        //deCodesImportXMLFileName = getXMLFileName( importAssementType, selectedOrgUnit.getId() );
        
        
        
        //System.out.println( " xmlMapping File Name : " + deCodesImportXMLFileName );
        
        /*
        if( xMLFileStrategy.equalsIgnoreCase( "" ) )
        {
            message = "The xml mapping file not found";
            //importStatusMsgList.add( message );
            return SUCCESS;
        }
        */
        
        System.out.println( "File name : " + fileName + " import Start " + new Date() );
        /*
        if( attributeTypeCOCUid != null && !attributeTypeCOCUid.equalsIgnoreCase( "NA" ) )
        {
            attributeOptionCombo = attributeTypeCOCUid;
        }
        else
        {
            CategoryOptionCombo defaultAttributeOptionCombo = categoryService.getDefaultCategoryOptionCombo();
            attributeOptionCombo = defaultAttributeOptionCombo.getUid();
        }
        */
        
        //deCodesImportXMLFileName = "dataValueSetMapping.xml";
        
        //categoryOptionComboIdUidMap = new HashMap<String, Integer>( getCategoryOptionComboIdUidList() );
        //dataElementIdUidMap = new HashMap<String, Integer>( getDataElementIdUidList());
        teiListByOrgUnitAndProgramMap = new HashMap<String, String>( getTEIListByOrgUnitAndProgram());
       
        //attributeCOCId = categoryOptionComboIdUidMap.get( attributeOptionCombo );
        //tempDataValueList = new ArrayList<String>();
        
        defaultCategoryOptionComboUID = "HllvX50cXC0";
         
        //DataValueSet dataValueSet = new DataValueSet();
        
        //Callable<DataValueSetReader> createReader = null; 
        
        //DataValueSetReader reader = createReader.call();
        
        //DataValueSet dataValueSet = reader.readHeader();
        
        List<DataValue> dataValues = new ArrayList<>();
        
        //FileInputStream inputFS = new FileInputStream(file);
        FileInputStream inputFS = new FileInputStream(file);
        //XSSFWorkbook workBook = new XSSFWorkbook ( fis );
        
        /*
        Sheet sheet = null;
        try 
        {

            Workbook workbook = WorkbookFactory.create(fis);
            sheet = workbook.getSheetAt(0);
            for ( int i = 0; i < workbook.getNumberOfSheets(); i++ )
            {
                System.out.println( " Import for Sheet name: " + workbook.getSheetName( i ) );
            }
        }
        catch (Exception e) 
        {
            
        }
        */
        
        FileInputStream inputFSS = new FileInputStream(file);
        //Workbook workbook = new XSSFWorkbook(inputFSS);
        
        /*
        XSSFWorkbook workbookFactory = (XSSFWorkbook) WorkbookFactory.create( inputFSS );
        
        FileInputStream inputStream = new FileInputStream(file);
        Workbook baeuldungWorkBook = new XSSFWorkbook(inputStream);
        for (Sheet sheet : baeuldungWorkBook) {
            sheet.getSheetName();
            System.out.println( " Import for wb Sheet name create : " + sheet.getSheetName() );
        }
        
        
        Sheet sheet = workbookFactory.getSheetAt(0);

        Map<Integer, List<String>> data = new HashMap<>();
        int i = 0;
        for (Row row : sheet) {
            data.put(i, new ArrayList<String>());
            for (Cell cell : row) {
                switch (cell.getCellType()) {
                    case STRING: break;
                    case NUMERIC: break;
                    case BOOLEAN: break;
                    case FORMULA: break;
                    default: data.get(new Integer(i)).add(" ");
                }
            }
            i++;
        }
        
        */
        
        
        
        XSSFWorkbook workBook = new XSSFWorkbook(inputFS);
        //Workbook workBook = new XSSFWorkbook(inputFS);
        
        //Workbook workBook = new XSSFWorkbook(file);
        
        
        
        //XSSFSheet wbSheet = wb.getSheetAt(0);
        
        //Workbook workBook = new XSSFWorkbook(inputFS); 
        
        FormulaEvaluator evaluator = workBook.getCreationHelper().createFormulaEvaluator();
        Map<String, FormulaEvaluator> workBooks = new HashMap<String, FormulaEvaluator>();
        
        //evaluator.evaluateAll();
        
        //workBook.getForceFormulaRecalculation();
        //System.out.println( " XSSF Sheet getPhysicalNumberOfRows --  : " + wbSheet.getPhysicalNumberOfRows() );
        
        /*
        for ( int j = 0; j < workBook.getNumberOfSheets(); j++ )
        {
            System.out.println( " Import for wb Sheet name create : " + workBook.getSheetName( j ) );
        }
        DataFormatter dataFormatter = new DataFormatter();
        Sheet sheet = workBook.getSheetAt(0);
        for (Row row : sheet) 
        {
            String dataCellValue = "";
            for (Cell cell : row) 
            {
                dataCellValue = dataFormatter.formatCellValue( cell );
                System.out.println( " dataCellValue : " + dataCellValue );
            }
        }
        */
        
        /*
        Workbook workBookFactory = WorkbookFactory.create( inputFS );
        
        for ( int i = 0; i < workBookFactory.getNumberOfSheets(); i++ )
        {
            System.out.println( " Import for Sheet name create : " + workBookFactory.getSheetName( i ) );
        }
        
        
        Sheet sheet = null;
        try 
        {
            POIFSFileSystem poifs = new POIFSFileSystem(inputFS);
            Workbook workbook = new HSSFWorkbook(poifs);
            sheet = workbook.getSheetAt(0);
            System.out.println( " Import for Sheet 1 : " + sheet );
            for ( int i = 0; i < workbook.getNumberOfSheets(); i++ )
            {
                System.out.println( " Import for Sheet name 1 : " + workbook.getSheetName( i ) );
            }
        }
        catch (Exception e) 
        {
        }

        if (sheet == null) {

            try 
            {
                Workbook workbook = new XSSFWorkbook(inputFS);
                sheet = workbook.getSheetAt(0);
                System.out.println( " Import for Sheet 2 : " + sheet );
                for ( int i = 0; i < workbook.getNumberOfSheets(); i++ )
                {
                    System.out.println( " Import for Sheet name 2 : " + workbook.getSheetName( i ) );
                }
            }
            catch (Exception e) 
            {
                
            }
        }
        */
        
        //System.out.println( " Import for Sheet 3 : " + wb.getSheetAt( 0 ) );
        

        
        // Return first sheet from the XLSX workbook
        
        //XSSFSheet sheet = workBook.getSheetAt(0);
        
        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();
        
        String xMLFileStrategy = "eventDataValueImportMapping_1.2 Strategy.xml";
        String xMLFileProjectDescription = "eventDataValueImportMapping_2.1 Project description.xml";
        
        String xMLFileProjectBudget = "eventDataValueImportMapping_2.2 Project budget.xml";
        
        List<XMLAttribute> xmlMappingListStrategy = new ArrayList<>();
        
        xmlMappingListStrategy.clear();

        if ( xmlMappingListStrategy.isEmpty() )
        {
            xmlMappingListStrategy = new ArrayList<>( getXMLAttribute( xMLFileStrategy ) );
        }
        
        List<XMLAttribute> xmlMappingListProjectDescription = new ArrayList<>();
        
        xmlMappingListProjectDescription.clear();

        if ( xmlMappingListProjectDescription.isEmpty() )
        {
            xmlMappingListProjectDescription = new ArrayList<>( getXMLAttribute( xMLFileProjectDescription ) );
        }
        
        List<XMLAttribute> xmlMappingListProjectBudget = new ArrayList<>();
        
        xmlMappingListProjectBudget.clear();
        if ( xmlMappingListProjectBudget.isEmpty() )
        {
            xmlMappingListProjectBudget = new ArrayList<>( getXMLAttributeYear( xMLFileProjectBudget ) );
        }
        
        if( xmlMappingListProjectBudget != null && xmlMappingListProjectBudget.size()> 0  )
        {
            Iterator<XMLAttribute> xmlMappingIterator = xmlMappingListProjectBudget.iterator();
            
            Set<org.hisp.dhis.dxf2.events.event.DataValue> eventDataValuesYear1 = new HashSet<>();
            Set<org.hisp.dhis.dxf2.events.event.DataValue> eventDataValuesYear2 = new HashSet<>();
            Set<org.hisp.dhis.dxf2.events.event.DataValue> eventDataValuesYear3 = new HashSet<>();
            String program = "";
            String programStage = "";
            Event addNewEventYear1 = new Event();
            Event addNewEventYear2 = new Event();
            Event addNewEventYear3 = new Event();
            while ( xmlMappingIterator.hasNext() )
            {
                XMLAttribute xmlAttribute = (XMLAttribute) xmlMappingIterator.next();
                
                program = xmlAttribute.getProgram();
                programStage = xmlAttribute.getProgramStage();
                
                String year = xmlAttribute.getYear();

                if ( year.equalsIgnoreCase( "Year1" ))
                {

                    String dataCellValue1 = "";
                    int tempRowNo = xmlAttribute.getRowno();
                    int tempColNo = xmlAttribute.getColno();
                    int sheetNo = xmlAttribute.getSheetno();
                    String dataElement = xmlAttribute.getDataElement();
                    
                    Sheet tempSheet = workBook.getSheetAt( sheetNo );
                    Row dataValueRow = tempSheet.getRow( tempRowNo );
                    Cell dataValueCell = dataValueRow.getCell( tempColNo);
                    
                    if( dataValueCell.getCellType() == CellType.FORMULA )
                    {
                        dataCellValue1 = getTemplateCellValue( dataValueCell, evaluator );
                        //System.out.println( " dataCellValue formula --  " + dataCellValue1 + " dataElement --  " + dataElement  + " tempRowNo --  " + tempRowNo + " tempColNo --  " + tempColNo );
                    }
                    
                    else
                    {
                        dataCellValue1 = dataFormatter.formatCellValue( dataValueCell );
                        //System.out.println( " dataCellValue no formula --  " + dataCellValue1 + " dataElement --  " + dataElement  + " tempRowNo --  " + tempRowNo + " tempColNo --  " + tempColNo );
                    }
                    
                    //System.out.println( " dataCellValue 11 --  " + dataCellValue1 + " dataElement --  " + dataElement  + " tempRowNo --  " + tempRowNo + " tempColNo --  " + tempColNo );
                    //dataCellValue = dataFormatter.formatCellValue( dataValueCell );
                    if( dataCellValue1 != null && !dataCellValue1.equalsIgnoreCase( "" ) )
                    {
                        // Finding string length
                        //int n = dataCellValue.length();
                 
                        // First character of a string
                        
                        //char firstChar = dataCellValue.charAt(0);
                 
                        // Last character of a string
                        //char lastChar = dataCellValue.charAt(n - 1);
                        /*
                        if( Character.compare(firstChar, '$') == 0 )
                        {
                            dataCellValue = dataCellValue.substring(1, dataCellValue.length());  
                        }
                        */
                        
                        dataCellValue1 = dataCellValue1.replaceAll("[,$]","");
                        
                        org.hisp.dhis.dxf2.events.event.DataValue eventDataValue = new org.hisp.dhis.dxf2.events.event.DataValue();
                        
                        eventDataValue.setDataElement( dataElement );
                        eventDataValue.setValue( dataCellValue1 );
                        eventDataValuesYear1.add( eventDataValue );
                        
                        //System.out.println( " dataCellValue 12 --  " + dataCellValue1 + " dataElement --  " + dataElement  + " tempRowNo --  " + tempRowNo + " tempColNo --  " + tempColNo );
                    }
                    
                    //System.out.println( " program 1 --  " + program + " dataElement --  " + dataElement  + " tempRowNo --  " + tempRowNo + " tempColNo --  " + tempColNo  + " programStage --  " + programStage + " sheetNo --  " + sheetNo);
                }
                
                if ( year.equalsIgnoreCase( "Year2" ))
                {
                    String dataCellValue2 = "";
                    int tempRowNo = xmlAttribute.getRowno();
                    int tempColNo = xmlAttribute.getColno();
                    int sheetNo = xmlAttribute.getSheetno();
                    String dataElement = xmlAttribute.getDataElement();
                    
                    Sheet tempSheet = workBook.getSheetAt( sheetNo );
                    Row dataValueRow = tempSheet.getRow( tempRowNo );
                    Cell dataValueCell = dataValueRow.getCell( tempColNo);
                    
                    if( dataValueCell.getCellType() == CellType.FORMULA )
                    {
                        dataCellValue2 = getTemplateCellValue( dataValueCell, evaluator );
                    }
                    
                    else
                    {
                        dataCellValue2 = dataFormatter.formatCellValue( dataValueCell );
                    }
                    //dataCellValue = dataFormatter.formatCellValue( dataValueCell );
                    
                    if( dataCellValue2 != null && !dataCellValue2.equalsIgnoreCase( "" ) )
                    {
                        // Finding string length
                        //int n = dataCellValue.length();
                 
                        // First character of a string
                        
                        //char firstChar = dataCellValue.charAt(0);
                 
                        // Last character of a string
                        //char lastChar = dataCellValue.charAt(n - 1);
                        /*
                        if( Character.compare(firstChar, '$') == 0 )
                        {
                            dataCellValue = dataCellValue.substring(1, dataCellValue.length());  
                        }
                        */
                        
                        dataCellValue2 = dataCellValue2.replaceAll("[,$]","");
                        
                        org.hisp.dhis.dxf2.events.event.DataValue eventDataValue = new org.hisp.dhis.dxf2.events.event.DataValue();
                        
                        eventDataValue.setDataElement( dataElement );
                        eventDataValue.setValue( dataCellValue2 );
                        eventDataValuesYear2.add( eventDataValue );
                        //System.out.println( " dataCellValue 2 --  " + dataCellValue2 + " dataElement --  " + dataElement  + " tempRowNo --  " + tempRowNo + " tempColNo --  " + tempColNo );
                    }
                    
                    //System.out.println( " program 2 --  " + program + " dataElement --  " + dataElement  + " tempRowNo --  " + tempRowNo + " tempColNo --  " + tempColNo  + " programStage --  " + programStage + " sheetNo --  " + sheetNo);
                }
                
                if ( year.equalsIgnoreCase( "Year3" ))
                {
                    String dataCellValue3 = "";
                    int tempRowNo = xmlAttribute.getRowno();
                    int tempColNo = xmlAttribute.getColno();
                    int sheetNo = xmlAttribute.getSheetno();
                    String dataElement = xmlAttribute.getDataElement();
                    
                    Sheet tempSheet = workBook.getSheetAt( sheetNo );
                    Row dataValueRow = tempSheet.getRow( tempRowNo );
                    Cell dataValueCell = dataValueRow.getCell( tempColNo);
                    if( dataValueCell.getCellType() == CellType.FORMULA )
                    {
                        dataCellValue3 = getTemplateCellValue( dataValueCell, evaluator );
                    }
                    
                    else
                    {
                        dataCellValue3 = dataFormatter.formatCellValue( dataValueCell );
                    }
                    //dataCellValue = dataFormatter.formatCellValue( dataValueCell );
                    
                    if( dataCellValue3 != null && !dataCellValue3.equalsIgnoreCase( "" ) )
                    {
                        // Finding string length
                        //int n = dataCellValue.length();
                 
                        // First character of a string
                        
                        //char firstChar = dataCellValue.charAt(0);
                 
                        // Last character of a string
                        //char lastChar = dataCellValue.charAt(n - 1);
                        /*
                        if( Character.compare(firstChar, '$') == 0 )
                        {
                            dataCellValue = dataCellValue.substring(1, dataCellValue.length());  
                        }
                        */
                        
                        dataCellValue3 = dataCellValue3.replaceAll("[,$]","");
                        org.hisp.dhis.dxf2.events.event.DataValue eventDataValue = new org.hisp.dhis.dxf2.events.event.DataValue();
                        
                        eventDataValue.setDataElement( dataElement );
                        eventDataValue.setValue( dataCellValue3 );
                        eventDataValuesYear3.add( eventDataValue );
                        //System.out.println( " dataCellValue 3 --  " + dataCellValue3 + " dataElement --  " + dataElement  + " tempRowNo --  " + tempRowNo + " tempColNo --  " + tempColNo );
                    }

                    //System.out.println( " program 3 --  " + program + " dataElement --  " + dataElement  + " tempRowNo --  " + tempRowNo + " tempColNo --  " + tempColNo  + " programStage --  " + programStage + " sheetNo --  " + sheetNo);
                }
            }
            
            // reporting period
            
            org.hisp.dhis.dxf2.events.event.DataValue eventDataValueReportingPeriod = new org.hisp.dhis.dxf2.events.event.DataValue();
            
            eventDataValueReportingPeriod.setDataElement( reportingPeriodDe );
            eventDataValueReportingPeriod.setValue( reportingPeriod );
            
            // for Year1
            
            org.hisp.dhis.dxf2.events.event.DataValue evDvYear1 = new org.hisp.dhis.dxf2.events.event.DataValue();
            evDvYear1.setDataElement( reportingYearDe );
            evDvYear1.setValue( year1 );
            
            eventDataValuesYear1.add( evDvYear1 );
            eventDataValuesYear1.add( eventDataValueReportingPeriod );
            
            addNewEventYear1.setDataValues( eventDataValuesYear1 );
            addNewEventYear1.setProgram( program );
            addNewEventYear1.setOrgUnit( selectedOrgUnit.getUid() );
            addNewEventYear1.setProgramStage( programStage );
            addNewEventYear1.setTrackedEntityInstance( teiListByOrgUnitAndProgramMap.get( program +":" + selectedOrgUnit.getUid() ) );
            addNewEventYear1.setEventDate( eventDate );

            
            importSummary = eventService.addEvent( addNewEventYear1, null, false );
            //System.out.println(  " importSummary -- " + importSummary.toString());
            
            if( importSummary.getStatus().toString().equalsIgnoreCase( "SUCCESS" ))
            {
                //importSummary.getImportCount().getImported()
                importStatusMessage = "Events created successfully for sheet 2.2 Project budget Event count:" + importSummary.getImportCount().getImported()+ ". imported event :" + importSummary.getReference();
                importStatusMsgList.add( importStatusMessage );
            }
            
            else
            {
                importStatusMessage = "Failed to create events for sheet 2.2 Project budget Error Details : " + importSummary.toString();
                importStatusMsgList.add( importStatusMessage );
            }
            
            // for year2
            org.hisp.dhis.dxf2.events.event.DataValue evDvYear2 = new org.hisp.dhis.dxf2.events.event.DataValue();
            evDvYear2.setDataElement( reportingYearDe );
            evDvYear2.setValue( year2 );
            
            eventDataValuesYear2.add( evDvYear2 );
            eventDataValuesYear2.add( eventDataValueReportingPeriod );
            
            addNewEventYear2.setDataValues( eventDataValuesYear2 );
            addNewEventYear2.setProgram( program );
            addNewEventYear2.setOrgUnit( selectedOrgUnit.getUid() );
            addNewEventYear2.setProgramStage( programStage );
            addNewEventYear2.setTrackedEntityInstance( teiListByOrgUnitAndProgramMap.get( program +":" + selectedOrgUnit.getUid() ) );
            addNewEventYear2.setEventDate( eventDate );

            importSummary = eventService.addEvent( addNewEventYear2, null, false );
            //System.out.println(  " importSummary -- " + importSummary.toString());
            
            if( importSummary.getStatus().toString().equalsIgnoreCase( "SUCCESS" ))
            {
                //importSummary.getImportCount().getImported()
                importStatusMessage = "Events created successfully for sheet 2.2 Project budget Event count:" + importSummary.getImportCount().getImported()+ ". imported event :" + importSummary.getReference();
                importStatusMsgList.add( importStatusMessage );
            }
            
            else
            {
                importStatusMessage = "Failed to create events for sheet 2.2 Project budget Error Details : " + importSummary.toString();
                importStatusMsgList.add( importStatusMessage );
            }
            
            // for year3
            org.hisp.dhis.dxf2.events.event.DataValue evDvYear3 = new org.hisp.dhis.dxf2.events.event.DataValue();
            evDvYear3.setDataElement( reportingYearDe );
            evDvYear3.setValue( year3 );
            
            eventDataValuesYear3.add( evDvYear3 );
            eventDataValuesYear3.add( eventDataValueReportingPeriod );
            
            addNewEventYear3.setDataValues( eventDataValuesYear3 );
            addNewEventYear3.setProgram( program );
            addNewEventYear3.setOrgUnit( selectedOrgUnit.getUid() );
            addNewEventYear3.setProgramStage( programStage );
            addNewEventYear3.setTrackedEntityInstance( teiListByOrgUnitAndProgramMap.get( program +":" + selectedOrgUnit.getUid() ) );
            addNewEventYear3.setEventDate( eventDate );

            importSummary = eventService.addEvent( addNewEventYear3, null, false );
            //System.out.println(  " importSummary -- " + importSummary.toString());
            
            if( importSummary.getStatus().toString().equalsIgnoreCase( "SUCCESS" ))
            {
                //importSummary.getImportCount().getImported()
                importStatusMessage = "Events created successfully for sheet 2.2 Project budget count:" + importSummary.getImportCount().getImported()+ ". imported event :" + importSummary.getReference();
                importStatusMsgList.add( importStatusMessage );
            }
            
            else
            {
                importStatusMessage = "Failed to create events for sheet 2.2 Project budget Error Details : " + importSummary.toString();
                importStatusMsgList.add( importStatusMessage );
            }
            
        }
        
      
        
        //System.out.println( " xmlMapping File Name : " + deCodesImportXMLFileName + " xmlMappingList size : " + xmlMappingListStrategy.size() );
                  

        // for Strategy
        if( xmlMappingListStrategy != null && xmlMappingListStrategy.size()> 0  )
        {
            Iterator<XMLAttribute> xmlMappingIterator = xmlMappingListStrategy.iterator();
            
            Set<org.hisp.dhis.dxf2.events.event.DataValue> eventDataValues = new HashSet<>();
            String program = "";
            String programStage = "";
            Event addNewEvent = new Event();
            while ( xmlMappingIterator.hasNext() )
            {
                XMLAttribute xmlAttribute = (XMLAttribute) xmlMappingIterator.next();
                String dataCellValue = "";
                int tempRowNo = xmlAttribute.getRowno();
                int tempColNo = xmlAttribute.getColno();
                int sheetNo = xmlAttribute.getSheetno();
                String dataElement = xmlAttribute.getDataElement();
                program = xmlAttribute.getProgram();
                programStage = xmlAttribute.getProgramStage();
                //String coc = xmlAttribute.getCategoryOptionCombo();
                //String orgUnit = xmlAttribute.getOrgUnit();
                
                //Sheet tempSheet = workBook.getSheetAt( sheetNo );
                Sheet tempSheet = workBook.getSheetAt( sheetNo );
                
                //System.out.println( " Import for Sheet : " + tempSheet );
                
                
                //System.out.println( coc + " --  " + dataElement  + " --  " + orgUnit + " --  " + isoPeriod + " --  " + tempRowNo + " --  " + tempColNo);
                
                Row dataValueRow = tempSheet.getRow( tempRowNo );
                
                Cell dataValueCell = dataValueRow.getCell( tempColNo);
                
                dataCellValue = dataFormatter.formatCellValue( dataValueCell );
                
                //System.out.println( program + " --  " + dataElement  + " --  " + selectedOrgUnit.getUid() + " --  " + dataCellValue  + " --  " + programStage );
                
                //System.out.println(" --  " + dataElement  + " --  " + dataCellValue );
                
                
                if( dataCellValue != null && !dataCellValue.equalsIgnoreCase( "" ) )
                {
                    org.hisp.dhis.dxf2.events.event.DataValue eventDataValue = new org.hisp.dhis.dxf2.events.event.DataValue();
                    
                    eventDataValue.setDataElement( dataElement );
                    eventDataValue.setValue( dataCellValue );
                    eventDataValues.add( eventDataValue );
                }
            }
            
            // reporting period
            
            org.hisp.dhis.dxf2.events.event.DataValue eventDataValueReportingPeriod = new org.hisp.dhis.dxf2.events.event.DataValue();
            
            eventDataValueReportingPeriod.setDataElement( "Cdxi6aNEkbf" );
            eventDataValueReportingPeriod.setValue( reportingPeriod );
            eventDataValues.add( eventDataValueReportingPeriod );
            
            //dataValueSet.setAttributeCategoryOptions( attributeCategoryOptions );
            addNewEvent.setDataValues( eventDataValues );
            
            addNewEvent.setProgram( program );
            addNewEvent.setOrgUnit( selectedOrgUnit.getUid() );
            addNewEvent.setProgramStage( programStage );
            addNewEvent.setTrackedEntityInstance( teiListByOrgUnitAndProgramMap.get( program +":" + selectedOrgUnit.getUid() ) );
            addNewEvent.setEventDate( eventDate );
            
            //System.out.println( " dataValue Size--  " + addNewEvent.getDataValues().size() );
            /*
            System.out.println( "program --  " + program );
            System.out.println( "selectedOrgUnit --  " + selectedOrgUnit.getUid() );
            System.out.println( "tei --  " + teiListByOrgUnitAndProgramMap.get( program +":" + selectedOrgUnit.getUid() ) );
            */
            
            importSummary = eventService.addEvent( addNewEvent, null, false );
            //System.out.println(  " importSummary -- " + importSummary.toString());
            
            
            if( importSummary.getStatus().toString().equalsIgnoreCase( "SUCCESS" ))
            {
                //importSummary.getImportCount().getImported()
                importStatusMessage = "Events created successfully for sheet 1.2 Strategy Event count:" + importSummary.getImportCount().getImported()+ ". imported event :" + importSummary.getReference();
                importStatusMsgList.add( importStatusMessage );
            }
            
            else
            {
                importStatusMessage = "Failed to create events for sheet 1.2 Strategy Error Details : " + importSummary.toString();
                importStatusMsgList.add( importStatusMessage );
            }
        }

        // for ProjectDescription
        if( xmlMappingListProjectDescription != null && xmlMappingListProjectDescription.size()> 0  )
        {
            Iterator<XMLAttribute> xmlMappingIterator = xmlMappingListProjectDescription.iterator();
            
            Set<org.hisp.dhis.dxf2.events.event.DataValue> eventDataValues = new HashSet<>();
            String program = "";
            String programStage = "";
            Event addNewEvent = new Event();
            while ( xmlMappingIterator.hasNext() )
            {
                XMLAttribute xmlAttribute = (XMLAttribute) xmlMappingIterator.next();
                String dataCellValue = "";
                int tempRowNo = xmlAttribute.getRowno();
                int tempColNo = xmlAttribute.getColno();
                int sheetNo = xmlAttribute.getSheetno();
                String dataElement = xmlAttribute.getDataElement();
                program = xmlAttribute.getProgram();
                programStage = xmlAttribute.getProgramStage();
                //String coc = xmlAttribute.getCategoryOptionCombo();
                //String orgUnit = xmlAttribute.getOrgUnit();
                
                //Sheet tempSheet = workBook.getSheetAt( sheetNo );
                Sheet tempSheet = workBook.getSheetAt( sheetNo );
                
                //System.out.println( " Import for Sheet : " + tempSheet );
                
                
                //System.out.println( coc + " --  " + dataElement  + " --  " + orgUnit + " --  " + isoPeriod + " --  " + tempRowNo + " --  " + tempColNo);
                
                Row dataValueRow = tempSheet.getRow( tempRowNo );
                
                Cell dataValueCell = dataValueRow.getCell( tempColNo);
                
                dataCellValue = dataFormatter.formatCellValue( dataValueCell );
                
                //System.out.println( program + " --  " + dataElement  + " --  " + selectedOrgUnit.getUid() + " --  " + dataCellValue  + " --  " + programStage );
                
                //System.out.println(" --  " + dataElement  + " --  " + dataCellValue );
                
                
                if( dataCellValue != null && !dataCellValue.equalsIgnoreCase( "" ) )
                {
                    org.hisp.dhis.dxf2.events.event.DataValue eventDataValue = new org.hisp.dhis.dxf2.events.event.DataValue();
                    
                    eventDataValue.setDataElement( dataElement );
                    eventDataValue.setValue( dataCellValue );
                    eventDataValues.add( eventDataValue );
                }
            }
            
            // reporting period
            
            org.hisp.dhis.dxf2.events.event.DataValue eventDataValueReportingPeriod = new org.hisp.dhis.dxf2.events.event.DataValue();
            
            eventDataValueReportingPeriod.setDataElement( "Cdxi6aNEkbf" );
            eventDataValueReportingPeriod.setValue( reportingPeriod );
            eventDataValues.add( eventDataValueReportingPeriod );
            
            //dataValueSet.setAttributeCategoryOptions( attributeCategoryOptions );
            addNewEvent.setDataValues( eventDataValues );
            
            addNewEvent.setProgram( program );
            addNewEvent.setOrgUnit( selectedOrgUnit.getUid() );
            addNewEvent.setProgramStage( programStage );
            addNewEvent.setTrackedEntityInstance( teiListByOrgUnitAndProgramMap.get( program +":" + selectedOrgUnit.getUid() ) );
            addNewEvent.setEventDate( eventDate );
 
            importSummary = eventService.addEvent( addNewEvent, null, false );
            //System.out.println(  " importSummary -- " + importSummary.toString());
            
            
            if( importSummary.getStatus().toString().equalsIgnoreCase( "SUCCESS" ))
            {
                //importSummary.getImportCount().getImported()
                importStatusMessage = "Events created successfully for sheet 2.1 Project description Event count:" + importSummary.getImportCount().getImported()+ ". imported event :" + importSummary.getReference();
                importStatusMsgList.add( importStatusMessage );
            }
            
            else
            {
                importStatusMessage = "Failed to create events for sheet 2.1 Project description Error Details : " + importSummary.toString();
                importStatusMsgList.add( importStatusMessage );
            }
        }
        
        
        //System.out.println( "Event DataValue ImportSummary " + importSummary );
        System.out.println( "File name : " + fileName + " import End at " + new Date() );
        
        return SUCCESS;
    }

    // Supportive methods
    public void importDataValueUsingQuery( List<String> tempDataValueList )
    {
        System.out.println( "inside  importDataValueUsingQuery " + tempDataValueList.size() );
        
        String importStatusDataValueUsingQuery = "";
        String storedBy = currentUserService.getCurrentUsername();
        int tempUpdateCount = 0;
        int tempInsertCount = 0;
        int count = 0;
        int totrec = 0;
        if( tempDataValueList != null && tempDataValueList.size() > 0 )
        {
            Date date = new Date();
            java.sql.Timestamp lastUpdatedDate = new Timestamp(date.getTime());
            java.sql.Timestamp createdDate = new Timestamp(date.getTime());
            //Integer periodId;
            int insertFlag = 1;
            String insertQuery = "INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, created, lastupdated, deleted, followup ) VALUES ";
            
            try
            {
                for( String tempDataValue : tempDataValueList  )
                {
                    
                    Integer deId = Integer.parseInt( tempDataValue.split( ":" )[0]);
                    Integer cocId = Integer.parseInt( tempDataValue.split( ":" )[1]);
                    Integer orgUnitId = Integer.parseInt( tempDataValue.split( ":" )[2]);
                    String dataValue = tempDataValue.split( ":" )[3];
                    
                    
                    if( deId != null && periodId != null && orgUnitId != null  && cocId != null && attributeCOCId != null )
                    {
                        String selectQuery = "SELECT value FROM datavalue WHERE dataelementid = " + deId
                            + " AND  periodid = " + periodId + " AND sourceid = " + orgUnitId
                            + " AND categoryoptioncomboid = " + cocId + " AND attributeoptioncomboid = " + attributeCOCId + " ";
                        //System.out.println( "selectQuery : " + selectQuery  );
                        SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( selectQuery );

                        if ( sqlResultSet != null && sqlResultSet.next() )
                        {
                            String updateQuery = "UPDATE datavalue SET value = '" + dataValue + "', storedby = '" + storedBy + "',lastupdated='" + lastUpdatedDate + "' WHERE dataelementid = " + deId + " AND periodid = "
                                + periodId + " AND sourceid = " + orgUnitId + " AND categoryoptioncomboid = " + cocId + " AND attributeoptioncomboid = " + attributeCOCId;
                            //System.out.println( "updateQuery : " + updateQuery  );
                            jdbcTemplate.update( updateQuery );
                            
                            tempUpdateCount++;
                        }
                        else
                        {
                            insertQuery += "( " + deId + ", " + periodId + ", " + orgUnitId + ", " + cocId +  ", " + attributeCOCId + ", '" +dataValue + "', '" + storedBy + "', '" + createdDate + "', '" + lastUpdatedDate + "', false, false ), ";
                            insertFlag = 2;
                            tempInsertCount++;
                        }
                        
                        if ( count == 1000 )
                        {
                            count = 1;

                            if ( insertFlag != 1 )
                            {
                                insertQuery = insertQuery.substring( 0, insertQuery.length() - 2 );
                                jdbcTemplate.update( insertQuery );
                            }

                            insertFlag = 1;

                            insertQuery = "INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, created, lastupdated, deleted, followup ) VALUES ";
                        }

                        count++;
                    }
                    
                }
                
                if ( insertFlag != 1 )
                {
                    insertQuery = insertQuery.substring( 0, insertQuery.length() - 2 );
                    
                    jdbcTemplate.update( insertQuery );
                }
                
                
                totrec = tempInsertCount + tempUpdateCount;
                /*
                importStatusDataValueUsingQuery = "Successfully populated aggregated data : "; 
                importStatusDataValueUsingQuery += "<br/> Total new records : " + insertCount;
                importStatusDataValueUsingQuery += "<br/> Total updated records : " + updateCount;
                importStatusDataValueUsingQuery += "<br/> Total records : " + totrec;
                
                //totrec = insertCount + updateCount;
                message = "The report has been imported successfully";
                importStatusDataValueUsingQuery = "Total records imported : " + totrec;
                importStatusDataValueUsingQuery = "New records added : " + insertCount;
                importStatusDataValueUsingQuery = "Records updated : " + updateCount;
                
                totrec = tempInsertCount + tempUpdateCount;
                importStatusDataValueUsingQuery = "Successfully populated aggregated data : "; 
                importStatusDataValueUsingQuery += "<br/> Total new records : " + insertCount;
                importStatusDataValueUsingQuery += "<br/> Total updated records : " + updateCount;
                importStatusDataValueUsingQuery += "<br/> Total records : " + totrec;
                */
                
                //totrec = insertCount + updateCount;
                message = "Successfully imported aggregated data : "; 
                totalCount = "Total records imported : " + totrec;
                insertCount = "New records imported : " + tempInsertCount;
                updateCount = "Records updated : " + tempUpdateCount;
                
                System.out.println( importStatusDataValueUsingQuery ); 
                
                
            }
            catch ( Exception e )
            {
                importStatusDataValueUsingQuery = "Exception occured while import, please check log for more details" + e.getMessage();
                //importStatusMsgList.add( importStatusDataValueUsingQuery );
            }
        }
        System.out.println( "importDataValueUsingQuery --  " + importStatusMsgList );
    }
    
    /*
    private static void evaluateAllFormulaCells(Workbook wb, FormulaEvaluator evaluator) 
    {
        for(int i=0; i<wb.getNumberOfSheets(); i++) 
        {
        Sheet sheet = wb.getSheetAt(i);

            for(Row r : sheet) 
            {
                for (Cell c : r) 
                {
                    if (c.getCellType() == Cell.CELL_TYPE_FORMULA) 
                    {
                        evaluator.evaluateFormulaCell(c);
                    }
                }
            }
        }        
   }
   */ 
    
    // method for get formula related cell value
    public String getTemplateCellValue( Cell cell, FormulaEvaluator formulaEvaluator )
    {
        String templateCellValue = "";
        
        /*
        if ( cell.getCellTypeEnum() == CellType.FORMULA) 
        {
            switch (cell.getCellType())
            {
                case Cell.CELL_TYPE_STRING:
                    System.out.println(cell.getBooleanCellValue());
                    templateCellValue = ""+ cell.getBooleanCellValue();
                    break;
                case NUMERIC:
                    System.out.println(cell.getNumericCellValue());
                    templateCellValue = ""+ (int)cell.getNumericCellValue();
                    break;
                case STRING:
                    System.out.println(cell.getStringCellValue());
                    templateCellValue = ""+ cell.getStringCellValue();
                    break;
            }
        }
        */
        //dataValueCell.getCellType() == CellType.FORMULA 
            
        
        
        if( formulaEvaluator.evaluateInCell(cell).getCellType() == cell.getCellType())
        {
            templateCellValue = ""+cell.getRichStringCellValue();
            
            if (cell.getCellType() == CellType.NUMERIC) 
            {
                double doubleValue = cell.getNumericCellValue();
                templateCellValue = ""+ (int)doubleValue;
                //break;
            }
            else if( cell.getCellType() == CellType.STRING )
            {
                //templateCellValue = ""+ cell.getStringCellValue();
                templateCellValue = ""+cell.getRichStringCellValue();
                //System.out.println( " inside formula String --  " + templateCellValue + " cell.getCellType() -- " + cell.getCellType() + " -- " + cell.getRichStringCellValue());
                //break;
            }
            
            else if( cell.getCellType() == CellType.BOOLEAN )
            {
                templateCellValue = ""+ cell.getStringCellValue();
                //break;
            }
            
            
            //System.out.println( " inside formula String 1 --  " + formulaEvaluator.evaluateInCell(cell).getCellType() + " cell.getCellType() -- " + cell.getCellType() + " -- " + cell.getRichStringCellValue());
        }
        
        
        if (formulaEvaluator.evaluateInCell(cell).getCellType() == CellType.FORMULA ) 
        {
            
            if (cell.getCellType() == CellType.NUMERIC) 
            {
                double doubleValue = cell.getNumericCellValue();
                templateCellValue = ""+ (int)doubleValue;
                //break;
            }
            else if( cell.getCellType() == CellType.STRING )
            {
                templateCellValue = ""+ cell.getStringCellValue();
                
                //System.out.println( " inside formula String --  " + templateCellValue + " cell.getCellType() -- " + cell.getCellType() + " -- " + cell.getRichStringCellValue());
                //break;
            }
            
            else if( cell.getCellType() == CellType.BOOLEAN )
            {
                templateCellValue = ""+ cell.getStringCellValue();
                //break;
            }
            
            System.out.println( " inside formula templateCellValue --  " + templateCellValue + " cell.getCellType() -- " + cell.getCellType() + " -- " + cell.getRichStringCellValue());
            
            /*
            switch (cell.getCachedFormulaResultType()) 
            {
                case Cell.CELL_TYPE_STRING:
                    //System.out.println( " STRING -- " + cell.getBooleanCellValue());
                    templateCellValue = ""+ cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    //System.out.println(  " NUMBER -- " + cell.getNumericCellValue());
                    double doubleValue = cell.getNumericCellValue();
                    templateCellValue = ""+ (int)doubleValue ;
                    //templateCellValue = String.format("%.2f", cell.getNumericCellValue() );
                    //System.out.println( doubleValue + " int value -- " + templateCellValue );
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    //System.out.println(  " BOOLEAN -- " + cell.getStringCellValue());
                    templateCellValue = ""+ cell.getStringCellValue();
                    break;
            }
            
            */
            
        }
        
        
        return templateCellValue;
    }
    
    // read xml file
    
    public Map<String, List<XMLAttribute>> getXMLAttributeValueMap( String fileName )
    {

        Map<String, List<XMLAttribute>> xMLAttributeValueMap = new HashMap<String, List<XMLAttribute>>();
        
        String id = "";
        String sheetno = "";
        String rowno = "";
        String colno = "";
        String program = "";
        String programStage = "";
        String dataElement = "";
        
        String excelImportFolderName = "excelimport";

        //List<XMLAttribute> xmlAttributes = new ArrayList<>();
        
        List<XMLRootAttribute> xmlRootAttributes = new ArrayList<>();
        

        String path = System.getProperty( "user.home" ) + File.separator + "dhis" + File.separator + raFolderName
            + File.separator + excelImportFolderName + File.separator + fileName;
        try
        {
            String newpath = System.getenv( "DHIS2_HOME" );
            if ( newpath != null )
            {
                path = newpath + File.separator + raFolderName + File.separator + excelImportFolderName
                    + File.separator + fileName;
            }
        }

        catch ( NullPointerException npe )
        {
            // do nothing, but we might be using this somewhere without
            // USER_HOME set, which will throw a NPE
        }

        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse( new File( path ) );
            if ( doc == null )
            {
                System.out.println( "XML File Not Found at user home" );
                return null;
            }

            NodeList nodeList = doc.getElementsByTagName( "Year" );
            int totalReports = nodeList.getLength();
            for ( int r = 0; r < totalReports; r++ )
            {
                XMLRootAttribute xmlRootAttribute = new XMLRootAttribute();
                
                
                Node node = nodeList.item( r );
                if ( node.getNodeType() == Node.ELEMENT_NODE )
                {
                    Element element = (Element) node;

                    // get id attribute
                    id = element.getAttribute("id");
                    
                    System.out.println( " inside method  root Element --  " + id );
                    
                    //xmlRootAttribute.setRootElement( id );
                    
                    NodeList listOfDECodes = doc.getElementsByTagName( "de-code" );
                    int totalDEcodes = listOfDECodes.getLength();
                    List<XMLAttribute> xmlAttributes = new ArrayList<>();
                    for ( int s = 0; s < totalDEcodes; s++ )
                    {
                        //xmlAttributes = new ArrayList<>();
                        Element deCodeElement = (Element) listOfDECodes.item( s );
                        /*
                        NodeList textDECodeList = deCodeElement.getChildNodes();
                        String expression = ((Node) textDECodeList.item( 0 )).getNodeValue().trim();
                        */
                        
                        XMLAttribute xmlAttribute = new XMLAttribute();
                        //xmlAttribute.setExpression( expression );
                        xmlAttribute.setSheetno( new Integer( deCodeElement.getAttribute( "sheetno" ) ) );
                        
                        xmlAttribute.setRowno( new Integer( deCodeElement.getAttribute( "rowno" ) ) );
                        xmlAttribute.setColno( new Integer( deCodeElement.getAttribute( "colno" ) ) );
                        
                        xmlAttribute.setProgram( deCodeElement.getAttribute( "program" ) );
                        xmlAttribute.setProgramStage( deCodeElement.getAttribute( "programStage" ) );
                        
                        xmlAttribute.setDataElement( deCodeElement.getAttribute( "dataElement" ) );
                        //xmlAttribute.setCategoryOptionCombo( deCodeElement.getAttribute( "categoryOptionCombo" ) );
                        //xmlAttribute.setOrgUnit( deCodeElement.getAttribute( "orgUnit" ) );
                        
                        System.out.println( " inside method --  dataElement --  " + deCodeElement.getAttribute( "dataElement" ) );
                        xmlAttributes.add( xmlAttribute );
                        
                        xMLAttributeValueMap.put( id, xmlAttributes );
                        //xmlRootAttribute.setXmlAttribute( xmlAttributes );
                        
                    }// end of for loop with s var
                    
                    //xmlRootAttribute.setXmlAttribute( xmlAttributes );
                    
                    //xmlRootAttributes.add( xmlRootAttribute );
                    
                    
                    // get text
                    /*
                    sheetno = element.getElementsByTagName("sheetno").item(0).getTextContent();
                    rowno = element.getElementsByTagName("rowno").item(0).getTextContent();
                    colno = element.getElementsByTagName("colno").item(0).getTextContent();
                    program = element.getElementsByTagName("program").item(0).getTextContent();
                    programStage = element.getElementsByTagName("programStage").item(0).getTextContent();
                    dataElement = element.getElementsByTagName("dataElement").item(0).getTextContent();
                    
                    
                    xmlAttribute.setId( id );
                    xmlAttribute.setSheetno(new Integer( sheetno ));
                    
                    xmlAttribute.setRowno( new Integer( rowno ) );
                    xmlAttribute.setColno( new Integer( colno ) );
                    
                    xmlAttribute.setProgram( program );
                    xmlAttribute.setProgramStage( programStage );
                    
                    xmlAttribute.setDataElement( dataElement );

                    
                    <salary currency="INR">200000</salary>
                    NodeList salaryNodeList = element.getElementsByTagName("salary");
                    String salary = salaryNodeList.item(0).getTextContent();

                    // get salary's attribute
                    String currency = salaryNodeList.item(0).getAttributes().getNamedItem("currency").getTextContent();
                    
                    
                    System.out.println("Current Element :" + node.getNodeName());
                    System.out.println("Year Id : " + id);
                    System.out.println("sheetno : " + sheetno);
                    System.out.println("rowno : " + rowno);
                    System.out.println("colno : " + colno);
                    System.out.println("program : " + program);
                    System.out.println("programStage : " + programStage);
                    System.out.println("dataElement : " + dataElement);
                    */
                    
                }
                
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
        return xMLAttributeValueMap;
    }// getDECodes end     
    
    
    public List<XMLAttribute> getXMLAttributeYear( String fileName )
    {

        String excelImportFolderName = "excelimport";

        List<XMLAttribute> xmlAttributes = new ArrayList<>();


        String path = System.getProperty( "user.home" ) + File.separator + "dhis" + File.separator + raFolderName
            + File.separator + excelImportFolderName + File.separator + fileName;
        try
        {
            String newpath = System.getenv( "DHIS2_HOME" );
            if ( newpath != null )
            {
                path = newpath + File.separator + raFolderName + File.separator + excelImportFolderName
                    + File.separator + fileName;
            }
        }

        catch ( NullPointerException npe )
        {
            // do nothing, but we might be using this somewhere without
            // USER_HOME set, which will throw a NPE
        }

        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse( new File( path ) );
            if ( doc == null )
            {
                // System.out.println( "There is no DECodes related XML file in
                // the user home" );
                return null;
            }

            NodeList listOfDECodes = doc.getElementsByTagName( "de-code" );
            int totalDEcodes = listOfDECodes.getLength();

            for ( int s = 0; s < totalDEcodes; s++ )
            {
                Element deCodeElement = (Element) listOfDECodes.item( s );
                /*
                NodeList textDECodeList = deCodeElement.getChildNodes();
                String expression = ((Node) textDECodeList.item( 0 )).getNodeValue().trim();
                */
                
                XMLAttribute xmlAttribute = new XMLAttribute();
                //xmlAttribute.setExpression( expression );
                xmlAttribute.setYear( deCodeElement.getAttribute( "year" ) );
                xmlAttribute.setSheetno( new Integer( deCodeElement.getAttribute( "sheetno" ) ) );
                
                xmlAttribute.setRowno( new Integer( deCodeElement.getAttribute( "rowno" ) ) );
                xmlAttribute.setColno( new Integer( deCodeElement.getAttribute( "colno" ) ) );
                
                xmlAttribute.setProgram( deCodeElement.getAttribute( "program" ) );
                xmlAttribute.setProgramStage( deCodeElement.getAttribute( "programStage" ) );
                
                xmlAttribute.setDataElement( deCodeElement.getAttribute( "dataElement" ) );
                //xmlAttribute.setCategoryOptionCombo( deCodeElement.getAttribute( "categoryOptionCombo" ) );
                //xmlAttribute.setOrgUnit( deCodeElement.getAttribute( "orgUnit" ) );
                
                xmlAttributes.add( xmlAttribute );
                
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
        return xmlAttributes;
    }// getDECodes end       
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public List<XMLAttribute> getXMLAttribute( String fileName )
    {

        String excelImportFolderName = "excelimport";

        List<XMLAttribute> xmlAttributes = new ArrayList<>();


        String path = System.getProperty( "user.home" ) + File.separator + "dhis" + File.separator + raFolderName
            + File.separator + excelImportFolderName + File.separator + fileName;
        try
        {
            String newpath = System.getenv( "DHIS2_HOME" );
            if ( newpath != null )
            {
                path = newpath + File.separator + raFolderName + File.separator + excelImportFolderName
                    + File.separator + fileName;
            }
        }

        catch ( NullPointerException npe )
        {
            // do nothing, but we might be using this somewhere without
            // USER_HOME set, which will throw a NPE
        }

        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse( new File( path ) );
            if ( doc == null )
            {
                // System.out.println( "There is no DECodes related XML file in
                // the user home" );
                return null;
            }

            NodeList listOfDECodes = doc.getElementsByTagName( "de-code" );
            int totalDEcodes = listOfDECodes.getLength();

            for ( int s = 0; s < totalDEcodes; s++ )
            {
                Element deCodeElement = (Element) listOfDECodes.item( s );
                /*
                NodeList textDECodeList = deCodeElement.getChildNodes();
                String expression = ((Node) textDECodeList.item( 0 )).getNodeValue().trim();
                */
                
                XMLAttribute xmlAttribute = new XMLAttribute();
                //xmlAttribute.setExpression( expression );
                xmlAttribute.setSheetno( new Integer( deCodeElement.getAttribute( "sheetno" ) ) );
                
                xmlAttribute.setRowno( new Integer( deCodeElement.getAttribute( "rowno" ) ) );
                xmlAttribute.setColno( new Integer( deCodeElement.getAttribute( "colno" ) ) );
                
                xmlAttribute.setProgram( deCodeElement.getAttribute( "program" ) );
                xmlAttribute.setProgramStage( deCodeElement.getAttribute( "programStage" ) );
                
                xmlAttribute.setDataElement( deCodeElement.getAttribute( "dataElement" ) );
                //xmlAttribute.setCategoryOptionCombo( deCodeElement.getAttribute( "categoryOptionCombo" ) );
                //xmlAttribute.setOrgUnit( deCodeElement.getAttribute( "orgUnit" ) );
                
                xmlAttributes.add( xmlAttribute );
                
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
        return xmlAttributes;
    }// getDECodes end   
    
    
    // Supportive methods
    public Map<String, Integer> getCategoryOptionComboIdUidList()
    {
        Map<String, Integer> categoryOptionComboIdUidMap = new HashMap<String, Integer>();

        String query = "";

        try
        {
           
            query = "SELECT categoryoptioncomboid, uid from categoryoptioncombo;";
            
            SqlRowSet rs1 = jdbcTemplate.queryForRowSet( query );

            while ( rs1.next() )
            {
                Integer cocID = rs1.getInt( 1 );
                String cocUID = rs1.getString( 2 );
                if( cocID != null && cocUID != null )
                {
                    categoryOptionComboIdUidMap.put( cocUID, cocID );
                }
            }
        }
        catch ( Exception e )
        {
            System.out.println( "SQL Exception : " + e.getMessage() );
            return null;
        }
 
        return categoryOptionComboIdUidMap;
    }
    
    public Map<String, Integer> getDataElementIdUidList()
    {
        Map<String, Integer> dataElementIdUidMap = new HashMap<String, Integer>();

        String query = "";

        try
        {
           
            query = "SELECT dataelementid, uid from dataelement;";
            
            SqlRowSet rs1 = jdbcTemplate.queryForRowSet( query );

            while ( rs1.next() )
            {
                Integer deID = rs1.getInt( 1 );
                String deUID = rs1.getString( 2 );
                if( deID != null && deUID != null )
                {
                    dataElementIdUidMap.put( deUID, deID );
                }
            }
        }
        catch ( Exception e )
        {
            System.out.println( "SQL Exception : " + e.getMessage() );
            return null;
        }
 
        return dataElementIdUidMap;
    }
    
    public Map<String, Integer> getOrganisationUnitIdUidList()
    {
        Map<String, Integer> organisationUnitIdUidMap = new HashMap<String, Integer>();

        String query = "";

        try
        {
           
            query = "SELECT organisationunitid, uid from organisationunit;";
            
            SqlRowSet rs1 = jdbcTemplate.queryForRowSet( query );

            while ( rs1.next() )
            {
                Integer orgID = rs1.getInt( 1 );
                String orgUID = rs1.getString( 2 );
                if( orgID != null && orgID != null )
                {
                    organisationUnitIdUidMap.put( orgUID, orgID );
                }
            }
        }
        catch ( Exception e )
        {
            System.out.println( "SQL Exception : " + e.getMessage() );
            return null;
        }
 
        return organisationUnitIdUidMap;
    }
    
    
    public Map<Long, String> getXMLFileNameList()
    {
        Map<Long, String> organisationUnitXMLFileMap = new HashMap<Long, String>();

        String query = "";

        try
        {
           
            query = "SELECT orgGrpMember.organisationunitid, orgunitgrp.name, "
                    + "cast(orgUnitGrpAttribute.value::json ->> 'value' AS VARCHAR) from orgunitgroup orgunitgrp "
                    + "JOIN json_each_text(orgunitgrp.attributevalues::json) orgUnitGrpAttribute ON TRUE "
                    + "INNER JOIN attribute attr ON attr.uid = orgUnitGrpAttribute.key "
                    + "INNER JOIN orgunitgroupmembers orgGrpMember ON orggrpmember.orgunitgroupid = orgunitgrp.orgunitgroupid "
                    + "WHERE attr.code = 'XMLName'; ";
                        
            
            SqlRowSet rs1 = jdbcTemplate.queryForRowSet( query );

            while ( rs1.next() )
            {
                Long orgUnitID = rs1.getLong( 1 );
                String xmlFileName = rs1.getString( 3 );
                if( orgUnitID != null && xmlFileName != null )
                {
                    organisationUnitXMLFileMap.put( orgUnitID, xmlFileName );
                }
            }
        }
        catch ( Exception e )
        {
            System.out.println( "SQL Exception : " + e.getMessage() );
            return null;
        }
 
        return organisationUnitXMLFileMap;
    }    
    
    public String getXMLFileName( String assementType, Long orgUnitID )
    {
        String xmlFileName = "";

        String query = "";

        try
        {
           
            query = "SELECT orgGrpMember.organisationunitid, orgunitgrp.name, "
                    + "cast(orgUnitGrpAttribute.value::json ->> 'value' AS VARCHAR) from orgunitgroup orgunitgrp "
                    + "JOIN json_each_text(orgunitgrp.attributevalues::json) orgUnitGrpAttribute ON TRUE "
                    + "INNER JOIN attribute attr ON attr.uid = orgUnitGrpAttribute.key "
                    + "INNER JOIN orgunitgroupmembers orgGrpMember ON orggrpmember.orgunitgroupid = orgunitgrp.orgunitgroupid "
                    + "WHERE attr.code = '" + assementType + "' AND orgGrpMember.organisationunitid = " + orgUnitID +" ";
                        
            //System.out.println( "SQL query : " + query );
            SqlRowSet rs1 = jdbcTemplate.queryForRowSet( query );
            
            while ( rs1.next() )
            {
                Long tempOrgUnitID = rs1.getLong( 1 );
                String tempXMLFileName = rs1.getString( 3 );
                if( tempOrgUnitID != null && tempXMLFileName != null )
                {
                    xmlFileName = tempXMLFileName;
                }
            }
        }
        catch ( Exception e )
        {
            System.out.println( "SQL Exception : " + e.getMessage() );
            return null;
        }
 
        return xmlFileName;
    }    

    
    public Map<String, String> getTEIEnrollmentUidList()
    {
        Map<String, String> teiEnrollmentMap = new HashMap<String, String>();

        String query = "";

        try
        {
           
            query = "SELECT tei.uid tei_uid, org.uid org_uid, org.name org_name, prg.uid prg_uid, "
                    + " prg.name prg_name, pi.uid enrollment from programinstance pi "
                    + " INNER JOIN trackedentityinstance tei on tei.trackedentityinstanceid =pi.trackedentityinstanceid "
                    + " INNER JOIN program prg ON prg.programid = pi.programid "
                    + " INNER JOIN organisationunit org ON org.organisationunitid = pi.organisationunitid; ";
            
            SqlRowSet rs1 = jdbcTemplate.queryForRowSet( query );

            while ( rs1.next() )
            {
                String tei_uid = rs1.getString( 1 );
                String org_uid = rs1.getString( 2 );
                String org_name = rs1.getString( 3 );
                String prg_uid = rs1.getString( 4 );
                String prg_name = rs1.getString( 5 );
                String enrollment = rs1.getString( 6 );
                if( tei_uid != null && org_uid != null && prg_uid != null )
                {
                    teiEnrollmentMap.put( tei_uid +":" + org_uid +":" + prg_uid, enrollment );
                }
            }
        }
        catch ( Exception e )
        {
            System.out.println( "SQL Exception : " + e.getMessage() );
            return null;
        }
 
        return teiEnrollmentMap;
    }
        
    
    public Map<String, String> getTEIListByOrgUnitAndProgram()
    {
        Map<String, String> teiListByOrgUnitAndProgramMap = new HashMap<String, String>();

        String query = "";

        try
        {
           
            query = "SELECT prg.uid prg_uid, org.uid org_uid,tei.uid tei_uid from programinstance pi "
                    + " INNER JOIN trackedentityinstance tei on tei.trackedentityinstanceid =pi.trackedentityinstanceid "
                    + " INNER JOIN program prg ON prg.programid = pi.programid "
                    + " INNER JOIN organisationunit org ON org.organisationunitid = pi.organisationunitid; ";
            
            SqlRowSet rs1 = jdbcTemplate.queryForRowSet( query );

            while ( rs1.next() )
            {
                String prg_uid = rs1.getString( 1 );
                String org_uid = rs1.getString( 2 );
                String tei_uid = rs1.getString( 3 );
                
                if( tei_uid != null && org_uid != null && prg_uid != null )
                {
                    teiListByOrgUnitAndProgramMap.put( prg_uid +":" + org_uid, tei_uid );
                }
            }
        }
        catch ( Exception e )
        {
            System.out.println( "SQL Exception : " + e.getMessage() );
            return null;
        }
 
        return teiListByOrgUnitAndProgramMap;
    }    
    
    
    
    
    
    
    
    
}
