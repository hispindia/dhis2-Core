package org.hisp.dhis.schedulinginspections.action;

import static org.hisp.dhis.i18n.I18nUtils.i18n;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.UnifiedJEXL.Exception;
import org.hisp.dhis.common.OrganisationUnitSelectionMode;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.paging.ActionPagingSupport;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStageInstanceStore;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceQueryParams;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceStore;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * @author Mithilesh Kumar Thakur
 */
public class GetTEIListAction extends ActionPagingSupport<ProgramStageInstance>
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private ProgramService programService;
    
    @Autowired
    private TrackedEntityInstanceService trackedEntityInstanceService;
    
    @Autowired
    private TrackedEntityInstanceStore trackedEntityInstanceStore;
    
    @Autowired
    protected ProgramStageInstanceService programStageInstanceService;
    
    @Autowired
    private ProgramStageInstanceStore programStageInstanceStore;
       
    @Autowired
    private ProgramInstanceService programInstanceService;
    
    
    @Autowired
    private TrackedEntityDataValueService trackedEntityDataValueService;
    
    @Autowired
    private I18nService i18nService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
    private Integer programId;
    
    public void setProgramId( Integer programId )
    {
        this.programId = programId;
    }

    private SimpleDateFormat simpleDateFormat;
    
    public SimpleDateFormat getSimpleDateFormat()
    {
        return simpleDateFormat;
    }
    
    private String list;
    
    public String getList()
    {
        return list;
    }
    
    private List<ProgramStageInstance> programStageInstances = new ArrayList<ProgramStageInstance>();
    
    public List<ProgramStageInstance> getProgramStageInstances()
    {
        return programStageInstances;
    }
    
    private Map<String, String> teiValueMap = new HashMap<String, String>();
    
    public Map<String, String> getTeiValueMap()
    {
        return teiValueMap;
    }
    
    private Map<String, String> teInstanceDataValueMap = new HashMap<String, String>();
    
    public Map<String, String> getTeInstanceDataValueMap()
    {
        return teInstanceDataValueMap;
    }

    private Map<Integer, String> teiMap = new HashMap<Integer, String>();
    
    public Map<Integer, String> getTeiMap()
    {
        return teiMap;
    }

    private List<Integer> teiIds = new ArrayList<Integer>();
    
    public List<Integer> getTeiIds()
    {
        return teiIds;
    }
    
    private List<TrackedEntityInstance> trackedEntityInstances = new ArrayList<TrackedEntityInstance>();
    
    public List<TrackedEntityInstance> getTrackedEntityInstances()
    {
        return trackedEntityInstances;
    }
    
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute() throws Exception
    {
        Program program = programService.getProgram( programId );
                
        TrackedEntityInstanceQueryParams teiQueryParams = new TrackedEntityInstanceQueryParams();
        
        //ProgramStatus programStatus = ProgramStatus.ACTIVE;
        teiQueryParams.setProgram( program );
        //teiQueryParams.setProgramStatus( programStatus );
        
        OrganisationUnitSelectionMode  organisationUnitMode = OrganisationUnitSelectionMode.ALL;
        teiQueryParams.setOrganisationUnitMode( organisationUnitMode );
        
        //trackedEntityInstances = new ArrayList<TrackedEntityInstance>(trackedEntityInstanceService.getTrackedEntityInstances( teiQueryParams ) );
        
        trackedEntityInstances = new ArrayList<TrackedEntityInstance>(getTrackedEntityInstances( program.getId()) );
        
//        params.setQuery( key );
//        params.setInactiveMonths( months );
//        params.setSelfRegistered( selfRegistered );
//        params.setInvitationStatus( UserInvitationStatus.fromValue( invitationStatus ) );
//        
        
        //list = "Tracked List";
        
        //this.paging = createPaging( trackedEntityInstances.size() );
        
        //trackedEntityInstances = getTrackedEntityInstancesBetween( paging.getStartPos(), paging.getPageSize() );
        
        String attributeIdsByComma = "89,698,699";
        String trackerDataElementIdsByComma = "1951,1952,1953,1954";
        Integer teNameAttributeId = 89;
        //ScheduledInspectionService  scheduledInspectionService = new ScheduledInspectionService();
        
        teiValueMap = new HashMap<String, String>( getTrackedEntityInstanceAttributeValuesByAttributeIds( attributeIdsByComma ));
        teInstanceDataValueMap = new HashMap<String, String>( getTrackedEntityDataValuesByDataElementIds( trackerDataElementIdsByComma ) );
        teiMap = new HashMap<Integer, String>( getTrackedEntityInstanceAttributeValuesByAttributeId( teNameAttributeId ) );
        teiIds = new ArrayList<Integer>( teiMap.keySet());
        
  
        
        return SUCCESS;
    }

    // Supportive Methods
    public List<TrackedEntityInstance> getTrackedEntityInstancesBetween( int min, int max )
    {
        return i18n( i18nService, trackedEntityInstanceStore.getAll(  min, max ) );
    }
    
    //--------------------------------------------------------------------------------
    // Get Tracked Entity Instance Attribute Values by attributeIds  
    //--------------------------------------------------------------------------------
    public Map<String, String> getTrackedEntityInstanceAttributeValuesByAttributeIds( String attributeIdsByComma )
    {
        Map<String, String> teiValueMap = new HashMap<String, String>();

//        SELECT trackedentityinstanceid, trackedentityattributeid, created, lastupdated, 
//        value, encryptedvalue
//        FROM trackedentityattributevalue where trackedentityattributeid IN( 89,698,699) order by trackedentityattributeid;
        //SELECT trackedentityinstanceid FROM programinstance where programid = 115 and status = 'ACTIVE';
        
        try
        {
            String query = "SELECT trackedentityinstanceid, trackedentityattributeid, value FROM trackedentityattributevalue " +
                            "WHERE trackedentityattributeid IN ( "+ attributeIdsByComma +")";
          
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiId = rs.getInt( 1 );
                Integer teiAttributeId = rs.getInt( 2 );                
                String teiAttributeValue = rs.getString( 3 );
                
                if ( teiAttributeValue != null )
                {
                    teiValueMap.put( teiId+ ":" +teiAttributeId, teiAttributeValue );
                }
            }

            return teiValueMap;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }
 
    
    //--------------------------------------------------------------------------------
    // Get Tracked Entity Instance Attribute Values by attributeIds  
    //--------------------------------------------------------------------------------
    public Map<Integer, String> getTrackedEntityInstanceAttributeValuesByAttributeId( Integer attributeId )
    {
        Map<Integer, String> teiValueMap = new HashMap<Integer, String>();


        //SELECT trackedentityinstanceid, value FROM trackedentityattributevalue where trackedentityattributeid IN( 89 ) order by value;
        
        try
        {
            String query = "SELECT trackedentityinstanceid, value FROM trackedentityattributevalue " +
                            "WHERE trackedentityattributeid IN ( "+ attributeId +")";
          
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiId = rs.getInt( 1 );
                String teiAttributeValue = rs.getString( 2 );
                
                if ( teiAttributeValue != null )
                {
                    teiValueMap.put( teiId, teiAttributeValue );
                }
            }

            return teiValueMap;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }
    
    //--------------------------------------------------------------------------------
    // Get Tracked Entity Instance Attribute Values by attributeIds  
    //--------------------------------------------------------------------------------
    public Map<String, String> getTrackedEntityDataValuesByDataElementIds( String dataElementIdsByComma )
    {
        Map<String, String> teiDataValueMap = new HashMap<String, String>();


//        SELECT programstageinstanceid, dataelementid, value from trackedentitydatavalue WHERE dataelementid in (1951,1952,1953,1954,1955);
        try
        {
            String query = "SELECT programstageinstanceid, dataelementid, value FROM trackedentitydatavalue  " +
                            "WHERE dataelementid IN ( "+ dataElementIdsByComma +")";
          
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer psiId = rs.getInt( 1 );
                Integer teiDataElementId = rs.getInt( 2 );                
                String teiDataValue = rs.getString( 3 );
                
                if ( teiDataValue != null )
                {
                    teiDataValueMap.put( psiId+ ":" + teiDataElementId, teiDataValue );
                }
            }

            return teiDataValueMap;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal DataElement id", e );
        }
    } 
    
    //--------------------------------------------------------------------------------
    // Get Active Tracked Entity Instance List By Program
    //--------------------------------------------------------------------------------
    public List<TrackedEntityInstance> getTrackedEntityInstances( Integer programId )
    {
        List<TrackedEntityInstance> teiList = new ArrayList<TrackedEntityInstance>();


//        SELECT trackedentityinstanceid FROM programinstance where programid = 115 and status = 'ACTIVE';
        try
        {
            String query = "SELECT trackedentityinstanceid FROM programinstance  " +
                            "WHERE programid = "+ programId + " AND status = 'ACTIVE'";
          
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiId = rs.getInt( 1 );
                
                if ( teiId != null )
                {
                    TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiId );
                    teiList.add( tei );
                }
            }

            return teiList;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Program id", e );
        }
    }
    
    
    
}
