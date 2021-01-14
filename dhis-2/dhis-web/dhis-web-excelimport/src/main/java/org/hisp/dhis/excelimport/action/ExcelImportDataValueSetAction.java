package org.hisp.dhis.excelimport.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.dxf2.datavalue.DataValue;
import org.hisp.dhis.dxf2.datavalueset.DataValueSet;
import org.hisp.dhis.dxf2.datavalueset.DataValueSetService;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.excelimport.api.XMLAttribute;
import org.hisp.dhis.excelimport.util.ReportService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */

public class ExcelImportDataValueSetAction implements Action
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
    private CurrentUserService currentUserService;
    
    @Autowired
    private DataValueSetService dataValueSetService;
    

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private DataElementService dataElementService;
    
    @Autowired
    private OrganisationUnitService organisationUnitService;
    
    @Autowired
    private DataValueService dataValueService;
    
    @Autowired
    private PeriodService periodService;
    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------
    
    private String importDate;
    
    public void setImportDate( String importDate )
    {
        this.importDate = importDate;
    }

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
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------



    public String execute()
        throws Exception
    {
        
        raFolderName = reportService.getRAFolderName();
        User user = currentUserService.getCurrentUser();
        String storedBy = currentUserService.getCurrentUsername();
        
        message = "";
        importStatusMsgList = new ArrayList<String>();

        
        System.out.println( "importDate " + importDate  );
        
        if( importDate != null && !importDate.equalsIgnoreCase( "" ))
        {
            isoPeriod = importDate.split( "-" )[0] + importDate.split( "-" )[1] + importDate.split( "-" )[2];
        }
        else
        {
            message = "Priod is not selected or ISO format";
            //importStatusMsgList.add( message );
            return SUCCESS;
        }
     
        System.out.println( "File name : " + fileName +  "  ISO period -- "+ isoPeriod + " import Start " + new Date() );
        
        String fileType = fileName.substring( fileName.indexOf( '.' ) + 1, fileName.length() );

        
        System.out.println( "File type : " + fileType );
        
        if ( !fileType.equalsIgnoreCase( "xlsx" ) )
        {
            message = "The file you are trying to import is not an excel file";
            //importStatusMsgList.add( message );

            return SUCCESS;
        }
        
        
        Period selectedPeriod = new Period();
        //selectedPeriod = periodService.getPeriod( isoPeriod );
        
        
        
        Integer periodId = null;
        if( isoPeriod != null )
        {
            selectedPeriod = PeriodType.getPeriodFromIsoString( isoPeriod );
            selectedPeriod = periodService.reloadPeriod( selectedPeriod );
            periodId = (int)selectedPeriod.getId();
        }
        
        
        //String excelImportFolderName = "excelimport";
        String deCodesImportXMLFileName = "dataValueSetMapping.xml";
        
        List<XMLAttribute> xmlMappingList = new ArrayList<>();
        
        xmlMappingList.clear();

        if ( xmlMappingList.isEmpty() )
        {
            xmlMappingList = new ArrayList<>( getXMLAttribute( deCodesImportXMLFileName ) );
        }
          
        DataValueSet dataValueSet = new DataValueSet();
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
        
        XSSFWorkbook workBook = new XSSFWorkbook(inputFS);   
        //XSSFSheet wbSheet = wb.getSheetAt(0);
        
        //Workbook workBook = new XSSFWorkbook(inputFS); 
        FormulaEvaluator evaluator = workBook.getCreationHelper().createFormulaEvaluator();
        Map<String, FormulaEvaluator> workBooks = new HashMap<String, FormulaEvaluator>();
        
        //evaluator.evaluateAll();
        
        workBook.getForceFormulaRecalculation();
        //System.out.println( " XSSF Sheet getPhysicalNumberOfRows --  : " + wbSheet.getPhysicalNumberOfRows() );
        
        
        for ( int i = 0; i < workBook.getNumberOfSheets(); i++ )
        {
            System.out.println( " Import for wb Sheet name create : " + workBook.getSheetName( i ) );
        }
        
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
        
        System.out.println( " xmlMappingList size : " + xmlMappingList.size() );
        
        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();
        Iterator<XMLAttribute> xmlMappingIterator = xmlMappingList.iterator();
        while ( xmlMappingIterator.hasNext() )
        {
            XMLAttribute xmlAttribute = (XMLAttribute) xmlMappingIterator.next();
            String dataCellValue = "";
            int tempRowNo = xmlAttribute.getRowno();
            int tempColNo = xmlAttribute.getColno();
            int sheetNo = xmlAttribute.getSheetno();
            String dataElementUid = xmlAttribute.getDataElement();
            String cocUid = xmlAttribute.getCategoryOptionCombo();
            String orgUnitUid = xmlAttribute.getOrgUnit();
            
            //Sheet tempSheet = workBook.getSheetAt( sheetNo );
            Sheet tempSheet = workBook.getSheetAt( sheetNo );
            
            //System.out.println( " Import for Sheet : " + tempSheet );
            
            
            //System.out.println( coc + " --  " + dataElement  + " --  " + orgUnit + " --  " + isoPeriod + " --  " + tempRowNo + " --  " + tempColNo);
            
            Row dataValueRow = tempSheet.getRow( tempRowNo );
            
            Cell dataValueCell = dataValueRow.getCell( tempColNo);
            
            dataCellValue = getTemplateCellValue( dataValueCell, evaluator );
            
            
            if( dataValueCell.getCellTypeEnum() == CellType.FORMULA )
            {
                //CellValue cellValue = evaluator.evaluate( dataValueCell );
                
                //evaluateAllFormulaCells(workBook, evaluator);
                //CellValue cellValue =  evaluator.evaluateAll();
                
                //dataCellValue = ""+(int)cellValue.getNumberValue();
                
                //dataCellValue = ""+(int)workBook.getSheetAt(sheetNo).getRow(tempRowNo).getCell(tempColNo).getNumericCellValue();
                
                dataCellValue = getTemplateCellValue( dataValueCell, evaluator );
            }
            
            else
            {
                dataCellValue = dataFormatter.formatCellValue( dataValueCell );
            }
            
            /*
            DataElement dataElement = dataElementService.getDataElement( dataElementUid );
            CategoryOptionCombo categoryOptionCombo = categoryService.getCategoryOptionCombo( cocUid );
            
            OrganisationUnit  organisationUnit = organisationUnitService.getOrganisationUnit( orgUnitUid );
            
            
            org.hisp.dhis.datavalue.DataValue newDataValue = new org.hisp.dhis.datavalue.DataValue();

            newDataValue.setDataElement( dataElement );

            newDataValue.setPeriod( selectedPeriod );
            newDataValue.setSource( organisationUnit );
            
            newDataValue.setCategoryOptionCombo( categoryOptionCombo );
            //.setOptionCombo( currentOptionCombo );
            
            //dataValue.setTimestamp( new Date() );
            newDataValue.setLastUpdated( new Date() );
            newDataValue.setCreated( new Date() );
            newDataValue.setStoredBy( storedBy );
            
            
            org.hisp.dhis.datavalue.DataValue oldDataValue = new org.hisp.dhis.datavalue.DataValue();

            //oldValue = dataValueService.getDataValue( currentOrgUnit, currentDataElement, selectedPeriod, currentOptionCombo );
            oldDataValue = dataValueService.getDataValue( dataElement, selectedPeriod, organisationUnit, categoryOptionCombo );
            
            if ( oldDataValue != null )
            {
                try
                {
                    oldDataValue.setValue( dataCellValue );
                    //oldValue.setTimestamp( new Date() );
                    oldDataValue.setLastUpdated(  new Date() );
                    oldDataValue.setCreated( new Date() );
                    oldDataValue.setStoredBy( storedBy );

                    dataValueService.updateDataValue( oldDataValue );
                }
                catch ( Exception e )
                {
                    //throw new RuntimeException( "Cannot add datavalue", ex );
                    message = "Exception occured while import, please check log for more details" + e.getMessage();
                    //updateStatusMsgList.add( message );
                }
            }
            else
            {
                try
                {
                    dataValueService.addDataValue( newDataValue );
                }
                catch ( Exception e )
                {
                    //throw new RuntimeException( "Cannot add datavalue", ex );
                    message = "Exception occured while import, please check log for more details" + e.getMessage();
                    //updateStatusMsgList.add( message );
                }
            }
            
            */
            
            /*
            dataValueCell.getCellType().
            
            if (dataValueCell.getCellTypeEnum() == CellType.FORMULA) 
            {
                dataValueCell.get
            }
            */
            
            //dataValueCell.getRichStringCellValue();
            
            //String dataCellValue = dataFormatter.formatCellValue( dataValueCell );
            
            if( dataCellValue != null && !dataCellValue.equalsIgnoreCase( "" ) )
            {
                DataValue dataValue = new DataValue();
                dataValue.setDataElement( dataElementUid );
                dataValue.setCategoryOptionCombo( cocUid );
                dataValue.setOrgUnit( orgUnitUid );
                dataValue.setValue( dataCellValue );
                dataValue.setPeriod( isoPeriod );
                
                dataValues.add( dataValue );
                
                //System.out.println( coc + " 1 --  " + dataElement  + " --  " + orgUnit + " --  " + dataCellValue  + " --  " + isoPeriod );
            }
        }
        
        
        dataValueSet.setDataValues( dataValues );
        
        System.out.println( " dataValueSet Size--  " + dataValueSet.getDataValues().size() );
        
        /*
        for( DataValue dataSetDataValue : dataValueSet.getDataValues() )
        {
            System.out.println( "dataElement --  " + dataSetDataValue.getDataElement() );
            System.out.println( "coc --  " + dataSetDataValue.getCategoryOptionCombo() );
            System.out.println( "orgUnit --  " + dataSetDataValue.getOrgUnit() );
            System.out.println( "period --  " + dataSetDataValue.getPeriod() );
            System.out.println( "dataValue --  " + dataSetDataValue.getValue() );
        }
        */
        
        
        //System.out.println( "dataValueSet --  " + dataValueSet );
        
        //importStatusMsgList.add( dataValueSet.toString() );
        
        //ImportOptions importOptions = ImportOptions.getDefaultImportOptions();
        
        //importSummary = dataValueSetService.saveDataValueSetExcelImport( dataValueSet );
        
        /*
        importSummary.getConflicts();
        
        for ( ImportConflict impCon : importSummary.getConflicts())
        {
            impCon.getObject();
            impCon.getValue();
        }
        
        importSummary.getImportCount().getIgnored();
        
        importSummary.getImportCount().getDeleted();
        
        importSummary.getStatus();
        
        importSummary.getDescription();
        */
        
        importStatusMsgList.add( importSummary.toString() );
        System.out.println( "DataValue Set ImportSummary " + importSummary );
        
        return SUCCESS;
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
        
        
        if (cell.getCellTypeEnum() == CellType.FORMULA) 
        {
            switch (cell.getCachedFormulaResultType()) 
            {
                case Cell.CELL_TYPE_STRING:
                    //System.out.println( " STRING -- " + cell.getBooleanCellValue());
                    templateCellValue = ""+ cell.getBooleanCellValue();
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
        }
        
        
        return templateCellValue;
    }
    
    
    
    
    
    
    
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
                xmlAttribute.setDataElement( deCodeElement.getAttribute( "dataElement" ) );
                xmlAttribute.setCategoryOptionCombo( deCodeElement.getAttribute( "categoryOptionCombo" ) );
                xmlAttribute.setOrgUnit( deCodeElement.getAttribute( "orgUnit" ) );
                
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

    /*
    private ImportSummary saveDataValueSet(  ImportOptions importOptions, DataValueSet dataValueSet )
    {
        importOptions = ObjectUtils.firstNonNull( importOptions, ImportOptions.getDefaultImportOptions() );
        
        //ImportOptions importOptions = ImportOptions.getDefaultImportOptions();

        Clock clock = new Clock( log ).startClock().logTime( "Starting data value import, options: " + importOptions );
        

        ImportSummary summary = new ImportSummary()
            .setImportOptions( importOptions );

        boolean isIso8601 = calendarService.getSystemCalendar().isIso8601();
        boolean skipLockExceptionCheck = !lockExceptionStore.anyExists();

        log.info( String.format( "Is ISO calendar: %b, skip lock exception check: %b", isIso8601, skipLockExceptionCheck ) );

        I18n i18n = i18nManager.getI18n();
        final User currentUser = currentUserService.getCurrentUser();
        final String currentUserName = currentUser.getUsername();

        boolean hasSkipAuditAuth = currentUser != null && currentUser.isAuthorized( Authorities.F_SKIP_DATA_IMPORT_AUDIT );
        boolean skipAudit = importOptions.isSkipAudit() && hasSkipAuditAuth;

        log.info( String.format( "Skip audit: %b, has authority to skip: %b", skipAudit, hasSkipAuditAuth ) );

        // ---------------------------------------------------------------------
        // Get import options
        // ---------------------------------------------------------------------

        log.info( "Import options: " + importOptions );

        IdScheme dvSetIdScheme = IdScheme.from( dataValueSet.getIdSchemeProperty() );
        IdScheme dvSetDataElementIdScheme = IdScheme.from( dataValueSet.getDataElementIdSchemeProperty() );
        IdScheme dvSetOrgUnitIdScheme = IdScheme.from( dataValueSet.getOrgUnitIdSchemeProperty() );
        IdScheme dvSetCategoryOptComboIdScheme = IdScheme.from( dataValueSet.getCategoryOptionComboIdSchemeProperty() );
        IdScheme dvSetDataSetIdScheme = IdScheme.from( dataValueSet.getDataSetIdSchemeProperty() );

        log.info( "Data value set identifier scheme: " + dvSetIdScheme + ", data element: " + dvSetDataElementIdScheme +
            ", org unit: " + dvSetOrgUnitIdScheme + ", category option combo: " + dvSetCategoryOptComboIdScheme + ", data set: " + dvSetDataSetIdScheme );

        IdScheme idScheme = dvSetIdScheme.isNotNull() ? dvSetIdScheme : importOptions.getIdSchemes().getIdScheme();
        IdScheme dataElementIdScheme = dvSetDataElementIdScheme.isNotNull() ? dvSetDataElementIdScheme : importOptions.getIdSchemes().getDataElementIdScheme();
        IdScheme orgUnitIdScheme = dvSetOrgUnitIdScheme.isNotNull() ? dvSetOrgUnitIdScheme : importOptions.getIdSchemes().getOrgUnitIdScheme();
        IdScheme categoryOptComboIdScheme = dvSetCategoryOptComboIdScheme.isNotNull() ? dvSetCategoryOptComboIdScheme : importOptions.getIdSchemes().getCategoryOptionComboIdScheme();
        IdScheme dataSetIdScheme = dvSetDataSetIdScheme.isNotNull() ? dvSetDataSetIdScheme : importOptions.getIdSchemes().getDataSetIdScheme();

        log.info( "Identifier scheme: " + idScheme + ", data element: " + dataElementIdScheme +
            ", org unit: " + orgUnitIdScheme + ", category option combo: " + categoryOptComboIdScheme + ", data set: " + dataSetIdScheme );

        ImportStrategy strategy = dataValueSet.getStrategy() != null ?
            ImportStrategy.valueOf( dataValueSet.getStrategy() ) : importOptions.getImportStrategy();

          
        boolean dryRun = dataValueSet.getDryRun() != null ? dataValueSet.getDryRun() : importOptions.isDryRun();
        boolean skipExistingCheck = importOptions.isSkipExistingCheck();
        boolean strictPeriods = importOptions.isStrictPeriods() || (Boolean) systemSettingManager.getSystemSetting( SettingKey.DATA_IMPORT_STRICT_PERIODS );
        boolean strictDataElements = importOptions.isStrictDataElements() || (Boolean) systemSettingManager.getSystemSetting( SettingKey.DATA_IMPORT_STRICT_DATA_ELEMENTS );
        boolean strictCategoryOptionCombos = importOptions.isStrictCategoryOptionCombos() || (Boolean) systemSettingManager.getSystemSetting( SettingKey.DATA_IMPORT_STRICT_CATEGORY_OPTION_COMBOS );
        boolean strictAttrOptionCombos = importOptions.isStrictAttributeOptionCombos() || (Boolean) systemSettingManager.getSystemSetting( SettingKey.DATA_IMPORT_STRICT_ATTRIBUTE_OPTION_COMBOS );
        boolean strictOrgUnits = importOptions.isStrictOrganisationUnits() || (Boolean) systemSettingManager.getSystemSetting( SettingKey.DATA_IMPORT_STRICT_ORGANISATION_UNITS );
        boolean requireCategoryOptionCombo = importOptions.isRequireCategoryOptionCombo() || (Boolean) systemSettingManager.getSystemSetting( SettingKey.DATA_IMPORT_REQUIRE_CATEGORY_OPTION_COMBO );
        boolean requireAttrOptionCombo = importOptions.isRequireAttributeOptionCombo() || (Boolean) systemSettingManager.getSystemSetting( SettingKey.DATA_IMPORT_REQUIRE_ATTRIBUTE_OPTION_COMBO );
        boolean forceDataInput = inputUtils.canForceDataInput( currentUser, importOptions.isForce() );
        
            
        // ---------------------------------------------------------------------
        // Create meta-data maps
        // ---------------------------------------------------------------------

        CachingMap<String, DataElement> dataElementMap = new CachingMap<>();
        CachingMap<String, OrganisationUnit> orgUnitMap = new CachingMap<>();
        CachingMap<String, CategoryOptionCombo> optionComboMap = new CachingMap<>();
        CachingMap<String, DataSet> dataElementDataSetMap = new CachingMap<>();
        CachingMap<String, Period> periodMap = new CachingMap<>();
        CachingMap<String, Set<PeriodType>> dataElementPeriodTypesMap = new CachingMap<>();
        CachingMap<String, Set<CategoryOptionCombo>> dataElementCategoryOptionComboMap = new CachingMap<>();
        CachingMap<String, Set<CategoryOptionCombo>> dataElementAttrOptionComboMap = new CachingMap<>();
        CachingMap<String, Boolean> dataElementOrgUnitMap = new CachingMap<>();
        CachingMap<String, Boolean> dataSetLockedMap = new CachingMap<>();
        CachingMap<String, Period> dataElementLatestFuturePeriodMap = new CachingMap<>();
        CachingMap<String, Boolean> orgUnitInHierarchyMap = new CachingMap<>();
        CachingMap<String, DateRange> attrOptionComboDateRangeMap = new CachingMap<>();
        CachingMap<String, Boolean> attrOptionComboOrgUnitMap = new CachingMap<>();
        CachingMap<String, Optional<Set<String>>> dataElementOptionsMap = new CachingMap<>();
        CachingMap<String, Boolean> approvalMap = new CachingMap<>();
        CachingMap<String, Boolean> lowestApprovalLevelMap = new CachingMap<>();
        CachingMap<String, Boolean> periodOpenForDataElement = new CachingMap<>();

        // ---------------------------------------------------------------------
        // Get meta-data maps
        // ---------------------------------------------------------------------

        IdentifiableObjectCallable<DataElement> dataElementCallable = new IdentifiableObjectCallable<>(
            identifiableObjectManager, DataElement.class, dataElementIdScheme, null );
        IdentifiableObjectCallable<OrganisationUnit> orgUnitCallable = new IdentifiableObjectCallable<>(
            identifiableObjectManager, OrganisationUnit.class, orgUnitIdScheme, trimToNull( dataValueSet.getOrgUnit() ) );
        IdentifiableObjectCallable<CategoryOptionCombo> categoryOptionComboCallable = new CategoryOptionComboAclCallable(
            categoryService, categoryOptComboIdScheme, null );
        IdentifiableObjectCallable<CategoryOptionCombo> attributeOptionComboCallable = new CategoryOptionComboAclCallable(
            categoryService, categoryOptComboIdScheme, null );
        IdentifiableObjectCallable<Period> periodCallable = new PeriodCallable( periodService, null, trimToNull( dataValueSet.getPeriod() ) );

        // ---------------------------------------------------------------------
        // Heat caches
        // ---------------------------------------------------------------------

        
        if ( importOptions.isPreheatCacheDefaultFalse() )
        {
            dataElementMap.load( identifiableObjectManager.getAll( DataElement.class ), o -> o.getPropertyValue( dataElementIdScheme ) );
            orgUnitMap.load( identifiableObjectManager.getAll( OrganisationUnit.class ), o -> o.getPropertyValue( orgUnitIdScheme ) );
            optionComboMap.load( identifiableObjectManager.getAll( CategoryOptionCombo.class ), o -> o.getPropertyValue( categoryOptComboIdScheme ) );
        }
        
        
        // ---------------------------------------------------------------------
        // Get outer meta-data
        // ---------------------------------------------------------------------

        DataSet dataSet = dataValueSet.getDataSet() != null ? identifiableObjectManager.getObject( DataSet.class, dataSetIdScheme, dataValueSet.getDataSet() ) : null;

        Date completeDate = parseDate( dataValueSet.getCompleteDate() );

        Period outerPeriod = periodMap.get( trimToNull( dataValueSet.getPeriod() ), periodCallable );

        OrganisationUnit outerOrgUnit = orgUnitMap.get( trimToNull( dataValueSet.getOrgUnit() ), orgUnitCallable );

        CategoryOptionCombo fallbackCategoryOptionCombo = categoryService.getDefaultCategoryOptionCombo();

        CategoryOptionCombo outerAttrOptionCombo = null;

        Set<DataElement> dataSetDataElements = dataSet != null ? dataSet.getDataElements() : new HashSet<>();

        if ( dataValueSet.getAttributeOptionCombo() != null )
        {
            outerAttrOptionCombo = optionComboMap.get( trimToNull( dataValueSet.getAttributeOptionCombo() ), attributeOptionComboCallable.setId( trimToNull( dataValueSet.getAttributeOptionCombo() ) ) );
        }
        else if ( dataValueSet.getAttributeCategoryOptions() != null )
        {
            outerAttrOptionCombo = inputUtils.getAttributeOptionCombo( dataSet.getCategoryCombo(),
                new HashSet<>( dataValueSet.getAttributeCategoryOptions() ), idScheme );
        }

        // ---------------------------------------------------------------------
        // Validation
        // ---------------------------------------------------------------------

        
        if ( dataSet == null && trimToNull( dataValueSet.getDataSet() ) != null )
        {
            summary.getConflicts().add( new ImportConflict( dataValueSet.getDataSet(), "Data set not found or not accessible" ) );
            summary.setStatus( ImportStatus.ERROR );
        }

        if ( dataSet != null && !aclService.canDataWrite( currentUser, dataSet ) )
        {
            summary.getConflicts().add( new ImportConflict( dataValueSet.getDataSet(), "User does not have write access for DataSet: " + dataSet.getUid() ) );
            summary.setStatus( ImportStatus.ERROR );
        }

        if ( dataSet == null && strictDataElements )
        {
            summary.getConflicts().add( new ImportConflict( "DATA_IMPORT_STRICT_DATA_ELEMENTS", "A valid dataset is required" ) );
            summary.setStatus( ImportStatus.ERROR );
        }
        
        
        if ( outerOrgUnit == null && trimToNull( dataValueSet.getOrgUnit() ) != null )
        {
            summary.getConflicts().add( new ImportConflict( dataValueSet.getOrgUnit(), "Org unit not found or not accessible" ) );
            summary.setStatus( ImportStatus.ERROR );
        }

        if ( outerAttrOptionCombo == null && trimToNull( dataValueSet.getAttributeOptionCombo() ) != null )
        {
            summary.getConflicts().add( new ImportConflict( dataValueSet.getAttributeOptionCombo(), "Attribute option combo not found or not accessible" ) );
            summary.setStatus( ImportStatus.ERROR );
        }

        if ( ImportStatus.ERROR.equals( summary.getStatus() ) )
        {
            summary.setDescription( "Import process was aborted" );
            //notifier.notify( id, WARN, "Import process aborted", true ).addJobSummary( id, summary, ImportSummary.class );
            dataValueSet.close();
            return summary;
        }

        
        if ( dataSet != null && completeDate != null )
        {
            notifier.notify( id, notificationLevel, "Completing data set" );
            handleComplete( dataSet, completeDate, outerPeriod, outerOrgUnit, fallbackCategoryOptionCombo, currentUserName, summary ); //TODO
        }
        else
        {
            summary.setDataSetComplete( Boolean.FALSE.toString() );
        }
        
        
        final Set<OrganisationUnit> currentOrgUnits = currentUserService.getCurrentUserOrganisationUnits();

        BatchHandler<org.hisp.dhis.datavalue.DataValue> dataValueBatchHandler = batchHandlerFactory.createBatchHandler( DataValueBatchHandler.class ).init();
        BatchHandler<DataValueAudit> auditBatchHandler = batchHandlerFactory.createBatchHandler( DataValueAuditBatchHandler.class ).init();

        int importCount = 0;
        int updateCount = 0;
        int deleteCount = 0;
        int totalCount = 0;

        // ---------------------------------------------------------------------
        // Data values
        // ---------------------------------------------------------------------

        Date now = new Date();

        clock.logTime( "Validated outer meta-data" );
        //notifier.notify( id, notificationLevel, "Importing data values" );

        while ( dataValueSet.hasNextDataValue() )
        {
            org.hisp.dhis.dxf2.datavalue.DataValue dataValue = dataValueSet.getNextDataValue();

            totalCount++;

            final DataElement dataElement =
                dataElementMap.get( trimToNull( dataValue.getDataElement() ), dataElementCallable.setId( trimToNull( dataValue.getDataElement() ) ) );
            final Period period = outerPeriod != null ? outerPeriod :
                periodMap.get( trimToNull( dataValue.getPeriod() ), periodCallable.setId( trimToNull( dataValue.getPeriod() ) ) );
            final OrganisationUnit orgUnit = outerOrgUnit != null ? outerOrgUnit :
                orgUnitMap.get( trimToNull( dataValue.getOrgUnit() ), orgUnitCallable.setId( trimToNull( dataValue.getOrgUnit() ) ) );
            CategoryOptionCombo categoryOptionCombo =
                optionComboMap.get( trimToNull( dataValue.getCategoryOptionCombo() ), categoryOptionComboCallable.setId( trimToNull( dataValue.getCategoryOptionCombo() ) ) );
            CategoryOptionCombo attrOptionCombo = outerAttrOptionCombo != null ? outerAttrOptionCombo :
                optionComboMap.get( trimToNull( dataValue.getAttributeOptionCombo() ), attributeOptionComboCallable.setId( trimToNull( dataValue.getAttributeOptionCombo() ) ) );

            // -----------------------------------------------------------------
            // Potentially heat caches
            // -----------------------------------------------------------------

            
            if ( !dataElementMap.isCacheLoaded() && dataElementMap.getCacheMissCount() > CACHE_MISS_THRESHOLD )
            {
                dataElementMap.load( identifiableObjectManager.getAll( DataElement.class ), o -> o.getPropertyValue( dataElementIdScheme ) );

                log.info( "Data element cache heated after cache miss threshold reached" );
            }

            if ( !orgUnitMap.isCacheLoaded() && orgUnitMap.getCacheMissCount() > CACHE_MISS_THRESHOLD )
            {
                orgUnitMap.load( identifiableObjectManager.getAll( OrganisationUnit.class ), o -> o.getPropertyValue( orgUnitIdScheme ) );

                log.info( "Org unit cache heated after cache miss threshold reached" );
            }

            if ( !optionComboMap.isCacheLoaded() && optionComboMap.getCacheMissCount() > CACHE_MISS_THRESHOLD )
            {
                optionComboMap.load( identifiableObjectManager.getAll( CategoryOptionCombo.class ), o -> o.getPropertyValue(
                    categoryOptComboIdScheme ) );

                log.info( "Category Option Combo cache heated after cache miss threshold reached" );
            }
            
            
            
            // -----------------------------------------------------------------
            // Validation
            // -----------------------------------------------------------------

            if ( dataElement == null )
            {
                summary.getConflicts().add( new ImportConflict( dataValue.getDataElement(), "Data element not found or not accessible" ) );
                continue;
            }

            if ( period == null )
            {
                summary.getConflicts().add( new ImportConflict( dataValue.getPeriod(), "Period not valid" ) );
                continue;
            }

            if ( orgUnit == null )
            {
                summary.getConflicts().add( new ImportConflict( dataValue.getOrgUnit(), "Organisation unit not found or not accessible" ) );
                continue;
            }

            if ( categoryOptionCombo == null && trimToNull( dataValue.getCategoryOptionCombo() ) != null )
            {
                summary.getConflicts().add( new ImportConflict( dataValue.getCategoryOptionCombo(), "Category option combo not found or not accessible for writing data" ) );
                continue;
            }

            if ( categoryOptionCombo != null )
            {
                List<String> errors = accessManager.canWrite( currentUser, categoryOptionCombo );

                if ( !errors.isEmpty() )
                {
                    summary.getConflicts().addAll( errors.stream().map( s -> new ImportConflict( "dataValueSet", s ) ).collect( Collectors.toList() ) );
                    continue;
                }
            }

           
            if ( attrOptionCombo == null && trimToNull( dataValue.getAttributeOptionCombo() ) != null )
            {
                summary.getConflicts().add( new ImportConflict( dataValue.getAttributeOptionCombo(), "Attribute option combo not found or not accessible for writing data" ) );
                continue;
            }

            if ( attrOptionCombo != null )
            {
                List<String> errors = accessManager.canWrite( currentUser, attrOptionCombo );

                if ( !errors.isEmpty() )
                {
                    summary.getConflicts().addAll( errors.stream().map( s -> new ImportConflict( "dataValueSet", s ) ).collect( Collectors.toList() ) );
                    continue;
                }
            }
           
            
            boolean inUserHierarchy = orgUnitInHierarchyMap.get( orgUnit.getUid(), () -> orgUnit.isDescendant( currentOrgUnits ) );

            if ( !inUserHierarchy )
            {
                summary.getConflicts().add( new ImportConflict( orgUnit.getUid(), "Organisation unit not in hierarchy of current user: " + currentUserName ) );
                continue;
            }

            if ( dataValue.isNullValue() && !dataValue.isDeletedValue() )
            {
                summary.getConflicts().add( new ImportConflict( "Value", "Data value or comment not specified for data element: " + dataElement.getUid() ) );
                continue;
            }

            dataValue.setValueForced( org.hisp.dhis.system.util.ValidationUtils.normalizeBoolean( dataValue.getValue(), dataElement.getValueType() ) );

            String valueValid = org.hisp.dhis.system.util.ValidationUtils.dataValueIsValid( dataValue.getValue(), dataElement );

            if ( valueValid != null )
            {
                summary.getConflicts().add( new ImportConflict( dataValue.getValue(), i18n.getString( valueValid ) + ", must match data element type: " + dataElement.getUid() ) );
                continue;
            }

            String commentValid = org.hisp.dhis.system.util.ValidationUtils.commentIsValid( dataValue.getComment() );

            if ( commentValid != null )
            {
                summary.getConflicts().add( new ImportConflict( "Comment", i18n.getString( commentValid ) ) );
                continue;
            }

            Optional<Set<String>> optionCodes = dataElementOptionsMap.get( dataElement.getUid(), () -> dataElement.hasOptionSet() ?
                Optional.of( dataElement.getOptionSet().getOptionCodesAsSet() ) : Optional.empty() );

            if ( optionCodes.isPresent() && !optionCodes.get().contains( dataValue.getValue() ) )
            {
                summary.getConflicts().add( new ImportConflict( dataValue.getValue(), "Data value is not a valid option of the data element option set: " + dataElement.getUid() ) );
                continue;
            }

            // -----------------------------------------------------------------
            // Constraints
            // -----------------------------------------------------------------

            if ( categoryOptionCombo == null )
            {
                if ( requireCategoryOptionCombo )
                {
                    summary.getConflicts().add( new ImportConflict( dataValue.getValue(), "Category option combo is required but is not specified" ) );
                    continue;
                }
                else
                {
                    categoryOptionCombo = fallbackCategoryOptionCombo;
                }
            }

            if ( attrOptionCombo == null )
            {
                if ( requireAttrOptionCombo )
                {
                    summary.getConflicts().add( new ImportConflict( dataValue.getValue(), "Attribute option combo is required but is not specified" ) );
                    continue;
                }
                else
                {
                    attrOptionCombo = fallbackCategoryOptionCombo;
                }
            }

            if ( strictPeriods && !dataElementPeriodTypesMap.get( dataElement.getUid(),
                    dataElement::getPeriodTypes).contains( period.getPeriodType() ) )
            {
                summary.getConflicts().add( new ImportConflict( dataValue.getPeriod(),
                    "Period type of period: " + period.getIsoDate() + " not valid for data element: " + dataElement.getUid() ) );
                continue;
            }

            if ( strictDataElements && !dataSetDataElements.contains( dataElement ) )
            {
                summary.getConflicts().add( new ImportConflict( "DATA_IMPORT_STRICT_DATA_ELEMENTS",
                    "Data element: " + dataValue.getDataElement() + " is not part of dataset: " + dataSet.getUid() ) );
                continue;
            }

            if ( strictCategoryOptionCombos && !dataElementCategoryOptionComboMap.get( dataElement.getUid(),
                    dataElement::getCategoryOptionCombos).contains( categoryOptionCombo ) )
            {
                summary.getConflicts().add( new ImportConflict( categoryOptionCombo.getUid(),
                    "Category option combo: " + categoryOptionCombo.getUid() + " must be part of category combo of data element: " + dataElement.getUid() ) );
                continue;
            }

            if ( strictAttrOptionCombos && !dataElementAttrOptionComboMap.get( dataElement.getUid(),
                    dataElement::getDataSetCategoryOptionCombos).contains( attrOptionCombo ) )
            {
                summary.getConflicts().add( new ImportConflict( attrOptionCombo.getUid(),
                    "Attribute option combo: " + attrOptionCombo.getUid() + " must be part of category combo of data sets of data element: " + dataElement.getUid() ) );
                continue;
            }

            if ( strictOrgUnits && org.apache.commons.lang3.BooleanUtils.isFalse( dataElementOrgUnitMap.get( dataElement.getUid() + orgUnit.getUid(),
                () -> orgUnit.hasDataElement( dataElement ) ) ) )
            {
                summary.getConflicts().add( new ImportConflict( orgUnit.getUid(),
                    "Data element: " + dataElement.getUid() + " must be assigned through data sets to organisation unit: " + orgUnit.getUid() ) );
                continue;
            }

            boolean zeroAndInsignificant = org.hisp.dhis.system.util.ValidationUtils.dataValueIsZeroAndInsignificant( dataValue.getValue(), dataElement );

            if ( zeroAndInsignificant )
            {
                continue; // Ignore value
            }

            String storedByValid = org.hisp.dhis.system.util.ValidationUtils.storedByIsValid( dataValue.getStoredBy() );

            if ( storedByValid != null )
            {
                summary.getConflicts().add( new ImportConflict( dataValue.getStoredBy(), i18n.getString( storedByValid ) ) );
                continue;
            }

            String storedBy = dataValue.getStoredBy() == null || dataValue.getStoredBy().trim().isEmpty() ? currentUserName : dataValue.getStoredBy();

            final CategoryOptionCombo aoc = attrOptionCombo;

            DateRange aocDateRange = attrOptionComboDateRangeMap.get( attrOptionCombo.getUid(), aoc::getDateRange);

            if ( ( aocDateRange.getStartDate() != null && aocDateRange.getStartDate().compareTo( period.getStartDate() ) > 0 )
                || ( aocDateRange.getEndDate() != null && aocDateRange.getEndDate().compareTo( period.getEndDate() ) < 0 ) )
            {
                summary.getConflicts().add( new ImportConflict( orgUnit.getUid(),
                    "Period: " + period.getIsoDate() + " is not within date range of attribute option combo: " + attrOptionCombo.getUid() ) );
                continue;
            }

            
            if ( !attrOptionComboOrgUnitMap.get( attrOptionCombo.getUid() + orgUnit.getUid(), () ->
            {
                Set<OrganisationUnit> aocOrgUnits = aoc.getOrganisationUnits();
                return aocOrgUnits == null || orgUnit.isDescendant( aocOrgUnits );
            } ) )
            {
                summary.getConflicts().add( new ImportConflict( orgUnit.getUid(),
                    "Organisation unit: " + orgUnit.getUid() + " is not valid for attribute option combo: " + attrOptionCombo.getUid() ) );
                continue;
            }
            
            
            final DataSet approvalDataSet = dataSet != null ? dataSet : dataElementDataSetMap.get( dataElement.getUid(),
                    dataElement::getApprovalDataSet);

            
            if ( approvalDataSet != null && !forceDataInput ) // Data element is assigned to at least one data set
            {
                if ( dataSetLockedMap.get( approvalDataSet.getUid() + period.getUid() + orgUnit.getUid(),
                    () -> isLocked( currentUser, approvalDataSet, period, orgUnit, skipLockExceptionCheck ) ) )
                {
                    summary.getConflicts().add( new ImportConflict( period.getIsoDate(), "Current date is past expiry days for period " +
                        period.getIsoDate() + " and data set: " + approvalDataSet.getUid() ) );
                    continue;
                }

                Period latestFuturePeriod = dataElementLatestFuturePeriodMap.get( dataElement.getUid(), dataElement::getLatestOpenFuturePeriod);

                if ( period.isAfter( latestFuturePeriod ) && isIso8601 )
                {
                    summary.getConflicts().add( new ImportConflict( period.getIsoDate(), "Period: " +
                        period.getIsoDate() + " is after latest open future period: " + latestFuturePeriod.getIsoDate() + " for data element: " + dataElement.getUid() ) );
                    continue;
                }

                DataApprovalWorkflow workflow = approvalDataSet.getWorkflow();

                
                if ( workflow != null )
                {
                    final String workflowPeriodAoc = workflow.getUid() + period.getUid() + attrOptionCombo.getUid();

                    if ( approvalMap.get( orgUnit.getUid() + workflowPeriodAoc, () ->
                    {
                        DataApproval lowestApproval = DataApproval.getLowestApproval( new DataApproval( null, workflow, period, orgUnit, aoc ) );

                        return lowestApproval != null && lowestApprovalLevelMap.get(
                            lowestApproval.getDataApprovalLevel().getUid()
                                + lowestApproval.getOrganisationUnit().getUid() + workflowPeriodAoc,
                            () -> approvalService.getDataApproval( lowestApproval ) != null );
                    } ) )
                    {
                        summary.getConflicts().add( new ImportConflict( orgUnit.getUid(),
                            "Data is already approved for data set: " + approvalDataSet.getUid() + " period: " + period.getIsoDate()
                                + " organisation unit: " + orgUnit.getUid() + " attribute option combo: " + attrOptionCombo.getUid() ) );
                        continue;
                    }
                }
                
                
            }
            
            
            if ( approvalDataSet != null && !forceDataInput && !approvalDataSet.isDataInputPeriodAndDateAllowed( period, new Date() ) )
            {
                summary.getConflicts().add( new ImportConflict( orgUnit.getUid(),
                    "Period: " + period.getIsoDate() + " is not open for this data set at this time: " + approvalDataSet.getUid() ) );
                continue;
            }

            if ( !forceDataInput && !periodOpenForDataElement.get( dataElement.getUid() + period.getIsoDate(), () -> dataElement.isDataInputAllowedForPeriodAndDate( period, new Date() ) ) )
            {
                summary.getConflicts().add( new ImportConflict( orgUnit.getUid(), "Period " + period.getName() + " does not conform to the open periods of associated data sets" ) );
                continue;
            }

            org.hisp.dhis.datavalue.DataValue actualDataValue = null;
            if ( strategy.isDelete() && dataElement.isFileType() )
            {
                actualDataValue = dataValueService.getDataValue( dataElement, period, orgUnit, categoryOptionCombo, attrOptionCombo );
                if ( actualDataValue == null )
                {
                    summary.getConflicts().add( new ImportConflict( dataElement.getUid(), "No data value for file resource exist for the given combination" ) );
                    continue;
                }
            }

            // -----------------------------------------------------------------
            // Create data value
            // -----------------------------------------------------------------

            org.hisp.dhis.datavalue.DataValue internalValue = new org.hisp.dhis.datavalue.DataValue();

            internalValue.setDataElement( dataElement );
            internalValue.setPeriod( period );
            internalValue.setSource( orgUnit );
            internalValue.setCategoryOptionCombo( categoryOptionCombo );
            internalValue.setAttributeOptionCombo( attrOptionCombo );
            internalValue.setValue( trimToNull( dataValue.getValue() ) );
            internalValue.setStoredBy( storedBy );
            internalValue.setCreated( dataValue.hasCreated() ? parseDate( dataValue.getCreated() ) : now );
            internalValue.setLastUpdated( dataValue.hasLastUpdated() ? parseDate( dataValue.getLastUpdated() ) : now );
            internalValue.setComment( trimToNull( dataValue.getComment() ) );
            internalValue.setFollowup( dataValue.getFollowup() );
            internalValue.setDeleted( org.apache.commons.lang3.BooleanUtils.isTrue( dataValue.getDeleted() ) );

            // -----------------------------------------------------------------
            // Save, update or delete data value
            // -----------------------------------------------------------------

            org.hisp.dhis.datavalue.DataValue existingValue = !skipExistingCheck ? dataValueBatchHandler.findObject( internalValue ) : null;

            // -----------------------------------------------------------------
            // Check soft deleted data values on update and import
            // -----------------------------------------------------------------

            if ( !skipExistingCheck && existingValue != null && !existingValue.isDeleted() )
            {
                if ( strategy.isCreateAndUpdate() || strategy.isUpdate() )
                {
                    org.hisp.dhis.common.AuditType auditType =  org.hisp.dhis.common.AuditType.UPDATE;

                    if ( internalValue.isNullValue() || internalValue.isDeleted() )
                    {
                        internalValue.setDeleted( true );

                        auditType =  org.hisp.dhis.common.AuditType.DELETE;

                        deleteCount++;
                    }
                    else
                    {
                        updateCount++;
                    }

                    if ( !dryRun )
                    {
                        dataValueBatchHandler.updateObject( internalValue );

                        if ( !skipAudit )
                        {
                            DataValueAudit auditValue = new DataValueAudit( internalValue, existingValue.getValue(), storedBy, auditType );

                            auditBatchHandler.addObject( auditValue );
                        }
                        
                        if ( dataElement.isFileType() )
                        {
                            org.hisp.dhis.fileresource.FileResource fr = fileResourceService.getFileResource( internalValue.getValue() );

                            fr.setAssigned( true );

                            fileResourceService.updateFileResource( fr );
                        }

                    }
                }
                else if ( strategy.isDelete() )
                {
                    internalValue.setDeleted( true );

                    deleteCount++;

                    if ( !dryRun )
                    {
                        if ( dataElement.isFileType() && actualDataValue != null )
                        {
                            org.hisp.dhis.fileresource.FileResource fr = fileResourceService.getFileResource( actualDataValue.getValue() );

                            fileResourceService.updateFileResource( fr );
                        }

                        dataValueBatchHandler.updateObject( internalValue );

                        if ( !skipAudit )
                        {
                            DataValueAudit auditValue = new DataValueAudit( internalValue, existingValue.getValue(), storedBy, org.hisp.dhis.common.AuditType.DELETE );

                            auditBatchHandler.addObject( auditValue );
                        }
                    }
                }
            }
            else
            {
                if ( strategy.isCreateAndUpdate() || strategy.isCreate() )
                {
                    if ( !internalValue.isNullValue() ) // Ignore null values
                    {
                        if ( existingValue != null && existingValue.isDeleted() )
                        {
                            importCount++;

                            if ( !dryRun )
                            {
                                dataValueBatchHandler.updateObject( internalValue );

                                if ( dataElement.isFileType() )
                                {
                                    org.hisp.dhis.fileresource.FileResource fr = fileResourceService.getFileResource( internalValue.getValue() );

                                    fr.setAssigned( true );

                                    fileResourceService.updateFileResource( fr );
                                }
                            }
                        }
                        else
                        {
                            boolean added = false;

                            if ( !dryRun )
                            {
                                added = dataValueBatchHandler.addObject( internalValue );

                                if ( added && dataElement.isFileType() )
                                {
                                    org.hisp.dhis.fileresource.FileResource fr = fileResourceService.getFileResource( internalValue.getValue() );

                                    fr.setAssigned( true );

                                    fileResourceService.updateFileResource( fr );
                                }
                            }

                            if ( dryRun || added )
                            {
                                importCount++;
                            }
                        }
                    }
                }
            }
        }

        dataValueBatchHandler.flush();
        auditBatchHandler.flush();

        int ignores = totalCount - importCount - updateCount - deleteCount;

        summary.setImportCount( new org.hisp.dhis.dxf2.importsummary.ImportCount( importCount, updateCount, ignores, deleteCount ) );
        summary.setStatus( summary.getConflicts().isEmpty() ? ImportStatus.SUCCESS : ImportStatus.WARNING );
        summary.setDescription( "Import process completed successfully" );

        clock.logTime( "Data value import done, total: " + totalCount + ", import: " + importCount + ", update: " + updateCount + ", delete: " + deleteCount );
        //notifier.notify( id, notificationLevel, "Import done", true ).addJobSummary( id, notificationLevel, summary, ImportSummary.class );

        dataValueSet.close();

        return summary;
    }
 */   
    
    
    
}
