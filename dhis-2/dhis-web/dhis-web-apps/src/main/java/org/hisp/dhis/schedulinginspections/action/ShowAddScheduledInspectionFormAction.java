package org.hisp.dhis.schedulinginspections.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.UnifiedJEXL.Exception;
import org.hisp.dhis.attribute.Attribute;
import org.hisp.dhis.attribute.AttributeService;
import org.hisp.dhis.external.conf.DhisConfigurationProvider;
import org.hisp.dhis.legend.LegendService;
import org.hisp.dhis.legend.LegendSet;
import org.hisp.dhis.option.Option;
import org.hisp.dhis.option.OptionService;
import org.hisp.dhis.option.OptionSet;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStageService;
import org.hisp.dhis.program.ProgramType;
import org.hisp.dhis.system.util.AttributeUtils;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityService;
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
public class ShowAddScheduledInspectionFormAction  implements Action
{
    private final String  INSPECTION_TYPE = "FS_INSPECTION_TYPE";
    private final String  INSPECTOR_USER_GROUP = "Inspector";
    
    // -------------------------------------------------------------------------
    // Dependency
    // -------------------------------------------------------------------------

    @Autowired
    private TrackedEntityAttributeService trackedEntityAttributeService;

    @Autowired
    private TrackedEntityService trackedEntityService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private ProgramStageService programStageService;
    
    @Autowired
    private ProgramStageInstanceService programInstanceStageService;
    
    @Autowired
    private PeriodService periodService;

    @Autowired
    private OptionService optionService;

    @Autowired
    private LegendService legendService;

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private DhisConfigurationProvider configurationProvider;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserGroupService userGroupService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private Integer id;

    public void setId( Integer id )
    {
        this.id = id;
    }

    private TrackedEntityAttribute attribute;

    public TrackedEntityAttribute getAttribute()
    {
        return attribute;
    }

    private List<Program> programs;

    public List<Program> getPrograms()
    {
        return programs;
    }

    private List<PeriodType> periodTypes;

    public List<PeriodType> getPeriodTypes()
    {
        return periodTypes;
    }

    private List<OptionSet> optionSets;

    public List<OptionSet> getOptionSets()
    {
        return optionSets;
    }

    private List<LegendSet> legendSets;

    public List<LegendSet> getLegendSets()
    {
        return legendSets;
    }

    private List<Attribute> attributes;

    public List<Attribute> getAttributes()
    {
        return attributes;
    }

    private Map<Integer, String> attributeValues = new HashMap<>();

    public Map<Integer, String> getAttributeValues()
    {
        return attributeValues;
    }

    private List<TrackedEntity> trackedEntities;

    public List<TrackedEntity> getTrackedEntities()
    {
        return trackedEntities;
    }

    private boolean encryptionAvailable;
    
    public boolean isEncryptionAvailable()
    {
        return encryptionAvailable;
    }

    private List<Option> options = new ArrayList<Option>();
    
    public List<Option> getOptions()
    {
        return options;
    }

    private List<User> users = new ArrayList<User>();

    public List<User> getUsers()
    {
        return users;
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
    
    private Map<String, String> teiValueMap = new HashMap<String, String>();
    
    public Map<String, String> getTeiValueMap()
    {
        return teiValueMap;
    }
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------



    @Override
    public String execute()
        throws Exception
    {
        if ( id != null )
        {
            attribute = trackedEntityAttributeService.getTrackedEntityAttribute( id );
            attributeValues = AttributeUtils.getAttributeValueMap( attribute.getAttributeValues() );

            programs = programService.getAllPrograms();
            programs.removeAll( programService.getPrograms( ProgramType.WITHOUT_REGISTRATION ) );
            Collections.sort( programs );
        }
        
        encryptionAvailable = configurationProvider.isEncryptionConfigured().isOk();
        
        periodTypes = periodService.getAllPeriodTypes();
        optionSets = optionService.getAllOptionSets();
        legendSets = legendService.getAllLegendSets();
        attributes = attributeService.getAttributes( TrackedEntityAttribute.class );
        trackedEntities = trackedEntityService.getAllTrackedEntity();
        
        programs = programService.getAllPrograms();
        OptionSet optionSet = optionService.getOptionSetByCode( INSPECTION_TYPE );
        
        //programStageService
        
        options = new ArrayList<Option>( optionSet.getOptions());
        
        //users = new ArrayList<User>( userService.getAllUsers() );
        
        UserGroup inspectorUserGroup = userGroupService.getUserGroupByName( INSPECTOR_USER_GROUP ).get( 0 );
        
        users = new ArrayList<User>( inspectorUserGroup.getMembers() );
        
        String attributeIdsByComma = "89,698,699";
        Integer teNameAttributeId = 89;
        
        teiValueMap = new HashMap<String, String>( getTrackedEntityInstanceAttributeValuesByAttributeIds( attributeIdsByComma ));
        teiMap = new HashMap<Integer, String>( getTrackedEntityInstanceAttributeValuesByAttributeId( teNameAttributeId ) );
        teiIds = new ArrayList<Integer>( teiMap.keySet());
        
        
        
        //ProgramStageInstance programStageInstance = new ProgramStageInstance();
        //programStageInstance.setp
       
        //programInstanceStageService.addProgramStageInstance( arg0 );
        
        
//        for( User user : users )
//        {
//            user.getName();
//           System.out.println( user.getId() + " -- " + user.getUser().getDisplayName() );
//        }
        
        Collections.sort( users );
        
        Collections.sort( optionSets );
        Collections.sort( legendSets );
        Collections.sort( trackedEntities );

        return SUCCESS;
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
    
}
