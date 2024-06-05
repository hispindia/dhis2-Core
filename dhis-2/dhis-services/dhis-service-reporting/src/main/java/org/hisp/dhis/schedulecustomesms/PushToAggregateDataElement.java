package org.hisp.dhis.schedulecustomesms;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hisp.dhis.system.notification.NotificationLevel.INFO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.feedback.ErrorReport;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.scheduling.Job;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobProgress;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.system.notification.Notifier;
import org.hisp.dhis.system.util.Clock;
import org.hisp.dhis.user.UserService;
//import org.hisp.dhis.jsontree.*;
//import org.json.*;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//import org.json.simple.*;
//import org.json.simple.parser.*;

//import com.nimbusds.jose.shaded.gson.JsonParser;

//import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ParseException;

@Slf4j
@Component( "pushToAggregateDataElementJob" )
public class PushToAggregateDataElement implements Job
{
    private static final Log log = LogFactory.getLog( PushToAggregateDataElement.class );
    
    private static final String KEY_TASK = "pushToAggregateDataElementTask";
    

    
    private int categoryOptionComboId = 16;
    private int attributeoptioncomboid = 16;
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private final Notifier notifier;
    
    private SystemSettingManager systemSettingManager;
    
    private JdbcTemplate jdbcTemplate;

    private TaskScheduler taskScheduler;
            
    private UserService userService;
    
    private PeriodService periodService;
    
    public PushToAggregateDataElement( Notifier notifier, SystemSettingManager systemSettingManager,
            JdbcTemplate jdbcTemplate, TaskScheduler taskScheduler,UserService userService,
            PeriodService periodService )
    {

        checkNotNull( notifier );
        checkNotNull( systemSettingManager );
        checkNotNull( jdbcTemplate );
        checkNotNull( taskScheduler );
        checkNotNull( userService );
        checkNotNull( periodService );  
        
        this.notifier = notifier;
        this.systemSettingManager = systemSettingManager;
        this.jdbcTemplate = jdbcTemplate;
        this.taskScheduler = taskScheduler;
        this.userService = userService;
        this.periodService = periodService;
        
    }
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
    // getter
    
    Properties properties = new Properties();
    
    private String baseurl, dhis2UserName, dhis2UserPassword;
    
    public String getBaseurl()
    {
        return properties.getProperty( "baseurl" );
    }

    public String getDhis2UserName()
    {
        return properties.getProperty( "dhis2UserName" );
    }

    public String getDhis2UserPassword()
    {
        return properties.getProperty( "dhis2UserPassword" );
    }
        
    private Map<String, String> orgUnitMappingMap = new HashMap<String, String>();
    private Map<String, String> dataElementsMappingMap = new HashMap<String, String>();
    private Map<String, String> aggregatedDataElementsMappingMap = new HashMap<String, String>();
    private List<String> dataValueList = new ArrayList<>();
    
    
    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    @Override
    public JobType getJobType()
    {
        return JobType.PUSH_TO_AGGREGATE_DATAELEMENT;
    }

    @Override
    public void execute( JobConfiguration jobConfiguration, JobProgress progress  )
    {
        System.out.println("INFO: PUSH_TO_AGGREGATE_DATAELEMENT job has started at : " + new Date() +" -- " + JobType.PUSH_TO_AGGREGATE_DATAELEMENT );
        
        Clock clock = new Clock().startClock();

        clock.logTime( "Starting PUSH_TO_AGGREGATE_DATAELEMENT job " );
        notifier.notify( jobConfiguration, INFO, "Start PUSH_TO_AGGREGATE_DATAELEMENT job ", true );

        //sendMessages();
        
        notifier.notify( jobConfiguration, INFO, String.format( "%s has started", KEY_TASK ) );


        log.info( String.format( "%s has started", KEY_TASK ) );
        
        //properties = new Properties();
        
        initializeOrgUnitMap();
        initializeDataElementMap();
        initializeAggregatedDataElementMap();

        try
        {
            properties.load( new FileReader( System.getenv( "DHIS2_HOME" ) + File.separator + "dhis.conf" ) );
        } 
        catch (FileNotFoundException ex) 
        {
            // FileNotFoundException catch is optional and can be collapsed
        } catch (IOException ex) {
            
        }
        
        System.out.println(getBaseurl());
        System.out.println(properties.getProperty("dhis2UserName"));
        System.out.println(properties.getProperty("dhis2UserPassword"));
          
        try
        {
            //readJsonFromUrl("EP0hdg2Q5q5", getBaseurl() + "/api/trackedEntityInstances/FM0rl23NklS.json?program=L78QzNqadTV");
            //https://links.hispindia.org/hivtracker/api/32/analytics/dataValueSet.json?dimension=dx:VtMffWN7ZIM&dimension=ou:USER_ORGUNIT_CHILDREN&dimension=pe:THIS_MONTH&showHierarchy=false&hierarchyMeta=false&includeMetadataDetails=true&includeNumDen=true&skipRounding=false&aggregationType=LAST&completedOnly=false
            
            //readJsonFromUrl( "iZh9grkk98m", getBaseurl() + "/api/analytics/dataValueSet.json?dimension=dx:VtMffWN7ZIM&dimension=ou:USER_ORGUNIT_CHILDREN&dimension=pe:THIS_MONTH&showHierarchy=false&hierarchyMeta=false&includeMetadataDetails=true&includeNumDen=true&skipRounding=false&aggregationType=LAST&completedOnly=false");
            readJsonFromUrl();
            
            
        }
        catch ( IOException e1 )
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        System.out.println("INFO: Scheduler job has ended at : " + new Date() );
        
    }
    
    // read JsonFrom URL
    public void readJsonFromUrl() throws IOException
    {
        try 
        {
           
           ObjectMapper objectMapper = new ObjectMapper(); 
           JsonNode jsonNode = objectMapper.readTree(new File(System.getenv( "DHIS2_HOME" ) + File.separator + "indicatorAPIURL.json" )); 
           //String name = jsonNode.get("deIndicatorMapping").asText(); 
           
           Iterator<JsonNode> indicatorAPIURL = jsonNode.get("indicatorAPIURL").elements(); 
           
           while (indicatorAPIURL.hasNext())
           {
               JsonNode tempJsonIndicatorAPIURL = indicatorAPIURL.next();
               
               String apiURL = tempJsonIndicatorAPIURL.get("apiURL").asText();
               
               //String apiURL = (String) apiJobject.get("apiURL").toString(); 
               System.out.println("apiURL - " + getBaseurl() + apiURL );
               
               String withBaseUrl = getBaseurl() + apiURL;
               
               URL tempURL = new URL(withBaseUrl);
               
               //URLConnection uc = tempURL.openConnection();
               
               
               //URL url = new URL(urlStr);
               HttpURLConnection httpURLConnection = (HttpURLConnection) tempURL.openConnection();
               
               
               
               String userpass = getDhis2UserName() + ":" + getDhis2UserPassword();
               String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
               httpURLConnection.setRequestProperty ("Authorization", basicAuth);
               //InputStreamReader inputStreamReader = uc.getInputStream();
               
               //System.out.println( "  --- url -- " + url );
               
               //InputStream is = new URL(url).openStream();
               //URLConnection con = tempURL.openConnection();
               //success -- httpURLConnection.getResponseCode() -- 200
               
               //1 --- httpURLConnection.getResponseCode() -- 409
               //2 --- httpURLConnection getResponseMessage -- null
               //3 -- httpURLConnection getResponseMessage -- null
               //System.out.println( "  1 --- httpURLConnection.getResponseCode() -- " + httpURLConnection.getResponseCode() );
               //System.out.println( "  2 --- httpURLConnection getResponseMessage -- " + httpURLConnection.getResponseMessage());
               //System.out.println( "  3 -- httpURLConnection getResponseMessage -- " + httpURLConnection.getResponseMessage());
               
               /*
               if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                   
                   try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
                     String line;
                     while ((line = bufferedReader.readLine()) != null) {
                       // ... do something with line
                     }
                   }
                 } else {
                   // ... do something with unsuccessful response
                 }
               
               */
               
               // end
               
               if( httpURLConnection.getResponseCode() == 200 )
               {
                   System.out.println( " httpURLConnection -- " + httpURLConnection.getResponseCode() );
                   InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                   
                   BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                   
                   
                   StringBuilder sb = new StringBuilder();
                   String line = null;
                   try 
                   {
                       while ((line = bufferedReader.readLine()) != null) 
                       {
                           sb.append(line).append("\n");
                       }
                   } 
                   catch (IOException e) 
                   {
                   } 
                   finally 
                   {
                       try 
                       {
                           inputStreamReader.close();
                       } 
                       catch (IOException e) 
                       {
                       }
                   }
                   
                   JsonNode jsonNodeApiResponse = objectMapper.readTree( sb.toString() );
                   /*
                   String httpStatus = jsonNodeApiResponse.get("httpStatus").asText();
                   System.out.println("httpStatus - " + httpStatus );
                   String httpStatusCode = jsonNodeApiResponse.get("httpStatusCode").asText();
                   System.out.println("httpStatusCode - " + httpStatusCode );
                   String status = jsonNodeApiResponse.get("status").asText();
                   System.out.println("status - " + status );
                   */
                   
                   System.out.println("jsonNodeApiResponse Size - " + jsonNodeApiResponse.size() );
                   
                   if( jsonNodeApiResponse.size() > 0 )
                   {
                       dataValueList = new ArrayList<String>();
                       //org.apache.activemq.artemis.json.JsonArray results = jsonObject.getJsonArray("dataValues" );
                       
                       Iterator<JsonNode> results = jsonNodeApiResponse.get("dataValues").elements();
                       
                       while (results.hasNext())
                       {
                           JsonNode jobject = results.next();
                           
                           String indicator = jobject.get("dataElement").asText(); 
                           System.out.println("dataElement - " + indicator );
              
                           String isoPeriod = jobject.get("period").asText(); 
                           System.out.println("period - " + isoPeriod );
                           
                           String orgUnit = jobject.get("orgUnit").asText();   
                           System.out.println("orgUnit - " + orgUnit );
                           
                           String aggregatedValue = jobject.get("value").asText();   
                           System.out.println("value - " + aggregatedValue );
                           
                           String storedBy = jobject.get("storedBy").asText();   
                           System.out.println("storedBy - " + storedBy );
                           
                           String created = jobject.get("created").asText();   
                           System.out.println("created - " + created );
                           
                           String lastUpdated = jobject.get("lastUpdated").asText();   
                           System.out.println("lastUpdated - " + lastUpdated );
                           
                           String comment = (String) jobject.get("comment").asText();   
                           System.out.println("comment - " + comment );
                           
                           String aggregatedDE = aggregatedDataElementsMappingMap.get( orgUnit + ":" + indicator );
                           //System.out.println( " aggregatedDE :" + aggregatedDE );
                           /*
                           DataValue dataValue = new DataValue();
                           dataValue.setDataElement( aggregatedDataElement );
                           dataValue.setCategoryOptionCombo( defaultCategoryOptionCombo.getUid() );
                           dataValue.setAttributeOptionCombo( defaultCategoryOptionCombo.getUid() );
                           dataValue.setOrgUnit( orgUnit );
                           dataValue.setValue( aggregatedValue );
                           dataValue.setPeriod( isoPeriod );
                           dataValue.setStoredBy( storedBy );
                           dataValue.setCreated( created );
                           dataValue.setLastUpdated( lastUpdated );
                           dataValue.setComment( comment );
                           
                           dataValues.add( dataValue );
                           */
                           
                           int periodId = getPeriodId( isoPeriod );
                           String orgUnitId = orgUnitMappingMap.get( orgUnit );
                           String dataElementId = dataElementsMappingMap.get( aggregatedDE );
                           //System.out.println( " dataElementId :" + dataElementId );
                           
                           if( aggregatedValue != null )
                           {
                               dataValueList.add( dataElementId + ":" + periodId + ":" + orgUnitId + ":" + aggregatedValue + ":" + comment + ":" + storedBy );
                               //System.out.println( dataElementId + ":" + periodId + ":" + orgUnitId + ":" + aggregatedValue + ":" + comment + ":" + storedBy );
                               insertUpdateDataValue( dataValueList );
                           }
                           
                       }
                   }
               }
               else
               {
                   log.info( "analytics_table does not exist" );
                   System.out.println( " httpURLConnection -- " + httpURLConnection.getResponseCode() );
                   System.out.println( "analytics_table does not exist" );
                   continue;
                   
               }

           }

        } 
        catch(Exception e) 
        {
           e.printStackTrace();
        }
    }
    
    public void initializeAggregatedDataElementMap()
    {
        aggregatedDataElementsMappingMap = new HashMap<String, String>();
        
        try 
        { 
           ObjectMapper objectMapper = new ObjectMapper(); 
           JsonNode jsonNode = objectMapper.readTree(new File(System.getenv( "DHIS2_HOME" ) + File.separator + "deIndicatorMapping.json" )); 
           //String name = jsonNode.get("deIndicatorMapping").asText(); 
           
           Iterator<JsonNode> deIndicatorMapping = jsonNode.get("deIndicatorMapping").elements(); 
           
           while (deIndicatorMapping.hasNext())
           {
               //String next = iter.next();
               //System.out.println(next);
               
               JsonNode tempJsonNode = deIndicatorMapping.next();
               
               String dataElement = tempJsonNode.get("dataElement").asText();
               //System.out.println("dataElement - " + dataElement );
               String indicator = tempJsonNode.get("indicator").asText(); 
               //System.out.println("indicator - " + indicator );
               String orgUnit = tempJsonNode.get("orgUnit").asText();   
               //System.out.println("orgUnit - " + orgUnit );
               
               aggregatedDataElementsMappingMap.put( orgUnit +":" + indicator , dataElement );
           }
           
           System.out.println("Size of aggregatedDataElementsMappingMap - " + aggregatedDataElementsMappingMap.size() );
           
        } 
        catch(Exception e) 
        {
           e.printStackTrace();
        }
    }    
    
    public void initializeOrgUnitMap()
    {
        orgUnitMappingMap = new HashMap<String, String>();
        try
        {
            String query = "SELECT uid,organisationunitid from organisationunit ";
           
           
            //System.out.println( "query = " + query );
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String orgUnitUid = rs.getString( 1 );
                String orgUnitId = rs.getString( 2 );
                if( orgUnitUid != null && orgUnitId != null  )
                {
                    orgUnitMappingMap.put( orgUnitUid, orgUnitId );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        System.out.println("Size of orgUnitMappingMap - " + orgUnitMappingMap.size() );
    }
    
    public void initializeDataElementMap()
    {
        dataElementsMappingMap = new HashMap<String, String>();
        try
        {
            String query = "select uid,dataelementid from dataelement where domaintype = 'AGGREGATE' ";
           
           
            //System.out.println( "query = " + query );
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String dataElementUid = rs.getString( 1 );
                String dataElementId = rs.getString( 2 );
                if( dataElementUid != null && dataElementId != null  )
                {
                    dataElementsMappingMap.put( dataElementUid, dataElementId );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        System.out.println("Size of dataElementsMappingMap - " + dataElementsMappingMap.size() );
    }
    private  Integer getPeriodId( String isoPeriod)
    {
        Period period = new Period();
        Integer periodId = null;
        
        if( isoPeriod != null )
        {
            period = PeriodType.getPeriodFromIsoString( isoPeriod );
            period = periodService.reloadPeriod( period );
            periodId = (int) period.getId();
        }
        
        return periodId;
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
            String storedBy = getDhis2UserName();
            int count = 1;
            int slNo = 1;
            long t;
            Date d = new Date();
            t = d.getTime();
            //java.sql.Date created = new java.sql.Date( t );
            //java.sql.Date lastUpdatedDate = new java.sql.Date( t );
            
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String created = timestamp.toString();
            String lastUpdatedDate = timestamp.toString();

            String query = "";
            int insertFlag = 1;
            String insertQuery = "INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, created, lastupdated, comment, deleted ) VALUES ";
            try
            {
                for( String combinedString : dataValueList )
                {
                    String dataElementId = combinedString.split( ":" )[0];
                    String periodId = combinedString.split( ":" )[1];
                    String sourceId = combinedString.split( ":" )[2];
                    String value = combinedString.split( ":" )[3];
                    String comment = combinedString.split( ":" )[4];
                    //String storedBy = combinedString.split( ":" )[5];
                    
                    //System.out.println( slNo + " -- update Query -  " + combinedString );
                    //System.out.println( dataElementId + ":" + periodId + ":" + sourceId + ":" + value + ":" + comment + ":" + storedBy );
                    
                    query = "SELECT value FROM datavalue WHERE dataelementid = " + dataElementId + " AND categoryoptioncomboid = " + categoryOptionComboId + " AND attributeoptioncomboid = " + attributeoptioncomboid + " AND periodid = " + periodId + " AND sourceid = " + sourceId;
                    SqlRowSet sqlResultSet1 = jdbcTemplate.queryForRowSet( query );
                    if ( sqlResultSet1 != null && sqlResultSet1.next() )
                    {
                        String updateQuery = "UPDATE datavalue SET value = '" + value + "', storedby = '" + storedBy + "',lastupdated='" + lastUpdatedDate + "',comment='" + comment + "' WHERE dataelementid = " + dataElementId + " AND periodid = "
                            + periodId + " AND sourceid = " + sourceId + " AND categoryoptioncomboid = " + categoryOptionComboId + " AND attributeoptioncomboid = " + attributeoptioncomboid;

                        jdbcTemplate.update( updateQuery );
                        
                        //System.out.println(" update Query -  " + updateQuery );
                        
                        updateCount++;
                    }
                    else
                    {
                        if ( value != null && !value.trim().equals( "" ) )
                        {
                            insertQuery += "( " + dataElementId + ", " + periodId + ", " + sourceId + ", " + categoryOptionComboId +  ", " + attributeoptioncomboid + ", '" + value + "', '" + storedBy + "', '" + created + "', '" + lastUpdatedDate + "', '" + comment + "', false ), ";
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
                    System.out.println(" insert Query 1 -  " );
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

    @Override
    public ErrorReport validate()
    {
        // TODO Auto-generated method stub
        System.out.println("INFO: Error in validate at : " + new Date() );
        return null;
    }
    

}
