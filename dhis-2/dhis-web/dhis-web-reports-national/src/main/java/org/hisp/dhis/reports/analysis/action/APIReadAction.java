package org.hisp.dhis.reports.analysis.action;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
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

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */


public class APIReadAction implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private PeriodService periodService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    private List<Period> selectedPeriodList = new ArrayList<>();
    private Map<String, String> dataElementsMappingMap = new HashMap<String, String>();
    private Map<String, String> orgUnitMappingMap = new HashMap<String, String>();
    private List<String> dataValueList = new ArrayList<String>();
    private int metaAttributeId = 64992555;
    

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        
        System.out.println("API for patient");
        
        int categoryOptionComboId = 15;
        int attributeoptioncomboid = 15;
        
        
        initializeDataElementMap();
        initializeOrgUnitMap();
        dataValueList = new ArrayList<String>();
        
        //JSONObject json = readJsonFromUrl("http://182.156.208.43:85/faber_maharashtra/services/service_phd2");
        //readJsonFromUrl("http://182.156.208.43:85/faber_maharashtra/services/service_phd2");
        
        readJsonFromUrlPatient("https://mahahindlabs.com/api/data_districtwise_patientdetails.php?month=05&year=2019");
        
        //System.out.println(json.toString());
        
        //System.out.println(json.get("id"));
        
        /*
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
                    
                    System.out.println( slNo + " -- update Query -  " + combinedString );
                    
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
        */
        
        return SUCCESS;
    }
    
    
    public void readJsonFromUrl(String url) throws IOException, JSONException, ParseException 
    {
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
              /*
              String year = (String) jobject.get("Year");   
              System.out.println("Year - " + year );
              
              String district = (String) jobject.get("District");   
              System.out.println("District - " + district );
              
              String mon = (String) jobject.get("month");   
              System.out.println("month - " + mon );
              
              String totalCalls = (String) jobject.get("Total_Calls");   
              System.out.println("Total_Calls - " + totalCalls );
              
              String openCalls = (String) jobject.get("Open_Calls");   
              System.out.println("Open_Calls - " + openCalls );
              
              String closedCalls = (String) jobject.get("Closed_Calls");   
              System.out.println("Closed_Calls - " + closedCalls );
              
              String calCloseCalls = (String) jobject.get("Cal_Close_Calls");   
              System.out.println("Cal_Close_Calls - " + calCloseCalls );
              
              
              String ppmCLoseCalls = (String) jobject.get("PPM_CLose_Calls");   
              System.out.println("PPM_CLose_Calls - " + ppmCLoseCalls );
              
              String bioEquipment = (String) jobject.get("Bio Equipment");   
              System.out.println("Bio Equipment - " + bioEquipment );
              */
              
              //String id =  jobject .getString("Id");
              
              //JSONArray innerArray = (JSONArray) outerArray.get( 0 );

              //for (int j = 0; j < innerArray.size(); j++) 
              //{
                  
                  //JSONObject innerObject = (JSONObject) innerArray.get(j);
                  
                  
                  //System.out.println(innerArray.get(j));
              //}
          }
          
          /*
          String jsonText = readAll(rd);
          JSONObject json = new JSONObject(jsonText);
          return json;
          */
          
          //Iterate over employee array
          //jsonResponseList.forEach( emp -> jsonResponseList( (JSONObject) emp ) );
          
          /*
          for( Object jsonObj : jsonResponseList )
          {
              JSONObject firstObj = (JSONObject) jsonObj;
              
              JSONObject yearObject = (JSONObject) firstObj.get("0");
              
              String year = (String) yearObject.get("Year");   
              System.out.println("Year - " + year );
              
              String district = (String) yearObject.get("District");   
              System.out.println("District - " + district );
              
              String mon = (String) yearObject.get("month");   
              System.out.println("month - " + mon );
              
              String totalCalls = (String) yearObject.get("Total_Calls");   
              System.out.println("Total_Calls - " + totalCalls );
              
              String openCalls = (String) yearObject.get("Open_Calls");   
              System.out.println("Open_Calls - " + openCalls );
              
              String closedCalls = (String) yearObject.get("Closed_Calls");   
              System.out.println("Closed_Calls - " + closedCalls );
              
              String calCloseCalls = (String) yearObject.get("Cal_Close_Calls");   
              System.out.println("Cal_Close_Calls - " + calCloseCalls );
              
              
              String ppmCLoseCalls = (String) yearObject.get("PPM_CLose_Calls");   
              System.out.println("PPM_CLose_Calls - " + ppmCLoseCalls );
              
              String bioEquipment = (String) yearObject.get("Bio Equipment");   
              System.out.println("Bio Equipment - " + bioEquipment );
          }
          */
          

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
       /*   
        } 
        finally 
        {
          is.close();
        }
        */
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

        /*
SELECT attrValue.value, orgUnitAttrValue.organisationunitid from attributevalue attrValue
INNER JOIN organisationunitattributevalues orgUnitAttrValue ON orgUnitAttrValue.attributevalueid = attrValue.attributevalueid
WHERE attrValue.attributeid = 64992555;
*/
        
        return periodId;
    }
    
    public void initializeDataElementMap()
    {
        dataElementsMappingMap = new HashMap<String, String>();
        
        dataElementsMappingMap.put( "Open_Calls", "64982715" );
        dataElementsMappingMap.put( "Closed_Calls", "64915059" );
        dataElementsMappingMap.put( "Total_Calls", "64915030" );
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
    
    public void readJsonFromUrlPatient(String url) throws IOException, JSONException, ParseException 
    {
        System.out.println("API for patient -- " + url );
        InputStream is = new URL(url).openStream();
        try 
        {
          //JSON parser object to parse read file
          JSONParser jsonParser = new JSONParser();
          
          //JSONObject jobj = (JSONObject)jsonParser.parse(inline); 
          
          BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
          
          //FileReader reader = new FileReader("employees.json");
          
          JSONObject obj = (JSONObject) jsonParser.parse(rd);
          
          //System.out.println(obj.toString());
          
          //JSONArray jsonResponseList = (JSONArray) obj;
          //System.out.println(jsonResponseList);
          System.out.println(obj.size());
          
          
          JSONArray results = (JSONArray) obj.get( "result" );
          
          
          //JSONArray arr = ((Object) obj).getJSONArray("result");
          for (int i = 0; i < results.size(); i++) 
          {
              JSONObject jobject = (JSONObject) results.get(i);
                           
              String districtName = (String) jobject.get("DISTNAME"); 
              
              System.out.println("districtName - " + districtName );
              
              String totalPatients = (String) jobject.get("TotalPatients");   
              System.out.println("totalPatients - " + totalPatients );
              
              String tATMET = (String) jobject.get("TATMET");   
              System.out.println("tATMET - " + tATMET );
              
              String tATFAIL = (String) jobject.get("TATFAIL");   
              System.out.println("TATFAIL - " + tATFAIL );
          }
          
          
          JSONArray outerArray = new JSONArray();
          
          /*
          outerArray = jsonResponseList;
          for (int i = 0; i < outerArray.size(); i++)
          {
              //System.out.println(outerArray.get(i));
              
              JSONObject jobject = (JSONObject) outerArray.get(i);
              
              
              
             
              String totalCalls = (String) jobject.get("Total_Calls");   
              System.out.println("Total_Calls - " + totalCalls );
              
              String openCalls = (String) jobject.get("Open_Calls");   
              System.out.println("Open_Calls - " + openCalls );
              
              String closedCalls = (String) jobject.get("Closed_Calls");   
              System.out.println("Closed_Calls - " + closedCalls );
              
              String calCloseCalls = (String) jobject.get("Cal_Close_Calls");   
              System.out.println("Cal_Close_Calls - " + calCloseCalls );
              
              
              String ppmCLoseCalls = (String) jobject.get("PPM_CLose_Calls");   
              System.out.println("PPM_CLose_Calls - " + ppmCLoseCalls );
              
              String bioEquipment = (String) jobject.get("Bio Equipment");   
              System.out.println("Bio Equipment - " + bioEquipment );
              
              
             
          }
          */
          /*
          String jsonText = readAll(rd);
          JSONObject json = new JSONObject(jsonText);
          return json;
          */
          
          //Iterate over employee array
          //jsonResponseList.forEach( emp -> jsonResponseList( (JSONObject) emp ) );
          
          /*
          for( Object jsonObj : jsonResponseList )
          {
              JSONObject firstObj = (JSONObject) jsonObj;
              
              JSONObject yearObject = (JSONObject) firstObj.get("0");
              
              String year = (String) yearObject.get("Year");   
              System.out.println("Year - " + year );
              
              String district = (String) yearObject.get("District");   
              System.out.println("District - " + district );
              
              String mon = (String) yearObject.get("month");   
              System.out.println("month - " + mon );
              
              String totalCalls = (String) yearObject.get("Total_Calls");   
              System.out.println("Total_Calls - " + totalCalls );
              
              String openCalls = (String) yearObject.get("Open_Calls");   
              System.out.println("Open_Calls - " + openCalls );
              
              String closedCalls = (String) yearObject.get("Closed_Calls");   
              System.out.println("Closed_Calls - " + closedCalls );
              
              String calCloseCalls = (String) yearObject.get("Cal_Close_Calls");   
              System.out.println("Cal_Close_Calls - " + calCloseCalls );
              
              
              String ppmCLoseCalls = (String) yearObject.get("PPM_CLose_Calls");   
              System.out.println("PPM_CLose_Calls - " + ppmCLoseCalls );
              
              String bioEquipment = (String) yearObject.get("Bio Equipment");   
              System.out.println("Bio Equipment - " + bioEquipment );
          }
          */
          

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
       /*   
        } 
        finally 
        {
          is.close();
        }
        */
      }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}