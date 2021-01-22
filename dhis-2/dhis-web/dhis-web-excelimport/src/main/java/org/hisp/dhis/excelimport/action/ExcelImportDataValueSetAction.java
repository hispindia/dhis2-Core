package org.hisp.dhis.excelimport.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private ReportService reportService;

    public void setReportService( ReportService reportService )
    {
        this.reportService = reportService;
    }

    @Autowired
    private DataValueSetService dataValueSetService;
    
    @Autowired
    private PeriodService periodService;

    @Autowired
    private OrganisationUnitService organisationUnitService;
    

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
    
    private ImportSummary importSummary;
    
    public ImportSummary getImportSummary()
    {
        return importSummary;
    }
    
    private Period selectedPeriod;
    
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
    
    private String reportList;

    public void setReportList( String reportList )
    {
        this.reportList = reportList;
    }
    
    private String deCodesImportXMLFileName = "";
    private List<XMLAttribute> xmlMappingList;
    private OrganisationUnit selectedOrgUnit;
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    

    public String execute() 
        throws Exception
    {
        
        raFolderName = reportService.getRAFolderName();
        //User user = currentUserService.getCurrentUser();
        
        message = "";
        importStatusMsgList = new ArrayList<String>();
        
        deCodesImportXMLFileName = reportList + "DECodes.xml";
        
        System.out.println( "importDate " + importDate  );
        
        selectedPeriod = periodService.getPeriod( availablePeriods );
        selectedOrgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );
        
        /*
        if( importDate != null && !importDate.equalsIgnoreCase( "" ))
        {
            isoPeriod = importDate.split( "-" )[0] + importDate.split( "-" )[1] + importDate.split( "-" )[2];
        }
        else
        {
            message = "Priod is not selected or ISO format";
            return SUCCESS;
        }
        */
        
     
        System.out.println( "File name : " + fileName +  "  ISO period -- "+ selectedPeriod.getIsoDate() + " import Start " + new Date() );
        
        String fileType = fileName.substring( fileName.indexOf( '.' ) + 1, fileName.length() );

        
        System.out.println( "File type : " + fileType );
        
        if ( !fileType.equalsIgnoreCase( "xlsx" ) && !fileType.equalsIgnoreCase( "xls" ))
        {
            message = "The file you are trying to import is not an excel file";
            System.out.println( " 1 " + message );
            //importStatusMsgList.add( message );

            return SUCCESS;
        }

        
        //String excelImportFolderName = "excelimport";
        //String deCodesImportXMLFileName = deCodesCheckerXMLFileName;
        
        xmlMappingList = new ArrayList<>();
        
        xmlMappingList.clear();

        if ( xmlMappingList.isEmpty() )
        {
            xmlMappingList = new ArrayList<>( getXMLAttribute( deCodesImportXMLFileName ) );
        }
          
        System.out.println( " xmlMappingList size : " + xmlMappingList.size() );
        DataValueSet dataValueSet = new DataValueSet();
        //List<DataValue> dataValues = new ArrayList<>();
        
        FileInputStream inputFS = new FileInputStream(file);
        
        if ( fileType.equalsIgnoreCase( "xlsx" ) )
        {
            XSSFWorkbook xssfWworkBook = new XSSFWorkbook(inputFS);
            
            dataValueSet = getDataValueSetByXLSX( inputFS, xssfWworkBook );
            /*
            for ( int i = 0; i < xssfWworkBook.getNumberOfSheets(); i++ )
            {
                System.out.println( " Import for XLSX Sheet name create : " + xssfWworkBook.getSheetName( i ) );
            }
            */
            
        }
        
        else if ( fileType.equalsIgnoreCase( "xls" ) )
        {
            HSSFWorkbook hssfWworkBook = new HSSFWorkbook(inputFS);
            dataValueSet = getDataValueSetByXLS( inputFS, hssfWworkBook );
        }
        
        System.out.println( "dataValueSet Size --  " + dataValueSet.getDataValues().size() );
        
        /*
        for( DataValue dataSetDataValue : dataValueSet.getDataValues() )
        {
           
            System.out.println( "dataElement --  " + dataSetDataValue.getDataElement() );
            System.out.println( "coc --  " + dataSetDataValue.getCategoryOptionCombo() );
            System.out.println( "orgUnit --  " + dataSetDataValue.getOrgUnit() );
            System.out.println( "period --  " + dataSetDataValue.getPeriod() );
            System.out.println( "dataValue --  " + dataSetDataValue.getValue() );
            
            
            System.out.println( dataSetDataValue.getDataElement() + " --  " + dataSetDataValue.getCategoryOptionCombo()  + " --  " + dataSetDataValue.getOrgUnit() + " --  " + dataSetDataValue.getPeriod() + " --  " + dataSetDataValue.getValue()   + " --  " + dataSetDataValue.getStoredBy()  + " --  " + dataSetDataValue.getLastUpdated() );
        }
        */
        
        
        //System.out.println( "dataValueSet Size --  " + dataValueSet.getDataValues().size() );
        
        System.out.println( "Importing start time --  : " + new Date() );
        //System.out.println( "dataValueSet --  " + dataValueSet );
        
        //ImportOptions importOptions = ImportOptions.getDefaultImportOptions();
        //importOptions.setPreheatCache( true );
        importSummary = dataValueSetService.saveDataValueSetExcelImport( dataValueSet );
        
        importStatusMsgList.add( importSummary.toString() );
        
        System.out.println( "Importing end time --  : " + new Date() );
        //System.out.println( "importSummary " + importSummary );
        
        return SUCCESS;
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
    
    
    public DataValueSet getDataValueSetByXLSX( FileInputStream inputFS, XSSFWorkbook xssfWworkBook ) throws IOException
    {
        System.out.println( " xlsx Template reading start time --  : " + new Date() );
        DataValueSet dataValueSet = new DataValueSet();
        List<DataValue> dataValues = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();
        
        Iterator<XMLAttribute> xmlMappingIterator = xmlMappingList.iterator();
        while ( xmlMappingIterator.hasNext() )
        {
            XMLAttribute xmlAttribute = (XMLAttribute) xmlMappingIterator.next();
            
            int tempRowNo = xmlAttribute.getRowno();
            int tempColNo = xmlAttribute.getColno();
            int sheetNo = xmlAttribute.getSheetno();
            String dataElement = xmlAttribute.getDataElement();
            String coc = xmlAttribute.getCategoryOptionCombo();
            String orgUnit = selectedOrgUnit.getUid();
            
            //Sheet tempSheet = workBook.getSheetAt( sheetNo );
            Sheet tempSheet = xssfWworkBook.getSheetAt( sheetNo );
            
            //System.out.println( " Import for Sheet : " + tempSheet );
            
            
            //System.out.println( coc + " --  " + dataElement  + " --  " + orgUnit + " --  " + isoPeriod + " --  " + tempRowNo + " --  " + tempColNo);
            
            Row dataValueRow = tempSheet.getRow( tempRowNo );
            Cell dataValueCell = dataValueRow.getCell( tempColNo);
            String dataCellValue = dataFormatter.formatCellValue( dataValueCell );
            if( dataCellValue != null && !dataCellValue.equalsIgnoreCase( "" ) )
            {
                DataValue dataValue = new DataValue();
                dataValue.setDataElement( dataElement );
                dataValue.setCategoryOptionCombo( coc );
                dataValue.setOrgUnit( orgUnit );
                dataValue.setValue( dataCellValue );
                dataValue.setPeriod( selectedPeriod.getIsoDate() );
                
                dataValues.add( dataValue );
                
                
                //System.out.println( coc + " 1 --  " + dataElement  + " --  " + orgUnit + " --  " + dataCellValue  + " --  " + isoPeriod );
            }
        }
        
        dataValueSet.setDataValues( dataValues );

        System.out.println( "xlsx Template reading end time --  : " + new Date() );
        return dataValueSet;
    }
    
    
    public DataValueSet getDataValueSetByXLS( FileInputStream inputFS, HSSFWorkbook  hssfWworkBook ) throws IOException
    {
        System.out.println( "xls Template reading start time --  : " + new Date() );
        DataValueSet dataValueSet = new DataValueSet();
        List<DataValue> dataValues = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();
        
        Iterator<XMLAttribute> xmlMappingIterator = xmlMappingList.iterator();
        while ( xmlMappingIterator.hasNext() )
        {
            XMLAttribute xmlAttribute = (XMLAttribute) xmlMappingIterator.next();
            
            int tempRowNo = xmlAttribute.getRowno();
            int tempColNo = xmlAttribute.getColno();
            int sheetNo = xmlAttribute.getSheetno();
            String dataElement = xmlAttribute.getDataElement();
            String coc = xmlAttribute.getCategoryOptionCombo();
            String orgUnit = selectedOrgUnit.getUid();
            
            //Sheet tempSheet = workBook.getSheetAt( sheetNo );
            Sheet tempSheet = hssfWworkBook.getSheetAt( sheetNo );
            
            //System.out.println( " Import for Sheet : " + tempSheet );
            
            
            //System.out.println( coc + " --  " + dataElement  + " --  " + orgUnit + " --  " + isoPeriod + " --  " + tempRowNo + " --  " + tempColNo);
            
            Row dataValueRow = tempSheet.getRow( tempRowNo );
            Cell dataValueCell = dataValueRow.getCell( tempColNo);
            String dataCellValue = dataFormatter.formatCellValue( dataValueCell );
            if( dataCellValue != null && !dataCellValue.equalsIgnoreCase( "" ) )
            {
                DataValue dataValue = new DataValue();
                dataValue.setDataElement( dataElement );
                dataValue.setCategoryOptionCombo( coc );
                dataValue.setOrgUnit( orgUnit );
                dataValue.setValue( dataCellValue );
                dataValue.setPeriod( selectedPeriod.getIsoDate() );
                
                dataValues.add( dataValue );
                
                
                //System.out.println( coc + " 1 --  " + dataElement  + " --  " + orgUnit + " --  " + dataCellValue  + " --  " + isoPeriod );
            }
        }
        
        dataValueSet.setDataValues( dataValues );
        
        System.out.println( "xls Template reading end time --  : " + new Date() );
        return dataValueSet;
    }
        
    
}
