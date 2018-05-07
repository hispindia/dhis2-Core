// Modified By Sunakshi on 7 may 2018
package org.hisp.dhis.excelimport.importexcel.action;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.excelimport.util.ExcelImport;
import org.hisp.dhis.excelimport.util.ExcelImportService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSetStore;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.WeeklyPeriodType;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

import au.com.bytecode.opencsv.CSVReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

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

  @Autowired
  private DataElementCategoryService categoryService;
  
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

  private Integer year;

  private String org;

  public void setorg( String org )
  {
      this.org = org;
  }

  public void setYear( Integer year )
  {
      this.year = year;
  }

  private String orgUnitGroupId;

  public void setOrgUnitGroupId( String orgUnitGroupId )
  {
      this.orgUnitGroupId = orgUnitGroupId;
  }

  private String weeklyPeriodTypeName;

  private String deCodesXMLFileName = "";

  private SimpleDateFormat simpleDateFormat;

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
  
  private JdbcTemplate jdbcTemplate;

  public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
  {
      this.jdbcTemplate = jdbcTemplate;
  }
  OrganisationUnitGroup orgUnitGroup;
  OrganisationUnitGroup orgUnitGroup1;

  String orgGroup ,orgGroupUid;

  private ArrayList<OrganisationUnitGroup> organisationUnitGroups;

  private String storedBy;
  
  String splitBy = ",";
 
 



  // -------------------------------------------------------------------------
  // Action implementation
  // -------------------------------------------------------------------------

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  public String execute()
      throws Exception
  {
    
      message = "";
      totalCount = "";
      addingCount = "";
      updatingCount = "";
      int totrec = 0;
      System.out.println( "Start Time : " + new Date() );
      simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );

      String startDate = "01-01-" + year;
      String endDate = "31-12-" + year;

      // period information
      weeklyPeriodTypeName = WeeklyPeriodType.NAME;
      PeriodType periodType = periodService.getPeriodTypeByName( weeklyPeriodTypeName );
  
      storedBy = currentUserService.getCurrentUsername();
      List<ExcelImport> excelExportDesignList = new ArrayList<ExcelImport>();
      
      String fileType = fileName.substring( fileName.indexOf( '.' ) + 1, fileName.length() );
      CSVReader csvReader = new CSVReader( new FileReader( file ), ',', '\'' );
      
     
      if ( !fileType.equalsIgnoreCase( "csv" ) )
        {
            message = "The file you are trying to import is not an csv file";
  
            return SUCCESS;
        }
      
      
      List allRows = new ArrayList<String>();
      String[] row = null;
      int count = 0;

      while ( (row = csvReader.readNext()) != null )
      {
          allRows.add( row );
      }
      
       csvReader.close();

      int count1 = 0;
      int addCount = 0;
      int updateCount = 0;
      
      for( Object obj : allRows )
      {

    	  String[] oneRow = (String[]) obj;
          int noOfCols = oneRow.length;
          
          Integer organisationUnitId = getOrgUnitIdByCode( oneRow[2] );
          Integer dataElementId = getDataElementByCode( oneRow[0] );               
          Integer categoryOptionComboId = getCategoryOptionComboByUid( oneRow[3] );
          Integer attributeOptionComboId = getCategoryOptionComboByUid( oneRow[4] );
  
          List<Period> periods = new ArrayList<Period>();
          Period tempPeriod  = new Period();
          if ( oneRow[1] != null )
          {

              Period period = periodService.reloadIsoPeriod( oneRow[1] );

              if ( period != null )
              {
                  periods = new ArrayList<Period>( periodService.getPeriodsBetweenDates( periodType,
                      period.getStartDate(), period.getEndDate() ) );
              }
              
              if( periods != null && periods.size() > 0 )
              {
            	  
            	  if( periods.size() == 4 )
            	  {  
            		  tempPeriod = periods.get(1);
            		
            	  }
            	  else
            	  {
            		  tempPeriod = periods.get(0);
            	  }
            	  
              }
          }
          
          if ( oneRow[5].equalsIgnoreCase( "" ) || oneRow[5] == null || oneRow[5].equalsIgnoreCase( " " ) )
          {
        	  noOfCols++;

              continue;
          }
          
          long t;
          Date d = new Date();
          t = d.getTime();
          java.sql.Date lastUpdatedDate = new java.sql.Date( t );
          
          String comment = "false";
          String followup = "false";
          String deleted = "false";    
    	  
          if( dataElementId != null && tempPeriod != null && organisationUnitId != null && categoryOptionComboId != null && attributeOptionComboId != null)
          {
     	 
        	  Integer periodId = tempPeriod.getId();
        	  
        	  String selectQuery = "SELECT * FROM datavalue WHERE dataelementid == " + dataElementId + " AND  periodid == " + periodId + " AND sourceid == " + organisationUnitId + " AND categoryoptioncomboid == " + categoryOptionComboId + " ";
    	          	  
        	  if ( selectQuery != null )
              {
        		
        		  try
                  {  
        			  String insertQuery = "INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, lastupdated,comment,followup, created,deleted ) VALUES "
            		  +"("+dataElementId+","+periodId+","+organisationUnitId+","+categoryOptionComboId+","+attributeOptionComboId+","+oneRow[5]+",'"+storedBy+"','"+lastUpdatedDate+"','"+comment+"','"+followup+"','"+lastUpdatedDate+"','"+deleted+"');";
        			  jdbcTemplate.update( insertQuery );

        			  addCount++;	
              
                  }
                  catch ( Exception ex )
                  {
                	  String updateQuery = "UPDATE datavalue SET value = '" + oneRow[5] + "', storedby = '" + storedBy
                              + "',lastupdated='" + lastUpdatedDate + "' WHERE dataelementid = " + dataElementId
                              + " AND periodid = " + periodId + " AND sourceid = " + organisationUnitId
                              + " AND categoryoptioncomboid = " + categoryOptionComboId;
                          
                          jdbcTemplate.update( updateQuery );
                          updateCount++;
                          
                  }
              }	        

      }
      }
    
    totrec = addCount+updateCount;
    message= "The report has been imported successfully";
    totalCount = "Total records imported : "+totrec;
    addingCount = "New records added : "+addCount;
    updatingCount = "Records updated : "+updateCount;


	try
	{
	}
	catch( Exception e )
	{
	}
	finally
	{
		if( inputStream != null )
			inputStream.close();		 
	}
		
	System.out.println( "End Time : " + new Date() );

      return SUCCESS;
  }

  // getting data value using Map
  private String getAggVal( String expression, Map<String, String> aggDeMap )
  {
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
          }
          catch ( Exception e )
          {
              d = 0.0;
              resultValue = "";
          }

          resultValue = "" + (int) d;

          return resultValue;
      }
      catch ( NumberFormatException ex )
      {
          throw new RuntimeException( "Illegal DataElement id", ex );
      }
  }


  public Integer getOrgUnitIdByCode( String orgUnitCode )
  {
	  Integer organisationId = null; 
      String query = "SELECT organisationunitid FROM organisationunit WHERE  code = '" + orgUnitCode + "'";
      
      SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

      while ( rs.next() )
      {
    	  Integer ouId = rs.getInt(1);

          if ( ouId != null  )
          {
        	  organisationId = ouId;
          }
      }
      
      return organisationId;
  }
  
  public Integer getCategoryOptionComboByUid( String categoryOptionComboUid )
  {
	  Integer categoryOptionComboId = null; 
      String query = "SELECT categoryoptioncomboid FROM categoryoptioncombo WHERE  uid = '" + categoryOptionComboUid + "'";
      
      SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

      while ( rs.next() )
      {
    	  Integer cocId = rs.getInt(1);

          if ( cocId != null  )
          {
        	  categoryOptionComboId = cocId;
          }
      }
      
      return categoryOptionComboId;
  }
  public Integer getAttributeOptionComboByUid( String attributeOptionComboUid )
  {
	  Integer attributeOptionComboId = null; 
      String query = "SELECT categoryoptioncomboid FROM categoryoptioncombo WHERE  uid = '" + attributeOptionComboUid + "'";
      
      SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

      while ( rs.next() )
      {
    	  Integer aocId = rs.getInt(1);

          if ( aocId != null  )
          {
        	  attributeOptionComboId = aocId;
          }
      }
      
      return attributeOptionComboId;
  }
  public Integer getDataElementByCode( String dataElementCode )
  {
	  Integer dataElementId = null; 
      String query = "SELECT dataelementid FROM dataelement WHERE code = '" + dataElementCode + "'";
      
      SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

      while ( rs.next() )
      {
    	  Integer deId = rs.getInt(1);

          if ( deId != null  )
          {
        	  dataElementId = deId;
          }
      }
      
      return dataElementId;
  }
  
    
  
  
}

