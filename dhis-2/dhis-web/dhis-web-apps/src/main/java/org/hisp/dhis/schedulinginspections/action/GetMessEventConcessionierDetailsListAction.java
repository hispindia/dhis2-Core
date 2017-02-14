package org.hisp.dhis.schedulinginspections.action;

import static org.hisp.dhis.i18n.I18nUtils.i18n;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.UnifiedJEXL.Exception;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.option.Option;
import org.hisp.dhis.option.OptionService;
import org.hisp.dhis.option.OptionSet;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStageInstanceStore;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserGroupService;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class GetMessEventConcessionierDetailsListAction implements Action
{
    private final String  INSPECTOR_USER_GROUP = "Inspector";
    
    private final String  ESCALATION_STATUS = "ESCALATION_STATUS";
    private final String  FS_ESTABLISHMENT_TYPE = "FS_ESTABLISHMENT_TYPE";
        
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    protected ProgramStageInstanceService programStageInstanceService;
    
    @Autowired
    private ProgramStageInstanceStore programStageInstanceStore;
       
    @Autowired
    private ProgramInstanceService programInstanceService;
    
    @Autowired
    private TrackedEntityDataValueService trackedEntityDataValueService;
    
    @Autowired
    private ProgramService programService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserGroupService userGroupService;
    
    @Autowired
    private OptionService optionService;
    
    @Autowired
    private OrganisationUnitService organisationUnitService;
    
    @Autowired
    private TrackedEntityInstanceService trackedEntityInstanceService;
    
    @Autowired
    private I18nService i18nService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
    private String selectedTeiUid;
    
    public void setSelectedTeiUid( String selectedTeiUid )
    {
        this.selectedTeiUid = selectedTeiUid;
    }

    private List<TrackedEntityInstance> trackedEntityInstances = new ArrayList<TrackedEntityInstance>();
   
    public List<TrackedEntityInstance> getTrackedEntityInstances()
    {
        return trackedEntityInstances;
    }


    private Map<String, String> teiAttributeValueMap = new HashMap<String, String>();
    
    public Map<String, String> getTeiAttributeValueMap()
    {
        return teiAttributeValueMap;
    }


    private Integer relShipId = 4912;
    
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute() throws Exception
    {
        System.out.println( "selectedTeiUid  -- " + selectedTeiUid );
        TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( selectedTeiUid );
        System.out.println( "tei -- " + tei.getId());
        if( tei != null )
        {
            trackedEntityInstances = new ArrayList<TrackedEntityInstance>( getRelatedTrackedEntityInstances( tei.getId() , relShipId ) );
            String attributeIdsByComma = "89,97,2736";
            teiAttributeValueMap = new HashMap<String, String>( getTrackedEntityInstanceAttributeValuesByAttributeIds( attributeIdsByComma ));
        }
          
        return SUCCESS;
    }

    //--------------------------------------------------------------------------------
    // Get Active Tracked Entity Instance List By Program
    //--------------------------------------------------------------------------------
    public List<TrackedEntityInstance> getRelatedTrackedEntityInstances( Integer teiId, Integer relShipId )
    {
        List<TrackedEntityInstance> teiList = new ArrayList<TrackedEntityInstance>();
        
        try
        {
            String query = "SELECT trackedentityinstanceaid,relationshiptypeid,trackedentityinstancebid from relationship  " +
                            "WHERE trackedentityinstanceaid = "+ teiId + " AND relationshiptypeid = " + relShipId;
          
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            System.out.println( "Query 1 -- " + query);
            while ( rs.next() )
            {
                Integer tempTeiId = rs.getInt( 3 );
                
                if ( teiId != null )
                {
                    TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( tempTeiId );
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
    
    //--------------------------------------------------------------------------------
    // Get Tracked Entity Instance Attribute Values by attributeIds  
    //--------------------------------------------------------------------------------
    public Map<String, String> getTrackedEntityInstanceAttributeValuesByAttributeIds( String attributeIdsByComma )
    {
        Map<String, String> teiValueMap = new HashMap<String, String>();

        try
        {
            String query = "SELECT trackedentityinstanceid, trackedentityattributeid, value FROM trackedentityattributevalue " +
                            "WHERE trackedentityattributeid IN ( "+ attributeIdsByComma +")";
          
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            System.out.println( "Query 2 -- " + query);
            
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

}