package org.hisp.dhis.scheduleapiintegration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * @author Mithilesh Kumar Thakur
 */
public class APIIntegration implements Runnable
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private PeriodService periodService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public static final String KEY_TASK = "scheduleAPIIntegrationTask";
    
    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    private List<Period> selectedPeriodList = new ArrayList<>();
    private Map<String, String> dataElementsMappingMap = new HashMap<String, String>();
    private Map<String, String> dataElementsMappingLabMap = new HashMap<String, String>();
    private Map<String, String> orgUnitMappingMap = new HashMap<String, String>();
    private Map<String, String> orgUnitMappingLabMap = new HashMap<String, String>();
    
    private List<String> dataValueList = new ArrayList<String>();
    private List<String> dataValueListLab = new ArrayList<String>();
    
    private int metaAttributeId = 64992555;
    private int categoryOptionComboId = 15;
    private int attributeoptioncomboid = 15;
    
    private SimpleDateFormat simpleDateFormat;

    String currentDate = "";

    String currentMonth = "";

    String currentYear = "";

    String todayDate = "";
    
    @Override
    public void run()
    {
        System.out.println( " API Integration Scheduler Started at : " + new Date() );
        
        initializeDataElementMap();
        initializeOrgUnitMap();
        dataValueList = new ArrayList<String>( );
        
        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        Date date = new Date();
        todayDate = simpleDateFormat.format( date );
        currentDate = simpleDateFormat.format( date ).split( "-" )[2];
        currentMonth = simpleDateFormat.format( date ).split( "-" )[1];
        currentYear = simpleDateFormat.format( date ).split( "-" )[0];
        
        //JSONObject json = readJsonFromUrl("http://182.156.208.43:85/faber_maharashtra/services/service_phd2");
        try
        {
            // for bioMedical
            dataValueList = new ArrayList<String>( readJsonFromUrl("http://182.156.208.43:85/faber_maharashtra/services/service_phd2") );
            insertUpdateDataValue( dataValueList );
            
            System.out.println( " API Integration Done for bioMedical -- " + dataValueList.size());
            
            // for lab
            String apiUrlForLab = "https://mahahindlabs.com/api/data_districtwise_patientdetails.php?month=" + currentMonth + "&year=" + currentYear;
            
            String isoPeriod = currentYear+currentMonth;
            Integer periodId = null;
            if( isoPeriod != null )
            {
                Period period = PeriodType.getPeriodFromIsoString( isoPeriod );
                period = periodService.reloadPeriod( period );
                periodId = period.getId();
            }
            
            dataValueListLab = new ArrayList<String>( readJsonFromUrlLab( apiUrlForLab, periodId ) );
            insertUpdateDataValue( dataValueListLab );
            System.out.println( " API Integration Done for lab -- " + dataValueListLab.size() );
            
        }
        catch ( JSONException | IOException | ParseException e1 )
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        System.out.println( " API Integration Scheduler Ended at : " + new Date() );
        
    }
    
    // read JsonFrom URL
    public List<String> readJsonFromUrl(String url) throws IOException, JSONException, ParseException 
    {
        dataValueList = new ArrayList<String>();
        InputStream is = new URL(url).openStream();
        try 
        {
          //JSON parser object to parse read file
          JSONParser jsonParser = new JSONParser();
          BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
          
          //FileReader reader = new FileReader("employees.json");
          
          Object obj = jsonParser.parse(rd);
          
          JSONArray jsonResponseList = (JSONArray) obj;
          //System.out.println(jsonResponseList);
          System.out.println(jsonResponseList.size());
          
          JSONArray outerArray = new JSONArray();
          
          outerArray = jsonResponseList;
          for (int i = 0; i < outerArray.size(); i++)
          {
              //System.out.println(outerArray.get(i));
              
              JSONObject jobject = (JSONObject) outerArray.get(i);
              
              String year = (String) jobject.get("Year");   
              //System.out.println("Year - " + year );
              
              if( year.equalsIgnoreCase( "2018-19" ) || year.equalsIgnoreCase("2019-20" ) )
              {
                  //System.out.println("Year - " + year );
                  
                  String district = (String) jobject.get("District");   
                  //System.out.println("District - " + district );
                  
                  String mon = (String) jobject.get("month");   
                  //System.out.println("month - " + mon );
                  
                  int periodId = getPeriodId( year, mon );
                  String orgUnitId = orgUnitMappingMap.get( district );
                  
                  for ( Map.Entry<String,String> de : dataElementsMappingMap.entrySet() ) 
                  {
                      if( de.getKey().equalsIgnoreCase( "Open_Calls" ))
                      {
                          String deId = de.getValue();
                          String openCalls = (String) jobject.get("Open_Calls");   
                          //System.out.println("Open_Calls - " + openCalls );
                          dataValueList.add( deId + ":" + periodId + ":" + orgUnitId + ":" + openCalls );
                      }
                      
                      if( de.getKey().equalsIgnoreCase( "Closed_Calls" ))
                      {
                          String deId = de.getValue();
                          String closedCalls = (String) jobject.get("Closed_Calls");   
                          //System.out.println("Closed_Calls - " + closedCalls );
                          dataValueList.add( deId + ":" + periodId + ":" + orgUnitId + ":" + closedCalls );
                      }
                      
                      if( de.getKey().equalsIgnoreCase( "Total_Calls" ))
                      {
                          String deId = de.getValue();
                          String totalCalls = (String) jobject.get("Total_Calls");   
                          //System.out.println("Total_Calls - " + totalCalls );
                          dataValueList.add( deId + ":" + periodId + ":" + orgUnitId + ":" + totalCalls );
                      }
                      //System.out.println("Key = " + de.getKey() +  ", Value = " + de.getValue()); 
                  }
              }
          }
      } 
      catch (FileNotFoundException e) 
      {
          e.printStackTrace();
      } 
      catch (IOException e) 
      {
          e.printStackTrace();
      }
      catch (ParseException e) 
      {
          e.printStackTrace();
      }
        
        return dataValueList;
       
    }
    
    private  String readAll(Reader rd) throws IOException 
    {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) 
        {
          sb.append((char) cp);
        }
        return sb.toString();
    }
    
    private void jsonResponseList(JSONObject employee)
    {
        //Get employee object within list
        JSONObject employeeObject = (JSONObject) employee.get("employee");
         
        //Get employee first name
        String firstName = (String) employeeObject.get("firstName");   
        System.out.println(firstName);
         
        //Get employee last name
        String lastName = (String) employeeObject.get("lastName"); 
        System.out.println(lastName);
         
        //Get employee website name
        String website = (String) employeeObject.get("website");   
        System.out.println(website);
    }
    
    
    private  Integer getPeriodId( String financialYear, String month)
    {
        Period period = new Period();
        Integer periodId = null;
        String isoPeriod = null;
        String startYear = financialYear.split("-")[0];
        String endYear =  financialYear.split("-")[0].substring(0, 2) + financialYear.split("-")[1];

        if ( month.equalsIgnoreCase( "April" ) )
        {
            isoPeriod = startYear+"04";
        }
        else if ( month.equalsIgnoreCase( "May" ) )
        {
            isoPeriod = startYear+"05";
        }
        else if ( month.equalsIgnoreCase( "June" ) )
        {
            isoPeriod = startYear+"06";
        }
        else if ( month.equalsIgnoreCase("July" ) )
        {
            isoPeriod = startYear+"07";
        }
        else if ( month.equalsIgnoreCase( "August" ) )
        {
            isoPeriod = startYear+"08";
        }
        else if ( month.equalsIgnoreCase( "September" ) )
        {
            isoPeriod = startYear+"09";
        }
        else if ( month.equalsIgnoreCase( "October" ) )
        {
            isoPeriod = startYear+"10";
        }
        else if ( month.equalsIgnoreCase( "November" ) )
        {
            isoPeriod = startYear+"11";
        }
        else if ( month.equalsIgnoreCase( "December" ) )
        {
            isoPeriod = startYear+"12";
        }
        else if ( month.equalsIgnoreCase( "January" ) )
        {
            isoPeriod = endYear+"01";
        }
        else if ( month.equalsIgnoreCase( "February" ))
        {
            isoPeriod = endYear+"02";
        }
        else if ( month.equalsIgnoreCase( "March" ) )
        {
            isoPeriod = endYear+"03";
        }
        
        if( isoPeriod != null )
        {
            period = PeriodType.getPeriodFromIsoString( isoPeriod );
            period = periodService.reloadPeriod( period );
            periodId = period.getId();
        }
        
        return periodId;
    }    
    
    public void initializeDataElementMap()
    {
        dataElementsMappingMap = new HashMap<String, String>();
        
        dataElementsMappingMap.put( "Open_Calls", "64982715" );
        dataElementsMappingMap.put( "Closed_Calls", "64915059" );
        dataElementsMappingMap.put( "Total_Calls", "64915030" );
        
        dataElementsMappingLabMap = new HashMap<String, String>();
        
        dataElementsMappingLabMap.put( "TotalPatients", "-1" );
        dataElementsMappingLabMap.put( "TATMET", "-2" );
        dataElementsMappingLabMap.put( "TATFAIL", "-3" );
        
    }
    
    public void initializeOrgUnitMap()
    {
        orgUnitMappingMap = new HashMap<String, String>();
        try
        {
            String query = " SELECT attrValue.value, orgUnitAttrValue.organisationunitid from attributevalue attrValue " +
                            " INNER JOIN organisationunitattributevalues orgUnitAttrValue ON orgUnitAttrValue.attributevalueid = attrValue.attributevalueid " +
                            " WHERE attrValue.attributeid = " + metaAttributeId +" ORDER BY attrValue.value ";
           
           
            //System.out.println( "query = " + query );
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String orgUnitName = rs.getString( 1 );
                String orgUnitId = rs.getString( 2 );
                if( orgUnitName != null && orgUnitId != null  )
                {
                    orgUnitMappingMap.put( orgUnitName, orgUnitId );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public void initializeOrgUnitLabMap()
    {
        orgUnitMappingLabMap = new HashMap<String, String>();
        try
        {
            String query = " SELECT attrValue.value, orgUnitAttrValue.organisationunitid from attributevalue attrValue " +
                            " INNER JOIN organisationunitattributevalues orgUnitAttrValue ON orgUnitAttrValue.attributevalueid = attrValue.attributevalueid " +
                            " WHERE attrValue.attributeid = " + metaAttributeId +" ORDER BY attrValue.value ";
           
           
            //System.out.println( "query = " + query );
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String orgUnitName = rs.getString( 1 );
                String orgUnitId = rs.getString( 2 );
                if( orgUnitName != null && orgUnitId != null  )
                {
                    orgUnitMappingLabMap.put( orgUnitName, orgUnitId );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
    
    // read JSON form lab URL
    public List<String> readJsonFromUrlLab( String url, Integer periodId ) throws IOException, JSONException, ParseException 
    {
        //System.out.println("API for patient -- " + url );
        dataValueListLab = new ArrayList<String>();
        InputStream is = new URL(url).openStream();
        if( periodId != null )
        {
            try 
            {
              //JSON parser object to parse read file
              JSONParser jsonParser = new JSONParser();
              
              BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                        
              JSONObject obj = (JSONObject) jsonParser.parse(rd);
              //System.out.println(obj.size());
              
              JSONArray results = (JSONArray) obj.get( "result" );

              for (int i = 0; i < results.size(); i++) 
              {
                  JSONObject jobject = (JSONObject) results.get(i);
                               
                  String districtName = (String) jobject.get("DISTNAME"); 
                  
                  String orgUnitId = orgUnitMappingLabMap.get( districtName );
                  
                  for ( Map.Entry<String,String> de : dataElementsMappingLabMap.entrySet() ) 
                  {
                      if( de.getKey().equalsIgnoreCase( "TotalPatients" ))
                      {
                          String deId = de.getValue();
                          String totalPatients = (String) jobject.get("TotalPatients");   
                          //System.out.println("Open_Calls - " + openCalls );
                          dataValueListLab.add( deId + ":" + periodId + ":" + orgUnitId + ":" + totalPatients );
                      }
                      
                      if( de.getKey().equalsIgnoreCase( "TATMET" ))
                      {
                          String deId = de.getValue();
                          String tATMET = (String) jobject.get("TATMET");   
                          //System.out.println("Closed_Calls - " + closedCalls );
                          dataValueListLab.add( deId + ":" + periodId + ":" + orgUnitId + ":" + tATMET );
                      }
                      
                      if( de.getKey().equalsIgnoreCase( "TATFAIL" ))
                      {
                          String deId = de.getValue();
                          String tATFAIL = (String) jobject.get("TATFAIL");   
                          //System.out.println("Total_Calls - " + totalCalls );
                          dataValueListLab.add( deId + ":" + periodId + ":" + orgUnitId + ":" + tATFAIL );
                      }
                      //System.out.println("Key = " + de.getKey() +  ", Value = " + de.getValue()); 
                  }
              }
          } 
          catch (FileNotFoundException e) 
          {
              e.printStackTrace();
          } 
          catch (IOException e) 
          {
              e.printStackTrace();
          }
          catch (ParseException e) 
          {
              e.printStackTrace();
          }
        }
        
        return dataValueListLab;
    }

    
    // insert dataValue
    public void insertUpdateDataValue( List<String> dataValueList )
    {
        String importStatus = "";
        Integer updateCount = 0;
        Integer insertCount = 0;
        
        System.out.println(" DataValue List Size - " + dataValueList.size() );
        if( dataValueList != null && dataValueList.size() > 0 )
        {
            String storedBy = "admin";
            int count = 1;
            int slNo = 1;
            long t;
            Date d = new Date();
            t = d.getTime();
            java.sql.Date created = new java.sql.Date( t );
            java.sql.Date lastUpdatedDate = new java.sql.Date( t );

            String query = "";
            int insertFlag = 1;
            String insertQuery = "INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, created, lastupdated, deleted ) VALUES ";
            try
            {
                for( String combinedString : dataValueList )
                {
                    String dataElementId = combinedString.split( ":" )[0];
                    String periodId = combinedString.split( ":" )[1];
                    String sourceId = combinedString.split( ":" )[2];
                    String value = combinedString.split( ":" )[3];
                    
                    //System.out.println( slNo + " -- update Query -  " + combinedString );
                    
                    query = "SELECT value FROM datavalue WHERE dataelementid = " + dataElementId + " AND categoryoptioncomboid = " + categoryOptionComboId + " AND attributeoptioncomboid = " + attributeoptioncomboid + " AND periodid = " + periodId + " AND sourceid = " + sourceId;
                    SqlRowSet sqlResultSet1 = jdbcTemplate.queryForRowSet( query );
                    if ( sqlResultSet1 != null && sqlResultSet1.next() )
                    {
                        String updateQuery = "UPDATE datavalue SET value = '" + value + "', storedby = '" + storedBy + "',lastupdated='" + lastUpdatedDate + "' WHERE dataelementid = " + dataElementId + " AND periodid = "
                            + periodId + " AND sourceid = " + sourceId + " AND categoryoptioncomboid = " + categoryOptionComboId + " AND attributeoptioncomboid = " + attributeoptioncomboid;

                        jdbcTemplate.update( updateQuery );
                        
                        //System.out.println(" update Query -  " + updateQuery );
                        
                        updateCount++;
                    }
                    else
                    {
                        if ( value != null && !value.trim().equals( "" ) )
                        {
                            insertQuery += "( " + dataElementId + ", " + periodId + ", " + sourceId + ", " + categoryOptionComboId +  ", " + attributeoptioncomboid + ", '" + value + "', '" + storedBy + "', '" + created + "', '" + lastUpdatedDate + "', false ), ";
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
                            System.out.println( " insert Query 2 -  " );
                            jdbcTemplate.update( insertQuery );
                        }

                        insertFlag = 1;

                        insertQuery = "INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, created, lastupdated, deleted ) VALUES ";
                    }

                    count++;
                    slNo++;
                }
                
                System.out.println(" Count - "  + count + " -- Insert Count : " + insertCount + "  Update Count -- " + updateCount );
                if ( insertFlag != 1 )
                {
                    insertQuery = insertQuery.substring( 0, insertQuery.length() - 2 );
                    System.out.println(" insert Query 1 -  ");
                    jdbcTemplate.update( insertQuery );
                }
                
                importStatus = "Successfully populated aggregated data : "; 
                importStatus += "<br/> Total new records : " + insertCount;
                importStatus += "<br/> Total updated records : " + updateCount;
                
                //System.out.println( importStatus );     
                
            }
            catch ( Exception e )
            {
                importStatus = "Exception occured while import, please check log for more details" + e.getMessage();
            }
        }
        
        
        System.out.println("Insert Count : " + insertCount + "  Update Count -- " + updateCount);
    }
    
    
    
}
