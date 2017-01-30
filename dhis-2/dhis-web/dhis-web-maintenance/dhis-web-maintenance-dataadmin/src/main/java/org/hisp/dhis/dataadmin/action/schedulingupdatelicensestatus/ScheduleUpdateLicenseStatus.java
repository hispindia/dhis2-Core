package org.hisp.dhis.dataadmin.action.schedulingupdatelicensestatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.jexl2.UnifiedJEXL.Exception;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class ScheduleUpdateLicenseStatus implements Action
{
    private final static int  LICENSE_STATUS_DATAELEMENT_ID = 3430;
    private final static int  LICENSE_VALIDITY_DATE_DATAELEMENT_ID = 2609;
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private ProgramStageInstanceService programStageInstanceService;

    @Autowired
    private CurrentUserService currentUserService;
    
    @Autowired
    private TrackedEntityDataValueService trackedEntityDataValueService;
    
    @Autowired
    private DataElementService dataElementService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
 
    // -------------------------------------------------------------------------
    // Action IMplementation
    // -------------------------------------------------------------------------
    
    public String execute() throws Exception
    {
        
        System.out.println("INFO: scheduler job has started at : " + new Date() );
        
        String storedBy = currentUserService.getCurrentUsername();
        String licenseExpireStatusCode = "2";
        Integer updateCount = 0;
        Integer insertCount = 0;
        
        long t;
        Date d = new Date();
        t = d.getTime();
        java.sql.Date lastUpdatedDate = new java.sql.Date( t );
        String query = "";
        
        List<ProgramStageInstance> programStageInstances = new ArrayList<ProgramStageInstance>( getProgramStageInstanceFromTedvByDataElementId( LICENSE_VALIDITY_DATE_DATAELEMENT_ID ) );
        System.out.println("programStageInstances list Size = " + programStageInstances.size() );
        if( programStageInstances != null && programStageInstances.size() > 0 )
        {
            for( ProgramStageInstance programStageInstance : programStageInstances )
            {
                try
                {
                    query = " SELECT programstageinstanceid, dataelementid, VALUE FROM trackedentitydatavalue WHERE programstageinstanceid = " + programStageInstance.getId() + 
                            " AND dataelementid = " + LICENSE_STATUS_DATAELEMENT_ID;
                    
                    SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( query );
                    
                    if ( sqlResultSet != null && sqlResultSet.next() )
                    {
                        //System.out.println( " Indide Update " );
                        
                        String updateQuery = " UPDATE trackedentitydatavalue SET VALUE = " + licenseExpireStatusCode + ", lastupdated = '" + lastUpdatedDate + "', storedby = '" + storedBy 
                                               + "' WHERE programstageinstanceid = " + programStageInstance.getId() + " AND dataelementid = " + LICENSE_STATUS_DATAELEMENT_ID + " AND value = '1' ";

                        jdbcTemplate.update( updateQuery );
                        System.out.println( " updateQuery -- " + updateQuery );
                        
                        updateCount++;
                    }
                    else
                    {
                        insertCount++;
                    }
                    
                }
                catch ( Exception e )
                {
                    System.out.println( "Exception occured while inserting/updating, please check log for more details" + e.getMessage() ) ;
                }
            }
        }
        
        System.out.println( "License Update Count = " + updateCount );
        
        System.out.println("INFO: Scheduler job has ended at : " + new Date() );
        
        return SUCCESS;
    }
    
    
    //--------------------------------------------------------------------------------
    // Get ProgramStageInstance List from trackedEntity dataValue table  
    //--------------------------------------------------------------------------------
    public List<ProgramStageInstance> getProgramStageInstanceFromTedvByDataElementId( Integer dataElementId )
    {
        List<ProgramStageInstance> programStageInstances = new ArrayList<ProgramStageInstance>();


        //SELECT programstageinstanceid FROM trackedentitydatavalue WHERE CURRENT_DATE > value::date and dataelementid = 2609 order by programstageinstanceid;
        try
        {
            String query = "SELECT programstageinstanceid FROM trackedentitydatavalue  " +
                            "WHERE CURRENT_DATE > value::date and dataelementid = "+ dataElementId +" ";
          
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer psiId = rs.getInt( 1 );
                
                if ( psiId != null )
                {
                    ProgramStageInstance psi = programStageInstanceService.getProgramStageInstance( psiId );
                    programStageInstances.add( psi );
                }
            }

            return programStageInstances;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal DataElement id", e );
        }
    }
}
