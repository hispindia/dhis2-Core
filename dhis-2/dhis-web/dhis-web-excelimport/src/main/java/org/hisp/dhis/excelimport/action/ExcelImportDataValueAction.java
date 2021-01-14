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
import org.hisp.dhis.system.util.MathUtils;
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
public class ExcelImportDataValueAction implements Action
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
    
    private List<String> ignoreStatusMsgList = new ArrayList<>();
    
    public List<String> getIgnoreStatusMsgList()
    {
        return ignoreStatusMsgList;
    }
    
    private List<String> insertStatusMsgList = new ArrayList<>();
    
    public List<String> getInsertStatusMsgList()
    {
        return insertStatusMsgList;
    }

    private List<String> updateStatusMsgList = new ArrayList<>();
    
    public List<String> getUpdateStatusMsgList()
    {
        return updateStatusMsgList;
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

        insertStatusMsgList = new ArrayList<String>();
        updateStatusMsgList = new ArrayList<String>();
        ignoreStatusMsgList = new ArrayList<String>();
        
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
        
        
        System.out.println( " Import start at  --  " + new Date() );
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
        int ingoreCount = 0;
        int updateCount = 0;
        int insertCount = 0;
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
            
            
            
            Row dataValueRow = tempSheet.getRow( tempRowNo );
            
            Cell dataValueCell = dataValueRow.getCell( tempColNo);
            
            //dataCellValue = getTemplateCellValue( dataValueCell, evaluator );
            
            dataCellValue = dataFormatter.formatCellValue( dataValueCell );
            
            //System.out.println( cocUid + " --  " + dataElementUid  + " --  " + orgUnitUid + " --  " + isoPeriod + " --  " + tempRowNo + " --  " + tempColNo + " -- " + dataValueCell );
            
            
            /*
            if( dataValueCell.getCellTypeEnum() == CellType.FORMULA )
            {
                dataCellValue = getTemplateCellValue( dataValueCell, evaluator );
            }
            
            else
            {
                dataCellValue = dataFormatter.formatCellValue( dataValueCell );
            }
            */
            
            
            DataElement dataElement = dataElementService.getDataElement( dataElementUid );
            CategoryOptionCombo categoryOptionCombo = categoryService.getCategoryOptionCombo( cocUid );
            
            OrganisationUnit  organisationUnit = organisationUnitService.getOrganisationUnit( orgUnitUid );
            
            
            org.hisp.dhis.datavalue.DataValue newDataValue = new org.hisp.dhis.datavalue.DataValue();

            newDataValue.setDataElement( dataElement );

            newDataValue.setPeriod( selectedPeriod );
            newDataValue.setSource( organisationUnit );
            
            newDataValue.setCategoryOptionCombo( categoryOptionCombo );
            //.setOptionCombo( currentOptionCombo );
            newDataValue.setValue( dataCellValue );
            //dataValue.setTimestamp( new Date() );
            newDataValue.setLastUpdated( new Date() );
            newDataValue.setCreated( new Date() );
            newDataValue.setStoredBy( storedBy );
            
            
            if ( dataCellValue.equalsIgnoreCase( "" ) || dataCellValue == null || dataCellValue.equalsIgnoreCase( " " ) )
            {
                ingoreCount++;
                int importRowNo = tempRowNo + 1;
                int importColNo = tempColNo + 1;
                ignoreStatusMsgList.add( "Row No - " + importRowNo + " Col No - " + importColNo +  " For DataElement - " + dataElement.getName() +  " and Category - " + categoryOptionCombo.getName() +" No value exist"  );
                continue;
            }
            else if ( dataElement.getValueType().isBoolean() && !MathUtils.isBool( dataCellValue ) )
            {
                ingoreCount++;
                int importRowNo = tempRowNo + 1;
                int importColNo = tempColNo + 1;
                ignoreStatusMsgList.add( "Row No - " + importRowNo + " Col No - " + importColNo +  " For DataElement - " + dataElement.getName() +  " and Category - " + categoryOptionCombo.getName() + " Value : " + dataCellValue + " is not True/False, is should be either True or False"  );
                continue;
            }
            else if ( dataElement.getValueType().isInteger() && !MathUtils.isInteger( dataCellValue ) )
            {
                ingoreCount++;
                int importRowNo = tempRowNo + 1;
                int importColNo = tempColNo + 1;
                ignoreStatusMsgList.add( "Row No - " + importRowNo + " Col No - " + importColNo +  " For DataElement - " + dataElement.getName() +  " and Category - " + categoryOptionCombo.getName() + " Value : " + dataCellValue + " is not Integer"  );
                continue;
            }
            else if ( dataElement.getValueType().isText()&& MathUtils.isInteger( dataCellValue ) )
            {
                ingoreCount++;
                int importRowNo = tempRowNo + 1;
                int importColNo = tempColNo + 1;
                ignoreStatusMsgList.add( "Row No - " + importRowNo + " Col No - " + importColNo +  " For DataElement - " + dataElement.getName() +  " and Category - " + categoryOptionCombo.getName() + " Value : " + dataCellValue + " is not Text"  );
                continue;
            }    
            
            else
            {
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
                        updateCount++;
                        int importRowNo = tempRowNo + 1;
                        int importColNo = tempColNo + 1;
                        //updateStatusMsgList.add( "Row No - " + importRowNo + " Col No - " + importColNo +  " For DataElement - " + dataElement.getName() +  " and Category - " + categoryOptionCombo.getName() + ". is succuessfully updated with value - "  + dataCellValue );
                    }
                    catch ( Exception e )
                    {
                        //throw new RuntimeException( "Cannot add datavalue", ex );
                        message = "Exception occured while import, please check log for more details" + e.getMessage();
                        updateStatusMsgList.add( message );
                    }
                }
                else
                {
                    try
                    {
                        dataValueService.addDataValue( newDataValue );
                        insertCount++;
                        int importRowNo = tempRowNo + 1;
                        int importColNo = tempColNo + 1;
                        //insertStatusMsgList.add( "Row No - " + importRowNo + " Col No - " + importColNo +  " For DataElement - " + dataElement.getName() +  " and Category - " + categoryOptionCombo.getName()  + ". is succuessfully imported with value - "  + newDataValue.getValue()  );
                    }
                    catch ( Exception e )
                    {
                        //throw new RuntimeException( "Cannot add datavalue", ex );
                        message = "Exception occured while import, please check log for more details" + e.getMessage();
                        updateStatusMsgList.add( message );
                    }
                }
            }
        }
        
        
        //System.out.println( " dataValueSet Size--  " + dataValueSet.getDataValues().size() );
        

        
        //System.out.println( "dataValueSet --  " + dataValueSet );
        
        //importStatusMsgList.add( dataValueSet.toString() );
        
        //ImportOptions importOptions = ImportOptions.getDefaultImportOptions();
        //importSummary = dataValueSetService.saveDataValueSetExcelImport( dataValueSet );
        
         String finalMsg = "XML Mapping List size : " + xmlMappingList.size()  + "  Insert Count : " + insertCount + "  Update Count -- " + updateCount  + "  Ignore Count ----- " + ingoreCount;
        
        importStatusMsgList.add( finalMsg );
        System.out.println( "DataValue Set ImportSummary " + finalMsg );
        
        System.out.println( " Import done at --  " + new Date() );
        
        return SUCCESS;
    }
    

    
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
    
}