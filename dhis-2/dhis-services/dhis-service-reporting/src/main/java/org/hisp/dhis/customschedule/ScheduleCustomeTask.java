package org.hisp.dhis.customschedule;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodStore;
import org.hisp.dhis.period.PeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * @author Mithilesh Kumar Thakur
 */
public class ScheduleCustomeTask implements Runnable
{
    private final String  CASH_FLOW_DATASET_ID = "CASH_FLOW_DATASET_ID";
    private final String  CASH_FLOW_RATE_DE_GROUP_ID = "CASH_FLOW_RATE_DE_GROUP_ID";  
 
    public static final String KEY_TASK = "scheduleCustomeTask";
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private PeriodService periodService;
    
    @Autowired
    private PeriodStore periodStore;
    
    @Autowired
    protected ConstantService constantService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private SimpleDateFormat simpleDateFormat;
    
    @Override
    public void run()
    {
        System.out.println("INFO: scheduler job has started at : " + new Date() );
        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        LocalDate today = LocalDate.now();
        //System.out.println("First day: " + today.withDayOfMonth(1));
        //System.out.println("Last day: " + today.withDayOfMonth(today.lengthOfMonth()));
        
        LocalDate startDateOfCurrentMonth = today.withDayOfMonth(1);
        LocalDate endDateOfCurrentMonth = today.withDayOfMonth(today.lengthOfMonth());
        
        Constant dataSetId = constantService.getConstantByName( CASH_FLOW_DATASET_ID );
        Constant deGrpId = constantService.getConstantByName( CASH_FLOW_RATE_DE_GROUP_ID );
        
        String importStatus = "";
        Integer updateCount = 0;
        Integer insertCount = 0;
        
        int categoryComboId = 15;
        int attributeoptioncomboid = 15;
        
        // one day before sixMonthAfterDate
        Calendar oneDayAfter = Calendar.getInstance();
        oneDayAfter.setTime( new Date() );
        oneDayAfter.add( Calendar.DATE, 1 );
        Date oneDayAfterDate = oneDayAfter.getTime();

        String oneDayAfterDateString = simpleDateFormat.format( oneDayAfterDate );
        
        //List<Integer> dataSetSources = new ArrayList<Integer>( getDataSetSources( (int)dataSetId.getValue() ) );
        //List<Integer> dataElementGrpMembers = new ArrayList<Integer>( getDataElementGrpMembers( (int)deGrpId.getValue() ) );
        
        //System.out.println( dataSetSources.size() + " ---  " + dataElementGrpMembers.size() + " --- "  + oneDayAfterDateString );
        
        List<String> latestData = new ArrayList<String>( getLatestDataValue( (int)dataSetId.getValue(), (int)deGrpId.getValue(), categoryComboId, attributeoptioncomboid ) );
        
        if( latestData != null && latestData.size() > 0 )
        {
            //System.out.println( " --- latestData  " + latestData.size() + " --- "  + oneDayAfterDateString );
            
            String storedBy = "admin";
            int count = 1;
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
                for( String combinedString : latestData )
                {
                    int periodId;

                    //System.out.println( " combinedString --  " + combinedString );
                    
                    Period period = new Period();
                    
                    period = PeriodType.getPeriodFromIsoString( oneDayAfterDateString.split( "-")[0] + oneDayAfterDateString.split( "-")[1] + oneDayAfterDateString.split( "-")[2] );
                    period = periodService.reloadPeriod( period );
                    periodId = period.getId();
                    
                    //System.out.println( combinedString + " ---  " + periodId );
                    
                    periodId = period.getId();
                    
                    query = "SELECT value FROM datavalue WHERE dataelementid = " + combinedString.split( ":" )[0] + " AND categoryoptioncomboid = " + combinedString.split( ":" )[2] + " AND attributeoptioncomboid = " + combinedString.split( ":" )[3] + " AND periodid = " + periodId + " AND sourceid = " + combinedString.split( ":" )[1];
                    SqlRowSet sqlResultSet1 = jdbcTemplate.queryForRowSet( query );
                    
                    if ( sqlResultSet1 != null && sqlResultSet1.next() )
                    {
                        String updateQuery = "UPDATE datavalue SET value = '" + combinedString.split( ":" )[4] + "', storedby = '" + storedBy + "',lastupdated='" + lastUpdatedDate + "' WHERE dataelementid = " + combinedString.split( ":" )[0] + " AND periodid = "
                            + periodId + " AND sourceid = " + combinedString.split( ":" )[1] + " AND categoryoptioncomboid = " + combinedString.split( ":" )[2] + " AND attributeoptioncomboid = " + combinedString.split( ":" )[3];

                        jdbcTemplate.update( updateQuery );
                        
                        //System.out.println(" update Query -  " + updateQuery );
                        
                        updateCount++;
                    }
                    else
                    {
                        if ( combinedString.split( ":" )[4] != null && !combinedString.split( ":" )[4].trim().equals( "" ) )
                        {
                            insertQuery += "( " + combinedString.split( ":" )[0] + ", " + periodId + ", " + combinedString.split( ":" )[1] + ", " + combinedString.split( ":" )[2] +  ", " + combinedString.split( ":" )[3] + ", '" + combinedString.split( ":" )[4] + "', '" + storedBy + "', '" + created + "', '" + lastUpdatedDate + "', false ), ";
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
                            //System.out.println( " insert Query 2 -  " );
                            jdbcTemplate.update( insertQuery );
                        }

                        insertFlag = 1;

                        insertQuery = "INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, created, lastupdated, deleted ) VALUES ";
                    }

                    count++;
                }
                
                System.out.println(" Count - "  + count + " -- Insert Count : " + insertCount + "  Update Count -- " + updateCount );
                if ( insertFlag != 1 )
                {
                    insertQuery = insertQuery.substring( 0, insertQuery.length() - 2 );
                    //System.out.println(" insert Query 1 -  ");
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
        
 
        
        System.out.println("INFO: scheduler job has ended at : " + new Date() );
    }
    
    //--------------------------------------------------------------------------------
    // Supportive Methods
    //--------------------------------------------------------------------------------
    
    //--------------------------------------------------------------------------------
    // Get DataSetSource Ids
    //--------------------------------------------------------------------------------
    public List<Integer> getDataSetSources( Integer dataSetId )
    {
        List<Integer> dataSetSources = new ArrayList<Integer>();
        
        try
        {
            String query = "SELECT sourceid from datasetsource WHERE datasetid =  "+ dataSetId +" ";
            
            //select sourceid from datasetsource where datasetid = 19968;
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer sourceId = rs.getInt( 1 );

                if ( sourceId != null )
                {
                    dataSetSources.add( sourceId );
                }
            }

            return dataSetSources;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal dataSet Id", e );
        }
    }
    
    //--------------------------------------------------------------------------------
    // Get DataSetSource Ids
    //--------------------------------------------------------------------------------
    public List<Integer> getDataElementGrpMembers( Integer deGrpId )
    {
        List<Integer> dataelementgroupmembers = new ArrayList<Integer>();
        
        try
        {
            String query = "SELECT dataelementid from dataelementgroupmembers where dataelementgroupid =  "+ deGrpId +" ";
            
            //SELECT dataelementid from dataelementgroupmembers where dataelementgroupid = 78457;
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer deId = rs.getInt( 1 );

                if ( deId != null )
                {
                    dataelementgroupmembers.add( deId );
                }
            }

            return dataelementgroupmembers;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal deGrp Id", e );
        }
    }    
    
    //--------------------------------------------------------------------------------
    // Get Latest DataValue by orgUnitID, dataElementID, categoryoptioncomboId, attributeoptioncomboId
    //--------------------------------------------------------------------------------
    public List<String> getLatestDataValue( Integer dataSetId, Integer dataElementGrpID, Integer categoryoptioncomboId, Integer attributeoptioncomboId )
    {
        List<String> latesData = new ArrayList<String>();
        
        List<Integer> dataSetSources = new ArrayList<Integer>( getDataSetSources( dataSetId ) );
        List<Integer> dataElementGrpMembers = new ArrayList<Integer>( getDataElementGrpMembers( dataElementGrpID ) );
        System.out.println( dataSetSources.size() + " ---  " + dataElementGrpMembers.size() );
        
        if( dataSetSources != null && dataSetSources.size() > 0 && dataElementGrpMembers != null && dataElementGrpMembers.size() > 0 )
        {
            for( Integer orgUnitId : dataSetSources )
            {
                for( Integer deId : dataElementGrpMembers )
                {
                    try
                    {
                        // on the basis of enrollment
                        String query = "SELECT dv.dataelementid, dv.periodid, dv.sourceid, dv.categoryoptioncomboid, dv.attributeoptioncomboid, dv.value " +
                                        " FROM datavalue dv INNER JOIN period p on p.periodid = dv.periodid " +
                                        " WHERE dv.dataelementid = "+ deId +" AND dv.sourceid = " + orgUnitId + " AND dv.categoryoptioncomboid = " +  categoryoptioncomboId + " " +
                                        " AND dv.attributeoptioncomboid = " + attributeoptioncomboId + " ORDER BY p.enddate desc LIMIT 1 ";
                        
                        SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

                        while ( rs.next() )
                        {
                            Integer dataElementId = rs.getInt( 1 );
                            Integer organisationUnitId = rs.getInt( 3 );
                            Integer ccoId = rs.getInt( 4 );
                            Integer attOptionId = rs.getInt( 5 );
                            String deValue = rs.getString( 6 );
                            
                            if ( dataElementId != null && organisationUnitId != null && ccoId != null && attOptionId != null && deValue != null )
                            {
                               String combinedString = dataElementId + ":" + organisationUnitId + ":" + ccoId + ":" + attOptionId + ":" + deValue;
                               //System.out.println(  " combinedString ---  " + combinedString );
                               latesData.add( combinedString );
                            }
                        }
                    }
                    catch ( Exception e )
                    {
                        throw new RuntimeException( "Illegal Attribute id", e );
                    }
                }
            }
        }
        return latesData;
    }
}
