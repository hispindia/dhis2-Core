// Modified By Sunakshi
// Line 218 to 328	
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

          DataElement dataElement = dataElementService.getDataElementByCode( oneRow[0] );
          DataElementCategoryOptionCombo categoryOptionCombo = categoryService.getDataElementCategoryOptionCombo( oneRow[3] );
          
          DataElementCategoryOptionCombo attributeOptionCombo = categoryService.getDataElementCategoryOptionCombo( oneRow[4] );
          
          OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnitByCode( oneRow[2] );
          
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
          
          Date now = new Date();

          if( dataElement != null && tempPeriod != null && organisationUnit != null && categoryOptionCombo != null && attributeOptionCombo != null )
          {
        	  DataValue dataValue = dataValueService.getDataValue( dataElement, tempPeriod, organisationUnit, categoryOptionCombo, attributeOptionCombo );
                      	  
              if ( dataValue == null )
              {
            	  try
                  {                 
                      dataValue = new DataValue();

                      dataValue.setDataElement( dataElement );
                      dataValue.setPeriod( tempPeriod );
                      dataValue.setSource( organisationUnit );
                      dataValue.setCategoryOptionCombo( categoryOptionCombo );
                      dataValue.setAttributeOptionCombo(attributeOptionCombo);
                      dataValue.setLastUpdated(  now );
                      dataValue.setStoredBy( storedBy );
                      dataValue.setValue(oneRow[5]);
                      dataValue.setCreated( now );
                      
                      dataValueService.addDataValue( dataValue );                
                      addCount++;
                      
                      
                  }
                  catch ( Exception ex )
                  {
                      throw new RuntimeException( "Cannot add datavalue", ex );
                  }
              } 
              else 
              {
                  try
                  {
                	  dataValue.setValue( oneRow[5] );
                	  dataValue.setLastUpdated( now );
                	  dataValue.setStoredBy( storedBy );

                      dataValueService.updateDataValue( dataValue );
                      
                      updateCount++;             
                  }
                  catch ( Exception ex )
                  {
                      throw new RuntimeException( "Cannot update datavalue", ex );
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

}

