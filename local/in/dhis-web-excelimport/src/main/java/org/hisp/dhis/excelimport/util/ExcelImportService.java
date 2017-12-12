package org.hisp.dhis.excelimport.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hisp.dhis.config.ConfigurationService;
import org.hisp.dhis.config.Configuration_IN;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ExcelImportService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    private ConfigurationService configurationService;
    
    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    @Autowired
    private DataElementService dataElementService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // -------------------------------------------------------------------------
    // Support Methods Defination
    // -------------------------------------------------------------------------
    public List<ExcelImport> getExcelImportDesignDesign( String xmlFileName )
    {
        List<ExcelImport> deCodes = new ArrayList<ExcelImport>();

        String raFolderName = configurationService.getConfigurationByKey( Configuration_IN.KEY_REPORTFOLDER )
            .getValue();

        String path = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName + File.separator
            + xmlFileName;

        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse( new File( path ) );
            if ( doc == null )
            {
                System.out.println( "DECodes related XML file not found" );
                return null;
            }

            NodeList listOfDECodes = doc.getElementsByTagName( "de-code" );
            int totalDEcodes = listOfDECodes.getLength();

            //System.out.print( "totalDEcodes"+totalDEcodes );
            for ( int s = 0; s < totalDEcodes; s++ )
            {
                Element deCodeElement = (Element) listOfDECodes.item( s );
                NodeList textDECodeList = deCodeElement.getChildNodes();

                String expression = ((Node) textDECodeList.item( 0 )).getNodeValue().trim();
              //  System.out.println("expression -- "  +expression);
                if( expression != null && !expression.equalsIgnoreCase( "0" ))
                {
                    String dataelement = deCodeElement.getAttribute( "dataelement" );
                    String orgunit = deCodeElement.getAttribute( "orgunit" );
                    String categoryoptioncombo = deCodeElement.getAttribute( "categoryoptioncombo" );
                    String attributeoptioncombo = deCodeElement.getAttribute( "attributeoptioncombo" );
                    String comment = deCodeElement.getAttribute( "comment" );
                    String orgunitgroup = deCodeElement.getAttribute( "orgunitgroup" );
                    
                    
                   // System.out.println("orgunitgroup"+orgunitgroup);
                    int sheetno = new Integer( deCodeElement.getAttribute( "sheetno" ) );
                    int rowno = new Integer( deCodeElement.getAttribute( "rowno" ) );
                    int colno = new Integer( deCodeElement.getAttribute( "colno" ) );

                    ExcelImport exportDataDesign = new ExcelImport( dataelement, orgunit, categoryoptioncombo, attributeoptioncombo, comment, orgunitgroup, sheetno, rowno, colno,
                        expression );

                    deCodes.add( exportDataDesign );
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
        return deCodes;
    }// getDECodes end

    public String getDataelementIds( List<ExcelImport> reportDesignList )
    {
        String dataElmentIdsByComma = "-1";
        for ( ExcelImport excelImportDesign : reportDesignList )
        {
            String formula = excelImportDesign.getExpression();
            
            //System.out.println( "** formula -- " + formula );
            try
            {
               // Pattern pattern = Pattern.compile( "(\\[\\d+\\.\\d+\\])" );
                Pattern pattern = Pattern.compile( "(\\[\\w+\\.\\w+\\])" );

                Matcher matcher = pattern.matcher( formula );
                
                //System.out.println( "** matcher -- " + matcher );
               /* System.out.println( "** matcher group -- " + matcher.group() );
                System.out.println( "** matcher find -- " + matcher.find() );
                System.out.println( "** matcher to string-- " + matcher.toString() );*/
                
                StringBuffer buffer = new StringBuffer();

                while ( matcher.find() )
                {
                	//System.out.println( "** matcher find -- " + matcher.find() );
                	
                    String replaceString = matcher.group();
                    
                    replaceString = replaceString.replaceAll( "[\\[\\]]", "" );
                    replaceString = replaceString.substring( 0, replaceString.indexOf( '.' ) );
                    
                    dataElementService.getDataElement(replaceString);
                    
                    //int dataElementId = Integer.parseInt( replaceString );
                    //System.out.println( "** replaceString -- " + replaceString );
                    
                    String dataElementUid = replaceString;
                    
                    DataElement de = dataElementService.getDataElement(dataElementUid);
                    if( de != null )
                    {
                    	int dataElementId = de.getId();
                        
                        dataElmentIdsByComma += "," + dataElementId;
                        replaceString = "";
                        matcher.appendReplacement( buffer, replaceString );
                    }
                    
                    //System.out.println( "** replaceString -- " + replaceString );
                }
            }
            catch ( Exception e )
            {

            }
        }

        return dataElmentIdsByComma;
    }
    
    
    public Map<String, String> getAggDataFromDataValueTable( String orgUnitIdsByComma, String dataElmentIdsByComma,
        String periodIdsByComma )
    {
        Map<String, String> aggDeMap = new HashMap<String, String>();
        
        try
        {
            String query = "";
            query = "SELECT dataelementid,categoryoptioncomboid, SUM( cast( value as numeric) ) FROM datavalue "
                + " WHERE dataelementid IN (" + dataElmentIdsByComma + " ) AND " + " sourceid IN ("
                + orgUnitIdsByComma + " ) AND " + " periodid IN (" + periodIdsByComma
                + ") GROUP BY dataelementid,categoryoptioncomboid";
            
            //System.out.println( " SQL Query --" + query );
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer deId = rs.getInt( 1 );
                Integer optionComId = rs.getInt( 2 );
                Double aggregatedValue = rs.getDouble( 3 );
                if ( aggregatedValue != null )
                {
                    aggDeMap.put( deId + "." + optionComId, "" + aggregatedValue );
                }
            }

            return aggDeMap;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal DataElement id", e );
        }
    }
    
    public List<ExcelImport> getExcelImportDesign( String xmlFileName )
    {
        List<ExcelImport> deCodes = new ArrayList<ExcelImport>();

        String raFolderName = configurationService.getConfigurationByKey( Configuration_IN.KEY_REPORTFOLDER )
            .getValue();

        String path = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName + File.separator
            + xmlFileName;

        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse( new File( path ) );
            if ( doc == null )
            {
                System.out.println( "DECodes related XML file not found" );
                return null;
            }

            NodeList listOfDECodes = doc.getElementsByTagName( "de-code" );
            int totalDEcodes = listOfDECodes.getLength();

            //System.out.print( "totalDEcodes -- "+ totalDEcodes );
            
            for ( int s = 0; s < totalDEcodes; s++ )
            {
                Element deCodeElement = (Element) listOfDECodes.item( s );
                NodeList textDECodeList = deCodeElement.getChildNodes();

                String expression = ((Node) textDECodeList.item( 0 )).getNodeValue().trim();
                //System.out.println("expression"+expression);
                if( expression != null && !expression.equalsIgnoreCase( "0" ))
                {
                    String dataelement = deCodeElement.getAttribute( "dataelement" );
                    String categoryoptioncombo = deCodeElement.getAttribute( "categoryoptioncombo" );
                    String attributeoptioncombo = deCodeElement.getAttribute( "attributeoptioncombo" );
                    String orgunit = deCodeElement.getAttribute( "orgunit" );
                    int sheetno = new Integer( deCodeElement.getAttribute( "sheetno" ) );
                    int rowno = new Integer( deCodeElement.getAttribute( "rowno" ) );
                    int colno = new Integer( deCodeElement.getAttribute( "colno" ) );

                    ExcelImport exportDataDesign = new ExcelImport( dataelement, orgunit, categoryoptioncombo, attributeoptioncombo,sheetno, rowno, colno, expression );

                    deCodes.add( exportDataDesign );
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
        return deCodes;
    }// getDECodes end    
    
    
    
}
