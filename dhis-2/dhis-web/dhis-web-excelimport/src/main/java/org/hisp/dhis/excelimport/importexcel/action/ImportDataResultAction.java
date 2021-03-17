// Modified By Sunakshi
package org.hisp.dhis.excelimport.importexcel.action;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.excelimport.util.ExcelImportService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSetStore;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import au.com.bytecode.opencsv.CSVReader;

import com.opensymphony.xwork2.Action;

public class ImportDataResultAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private OrganisationUnitGroupSetStore organisationUnitGroupSetStore;

    @Autowired
    private PeriodService periodService;

    /*
    @Autowired
    private DataElementCategoryService categoryService;
   */
    
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DataElementService dataElementService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private DataValueService dataValueService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ExcelImportService excelImportService;
    
    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------
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

    private InputStream inputStream;

    public InputStream getInputStream()
    {
        return inputStream;
    }

    private String message;

    public String getMessage()
    {
        return message;
    }

    private String totalCount;

    public String getTotalCount()
    {
        return totalCount;
    }

    private String addingCount;

    public String getAddingCount()
    {
        return addingCount;
    }

    private String updatingCount;

    public String getUpdatingCount()
    {
        return updatingCount;
    }

    private String availablePeriods;

    public void setAvailablePeriods( String availablePeriods )
    {
        this.availablePeriods = availablePeriods;
    }

    private OrganisationUnitGroup orgUnitGroup;

    private OrganisationUnitGroup orgUnitGroup1;

    //private String orgGroup, orgGroupUid;

    private String storedBy;

    //private String splitBy = ",";

    private Map<String, Integer> dataElementIdCodeMap;
    private Map<String, Integer> cocIdUidMap;
    private Map<String, Integer> orgUnitIdUidMap;
    
    private Period selectedPeriod;
    
    //private Integer periodId = null;
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @SuppressWarnings( { "unchecked", "rawtypes", "resource", "unused" } )
    public String execute()
        throws Exception
    {

        message = "";
        totalCount = "";
        addingCount = "";
        updatingCount = "";
        int totrec = 0;
        
        //System.out.println( "Start Importing Time : " + new Date() + "-- " + selectedPeriodId );
       
        dataElementIdCodeMap = new HashMap<String, Integer>( excelImportService.getDataElementsIdCodeMap() );
        cocIdUidMap = new HashMap<String, Integer>( excelImportService.getCOCIdUidMap() );
        orgUnitIdUidMap = new HashMap<String, Integer>( excelImportService.getOrgUnitIdCodeMap() );
        
        storedBy = currentUserService.getCurrentUsername();

        String fileType = fileName.substring( fileName.indexOf( '.' ) + 1, fileName.length() );
        CSVReader csvReader = new CSVReader( new FileReader( file ), ',', '\'' );

        if ( !fileType.equalsIgnoreCase( "csv" ) )
        {
            message = "The file you are trying to import is not an csv file";

            return SUCCESS;
        }
        
        /*
        if( availablePeriods == null )
        {
            message = "Period missing Please select Period";

            return SUCCESS;
        }
        */
        /*
        else
        {
            Period period = periodService.reloadIsoPeriod( availablePeriods );
            
            periodId = (int) period.getId();
            System.out.println( "Start Importing Time : " + new Date() + " -- " + availablePeriods + " Period-Id -- " + periodId );
        }
        */
        
        //selectedPeriod = periodService.getPeriod( availablePeriods );
        
        List allRows = new ArrayList<String>();
        String[] row = null;
        int count = 0;

        
        while ( (row = csvReader.readNext()) != null )
        {
            allRows.add( row );
        }
        
        

        /*
        for( int i=1; (row = csvReader.readNext()) != null; i++)
        {
            allRows.add( row );
        }
        */

      
       csvReader.close();  
        
        
        
        String importStatus = "";
        //int count1 = 0;
        //int addCount = 0;
        int updateCount = 0;
        int insertCount = 0;
        
        if( allRows != null && allRows.size() > 0 )
        {
            
            Date date = new Date();
            java.sql.Timestamp lastUpdatedDate = new Timestamp(date.getTime());
            java.sql.Timestamp createdDate = new Timestamp(date.getTime());
            //Integer periodId;
            int insertFlag = 1;
            String insertQuery = "INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, created, lastupdated, deleted, comment, followup ) VALUES ";
            try
            {
                int rowCount = 0;
                //for( Object obj : allRows )
                for( int i = 1; i< allRows.size(); i++  )    
                //Iterator<String[]>rowString= allRows.iterator();
                
                //while(rowString.hasNext())
                {
                    /*
                    if( rowCount == 0 ) 
                    {
                        rowCount++;  
                        continue;
                    }
                    */
                    
                    String[] oneRow = (String[]) allRows.get( i );
                    //System.out.println( "rowCount : " + rowCount  );
                    //String[] oneRow = rowString.next();
                    int noOfCols = oneRow.length;
                    //System.out.println( " allRows -- " + allRows.size() + " oneRow[2] : " + oneRow[2] + " oneRow[3] -- " + oneRow[3] );
                    //System.out.println( " noOfCols -- " + noOfCols + " orgUnitIdUidMap : " + orgUnitIdUidMap.size() + " dataElementIdCodeMap -- " + dataElementIdCodeMap.size() );
                    //System.out.println( " oneRow -- " + oneRow + " oneRow[0] : " + oneRow[0] + " oneRow[1] -- " + oneRow[1] );
                    Integer organisationUnitId = orgUnitIdUidMap.get( oneRow[2] );
                    Integer dataElementId = dataElementIdCodeMap.get( oneRow[0] );
                    Integer periodId = (int) periodService.reloadIsoPeriod( oneRow[1] ).getId();
                    //Integer periodId = null;
                    //String isoPeriod = oneRow[1];
                    //System.out.println( "isoPeriod : " + isoPeriod + " Period-Id -- " + periodId );
                    //Period period = periodService.reloadIsoPeriod( isoPeriod );
                    
                    //System.out.println( " organisationUnitId -- " + organisationUnitId + " dataElementId : " + dataElementId + " Period-Id -- " + periodId );
                    
                    /*
                    if( period != null )
                    {
                        //message = "Period missing Please select Period";
                        periodId = (int) period.getId();
                        //continue;
                        //return SUCCESS;
                    }
                    */
                    
                    Integer categoryOptionComboId = cocIdUidMap.get( oneRow[3] );
                    Integer attributeOptionComboId = cocIdUidMap.get( oneRow[4] );

                    //Integer periodId = excelImportService.getSecondWeekPeriodId( oneRow[1] );
                    
                    /*
                    if ( oneRow[5].equalsIgnoreCase( "" ) || oneRow[5] == null || oneRow[5].equalsIgnoreCase( " " ) )
                    {
                        noOfCols++;

                        continue;
                    }
                    */
                    

                    if( dataElementId != null && periodId != null && organisationUnitId != null  && categoryOptionComboId != null && attributeOptionComboId != null )
                    {
                        String selectQuery = "SELECT value FROM datavalue WHERE dataelementid = " + dataElementId
                            + " AND  periodid = " + periodId + " AND sourceid = " + organisationUnitId
                            + " AND categoryoptioncomboid = " + categoryOptionComboId + " AND attributeoptioncomboid = " + attributeOptionComboId + " ";
                        //System.out.println( "selectQuery : " + selectQuery  );
                        SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( selectQuery );

                        if ( sqlResultSet != null && sqlResultSet.next() )
                        {
                            String updateQuery = "UPDATE datavalue SET value = '" + oneRow[5] + "', storedby = '" + storedBy + "',lastupdated='" + lastUpdatedDate + "' WHERE dataelementid = " + dataElementId + " AND periodid = "
                                + periodId + " AND sourceid = " + organisationUnitId + " AND categoryoptioncomboid = " + categoryOptionComboId + " AND attributeoptioncomboid = " + attributeOptionComboId;
                            //System.out.println( "updateQuery : " + updateQuery  );
                            jdbcTemplate.update( updateQuery );
                            
                            updateCount++;
                        }
                        else
                        {
                            if ( oneRow[5] != null && !oneRow[5].trim().equals( "" ) )
                            {
                                insertQuery += "( " + dataElementId + ", " + periodId + ", " + organisationUnitId + ", " + categoryOptionComboId +  ", " + attributeOptionComboId + ", '" + oneRow[5] + "', '" + storedBy + "', '" + createdDate + "', '" + lastUpdatedDate + "', false, false, false ), ";
                                insertFlag = 2;
                                insertCount++;
                            }
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

                            insertQuery = "INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, created, lastupdated, deleted, comment, followup ) VALUES ";
                        }

                        count++;
                    }
                    /*
                    else
                    {
                        continue;
                    }
                    */
                    rowCount++;
                }
                
                if ( insertFlag != 1 )
                {
                    insertQuery = insertQuery.substring( 0, insertQuery.length() - 2 );
                    
                    jdbcTemplate.update( insertQuery );
                }
                
                totrec = insertCount + updateCount;
                importStatus = "Successfully populated aggregated data : "; 
                importStatus += "<br/> Total new records : " + insertCount;
                importStatus += "<br/> Total updated records : " + updateCount;
                importStatus += "<br/> Total records : " + totrec;
                
                //totrec = insertCount + updateCount;
                message = "The report has been imported successfully";
                totalCount = "Total records imported : " + totrec;
                addingCount = "New records added : " + insertCount;
                updatingCount = "Records updated : " + updateCount;
                
                System.out.println( importStatus );     
                
            }
            catch ( Exception e )
            {
                importStatus = "Exception occured while import, please check log for more details" + e.getMessage();
            }
            
        }
        
        try
        {
        }
        catch ( Exception e )
        {
        }
        finally
        {
            if ( inputStream != null )
            {
                inputStream.close();
            }
        }

        System.out.println( "End Importing Time : " + new Date() );

        return SUCCESS;
    }

}
