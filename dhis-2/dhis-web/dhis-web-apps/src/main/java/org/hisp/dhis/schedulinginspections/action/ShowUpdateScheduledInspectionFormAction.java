package org.hisp.dhis.schedulinginspections.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.UnifiedJEXL.Exception;
import org.hisp.dhis.option.Option;
import org.hisp.dhis.option.OptionService;
import org.hisp.dhis.option.OptionSet;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStageInstanceStore;
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
public class ShowUpdateScheduledInspectionFormAction implements Action
{
    private final String  INSPECTION_TYPE = "FS_INSPECTION_TYPE";
    private final String  INSPECTOR_USER_GROUP = "Inspector";
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private ProgramStageInstanceService programStageInstanceService;

    @Autowired
    private ProgramStageInstanceStore programStageInstanceStore;
    
    @Autowired
    private OptionService optionService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserGroupService userGroupService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
    private Integer programStageInstanceId;
    
    public Integer getProgramStageInstanceId()
    {
        return programStageInstanceId;
    }
    
    public void setProgramStageInstanceId( Integer programStageInstanceId )
    {
        this.programStageInstanceId = programStageInstanceId;
    }
    
    private String teiName;
    
    public String getTeiName()
    {
        return teiName;
    }
    
    public void setTeiName( String teiName )
    {
        this.teiName = teiName;
    }

    private String teiDistrictName;
    
    public String getTeiDistrictName()
    {
        return teiDistrictName;
    }

    public void setTeiDistrictName( String teiDistrictName )
    {
        this.teiDistrictName = teiDistrictName;
    }

    private String teiCommunityName;
    
    public String getTeiCommunityName()
    {
        return teiCommunityName;
    }
    
    public void setTeiCommunityName( String teiCommunityName )
    {
        this.teiCommunityName = teiCommunityName;
    }

    private ProgramStageInstance programStageInstance;
    
    public ProgramStageInstance getProgramStageInstance()
    {
        return programStageInstance;
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
    
    private Map<Integer, String> teInstanceDataValueMap = new HashMap<Integer, String>();
    
    public Map<Integer, String> getTeInstanceDataValueMap()
    {
        return teInstanceDataValueMap;
    }

    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------
    


    @Override
    public String execute()throws Exception
    {
        programStageInstance = programStageInstanceService.getProgramStageInstance( programStageInstanceId );
        
        programStageInstance.getProgramInstance().getProgram().getName();
        programStageInstance.getProgramStage().getName();
        
        OptionSet optionSet = optionService.getOptionSetByCode( INSPECTION_TYPE );
        
        //programStageService
        
        options = new ArrayList<Option>( optionSet.getOptions());
        
        //users = new ArrayList<User>( userService.getAllUsers() );
        
        UserGroup inspectorUserGroup = userGroupService.getUserGroupByName( INSPECTOR_USER_GROUP ).get( 0 );
        
        users = new ArrayList<User>( inspectorUserGroup.getMembers() );
        
        
        teInstanceDataValueMap = new HashMap<Integer, String>( getTrackedEntityDataValuesByProgramStageInstanceId( programStageInstanceId ) );
        
        //System.out.println( teInstanceDataValueMap.size() + " -- " + teInstanceDataValueMap );
        return SUCCESS;
    }
    
    
    
    // Supportive Methods
    //--------------------------------------------------------------------------------
    // Get Tracked Entity Instance Attribute Values by attributeIds  
    //--------------------------------------------------------------------------------
    public Map<Integer, String> getTrackedEntityDataValuesByProgramStageInstanceId( Integer programStageInstanceId )
    {
        Map<Integer, String> teiDataValueMap = new HashMap<Integer, String>();


        //SELECT dataelementid, value FROM trackedentitydatavalue WHERE programstageinstanceid = 1003;
        try
        {
            String query = "SELECT dataelementid, value FROM trackedentitydatavalue  " +
                            "WHERE programStageInstanceId = " + programStageInstanceId + " ";
          
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            //System.out.println(  "query -- " + query );
            
            while ( rs.next() )
            {
                Integer teiDataElementId = rs.getInt( 1 );                
                String teiDataValue = rs.getString( 2 );
                
                if ( teiDataValue != null )
                {
                    teiDataValueMap.put( teiDataElementId , teiDataValue );
                }
            }

            return teiDataValueMap;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal programStageInstance Id", e );
        }
    }    
    
    
    
}
