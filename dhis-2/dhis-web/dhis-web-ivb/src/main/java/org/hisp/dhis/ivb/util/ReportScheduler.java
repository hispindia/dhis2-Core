package org.hisp.dhis.ivb.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class ReportScheduler implements Action
{
    public final static String KEY_FLAG = "Key Flag";
    
    final String OPERAND_EXPRESSION = "#\\{(\\w+)\\.?(\\w*)\\}";
    final Pattern OPERAND_PATTERN = Pattern.compile( OPERAND_EXPRESSION );
    
    //private static final String KEYFLAG_INDICATOR_ATTRIBUTE_ID = "KEYFLAG_INDICATOR_ATTRIBUTE_ID";//4
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    /*
    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }
    */
    
    private IndicatorService indicatorService;
    
    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }
    
    private LookupService lookupService;
    
    public void setLookupService( LookupService lookupService )
    {
        this.lookupService = lookupService;
    }
    
    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    private KeyFlagCalculation keyFlagCalculation;
    
    public void setKeyFlagCalculation( KeyFlagCalculation keyFlagCalculation )
    {
        this.keyFlagCalculation = keyFlagCalculation;
    }
    
    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }
    
    /*
    private KeyFlagAnalyticsService keyFlagAnalyticsService;
    
    public void setKeyFlagAnalyticsService( KeyFlagAnalyticsService keyFlagAnalyticsService )
    {
        this.keyFlagAnalyticsService = keyFlagAnalyticsService;
    }
    */
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
 
    // -------------------------------------------------------------------------
    // Action IMplementation
    // -------------------------------------------------------------------------

    public String execute() throws Exception
    {
        System.out.println("INFO: Keyflaganalystics scheduler job has started at : " + new Date() );
        
        //List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();
        
        //orgUnitList = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitsAtLevel( 3 ) );
        
        scheduledReport();
        
        /*
        
        List<DataSet> datasetList = new ArrayList<DataSet>();
        
        datasetList = new ArrayList<DataSet>( dataSetService.getAllDataSets());
        
        List<Indicator> indicators = new ArrayList<Indicator>();
        
        indicators = new ArrayList<Indicator>( indicatorService.getAllIndicators() );
        
       
        Set<Indicator> indicatorList = new HashSet<Indicator>();
        if( indicators != null && indicators.size() > 0 )
        {
            for( Indicator indicator : indicators )
            {
                List<AttributeValue> attributeValueList = new ArrayList<AttributeValue>( indicator.getAttributeValues() );
                for ( AttributeValue attributeValue : attributeValueList )
                {
                    if ( attributeValue.getAttribute().getName().equalsIgnoreCase( KEY_FLAG ) && attributeValue.getValue().equals( "true" ) )
                    {
                        indicatorList.add( indicator );
                    }
                }
            }
        }
        
        String dataSetIdsByComma = "-1";
        Collection<Integer> dataSetIds = new ArrayList<Integer>();
        if( datasetList != null && datasetList.size() > 0 )
        {
            dataSetIds = new ArrayList<Integer>( getIdentifiers( DataSet.class, datasetList ) );
            dataSetIdsByComma = getCommaDelimitedString( dataSetIds );
        }
        
        List<Indicator> indicatorList = new ArrayList<Indicator>();
        indicatorList = new ArrayList<Indicator>( getIndicatorList( dataSetIdsByComma ) );
        
         //Constant keyFlagIndicatorAttributeConstant = constantService.getConstantByName( KEYFLAG_INDICATOR_ATTRIBUTE_ID );
        */
        
        /*
        Lookup keyFlagIndicatorAttributeLookup = lookupService.getLookupByName( Lookup.KEYFLAG_INDICATOR_ATTRIBUTE_ID );
        
        List<Indicator> indicatorList = new ArrayList<Indicator>();
        indicatorList = new ArrayList<Indicator>( getKeyFlagIndicatorList( Integer.parseInt( keyFlagIndicatorAttributeLookup.getValue() ) ) );
        
        System.out.println( " OrgUnit Size : " + orgUnitList.size() +   " Indicator Size :" + indicatorList.size() );
        
        
        Integer updateCount = 0;
        Integer insertCount = 0;
        
        if( orgUnitList != null && orgUnitList.size() > 0 )
        {
            for ( OrganisationUnit organisationUnit : orgUnitList )
            {
                for ( Indicator indicator : indicatorList )
                {
                    //KeyFlagAnalytics keyFlagAnalytics = new KeyFlagAnalytics();
                    
                    //keyFlagAnalytics.setOrganisationUnit( organisationUnit );
                    //keyFlagAnalytics.setIndicator( indicator );
                    
                    System.out.println( " OrgUnit  : " + organisationUnit.getId() +   " Indicator  :" + indicator.getId() );
                    
                    String keyFlagValue = "";
                    String comment = "";
                    String source = "";
                    String user = "";
                    String period = "";
                    String color = "";
                    String deValue = "";
                    String lastUpdated = "";
                    
                    String keyIndicatorValue = "";
                    String exString = indicator.getNumerator(); 
                    
                    if(exString.contains(KeyFlagCalculation.NESTED_OPERATOR_AND) || exString.contains( KeyFlagCalculation.NESTED_OPERATOR_OR ) )
                    { 
                        keyIndicatorValue = keyFlagCalculation.getNestedKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator
                            .getUid(), organisationUnit ); 
                    }
                    else
                    {
                        keyIndicatorValue = keyFlagCalculation.getKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator
                            .getUid(), organisationUnit ); 
                    }
                    
                    try 
                    {
                        Integer factor = indicator.getIndicatorType().getFactor();
                        double keyIndicatorVal = Double.parseDouble( keyIndicatorValue );
                        keyIndicatorVal = keyIndicatorVal * factor;
                        keyIndicatorVal = Math.round(keyIndicatorVal * 100);
                        keyIndicatorVal = keyIndicatorVal/100;
                        keyIndicatorValue = ""+keyIndicatorVal;
                    } 
                    catch (Exception e) 
                    {}
                    
                    String mapKey =  indicator.getUid() + "-" + organisationUnit.getUid();
                    
                    if( keyFlagCalculation.getValueMap().get( mapKey ) != null && !keyFlagCalculation.getValueMap().get( mapKey ).equalsIgnoreCase( "" ) )
                    {
                        keyFlagValue = keyFlagCalculation.getValueMap().get( mapKey );
                    }
                    
                    if( keyFlagCalculation.getCommentMap().get( mapKey ) != null && !keyFlagCalculation.getCommentMap().get( mapKey ).equalsIgnoreCase( "" ) )
                    {
                        comment = keyFlagCalculation.getCommentMap().get( mapKey );
                    }
                    
                    if( keyFlagCalculation.getSourceMap().get( mapKey ) != null && !keyFlagCalculation.getSourceMap().get( mapKey ).equalsIgnoreCase( "" ) )
                    {
                        source = keyFlagCalculation.getSourceMap().get( mapKey );
                        
                        if( source != null && !source.equalsIgnoreCase( "" ) )
                        {
                            String[] splitedString = source.split(" ");
                            
                            user = splitedString[0].trim();
                        }
                        
                    }
                    
                    if( keyFlagCalculation.getPeriodMap().get( mapKey ) != null && !keyFlagCalculation.getPeriodMap().get( mapKey ).equalsIgnoreCase( "" ) )
                    {
                        period = keyFlagCalculation.getPeriodMap().get( mapKey );
                    }
                    
                    if( keyFlagCalculation.getColorMap().get( mapKey ) != null && !keyFlagCalculation.getColorMap().get( mapKey ).equalsIgnoreCase( "" ) )
                    {
                        color = keyFlagCalculation.getColorMap().get( mapKey );
                    }
                    
                    if( keyIndicatorValue != null && !keyIndicatorValue.equalsIgnoreCase( "" ) )
                    {
                        deValue = keyIndicatorValue;
                    }
                    
                    if( keyFlagCalculation.getLastUpdated() != null && !keyFlagCalculation.getLastUpdated().equalsIgnoreCase( "" ) )
                    {
                        lastUpdated = keyFlagCalculation.getLastUpdated();
                    }
                    
                   
                   
                    
                    long t;
                    Date d = new Date();
                    t = d.getTime();
                    java.sql.Date lastUpdatedDate = new java.sql.Date( t );
                    
                    
                    String query = "";
                    int insertFlag = 1;
                    
                    
                    
                    try
                    {
                        int count = 1;
                        
                        query = " SELECT orgunitid, indicatorid FROM keyflag_analytics WHERE orgunitid = " + organisationUnit.getId() + " AND indicatorid = " + indicator.getId();
                        
                        SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( query );
                        
                        if ( sqlResultSet != null && sqlResultSet.next() )
                        {
                            System.out.println( " Indide Update "   );
                            
                            String updateQuery = " UPDATE keyflag_analytics SET keyflagvalue = '" + keyFlagValue + "', comment = '" + comment 
                                                   + "', source = '" + source + "', user = '" + user + "', period = '" + period + "', color = '" + color
                                                   + "', devalue = '" + deValue + "', lastupdated = '" + lastUpdated + "', lastscheduled = '" + lastUpdatedDate 
                                                   + "' WHERE orgunitid = " + organisationUnit.getId() + " AND indicatorid = "  + indicator.getId();

                            jdbcTemplate.update( updateQuery );
                            
                            System.out.println( " Updated -- orgunitid - " + organisationUnit.getId() + " indicatorid - " + indicator.getId() 
                                +" keyFlagValue - " + keyFlagValue + " comment -" + comment
                                +" source - " + source + " user -" + user  +" period -" + period + " color - " + color
                                +" deValue -" + deValue + " lastUpdated -" + lastUpdated  +" lastUpdatedDate -" + lastUpdatedDate  );
                            
                            
                            updateCount++;
                        }
                        else
                        {
                            System.out.println( " Indide Insert "   );
                            
                            String insertQuery = " INSERT INTO keyflag_analytics ( orgunitid, indicatorid, keyflagvalue, comment, source, user, period, color, devalue, lastupdated, lastscheduled ) VALUES ";
                            
                            insertQuery += "( " + organisationUnit.getId() + ", " + indicator.getId() + ", '" + keyFlagValue +  "', '" + comment + "', '" 
                                                + source + "', '"  + user  + "', '" + period + "','" + color + "' ,'" 
                                                + deValue + "', '" + lastUpdated + "',' "  + lastUpdatedDate + "' ) ";
                            
                            System.out.println( " insertQuery -- " + insertQuery   );
                            
                            jdbcTemplate.update( insertQuery );
                            
                            System.out.println( " Inserted -- orgunitid- " + organisationUnit.getId() + " indicatorid - " + indicator.getId() 
                                +" keyFlagValue - " + keyFlagValue + " comment - " + comment
                                +" source - " + source + " user - " + user  +" period - " + period + " color -" + color
                                +" deValue - " + deValue + " lastUpdated - " + lastUpdated  +" lastUpdatedDate - " + lastUpdatedDate  );
                            
                            insertFlag = 2;
                            insertCount++;
                        }
                        
                    }
                    
                    catch ( Exception e )
                    {
                        System.out.println( "Exception occured while inserting/updating, please check log for more details" + e.getMessage() ) ;
                    }
                    
                    
                    System.out.println( " insertCount " + insertCount + " -- updateCount " + updateCount  );
                    
 
                    
                }
            }
        }
        
        
      */
      
      /*
        if( datasetList != null && datasetList.size() > 0 )
        {
            for( DataSet dataSet : datasetList )
            {
                for( Indicator indicator : dataSet.getIndicators() )
                {
                    List<AttributeValue> attributeValueList = new ArrayList<AttributeValue>( indicator.getAttributeValues() );
                    for ( AttributeValue attributeValue : attributeValueList )
                    {
                        if ( attributeValue.getAttribute().getName().equalsIgnoreCase( KEY_FLAG ) && attributeValue.getValue().equals( "true" ) )
                        {
                            indicaList.add( indicator );
                        }
                    }
                }
                
            }
        }
       */
        
        
        //System.out.println( " OrgUnit Size : " + orgUnitList.size() + " DataSet Size :" + datasetList.size() + " All Indicator Size :" + indicators.size() + " Indicator Size :" + indicatorList.size() );
        
        System.out.println("INFO: Keyflaganalystics scheduler job has ended at : " + new Date() );
        
        return SUCCESS;
    }
    

    // get indicator list from dataSet
    public List<Indicator> getIndicatorList( String dataSetIdsByComma )
    {
        List<Indicator> indicatorList = new ArrayList<Indicator>();
        
        try
        {
            String query = "SELECT indicatorid FROM datasetindicators " +
                            " WHERE  datasetid  in (  " + dataSetIdsByComma  + ") ";
          
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer indicatorId = rs.getInt( 1 );
                
                if ( indicatorId != null )
                {
                    Indicator indicator = indicatorService.getIndicator(indicatorId );
                    indicatorList.add( indicator );
                }
            }

            return indicatorList;
        }
        
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal DataSet Ids or Indicator Id ", e );
        }
        
    }    
     
    
    // get Key flag indicator list
    public List<Indicator> getKeyFlagIndicatorList( Integer attributeId )
    {
        List<Indicator> indicatorList = new ArrayList<Indicator>();
        
        try
        {
            String query = " SELECT indicatorattributevalues.indicatorid  FROM indicatorattributevalues " +
                           " INNER JOIN attributevalue ON attributevalue.attributevalueid = indicatorattributevalues.attributevalueid " +
                           " WHERE attributevalue.attributevalueid IN ( SELECT attributevalueid FROM indicatorattributevalues )  " +
                            " AND attributevalue.attributeid = " + attributeId + " AND attributevalue.value = 'true' ";
          
           
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer indicatorId = rs.getInt( 1 );
                
                if ( indicatorId != null )
                {
                    Indicator indicator = indicatorService.getIndicator(indicatorId );
                    indicatorList.add( indicator );
                }
            }

            return indicatorList;
        }
        
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal DataSet Ids or Indicator Id ", e );
        }
        
    }    
    
    // for scheduled report
    public void scheduledReportOld() 
    {
        System.out.println("* INFO: Keyflaganalystics scheduler job has started at : " + new Date() );
        
        List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();
        
        orgUnitList = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitsAtLevel( 3 ) );
        
        
        Lookup keyFlagIndicatorAttributeLookup = lookupService.getLookupByName( Lookup.KEYFLAG_INDICATOR_ATTRIBUTE_ID );
        
        //Constant keyFlagIndicatorAttributeConstant = constantService.getConstantByName( KEYFLAG_INDICATOR_ATTRIBUTE_ID );
        
        List<Indicator> indicatorList = new ArrayList<Indicator>();
        indicatorList = new ArrayList<Indicator>( getKeyFlagIndicatorList( Integer.parseInt( keyFlagIndicatorAttributeLookup.getValue() ) ) );
        
        //System.out.println( " OrgUnit Size : " + orgUnitList.size() +   " Indicator Size :" + indicatorList.size() );
        
        Integer updateCount = 0;
        Integer insertCount = 0;
        
        if( orgUnitList != null && orgUnitList.size() > 0 )
        {
            for ( OrganisationUnit organisationUnit : orgUnitList )
            {
                for ( Indicator indicator : indicatorList )
                {
                    //KeyFlagAnalytics keyFlagAnalytics = new KeyFlagAnalytics();
                    
                    //keyFlagAnalytics.setOrganisationUnit( organisationUnit );
                    //keyFlagAnalytics.setIndicator( indicator );
                    
                    //System.out.println( " OrgUnit  : " + organisationUnit.getId() +   " Indicator  :" + indicator.getId() );
                    
                    String keyFlagValue = "";
                    String comment = "";
                    String source = "";
                    String user = "";
                    String period = "";
                    String color = "";
                    String deValue = "";
                    String lastUpdated = "";
                    
                    String keyIndicatorValue = "";
                    String exString = indicator.getNumerator(); 
                    
                    if(exString.contains(KeyFlagCalculation.NESTED_OPERATOR_AND) || exString.contains( KeyFlagCalculation.NESTED_OPERATOR_OR ) )
                    { 
                        keyIndicatorValue = keyFlagCalculation.getNestedKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator
                            .getUid(), organisationUnit ); 
                    }
                    else
                    {
                        keyIndicatorValue = keyFlagCalculation.getKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator
                            .getUid(), organisationUnit ); 
                    }
                    
                    
                    if( keyFlagCalculation.getIsthresoldrev() == true && keyIndicatorValue != null && keyIndicatorValue.equalsIgnoreCase("Yes") )
                    {
                        keyIndicatorValue = "No";
                    }
                    else if( keyFlagCalculation.getIsthresoldrev() == true && keyIndicatorValue != null && keyIndicatorValue.equalsIgnoreCase("No") )
                    {       
                        keyIndicatorValue = "Yes";
                    }

                    try 
                    {
                        Integer factor = indicator.getIndicatorType().getFactor();
                        double keyIndicatorVal = Double.parseDouble( keyIndicatorValue );
                        keyIndicatorVal = keyIndicatorVal * factor;
                        keyIndicatorVal = Math.round(keyIndicatorVal * 100);
                        keyIndicatorVal = keyIndicatorVal/100;
                        keyIndicatorValue = ""+keyIndicatorVal;
                    } 
                    catch (Exception e) 
                    {}
                    
                    String mapKey =  indicator.getUid() + "-" + organisationUnit.getUid();
                    
                    if( keyFlagCalculation.getValueMap().get( mapKey ) != null && !keyFlagCalculation.getValueMap().get( mapKey ).equalsIgnoreCase( "" ) )
                    {
                        keyFlagValue = keyFlagCalculation.getValueMap().get( mapKey );
                        
                        keyFlagValue = keyFlagValue.trim();
                        keyFlagValue = keyFlagValue.replaceAll( "'", "\\\\\'" );
                    }
                    
                    
                    if( keyFlagCalculation.getCommentMap().get( mapKey ) != null && !keyFlagCalculation.getCommentMap().get( mapKey ).equalsIgnoreCase( "" ) )
                    {
                        comment = keyFlagCalculation.getCommentMap().get( mapKey );
                        
                        comment = comment.trim();
                        comment = comment.replaceAll( "'", "\\\\\'" );
                    }
                    
                    if( keyFlagCalculation.getSourceMap().get( mapKey ) != null && !keyFlagCalculation.getSourceMap().get( mapKey ).equalsIgnoreCase( "" ) )
                    {
                        source = keyFlagCalculation.getSourceMap().get( mapKey );
                        /*
                        if( source != null && !source.equalsIgnoreCase( "" ) )
                        {
                            String[] splitedString = source.split(" ");
                            
                            user = splitedString[0].trim();
                        }
                        */
                        
                    }
                    
                    if( keyFlagCalculation.getUserInfoMap().get( mapKey ) != null && !keyFlagCalculation.getUserInfoMap().get( mapKey ).equalsIgnoreCase( "" ) )
                    {
                        user = keyFlagCalculation.getUserInfoMap().get( mapKey );
                        user = user.trim();
                        user = user.replaceAll( "'", "\\\\\'" );
                    }
                    
                    if( keyFlagCalculation.getPeriodMap().get( mapKey ) != null && !keyFlagCalculation.getPeriodMap().get( mapKey ).equalsIgnoreCase( "" ) )
                    {
                        period = keyFlagCalculation.getPeriodMap().get( mapKey );
                    }
                    
                    if( keyFlagCalculation.getColorMap().get( mapKey ) != null && !keyFlagCalculation.getColorMap().get( mapKey ).equalsIgnoreCase( "" ) )
                    {
                        color = keyFlagCalculation.getColorMap().get( mapKey );
                    }
                    
                    if( keyIndicatorValue != null && !keyIndicatorValue.equalsIgnoreCase( "" ) )
                    {
                        deValue = keyIndicatorValue;
                        deValue = deValue.trim();
                        deValue = deValue.replaceAll( "'", "\\\\\'" );
                        if ( deValue.endsWith( ".0" ) )
                        {
                        	deValue = deValue.replace( ".0", "" );
                        }

                    }
                    
                    if( keyFlagCalculation.getLastUpdated() != null && !keyFlagCalculation.getLastUpdated().equalsIgnoreCase( "" ) )
                    {
                        lastUpdated = keyFlagCalculation.getLastUpdated();
                    }
                    
                    //keyFlagAnalytics.setDeValue( keyIndicatorValue );
                    
                    //keyFlagAnalytics.setKeyFlagValue( keyFlagCalculation.getValueMap().get( mapKey ) );
                    
                    //keyFlagAnalytics.setColor( keyFlagCalculation.getColorMap().get( mapKey ) );
                    
                    //keyFlagAnalytics.setComment( keyFlagCalculation.getCommentMap().get( mapKey ) );
                    
                    //keyFlagAnalytics.setSource( keyFlagCalculation.getSourceMap().get( mapKey ) );
                    
                    
                    /*
                    String sourceValue = keyFlagCalculation.getSourceMap().get( mapKey );
                    
                    if( sourceValue != null && !sourceValue.equalsIgnoreCase( "" ) )
                    {
                        user = sourceValue.split( "(" )[0];
                    }
                    */
                    
                    //keyFlagAnalytics.setPeriod( keyFlagCalculation.getPeriodMap().get( mapKey ) );
                    
                    //keyFlagAnalytics.setLastUpdated( keyFlagCalculation.getLastUpdated() );
                    
                    //keyFlagAnalytics.setLastScheduled( new Date() );
                    
                    //keyFlagAnalyticsService.saveRegionalReport( keyFlagAnalytics );
                    
                    long t;
                    Date d = new Date();
                    t = d.getTime();
                    java.sql.Date lastUpdatedDate = new java.sql.Date( t );
                    
                    String query = "";
                    //int insertFlag = 1;
                    
                    //String insertQuery = " INSERT INTO keyflag_analytics ( orgunitid, indicatorid, keyflagvalue, comment, source, user, period, color, devalue, lastupdated, lastscheduled ) VALUES ";
                    
                    try
                    {
                        //int count = 1;
                        
                        query = " SELECT orgunitid, indicatorid FROM keyflag_analytics WHERE orgunitid = " + organisationUnit.getId() + " AND indicatorid = " + indicator.getId();
                        
                        SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( query );
                        
                        if ( sqlResultSet != null && sqlResultSet.next() )
                        {
                            //System.out.println( " Indide Update "   );
                            
                            String updateQuery = " UPDATE keyflag_analytics SET keyflagvalue = '" + keyFlagValue + "', comment = '" + comment 
                                                   + "', source = '" + source + "', user = '" + user + "', period = '" + period + "', color = '" + color
                                                   + "', devalue = '" + deValue + "', lastupdated = '" + lastUpdated + "', lastscheduled = '" + lastUpdatedDate 
                                                   + "' WHERE orgunitid = " + organisationUnit.getId() + " AND indicatorid = "  + indicator.getId();

                            jdbcTemplate.update( updateQuery );
                            
                            /*
                            System.out.println( " Updated -- orgunitid - " + organisationUnit.getId() + " indicatorid - " + indicator.getId() 
                                +" keyFlagValue - " + keyFlagValue + " comment -" + comment
                                +" source - " + source + " user -" + user  +" period -" + period + " color - " + color
                                +" deValue -" + deValue + " lastUpdated -" + lastUpdated  +" lastUpdatedDate -" + lastUpdatedDate  );
                            
                            */
                            
                            updateCount++;
                        }
                        else
                        {
                            //System.out.println( " Indide Insert "   );
                            
                            String insertQuery = " INSERT INTO keyflag_analytics ( orgunitid, indicatorid, keyflagvalue, comment, source, user, period, color, devalue, lastupdated, lastscheduled ) VALUES ";
                            
                            insertQuery += "( " + organisationUnit.getId() + ", " + indicator.getId() + ", '" + keyFlagValue +  "', '" + comment + "', '" 
                                                + source + "', '"  + user  + "', '" + period + "','" + color + "' ,'" 
                                                + deValue + "', '" + lastUpdated + "',' "  + lastUpdatedDate + "' ) ";
                            
                            //System.out.println( " insertQuery -- " + insertQuery   );
                            
                            jdbcTemplate.update( insertQuery );
                            
                            /*
                            System.out.println( " Inserted -- orgunitid- " + organisationUnit.getId() + " indicatorid - " + indicator.getId() 
                                +" keyFlagValue - " + keyFlagValue + " comment - " + comment
                                +" source - " + source + " user - " + user  +" period - " + period + " color -" + color
                                +" deValue - " + deValue + " lastUpdated - " + lastUpdated  +" lastUpdatedDate - " + lastUpdatedDate  );
                            */
                            
                            //insertFlag = 2;
                            insertCount++;
                        }
                        
                    }
                    catch ( Exception e )
                    {
                        System.out.println( "Exception occured while inserting/updating, please check log for more details" + e.getMessage() ) ;
                    }
                    
                    
                    //System.out.println( " insertCount " + insertCount + " -- updateCount " + updateCount  );
                    
                    /*
                    
                    insertQuery += "( " + organisationUnit.getId() + ", " + indicator.getId() + ", '" + keyFlagValue +  "', '" + comment + "', '" + source + "', '"  + user
                                     + "', '" + period + "','" + color + "' ,'" 
                                     + deValue + "', '" + lastUpdated + "',' "  + lastUpdatedDate + "' ) ";
                    
                    jdbcTemplate.update( insertQuery );
                    
                    */
                    /*
                    System.out.println( " Inserted -- orgunitid " + organisationUnit.getId() + " indicatorid " + indicator.getId() 
                                         +" keyFlagValue " + keyFlagValue + " comment " + comment
                                         +" source " + source + " user " + user  +" period " + period + " color " + color
                                         +" deValue " + deValue + " lastUpdated " + lastUpdated  +" lastUpdatedDate " + lastUpdatedDate  );
                    */
                    
                    //System.out.println( " Record inserted "  );
                    
                }
            }
        }
        
        System.out.println("* INFO: Keyflaganalystics scheduler job has ended at : " + new Date() );
    }
    

    
    public void scheduledReport()
    {
        List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();
        
        orgUnitList = new ArrayList<OrganisationUnit>( organisationUnitService.getAllOrganisationUnits() );
        
        Lookup keyFlagIndicatorAttributeLookup = lookupService.getLookupByName( Lookup.KEYFLAG_INDICATOR_ATTRIBUTE_ID );
        
        List<Indicator> indicatorList = new ArrayList<Indicator>();
        indicatorList = new ArrayList<Indicator>( getKeyFlagIndicatorList( Integer.parseInt( keyFlagIndicatorAttributeLookup.getValue() ) ) );
        
        Integer updateCount = 0;
        Integer insertCount = 0;
        
        /*
        String keyFlagValue = "";
        String comment = "";
        String source = "";
        String user = "";
        String period = "";
        String color = "";
        String deValue = "";
        String lastUpdated = "";
        
        String keyIndicatorValue = "";
        */
        
        long t;
        Date d = new Date();
        t = d.getTime();
        java.sql.Date lastUpdatedDate = new java.sql.Date( t );
        
        String query = "";
        
        int insertFlag = 1;
        
        String insertQuery = " INSERT INTO keyflag_analytics ( orgunitid, indicatorid, keyflagvalue, comment, source, user, period, color, devalue, lastupdated, lastscheduled ) VALUES ";
        
        try
        {
            int count = 1;
            if( orgUnitList != null && orgUnitList.size() > 0 )
            {
                for ( OrganisationUnit organisationUnit : orgUnitList )
                {
                    if ( organisationUnit.getHierarchyLevel() != 3 )
                    {
                    	continue;
                    }

                    for ( Indicator indicator : indicatorList )
                    {
                    	 String keyFlagValue = "";
                         String comment = "";
                         String source = "";
                         String user = "";
                         String period = "";
                         String color = "";
                         String deValue = "";
                         String lastUpdated = "";
                         
                         String keyIndicatorValue = "";
                         
                        String exString = indicator.getNumerator(); 
                        
                        if(exString.contains(KeyFlagCalculation.NESTED_OPERATOR_AND) || exString.contains( KeyFlagCalculation.NESTED_OPERATOR_OR ) )
                        { 
                            keyIndicatorValue = keyFlagCalculation.getNestedKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator.getUid(), organisationUnit ); 
                        }
                        else
                        {
                            keyIndicatorValue = keyFlagCalculation.getKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator.getUid(), organisationUnit ); 
                        }
                        
                        if( keyFlagCalculation.getIsthresoldrev() == true && keyIndicatorValue != null && keyIndicatorValue.equalsIgnoreCase("Yes") )
                        {
                            keyIndicatorValue = "No";
                        }
                        else if( keyFlagCalculation.getIsthresoldrev() == true && keyIndicatorValue != null && keyIndicatorValue.equalsIgnoreCase("No") )
                        {       
                            keyIndicatorValue = "Yes";
                        }

                        try 
                        {
                            Integer factor = indicator.getIndicatorType().getFactor();
                            double keyIndicatorVal = Double.parseDouble( keyIndicatorValue );
                            keyIndicatorVal = keyIndicatorVal * factor;
                            keyIndicatorVal = Math.round(keyIndicatorVal * 100);
                            keyIndicatorVal = keyIndicatorVal/100;
                            keyIndicatorValue = ""+keyIndicatorVal;
                        } 
                        catch (Exception e) 
                        {}
                        
                        //System.out.println( indicator.getName() + " : " + organisationUnit.getName() + " : " + keyIndicatorValue );
                        
                        String mapKey =  indicator.getUid() + "-" + organisationUnit.getUid();
                        
                        if( keyFlagCalculation.getValueMap().get( mapKey ) != null && !keyFlagCalculation.getValueMap().get( mapKey ).equalsIgnoreCase( "" ) )
                        {
                            keyFlagValue = new String( keyFlagCalculation.getValueMap().get( mapKey ).getBytes("UTF-8"), "UTF-8" );
                            
                            keyFlagValue = keyFlagValue.trim();
                            keyFlagValue = keyFlagValue.replaceAll( "'", "\\\\\'" );
                        }
                        
                        if( keyFlagCalculation.getCommentMap().get( mapKey ) != null && !keyFlagCalculation.getCommentMap().get( mapKey ).equalsIgnoreCase( "" ) )
                        {
                            comment = new String( keyFlagCalculation.getCommentMap().get( mapKey ).getBytes("UTF-8"), "UTF-8" );
                            
                            comment = comment.trim();
                            //comment = comment.replaceAll( "'", "\\\\\'" );
                            comment = comment.replaceAll( "\'", "''" );
                            //comment = comment.replaceAll( "'", "''" );
                            
                            comment = new String( comment.getBytes("UTF-8"), "UTF-8" );
                        }
                        
                        if( keyFlagCalculation.getSourceMap().get( mapKey ) != null && !keyFlagCalculation.getSourceMap().get( mapKey ).equalsIgnoreCase( "" ) )
                        {
                            source = keyFlagCalculation.getSourceMap().get( mapKey );
                        }
                        
                        if( keyFlagCalculation.getUserInfoMap().get( mapKey ) != null && !keyFlagCalculation.getUserInfoMap().get( mapKey ).equalsIgnoreCase( "" ) )
                        {
                            user = keyFlagCalculation.getUserInfoMap().get( mapKey );
                            user = user.trim();
                            //user = user.replaceAll( "'", "\\\\\'" );
                            user = user.replaceAll( "'", "''" );
                        }
                        
                        if( keyFlagCalculation.getPeriodMap().get( mapKey ) != null && !keyFlagCalculation.getPeriodMap().get( mapKey ).equalsIgnoreCase( "" ) )
                        {
                            period = keyFlagCalculation.getPeriodMap().get( mapKey );
                        }
                        
                        if( keyFlagCalculation.getColorMap().get( mapKey ) != null && !keyFlagCalculation.getColorMap().get( mapKey ).equalsIgnoreCase( "" ) )
                        {
                            color = keyFlagCalculation.getColorMap().get( mapKey );
                        }
                        
                        if( keyIndicatorValue != null && !keyIndicatorValue.equalsIgnoreCase( "" ) )
                        {
                            deValue = keyIndicatorValue;
                            deValue = deValue.trim();
                            //deValue = deValue.replaceAll( "'", "\\\\\'" );
                            deValue = deValue.replaceAll( "'", "''" );
                            if ( deValue.endsWith( ".0" ) )
                            {
                            	deValue = deValue.replace( ".0", "" );
                            }
                        }
                        
                        if( keyFlagCalculation.getLastUpdated() != null && !keyFlagCalculation.getLastUpdated().equalsIgnoreCase( "" ) )
                        {
                            lastUpdated = keyFlagCalculation.getLastUpdated();
                        }
                        
                        // for updating
                        query = " SELECT orgunitid, indicatorid FROM keyflag_analytics WHERE orgunitid = " + organisationUnit.getId() + " AND indicatorid = " + indicator.getId();
                        
                        SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( query );
                        
                        if ( sqlResultSet != null && sqlResultSet.next() )
                        {
                            String updateQuery = " UPDATE keyflag_analytics SET keyflagvalue = '" + keyFlagValue + "', comment = '" + comment 
                                                   + "', source = '" + source + "', user = '" + user + "', period = '" + period + "', color = '" + color
                                                   + "', devalue = '" + deValue + "', lastupdated = '" + lastUpdated + "', lastscheduled = '" + lastUpdatedDate 
                                                   + "' WHERE orgunitid = " + organisationUnit.getId() + " AND indicatorid = "  + indicator.getId();

                            jdbcTemplate.update( updateQuery );
                            
                            updateCount++;
                        }                        
                        else
                        {
                            insertQuery += "( " + organisationUnit.getId() + ", " + indicator.getId() + ", '" + keyFlagValue +  "', '" + comment + "', '" 
                                                + source + "', '"  + user  + "', '" + period + "','" + color + "' ,'" 
                                                + deValue + "', '" + lastUpdated + "',' "  + lastUpdatedDate + "' ), ";
                            
                            insertFlag = 2;
                            insertCount++;
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

                            insertQuery = "INSERT INTO keyflag_analytics ( orgunitid, indicatorid, keyflagvalue, comment, source, user, period, color, devalue, lastupdated, lastscheduled ) VALUES ";
                        }
                        
                        count++;
                    }
                }
            }
            
            if ( insertFlag != 1 )
            {
                insertQuery = insertQuery.substring( 0, insertQuery.length() - 2 );
                insertQuery = new String( insertQuery.getBytes("UTF-8"), "UTF-8" );
                jdbcTemplate.update( insertQuery );
            }
            
        }
        catch ( Exception e )
        {
            System.out.println( "Exception occured while inserting/updating, please check log for more details" + e.getMessage() ) ;
        }
        
    }
    
       
    
    // update Single Key Flag Analytic 
    public void updateSingleKeyFlagAnalytic( OrganisationUnit organisationUnit, Indicator indicator )
    {
        //edSystem.out.println( " Inside Update KeyFlag Analytic" ) ;
        
        Integer updateCount = 0;
        Integer insertCount = 0;
        
        String keyFlagValue = "";
        String comment = "";
        String source = "";
        String user = "";
        String period = "";
        String color = "";
        String deValue = "";
        String lastUpdated = "";
        
        String keyIndicatorValue = "";
        String exString = indicator.getNumerator(); 
        
        try
        {
        	
	        
	        if(exString.contains( KeyFlagCalculation.NESTED_OPERATOR_AND) || exString.contains( KeyFlagCalculation.NESTED_OPERATOR_OR ) )
	        { 
	            keyIndicatorValue = keyFlagCalculation.getNestedKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator
	                .getUid(), organisationUnit ); 
	        }
	        else
	        {
	            keyIndicatorValue = keyFlagCalculation.getKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator
	                .getUid(), organisationUnit ); 
	        }
	        
	        
	        if( keyFlagCalculation.getIsthresoldrev() == true && keyIndicatorValue != null && keyIndicatorValue.equalsIgnoreCase("Yes") )
	        {
	            keyIndicatorValue = "No";
	        }
	        else if( keyFlagCalculation.getIsthresoldrev() == true && keyIndicatorValue != null && keyIndicatorValue.equalsIgnoreCase("No") )
	        {       
	            keyIndicatorValue = "Yes";
	        }
	
	        try 
	        {
	            Integer factor = indicator.getIndicatorType().getFactor();
	            double keyIndicatorVal = Double.parseDouble( keyIndicatorValue );
	            keyIndicatorVal = keyIndicatorVal * factor;
	            keyIndicatorVal = Math.round(keyIndicatorVal * 100);
	            keyIndicatorVal = keyIndicatorVal/100;
	            keyIndicatorValue = ""+keyIndicatorVal;
	        } 
	        catch (Exception e) 
	        {}
	        
	        String mapKey =  indicator.getUid() + "-" + organisationUnit.getUid();
	        
	        if( keyFlagCalculation.getValueMap().get( mapKey ) != null && !keyFlagCalculation.getValueMap().get( mapKey ).equalsIgnoreCase( "" ) )
	        {
	            keyFlagValue = keyFlagCalculation.getValueMap().get( mapKey );
	            
	            keyFlagValue = keyFlagValue.trim();
	            keyFlagValue = keyFlagValue.replaceAll( "'", "\\\\\'" );
	        }
	        
	        
	        if( keyFlagCalculation.getCommentMap().get( mapKey ) != null && !keyFlagCalculation.getCommentMap().get( mapKey ).equalsIgnoreCase( "" ) )
	        {
	            comment = keyFlagCalculation.getCommentMap().get( mapKey );
	            
	            comment = comment.trim();
	            comment = comment.replaceAll( "'", "\\\\\'" );
	            comment = new String( comment.getBytes("UTF-8"), "UTF-8" );
	        }
	        
	        if( keyFlagCalculation.getSourceMap().get( mapKey ) != null && !keyFlagCalculation.getSourceMap().get( mapKey ).equalsIgnoreCase( "" ) )
	        {
	            source = keyFlagCalculation.getSourceMap().get( mapKey );
	        }
	        
	        if( keyFlagCalculation.getUserInfoMap().get( mapKey ) != null && !keyFlagCalculation.getUserInfoMap().get( mapKey ).equalsIgnoreCase( "" ) )
	        {
	            user = keyFlagCalculation.getUserInfoMap().get( mapKey );
	            user = user.trim();
	            user = user.replaceAll( "'", "\\\\\'" );
	        }
	        
	        if( keyFlagCalculation.getPeriodMap().get( mapKey ) != null && !keyFlagCalculation.getPeriodMap().get( mapKey ).equalsIgnoreCase( "" ) )
	        {
	            period = keyFlagCalculation.getPeriodMap().get( mapKey );
	        }
	        
	        if( keyFlagCalculation.getColorMap().get( mapKey ) != null && !keyFlagCalculation.getColorMap().get( mapKey ).equalsIgnoreCase( "" ) )
	        {
	            color = keyFlagCalculation.getColorMap().get( mapKey );
	        }
	        
	        if( keyIndicatorValue != null && !keyIndicatorValue.equalsIgnoreCase( "" ) )
	        {
	            deValue = keyIndicatorValue;
	            deValue = deValue.trim();
	            deValue = deValue.replaceAll( "'", "\\\\\'" );
	
	        }
	        
	        if( keyFlagCalculation.getLastUpdated() != null && !keyFlagCalculation.getLastUpdated().equalsIgnoreCase( "" ) )
	        {
	            lastUpdated = keyFlagCalculation.getLastUpdated();
	        }
	        
	        long t;
	        Date d = new Date();
	        t = d.getTime();
	        java.sql.Date lastUpdatedDate = new java.sql.Date( t );
	        
	        String query = "";

            
            query = " SELECT orgunitid, indicatorid FROM keyflag_analytics WHERE orgunitid = " + organisationUnit.getId() + " AND indicatorid = " + indicator.getId();
            
            SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( query );
            
            if ( sqlResultSet != null && sqlResultSet.next() )
            {
                //System.out.println( " Indide Update "   );
                
                String updateQuery = " UPDATE keyflag_analytics SET keyflagvalue = '" + keyFlagValue + "', comment = '" + comment 
                                       + "', source = '" + source + "', user = '" + user + "', period = '" + period + "', color = '" + color
                                       + "', devalue = '" + deValue + "', lastupdated = '" + lastUpdated + "', lastscheduled = '" + lastUpdatedDate 
                                       + "' WHERE orgunitid = " + organisationUnit.getId() + " AND indicatorid = "  + indicator.getId();

                jdbcTemplate.update( updateQuery );
                
                updateCount++;
            }
            else
            {
                
                String insertQuery = " INSERT INTO keyflag_analytics ( orgunitid, indicatorid, keyflagvalue, comment, source, user, period, color, devalue, lastupdated, lastscheduled ) VALUES ";
                
                insertQuery += "( " + organisationUnit.getId() + ", " + indicator.getId() + ", '" + keyFlagValue +  "', '" + comment + "', '" 
                                    + source + "', '"  + user  + "', '" + period + "','" + color + "' ,'" 
                                    + deValue + "', '" + lastUpdated + "',' "  + lastUpdatedDate + "' ) ";
                
                
                jdbcTemplate.update( insertQuery );
                
              
                insertCount++;
            }
            
        }
        
        catch ( Exception e )
        {
            System.out.println( "Exception occured while inserting/updating, please check log for more details" + e.getMessage() ) ;
        }
        
    }
    
    
    // find dataElement from Expression   
    public Set<DataElement> getDataElementsInExpression( String expression )
    {
        Set<DataElement> dataElements = null;

        if ( expression != null )
        {
            dataElements = new HashSet<>();

            final Matcher matcher = OPERAND_PATTERN.matcher( expression );

            while ( matcher.find() )
            {
                String deUID =  matcher.group( 1 );
                
                DataElement dataElement = dataElementService.getDataElement( deUID );
                
                dataElements.add( dataElement );
            }
        }
        return dataElements;
    }
    
    

}
