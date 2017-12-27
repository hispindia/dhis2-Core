package org.hisp.dhis.dataadmin.action.scheduecalculatetei;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodStore;
import org.hisp.dhis.period.PeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.opensymphony.xwork2.Action;

public class ScheduleCountTEI implements Action
{
    private final static int  BLOCK_TALUK_ATTRIBUTE_ID = 35830;
    private final String  AES_PROGRAM_DE_ID = "AES_PROGRAM_DE_ID";
    private final String  AMES_PROGRAM_DE_ID = "AMES_PROGRAM_DE_ID";
    
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
    
    @Autowired
    private OrganisationUnitService organisationUnitService;
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
 
    // -------------------------------------------------------------------------
    // Action IMplementation
    // -------------------------------------------------------------------------
    
    public String execute() throws Exception
    {
        System.out.println("INFO: scheduler job has started at : " + new Date() );
        
        LocalDate today = LocalDate.now();
        //System.out.println("First day: " + today.withDayOfMonth(1));
        //System.out.println("Last day: " + today.withDayOfMonth(today.lengthOfMonth()));
        
        LocalDate startDateOfCurrentMonth = today.withDayOfMonth(1);
        LocalDate endDateOfCurrentMonth = today.withDayOfMonth(today.lengthOfMonth());
        
        Constant aesDeId = constantService.getConstantByName( AES_PROGRAM_DE_ID );
        Constant amesDeId = constantService.getConstantByName( AMES_PROGRAM_DE_ID );
        
        //String dailyPeriodTypeName = DailyPeriodType.NAME;
        
        //PeriodType periodType = periodService.getPeriodTypeByName( dailyPeriodTypeName );
        
        String importStatus = "";
        Integer updateCount = 0;
        Integer insertCount = 0;
        
        List<String> trackedEntityInstances = new ArrayList<String>( getTrackedEntityInstanceCountByAttributeId( BLOCK_TALUK_ATTRIBUTE_ID, startDateOfCurrentMonth, endDateOfCurrentMonth ) );
        
        if( trackedEntityInstances != null && trackedEntityInstances.size() > 0 )
        {
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
                for( String combinedString : trackedEntityInstances )
                {
                    int periodId;
                    Integer sourceId;
                    int dataElementId = 0;
                    int categoryComboId = 15;
                    int attributeoptioncomboid = 15;
                    String value;
                    
                    Period period = new Period();
                    //System.out.println(" Created day: " + combinedString.split( ":" )[2]);
                    
                    String createdDate = combinedString.split( ":" )[2];
                    //System.out.println(" Created day 2 : " + createdDate.split( "-")[0] + createdDate.split( "-")[1] + createdDate.split( "-")[2] );
                    
                    period = PeriodType.getPeriodFromIsoString( createdDate.split( "-")[0] + createdDate.split( "-")[1] + createdDate.split( "-")[2] );
                    period = periodService.reloadPeriod( period );
                    
                    periodId = period.getId();
                    
                    //System.out.println("Period Id -  " + period.getId() );
                    
                    sourceId = getOrganisationUnitId( combinedString.split( ":" )[0] );
                    
                    if( sourceId != null )
                    {
                        value =  combinedString.split( ":" )[3];
                        
                        if( combinedString.split( ":" )[1].equalsIgnoreCase( "201" ))
                        {
                            dataElementId = (int)aesDeId.getValue();
                        }
                        else if( combinedString.split( ":" )[1].equalsIgnoreCase( "23824" ) )
                        {
                            dataElementId = (int)amesDeId.getValue();
                        }
                      
                        query = "SELECT value FROM datavalue WHERE dataelementid = " + dataElementId + " AND categoryoptioncomboid = " + categoryComboId + " AND attributeoptioncomboid = " + attributeoptioncomboid + " AND periodid = " + periodId + " AND sourceid = " + sourceId;
                        SqlRowSet sqlResultSet1 = jdbcTemplate.queryForRowSet( query );
                        if ( sqlResultSet1 != null && sqlResultSet1.next() )
                        {
                            String updateQuery = "UPDATE datavalue SET value = '" + value + "', storedby = '" + storedBy + "',lastupdated='" + lastUpdatedDate + "' WHERE dataelementid = " + dataElementId + " AND periodid = "
                                + periodId + " AND sourceid = " + sourceId + " AND categoryoptioncomboid = " + categoryComboId + " AND attributeoptioncomboid = " + attributeoptioncomboid;

                            jdbcTemplate.update( updateQuery );
                            
                            //System.out.println(" update Query -  " + updateQuery );
                            
                            updateCount++;
                        }
                        else
                        {
                            if ( value != null && !value.trim().equals( "" ) )
                            {
                                insertQuery += "( " + dataElementId + ", " + periodId + ", " + sourceId + ", " + categoryComboId +  ", " + attributeoptioncomboid + ", '" + value + "', '" + storedBy + "', '" + created + "', '" + lastUpdatedDate + "', false ), ";
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
                            
                        //System.out.println(" program id - "  + combinedString.split( ":" )[1]  +   " de : " + dataElementId + " value -- " + value + " orgUnit -- " +  sourceId + " period id -- " + periodId );
                    }
                   
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
        System.out.println("INFO: Scheduler job has ended at : " + new Date() );
        
        return SUCCESS;
    }    
               
    //--------------------------------------------------------------------------------
    // Get ProgramStageInstance List from trackedEntity dataValue table  
    //--------------------------------------------------------------------------------
    
    //--------------------------------------------------------------------------------
    // Get TrackedEntityInstance List from tracked entity attribute value
    //--------------------------------------------------------------------------------
    public List<String> getTrackedEntityInstanceCountByAttributeId( Integer attributeId, LocalDate startDateOfCurrentMonth, LocalDate endDateOfCurrentMonth )
    {
        List<String> trackedEntityInstances = new ArrayList<String>();
        
        //String startDateOfCurrentMonth1 = "2017-01-01";
        //String endDateOfCurrentMonth1 = "2017-12-31";
        try
        {
            /* on the basis of TEI Creation
            String query = "SELECT teiav.value,pi.programid,tei.created::date,count(teiav.value) As TOTAL from trackedentityinstance tei " +
                            "INNER JOIN trackedentityattributevalue teiav on tei.trackedentityinstanceid=teiav.trackedentityinstanceid " +
                            "INNER JOIN programinstance pi on pi.trackedentityinstanceid = tei.trackedentityinstanceid " +
                            "WHERE teiav.trackedentityattributeid =  "+ attributeId +" and tei.created::date between '" + startDateOfCurrentMonth1 + "' AND '" +  endDateOfCurrentMonth1 + "' " +
                            " group by teiav.value, pi.programid,tei.created::date order by tei.created::date DESC ";
          
            
            */
            // on the basis of enrollment
            String query = "SELECT teiav.value,pi.programid,pi.enrollmentdate::date,count(teiav.value) As TOTAL from trackedentityinstance tei " +
                "INNER JOIN trackedentityattributevalue teiav on tei.trackedentityinstanceid=teiav.trackedentityinstanceid " +
                "INNER JOIN programinstance pi on pi.trackedentityinstanceid = tei.trackedentityinstanceid " +
                "WHERE teiav.trackedentityattributeid =  "+ attributeId +" and pi.enrollmentdate::date <= '" +  endDateOfCurrentMonth + "' " +
                " group by teiav.value, pi.programid,pi.enrollmentdate::date order by pi.enrollmentdate::date ASC ";            
            
            /*
            SELECT teiav.value,pi.programid,tei.created::date,count(teiav.value) As TOTAL
            from trackedentityinstance tei
            inner join trackedentityattributevalue teiav on tei.trackedentityinstanceid=teiav.trackedentityinstanceid
            inner join programinstance pi on pi.trackedentityinstanceid = tei.trackedentityinstanceid
            where teiav.trackedentityattributeid = 35830 and tei.created::date between '2017-01-01' AND '2017-12-31'
            group by teiav.value, pi.programid,tei.created::date order by tei.created::date DESC;
            */
            /*
            SELECT teiav.value,pi.programid,pi.enrollmentdate::date,count(teiav.value) As TOTAL
            from trackedentityinstance tei
            inner join trackedentityattributevalue teiav on tei.trackedentityinstanceid=teiav.trackedentityinstanceid
            inner join programinstance pi on pi.trackedentityinstanceid = tei.trackedentityinstanceid
            where teiav.trackedentityattributeid = 35830 and pi.enrollmentdate::date <= '2017-12-31'
            group by teiav.value, pi.programid,pi.enrollmentdate::date order by pi.enrollmentdate::date ASC;
            */
            
            //System.out.println( "query = " + query );
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String orgUnitName = rs.getString( 1 );
                Integer programId = rs.getInt( 2 );
                String createdDate = rs.getString( 3 );
                Integer teiCount = rs.getInt( 4 );
                
                if ( orgUnitName != null & programId != null & createdDate != null & teiCount != null )
                {
                   String combinedString = orgUnitName + ":" + programId + ":" + createdDate + ":" + teiCount;
                   trackedEntityInstances.add( combinedString );
                }
            }

            return trackedEntityInstances;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }
    // get OrganisationUnit id by name and hierarchy level
    public Integer getOrganisationUnitId( String orgUnitName )
    {
        Integer organisationUnitId = null;
        
        List<Integer> organisationUnitIds = new ArrayList<Integer>();
        
        try
        {
            String query = " SELECT organisationunitid, uid, hierarchylevel, featuretype, coordinates FROM organisationunit"
                                   + " WHERE hierarchylevel = 6 AND name ILIKE '" + orgUnitName +"'";
                
            //SELECT organisationunitid, uid, hierarchylevel, featuretype, coordinates FROM organisationunit"+ " WHERE hierarchylevel = 6 AND name ILIKE '%" + orgUnitName +"%'";
            //System.out.println( "query = " + query );
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer orgUnitId = rs.getInt( 1 );
                //System.out.println( orgUnitName + " -- orgUnitCoordinate -- " + orgUnitId );
                if( orgUnitId != null  )
                {
                    organisationUnitIds.add( orgUnitId );
                }
            }
            //System.out.println( orgUnitName + " -- Count -- " + organisationUnitIds.size() );
            if( organisationUnitIds != null && organisationUnitIds.size() > 0 )
            {
                organisationUnitId = organisationUnitIds.get( 0 );
            }
            //System.out.println( organisationUnitId + " -- organisationUnitId -- " );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
 
              
        return organisationUnitId;
    }    
    
    
}