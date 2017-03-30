package org.hisp.dhis.ivb.util;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AggregationManager
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private DataElementCategoryService dataElementCategoryService;

    public void setDataElementCategoryService( DataElementCategoryService dataElementCategoryService )
    {
        this.dataElementCategoryService = dataElementCategoryService;
    }

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    private ExpressionService expressionService;

    public void setExpressionService( ExpressionService expressionService )
    {
        this.expressionService = expressionService;
    }

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }

    private KeyFlagCalculation keyFlagCalculation;
    
    public void setKeyFlagCalculation( KeyFlagCalculation keyFlagCalculation )
    {
        this.keyFlagCalculation = keyFlagCalculation;
    }

    // -------------------------------------------------------------------------
    //
    // -------------------------------------------------------------------------

    public String prepareQueryForByOption( Integer dataElementId, String optionValue )
    {
        String query = "SELECT dv.sourceid, COUNT(*) FROM datavalue dv "
            + " INNER JOIN period p ON dv.periodid = p.periodid " + " WHERE "
            + " CONCAT(dv.sourceid,\",\",dv.dataelementid,\",\",p.startdate) " + " IN ( "
            + " SELECT CONCAT( sourceid,\",\",dataelementid,\",\",MAX(period.startdate) ) FROM datavalue "
            + " INNER JOIN period ON datavalue.periodid = period.periodid " + " WHERE " + " sourceid IN ("
            + Lookup.ORGUNITID_BY_COMMA + ") AND " + " dataelementid IN (" + dataElementId + ") AND "
            + " period.startdate <= '" + Lookup.CURRENT_PERIOD_ENDDATE + "'" + " GROUP BY sourceid,dataelementid"
            + " ) " + " AND VALUE LIKE '" + optionValue + "'" + " GROUP BY dv.sourceid";

        return query;
    }

    public String prepareQueryForByOptionScore( Integer dataElementId )
    {
        String query = "SELECT dv.sourceid, VALUE, osm.optionvalue, osm.sort_order FROM datavalue dv "
            + " INNER JOIN period p ON dv.periodid = p.periodid "
            + " INNER JOIN dataelement de ON dv.dataelementid = de.dataelementid "
            + " INNER JOIN optionset os ON de.optionsetid = os.optionsetid "
            + " INNER JOIN optionsetmembers osm ON dv.value = osm.optionvalue " + " WHERE "
            + " CONCAT(dv.sourceid,\",\",dv.dataelementid,\",\",p.startdate) " + " IN ( "
            + " SELECT CONCAT( sourceid,\",\",dataelementid,\",\",MAX(period.startdate) ) FROM datavalue "
            + " INNER JOIN period ON datavalue.periodid = period.periodid " + " WHERE " + " sourceid IN ("
            + Lookup.ORGUNITID_BY_COMMA + ") AND " + " dataelementid IN (" + dataElementId + ") AND "
            + " period.startdate <= '" + Lookup.CURRENT_PERIOD_ENDDATE + "'" + " GROUP BY sourceid,dataelementid"
            + " ) " + "AND osm.optionsetid = de.optionsetid  " + " GROUP BY dv.sourceid";

        return query;
    }

    public Map<String, Integer> calculateByOptionCount( Period period, DataElement dataElement,
        Set<OrganisationUnit> orgUnits, String query )
    {
        Map<String, Integer> aggregationResultMap = new HashMap<String, Integer>();

        try
        {
            Collection<Integer> orgUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnits ) );
            String orgUnitIdsByComma = getCommaDelimitedString( orgUnitIds );

            query = query.replace( Lookup.ORGUNITID_BY_COMMA, orgUnitIdsByComma );

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );

            query = query.replace( Lookup.CURRENT_PERIOD_ENDDATE, simpleDateFormat.format( period.getEndDate() ) );

            System.out.println( query );

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            while ( rs.next() )
            {
                Integer orgUnitId = rs.getInt( 1 );
                Integer countValue = rs.getInt( 2 );
                aggregationResultMap.put( orgUnitId + ":" + dataElement.getId(), countValue );
            }
        }
        catch ( Exception e )
        {
            System.out.println( "Exception :" + e.getMessage() );
        }

        return aggregationResultMap;
    }

    public Map<String, Integer> calculateByOptionScore( Period period, DataElement dataElement,
        Set<OrganisationUnit> orgUnits, String query )
    {
        Map<String, Integer> aggregationResultMap = new HashMap<String, Integer>();

        try
        {
            Collection<Integer> orgUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnits ) );
            String orgUnitIdsByComma = getCommaDelimitedString( orgUnitIds );

            query = query.replace( Lookup.ORGUNITID_BY_COMMA, orgUnitIdsByComma );

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );

            query = query.replace( Lookup.CURRENT_PERIOD_ENDDATE, simpleDateFormat.format( period.getEndDate() ) );

            System.out.println( query );

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            while ( rs.next() )
            {
                Integer orgUnitId = rs.getInt( 1 );
                Integer scoreValue = rs.getInt( 4 );
                aggregationResultMap.put( orgUnitId + ":" + dataElement.getId(), scoreValue );
            }
        }
        catch ( Exception e )
        {
            System.out.println( "Exception :" + e.getMessage() );
        }

        return aggregationResultMap;
    }

    public String importData( Map<String, Integer> aggregationResultMap, Period period )
    {
        String importStatus = "";

        Integer updateCount = 0;
        Integer insertCount = 0;

        String storedBy = currentUserService.getCurrentUsername();
        if ( storedBy == null )
        {
            storedBy = "[unknown]";
        }

        long t;
        Date d = new Date();
        t = d.getTime();
        java.sql.Date lastUpdatedDate = new java.sql.Date( t );

        String query = "";
        int insertFlag = 1;
        String insertQuery = "INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, lastupdated ) VALUES ";

        try
        {
            int count = 1;
            for ( String cellKey : aggregationResultMap.keySet() )
            {
                // Orgunit
                String[] oneRow = cellKey.split( ":" );
                Integer orgUnitId = Integer.parseInt( oneRow[0] );
                Integer deId = Integer.parseInt( oneRow[1] );
                Integer deCOCId = dataElementCategoryService.getDefaultDataElementCategoryOptionCombo().getId();
                Integer periodId = period.getId();
                String value = aggregationResultMap.get( cellKey ) + "";

                query = "SELECT value FROM datavalue WHERE dataelementid = " + deId + " AND categoryoptioncomboid = "
                    + deCOCId + " AND periodid = " + periodId + " AND sourceid = " + orgUnitId;
                SqlRowSet sqlResultSet1 = jdbcTemplate.queryForRowSet( query );
                if ( sqlResultSet1 != null && sqlResultSet1.next() )
                {
                    String updateQuery = "UPDATE datavalue SET value = '" + value + "', storedby = '" + storedBy
                        + "',lastupdated='" + lastUpdatedDate + "' WHERE dataelementid = " + deId + " AND periodid = "
                        + periodId + " AND sourceid = " + orgUnitId + " AND categoryoptioncomboid = " + deCOCId;

                    jdbcTemplate.update( updateQuery );
                    updateCount++;
                }
                else
                {
                    if ( value != null && !value.trim().equals( "" ) )
                    {
                        insertQuery += "( " + deId + ", " + periodId + ", " + orgUnitId + ", " + deCOCId + ", "
                            + deCOCId + ", '" + value + "', '" + storedBy + "', '" + lastUpdatedDate + "'), ";
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

                    insertQuery = "INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, lastupdated ) VALUES ";
                }

                count++;
            }

            if ( insertFlag != 1 )
            {
                insertQuery = insertQuery.substring( 0, insertQuery.length() - 2 );
                jdbcTemplate.update( insertQuery );
            }

           // importStatus = "Successfully populated aggregated data for the period : " + period.getStartDateString();
          //  importStatus += "<br/> Total new records : " + insertCount;
         //   importStatus += "<br/> Total updated records : " + updateCount;
            importStatus = "Successfully populated aggregated data " ;
            importStatus += "<br/>";
            importStatus += "<br/>";
        }
        catch ( Exception e )
        {
            importStatus = "Exception occured while import, please check log for more details" + e.getMessage();
        }

        return importStatus;
    }

    // ------------------------------------------------------------------------------------------------------------------------
    // KEY FLAG SCORE CALCULATION
    // ------------------------------------------------------------------------------------------------------------------------

    public Map<Indicator, Map<OrganisationUnit, Integer>> calculateKeyFlagScore( Period period, DataElement dataElement, Set<OrganisationUnit> orgUnits )
    {

        Map<Indicator, Map<OrganisationUnit, Integer>> individualKeyFlagResultMap = new HashMap<Indicator, Map<OrganisationUnit, Integer>>();

        Set<Indicator> indicators = new HashSet<Indicator>( indicatorService.getAllIndicators() );

        Iterator<Indicator> iterator = indicators.iterator();
        while ( iterator.hasNext() )
        {
            Indicator indicator = iterator.next();

            int flag = 1;
            Set<AttributeValue> attributeValues = new HashSet<AttributeValue>( indicator.getAttributeValues() );

            for ( AttributeValue attributeValue : attributeValues )
            {
                if ( attributeValue.getAttribute().getName().equalsIgnoreCase( KeyFlagCalculation.KEY_FLAG ) )
                {
                    flag = 2;
                    break;
                }                
            }

            if ( flag == 2 )
            {
                individualKeyFlagResultMap.put( indicator, calculateKeyFlagCount( period, dataElement, orgUnits, indicator ) );
            }
        }

        return individualKeyFlagResultMap;
    }

    // ------------------------------------------------------------------------------------------------------------------------
    // KEY FLAG CALCULATION
    // ------------------------------------------------------------------------------------------------------------------------
    
    public Map<OrganisationUnit, Integer> calculateKeyFlagCount( Period period, DataElement dataElement, Set<OrganisationUnit> orgUnits, Indicator keyFlagIndicator )
    {
        Map<OrganisationUnit, Integer> aggregationResultMap = new HashMap<OrganisationUnit, Integer>();

        for ( OrganisationUnit orgUnit : orgUnits )
        {
            String exString = keyFlagIndicator.getNumerator();
            if ( exString.contains( KeyFlagCalculation.NESTED_OPERATOR_AND ) || exString.contains( KeyFlagCalculation.NESTED_OPERATOR_OR ) )
            {
                keyFlagCalculation.getNestedKeyIndicatorValueWithThresoldValue( keyFlagIndicator.getNumerator(), keyFlagIndicator.getUid(), orgUnit );
            }
            else
            {
                keyFlagCalculation.getKeyIndicatorValueWithThresoldValue( keyFlagIndicator.getNumerator(), keyFlagIndicator.getUid(), orgUnit );
            }
            
            Map<String, String> colorMap = keyFlagCalculation.getColorMap();
            
            String keyFlagColor = colorMap.get( keyFlagIndicator.getUid() + "-" + orgUnit.getUid() );
            
            if ( keyFlagColor == null || keyFlagColor.trim().equals( "" )  || keyFlagColor.equals( KeyFlagCalculation.KEYFLAG_GREY ) )
            {
                aggregationResultMap.put( orgUnit, 2 );
            }
            else if ( keyFlagColor.equals( KeyFlagCalculation.KEYFLAG_RED ) )
            {
                aggregationResultMap.put( orgUnit, 1 );
            }
            else if( keyFlagColor.equals( KeyFlagCalculation.KEYFLAG_GREEN ) )
            {
                aggregationResultMap.put( orgUnit, 3 );
            }
            else
            {
                aggregationResultMap.put( orgUnit, 2 );
            }
        }

        return aggregationResultMap;

    }

}
