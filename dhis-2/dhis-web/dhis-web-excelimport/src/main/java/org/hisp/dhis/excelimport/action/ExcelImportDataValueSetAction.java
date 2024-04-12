package org.hisp.dhis.excelimport.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
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
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.dxf2.datavalue.DataValue;
import org.hisp.dhis.dxf2.datavalueset.DataValueSet;
import org.hisp.dhis.dxf2.datavalueset.DataValueSetService;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.excelimport.api.XMLAttribute;
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
    private Map<String, Integer> organisationUnitIdUidMap = new HashMap<String, Integer>();
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
    
    private String availablePeriods;

    public void setAvailablePeriods( String availablePeriods )
    {
        this.availablePeriods = availablePeriods;
    }
    
    private String assessmentType;
    
    public void setAssessmentType( String assessmentType )
    {
        this.assessmentType = assessmentType;
    }

    private String organisationUnitName;
    private String defaultCategoryOptionComboUID;
    private OrganisationUnit selectedOrgUnit;
    
    private Map<Long, String> organisationUnitXMLFileMap = new HashMap<Long, String>();
    
    private String importAssementType = "";
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
        organisationUnitName = selectedOrgUnit.getName();
        
        
        
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
     
        System.out.println( "importAssementType " + assessmentType  );
        
        if( assessmentType != null )
        {
            
            importAssementType = assessmentType;
        }
        else
        {
            message = "Assement Type not selected";
            //importStatusMsgList.add( message );
            return SUCCESS;
        }
        
        periodId = (int) periodService.reloadIsoPeriod( isoPeriod ).getId();
        
        System.out.println( "File name : " + fileName +  "  ISO period -- "+ isoPeriod + " periodID " + periodId + " import Start " + new Date() );
        
        String fileType = fileName.substring( fileName.indexOf( '.' ) + 1, fileName.length() );
                
        System.out.println( "File type : " + fileType );
        
        
        if ( !fileType.equalsIgnoreCase( "xlsx" ) )
        {
            message = "The file you are trying to import is not an excel file";
            //importStatusMsgList.add( message );

            return SUCCESS;
        }
        
        //deCodesImportXMLFileName = "dataValueSetMappingPHCToolKit.xml";
        
        organisationUnitXMLFileMap = new HashMap<Long, String>( getXMLFileNameList() );
        
        //deCodesImportXMLFileName = organisationUnitXMLFileMap.get( selectedOrgUnit.getId() );
        
        deCodesImportXMLFileName = getXMLFileName( importAssementType, selectedOrgUnit.getId() );
        
        System.out.println( " xmlMapping File Name : " + deCodesImportXMLFileName );
        
        if( deCodesImportXMLFileName.equalsIgnoreCase( "" ) )
        {
            message = "The xml mapping file not found";
            //importStatusMsgList.add( message );
            return SUCCESS;
        }
        
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
        
        categoryOptionComboIdUidMap = new HashMap<String, Integer>( getCategoryOptionComboIdUidList() );
        dataElementIdUidMap = new HashMap<String, Integer>( getDataElementIdUidList());
        organisationUnitIdUidMap = new HashMap<String, Integer>( getOrganisationUnitIdUidList());
        

        
        attributeCOCId = categoryOptionComboIdUidMap.get( attributeOptionCombo );
        tempDataValueList = new ArrayList<String>();
        
        defaultCategoryOptionComboUID = "HllvX50cXC0";
         
        List<XMLAttribute> xmlMappingList = new ArrayList<>();
        
        xmlMappingList.clear();

        if ( xmlMappingList.isEmpty() )
        {
            xmlMappingList = new ArrayList<>( getXMLAttribute( deCodesImportXMLFileName ) );
        }
        
        System.out.println( " xmlMapping File Name : " + deCodesImportXMLFileName + " xmlMappingList size : " + xmlMappingList.size() );
          
        DataValueSet dataValueSet = new DataValueSet();
        
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
        
        XSSFWorkbook workBook = new XSSFWorkbook(inputFS);
        
        //Workbook workBook = new XSSFWorkbook(file);
        
        
        
        //XSSFSheet wbSheet = wb.getSheetAt(0);
        
        //Workbook workBook = new XSSFWorkbook(inputFS); 
        
        FormulaEvaluator evaluator = workBook.getCreationHelper().createFormulaEvaluator();
        Map<String, FormulaEvaluator> workBooks = new HashMap<String, FormulaEvaluator>();
        
        //evaluator.evaluateAll();
        
        //workBook.getForceFormulaRecalculation();
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
            String dataElement = xmlAttribute.getDataElement();
            //String coc = xmlAttribute.getCategoryOptionCombo();
            //String orgUnit = xmlAttribute.getOrgUnit();
            
            //Sheet tempSheet = workBook.getSheetAt( sheetNo );
            Sheet tempSheet = workBook.getSheetAt( sheetNo );
            
            //System.out.println( " Import for Sheet : " + tempSheet );
            
            
            //System.out.println( coc + " --  " + dataElement  + " --  " + orgUnit + " --  " + isoPeriod + " --  " + tempRowNo + " --  " + tempColNo);
            
            Row dataValueRow = tempSheet.getRow( tempRowNo );
            
            Cell dataValueCell = dataValueRow.getCell( tempColNo);
            
            
            dataCellValue = getTemplateCellValue( dataValueCell, evaluator );
            
            //System.out.println( " 1 --  " + dataElement  + " --  " + selectedOrgUnit.getUid() + " --  " + dataCellValue  + " --  " + isoPeriod );
            
            if( dataValueCell.getCellType() == CellType.FORMULA )
            {
                dataCellValue = getTemplateCellValue( dataValueCell, evaluator );
            }
            
            else
            {
                dataCellValue = dataFormatter.formatCellValue( dataValueCell );
            }
            
            System.out.println( " 2 --  " + dataElement  + " --  " + selectedOrgUnit.getUid() + " --  " + dataCellValue  + " --  " + isoPeriod );
            //dataCellValue = dataFormatter.formatCellValue( dataValueCell );
            
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
                
                // Finding string length
                int n = dataCellValue.length();
         
                // First character of a string
                //char first = dataCellValue.charAt(0);
         
                // Last character of a string
                char lastChar = dataCellValue.charAt(n - 1);
                
                if( Character.compare(lastChar, '%') == 0 )
                {
                    dataCellValue = dataCellValue.substring(0, dataCellValue.length() - 1);  
                }
                
                DataValue dataValue = new DataValue();
                dataValue.setDataElement( dataElement );
                dataValue.setCategoryOptionCombo( defaultCategoryOptionComboUID );
                dataValue.setAttributeOptionCombo( defaultCategoryOptionComboUID );
                
                dataValue.setOrgUnit( selectedOrgUnit.getUid() );
                dataValue.setValue( dataCellValue );
                dataValue.setPeriod( isoPeriod );
                
                dataValues.add( dataValue );
                /*
                Integer deId = dataElementIdUidMap.get( dataElement );
                Integer cocId = categoryOptionComboIdUidMap.get( coc );
                Integer orgUnitId = organisationUnitIdUidMap.get( orgUnit );
                
                String completeDataValue = deId+":" + cocId +":" + orgUnitId +":" + dataCellValue;
                
                tempDataValueList.add( completeDataValue );
                */
                
                System.out.println( " 3 --  " + dataElement  + " --  " + selectedOrgUnit.getUid() + " --  " + dataCellValue  + " --  " + isoPeriod );
            }
        }
        
        
        workBook.close();
        
        //dataValueSet.setAttributeCategoryOptions( attributeCategoryOptions );
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
        
        
        // import from dataValueSetService
        
        
        importSummary = dataValueSetService.importDataValueSetExcelImport( dataValueSet );
        importStatusMsgList.add( importSummary.toString() );
        
        
        //importSummaryDescription = "The import process failed: Failed to create statement";
        //importSummaryDescription.length()
        importSummaryDescription = importSummary.getDescription();
        // import from sql-query-script
        
        /*
        if( importSummaryDescription.equalsIgnoreCase( "The import process failed: Failed to create statement" ))
        {
            importDataValueUsingQuery( tempDataValueList  );
        }
        */
        
        // import from sql-query-script
        //importDataValueUsingQuery( tempDataValueList  );
        
        
        
        
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
        
        
        System.out.println( "DataValue Set ImportSummary " + importSummary );
        
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
                //break;
            }
            
            else if( cell.getCellType() == CellType.BOOLEAN )
            {
                templateCellValue = ""+ cell.getStringCellValue();
                //break;
            }
            
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
    
}
