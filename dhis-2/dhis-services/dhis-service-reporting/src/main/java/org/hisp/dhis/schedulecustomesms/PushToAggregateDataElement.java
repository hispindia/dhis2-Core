package org.hisp.dhis.schedulecustomesms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.setting.SettingKey;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.scheduling.AbstractJob;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobType;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.user.UserService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.sun.tools.rngom.parse.compact.ParseException;

@Slf4j
@Component( "pushToAggregateDataElementJob" )
public class PushToAggregateDataElement extends AbstractJob
{
    private static final Log log = LogFactory.getLog( PushToAggregateDataElement.class );
    
    private static final String KEY_TASK = "pushToAggregateDataElementTask";
    

    
    private int categoryOptionComboId = 16;
    private int attributeoptioncomboid = 16;
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private SystemSettingManager systemSettingManager;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskScheduler taskScheduler;
            
    @Autowired
    private UserService userService;
    
    @Autowired
    private PeriodService periodService;
    
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
    public void execute( JobConfiguration jobConfiguration )
    {
        System.out.println("INFO: PUSH_TO_AGGREGATE_DATAELEMENT job has started at : " + new Date() +" -- " + JobType.PUSH_TO_AGGREGATE_DATAELEMENT );
        boolean isPushToAggeDeJobEnabled = (Boolean) systemSettingManager.getSystemSetting( SettingKey.PUSH_TO_AGGREGATE_DATAELEMENT );
        System.out.println( "is PUSH_TO_AGGREGATE_DATAELEMENT Job Enabled -- " + isPushToAggeDeJobEnabled );
        
        if ( !isPushToAggeDeJobEnabled )
        {
            log.info( String.format( "%s aborted. custom PUSH_TO_AGGREGATE_DATAELEMENT Job are disabled", KEY_TASK ) );

            return;
        }

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
        catch ( IOException | ParseException e1 )
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        System.out.println("INFO: Scheduler job has ended at : " + new Date() );
        
    }
    
    // read JsonFrom URL
    public void readJsonFromUrl() throws IOException,ParseException 
    {
        //dataValueList = new ArrayList<String>();
        //DataValueSet dataValueSet = new DataValueSet();
        //List<DataValue> dataValues = new ArrayList<>();
        //CategoryOptionCombo defaultCategoryOptionCombo = categoryService.getDefaultCategoryOptionCombo();
        //attributeOptionCombo = defaultAttributeOptionCombo.getUid();
        
        JSONParser parser = new JSONParser();
        try 
        {
           Object obj = parser.parse(new FileReader( System.getenv( "DHIS2_HOME" ) + File.separator + "indicatorAPIURL.json" ) );
           
           JSONObject apiJsonObject = (JSONObject)obj;
           //String name = (String)jsonObject.get("Name");
           //String course = (String)jsonObject.get("Course");
           JSONArray indicatorAPIURL = (JSONArray)apiJsonObject.get("indicatorAPIURL");
           
           for (int i = 0; i < indicatorAPIURL.size(); i++) 
           {
               JSONObject apiJobject = (JSONObject) indicatorAPIURL.get(i);
                            
               String apiURL = (String) apiJobject.get("apiURL"); 
               System.out.println("apiURL - " + getBaseurl() + apiURL );
               
               String withBaseUrl = getBaseurl() + apiURL;
               
               URL tempURL = new URL(withBaseUrl);
               
               //InputStream is = new URL(url).openStream();
               
               
               URLConnection uc = tempURL.openConnection();
               String userpass = getDhis2UserName() + ":" + getDhis2UserPassword();
               String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
               uc.setRequestProperty ("Authorization", basicAuth);
               //InputStreamReader inputStreamReader = uc.getInputStream();
               
               //System.out.println( "  --- url -- " + url );
               
               //InputStream is = new URL(url).openStream();
               //URLConnection con = tempURL.openConnection();
               
             
               InputStreamReader inputStreamReader = new InputStreamReader(uc.getInputStream());
             
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
              
             //System.out.println( url + "  --- bdbdbbdd -- " + inputStreamReader.toString());
             //JSON parser object to parse read file
             JSONParser jsonParser = new JSONParser();
             
             //BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
             //Reader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
             //InputStreamReader isr = new InputStreamReader(is, "UTF-8");
             
             //System.out.println( "sbbbbb -- " + bufferedReader.toString());
             
             //JSONParser jsonParser = new JSONParser();
             JSONObject jsonObject = (JSONObject) jsonParser.parse(sb.toString());
             
             //JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(new FileInputStream(is.toString())));

             //JSONArray upstreamJobInfoArray = jsonObject.getJSONArray("causes");

             //JsonElement jelement = new JsonParser().parse(isr.toString());
             
             //JsonObject  jobject = jelement.getAsJsonObject();
             
             //JSONObject obj = (JSONObject) jsonParser.parse(isr);
             
             System.out.println( " - jsonObject - " + jsonObject.size());
             
             // type casting obj to JSONObject
             JSONObject root = (JSONObject) jsonObject;
               
             // getting firstName and lastName
             
             /*
             String trackedEntityInstance = (String) root.get("trackedEntityInstance");
             String orgUnit = (String) root.get("orgUnit");
             
             
             System.out.println("trackedEntityInstance - " + trackedEntityInstance );
             System.out.println("orgUnit - " + orgUnit );
             
             int periodId = getPeriodId( "202305" );
             String orgUnitId = orgUnitMappingMap.get( orgUnit );
             String dataElementId = dataElementsMappingMap.get( aggregatedDataElement );
             dataValueList.add( dataElementId + ":" + periodId + ":" + orgUnitId + ":" + "144.4" + ":" + "[aggregated]" );
             */
             
             if( jsonObject.size() > 0 )
             {
                 dataValueList = new ArrayList<String>();
                 JSONArray results = (JSONArray) jsonObject.get( "dataValues" );
                 
                 for (int j = 0; j < results.size(); j++) 
                 {
                     JSONObject jobject = (JSONObject) results.get(j);
                                  
                     String indicator = (String) jobject.get("dataElement"); 
                     System.out.println("dataElement - " + indicator );
        
                     String isoPeriod = (String) jobject.get("period"); 
                     System.out.println("period - " + isoPeriod );
                     
                     String orgUnit = (String) jobject.get("orgUnit");   
                     System.out.println("orgUnit - " + orgUnit );
                     
                     String aggregatedValue = (String) jobject.get("value");   
                     System.out.println("value - " + aggregatedValue );
                     
                     String storedBy = (String) jobject.get("storedBy");   
                     System.out.println("storedBy - " + storedBy );
                     
                     String created = (String) jobject.get("created");   
                     System.out.println("created - " + created );
                     
                     String lastUpdated = (String) jobject.get("lastUpdated");   
                     System.out.println("lastUpdated - " + lastUpdated );
                     
                     String comment = (String) jobject.get("comment");   
                     System.out.println("comment - " + comment );
                     
                     String aggregatedDE = aggregatedDataElementsMappingMap.get( orgUnit + ":" + indicator );
                     
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
                     
                     if( aggregatedValue != null )
                     {
                         dataValueList.add( dataElementId + ":" + periodId + ":" + orgUnitId + ":" + aggregatedValue + ":" + comment + ":" + storedBy );
                         insertUpdateDataValue( dataValueList );
                     }
                 }
             }
  
           }
          
           //insertUpdateDataValue( dataValueList );
           /*
           System.out.println("Subjects:");
           Iterator iterator = subjects.iterator();
           while (iterator.hasNext()) {
              System.out.println(iterator.next());
           }
           */
           
        } 
        catch(Exception e) 
        {
           e.printStackTrace();
        }
        
        /*
        try 
        {
          
            URL tempURL = new URL(url);
            
            //InputStream is = new URL(url).openStream();
            
            
            
            URLConnection uc = tempURL.openConnection();
            String userpass = getDhis2UserName() + ":" + getDhis2UserPassword();
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
            uc.setRequestProperty ("Authorization", basicAuth);
            //InputStreamReader inputStreamReader = uc.getInputStream();
            
            System.out.println( "  --- url -- " + url );
            
            //InputStream is = new URL(url).openStream();
            //URLConnection con = tempURL.openConnection();
            
          
            InputStreamReader inputStreamReader = new InputStreamReader(uc.getInputStream());
          
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            
            
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
            } finally {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                }
            }
           
            
          //System.out.println( url + "  --- bdbdbbdd -- " + inputStreamReader.toString());
          //JSON parser object to parse read file
          JSONParser jsonParser = new JSONParser();
          
          //BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
          //Reader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
          //InputStreamReader isr = new InputStreamReader(is, "UTF-8");
          
          //System.out.println( "sbbbbb -- " + bufferedReader.toString());
          
          //JSONParser jsonParser = new JSONParser();
          JSONObject jsonObject = (JSONObject) jsonParser.parse(sb.toString());
          
          //JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(new FileInputStream(is.toString())));

          //JSONArray upstreamJobInfoArray = jsonObject.getJSONArray("causes");

          //JsonElement jelement = new JsonParser().parse(isr.toString());
          
          //JsonObject  jobject = jelement.getAsJsonObject();
          
          //JSONObject obj = (JSONObject) jsonParser.parse(isr);
          
          System.out.println( " - jsonObject - " +jsonObject.size());
          
          // typecasting obj to JSONObject
          JSONObject root = (JSONObject) jsonObject;
            
          // getting firstName and lastName
          
          /*
          String trackedEntityInstance = (String) root.get("trackedEntityInstance");
          String orgUnit = (String) root.get("orgUnit");
          
          
          System.out.println("trackedEntityInstance - " + trackedEntityInstance );
          System.out.println("orgUnit - " + orgUnit );
          
          int periodId = getPeriodId( "202305" );
          String orgUnitId = orgUnitMappingMap.get( orgUnit );
          String dataElementId = dataElementsMappingMap.get( aggregatedDataElement );
          dataValueList.add( dataElementId + ":" + periodId + ":" + orgUnitId + ":" + "144.4" + ":" + "[aggregated]" );
          */
          
          /*
          JSONArray results = (JSONArray) jsonObject.get( "dataValues" );
          
          for (int i = 0; i < results.size(); i++) 
          {
              JSONObject jobject = (JSONObject) results.get(i);
                           
              String dataElement = (String) jobject.get("dataElement"); 
              System.out.println("dataElement - " + dataElement );
 
              String isoPeriod = (String) jobject.get("period"); 
              System.out.println("period - " + isoPeriod );
              
              String orgUnit = (String) jobject.get("orgUnit");   
              System.out.println("orgUnit - " + orgUnit );
              
              String aggregatedValue = (String) jobject.get("value");   
              System.out.println("value - " + aggregatedValue );
              
              String storedBy = (String) jobject.get("storedBy");   
              System.out.println("storedBy - " + storedBy );
              
              String created = (String) jobject.get("created");   
              System.out.println("created - " + created );
              
              String lastUpdated = (String) jobject.get("lastUpdated");   
              System.out.println("lastUpdated - " + lastUpdated );
              
              String comment = (String) jobject.get("comment");   
              System.out.println("comment - " + comment );
              
              String aggregatedDE = aggregatedDataElementsMappingMap.get( orgUnit + ":" + dataElement );
              
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
              /*
              int periodId = getPeriodId( isoPeriod );
              String orgUnitId = orgUnitMappingMap.get( orgUnit );
              String dataElementId = dataElementsMappingMap.get( aggregatedDE );
              
              dataValueList.add( dataElementId + ":" + periodId + ":" + orgUnitId + ":" + aggregatedValue + ":" + comment + ":" + storedBy );
          }
         
          /*
          //userService.getUserCredentialsByUsername( getDhis2UserName()).getUser().getUsername();
          
          //System.out.println("current user - " + userService.getUserCredentialsByUsername( getDhis2UserName()).getUser().getUsername() );
          DataValue dataValue = new DataValue();
          
          dataValue.setDataElement( aggregatedDataElement );
          dataValue.setCategoryOptionCombo( defaultCategoryOptionCombo.getUid() );
          dataValue.setAttributeOptionCombo( defaultCategoryOptionCombo.getUid() );
          dataValue.setOrgUnit( "cCTQiGkKcTk" );
          dataValue.setValue( "144.4" );
          dataValue.setPeriod( "202305" );
          dataValue.setStoredBy( getDhis2UserName() );
          dataValue.setCreated( "2023-05-04" );
          dataValue.setLastUpdated( "2023-05-04" );
          dataValue.setComment( "[aggregated]" );
          
          dataValues.add( dataValue );
          
          dataValueSet.setDataValues( dataValues );
          System.out.println("aggregatedDataElement - " + defaultCategoryOptionCombo.getUid() );
          System.out.println("aggregatedDataElement - " + aggregatedDataElement );
          
          System.out.println("dataValueSet - " + dataValueSet );
          System.out.println("dataValue - " + dataValue );
          */

          
          /*
          Date now = new Date();
          Period period = new Period();
          
          period = PeriodType.getPeriodFromIsoString( "202305" );
          
          period = periodService.reloadPeriod( period );
          
          if ( period == null )
          {
              //return log( "Illegal period identifier: " + period );
              log.info( String.format(  "Illegal period identifier: " + period ) );
          }
          
          //System.out.println( "Next ISO Period Id " + nextperiod.getId() );
          
          //CategoryOptionCombo defaultAttributeOptionCombo = categoryService.getDefaultCategoryOptionCombo();
          
          DataElement dataElement = dataElementService.getDataElement( aggregatedDataElement );
          OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( orgUnit );
          
          DataValue dataValue = dataValueService.getDataValue( dataElement, period, organisationUnit, defaultCategoryOptionCombo );
          
          if ( dataValue == null )
          {                       
              dataValue = new  DataValue( dataElement, period, organisationUnit, defaultCategoryOptionCombo, defaultCategoryOptionCombo, "144.4", getDhis2UserName(), now, "[aggregated]" );
              dataValueService.addDataValue( dataValue );
              
              //System.out.println( "Data Added for " + nextperiod.getId() );
          }
          else
          {
              dataValue.setValue( "144.4" );
              dataValue.setLastUpdated( now );
              //nextdataValue.setTimestamp( now );
              dataValueService.updateDataValue( dataValue );
              
              //System.out.println( "Data updated " + nextperiod.getId() );
          }        
          */
          
          //importSummary = dataValueSetService.saveDataValueSetDataValueSet( dataValueSet );
         
          //System.out.println( "DataValue Set ImportSummary " + importSummary.toString() );

          /*
      } 

      catch (IOException e) 
      {
          e.printStackTrace();
      }
      catch (ParseException e) 
      {
          e.printStackTrace();
      }
      */
    }
    
    public void initializeAggregatedDataElementMap()
    {
        aggregatedDataElementsMappingMap = new HashMap<String, String>();
        
        JSONParser parser = new JSONParser();
        try 
        {
           Object obj = parser.parse(new FileReader( System.getenv( "DHIS2_HOME" ) + File.separator + "deIndicatorMapping.json" ) );
           
           JSONObject jsonObject = (JSONObject)obj;
           //String name = (String)jsonObject.get("Name");
           //String course = (String)jsonObject.get("Course");
           JSONArray deIndicatorMapping = (JSONArray)jsonObject.get("deIndicatorMapping");
           
           for (int i = 0; i < deIndicatorMapping.size(); i++) 
           {
               JSONObject jobject = (JSONObject) deIndicatorMapping.get(i);
                            
               String dataElement = (String) jobject.get("dataElement"); 
               //System.out.println("dataElement - " + dataElement );
  
               String indicator = (String) jobject.get("indicator"); 
               //System.out.println("indicator - " + indicator );
               
               String orgUnit = (String) jobject.get("orgUnit");   
               //System.out.println("orgUnit - " + orgUnit );
               
               aggregatedDataElementsMappingMap.put( orgUnit +":" + indicator , dataElement );
           }
           System.out.println("Size of aggregatedDataElementsMappingMap - " + aggregatedDataElementsMappingMap.size() );
          
           /*
           System.out.println("Subjects:");
           Iterator iterator = subjects.iterator();
           while (iterator.hasNext()) {
              System.out.println(iterator.next());
           }
           */
           
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
            String query = " SELECT uid,organisationunitid from organisationunit ";
           
           
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
            String query = " select uid,dataelementid from dataelement where domaintype = 'AGGREGATE' ";
           
           
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
