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
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.paging.ActionPagingSupport;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStageInstanceStore;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserGroupService;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * @author Mithilesh Kumar Thakur
 */
public class GetSchedulingInspectionsListAction extends ActionPagingSupport<ProgramStageInstance>
{
    private final String  INSPECTION_TYPE = "FS_INSPECTION_TYPE";
    private final String  INSPECTOR_USER_GROUP = "Inspector";
    
    
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
    private I18nService i18nService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    
//    @Autowired
//    private ScheduledInspectionService scheduledInspectionService;
//    
//    public void setScheduledInspectionService( ScheduledInspectionService scheduledInspectionService )
//    {
//        this.scheduledInspectionService = scheduledInspectionService;
//    }

    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
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
    
    private List<Program> programs;

    public List<Program> getPrograms()
    {
        return programs;
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
    
    private String scheduleStartDate;
    
    public String getScheduleStartDate()
    {
        return scheduleStartDate;
    }
    
    public void setScheduleStartDate( String scheduleStartDate )
    {
        this.scheduleStartDate = scheduleStartDate;
    }
    
    private String scheduleEndDate;
    
    public String getScheduleEndDate()
    {
        return scheduleEndDate;
    }
    
    public void setScheduleEndDate( String scheduleEndDate )
    {
        this.scheduleEndDate = scheduleEndDate;
    }

    private Integer programId;
    
    public Integer getProgramId()
    {
        return programId;
    }
    
    public void setProgramId( Integer programId )
    {
        this.programId = programId;
    }

    private String inspectionType;
    
    public String getInspectionType()
    {
        return inspectionType;
    }
    
    public void setInspectionType( String inspectionType )
    {
        this.inspectionType = inspectionType;
    }

    private String assignedInspector;
    
    public String getAssignedInspector()
    {
        return assignedInspector;
    }
    
    public void setAssignedInspector( String assignedInspector )
    {
        this.assignedInspector = assignedInspector;
    }

    private boolean listAll;
    
    public boolean isListAll()
    {
        return listAll;
    }

    public void setListAll( boolean listAll )
    {
        this.listAll = listAll;
    }
    
    private String districtName;
    
    public String getDistrictName()
    {
        return districtName;
    }

    public void setDistrictName( String districtName )
    {
        this.districtName = districtName;
    }
    
    private String communityName;
    
    public String getCommunityName()
    {
        return communityName;
    }

    public void setCommunityName( String communityName )
    {
        this.communityName = communityName;
    }
    
    private String status;
    
    public String getStatus()
    {
        return status;
    }

    public void setStatus( String status )
    {
        this.status = status;
    }

    private List<OrganisationUnit> districtList = new ArrayList<OrganisationUnit>();
    
    public List<OrganisationUnit> getDistrictList()
    {
        return districtList;
    }
    
    //private String listAllCondition;
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute() throws Exception
    {
        list = "Scheduling Inspections List";
        
        System.out.println( "listAll -- " + listAll);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
//        System.out.println( "scheduleStartDate -- " + scheduleStartDate);
//        System.out.println( "scheduleEndDate -- " + scheduleEndDate);
//        System.out.println( "programId -- " + programId );
//        System.out.println( "inspectionType -- " + inspectionType);
//        System.out.println( "assignedInspector -- " + assignedInspector);
//        
        if( listAll )
        { 
            System.out.println( " inside list All " + listAll  );
            programStageInstances = new ArrayList<ProgramStageInstance>( programStageInstanceStore.getAll() );
            
            this.paging = createPaging( programStageInstances.size() );
            
            programStageInstances = getProgramStageInstancesBetween( paging.getStartPos(), paging.getPageSize() );
        }
        
        else
        {
            
            String query = "";
            
            int inspectionCheck = 0;
            int inspectorCheck = 0;
            int programCheck = 0;
            int dateCheck = 0;
            int distCommunityCheck = 0;
            
            
            if( !scheduleStartDate.equalsIgnoreCase( "" ) )
            { 
                dateCheck = 1;
            }
            else
            {
                //scheduleStartDate ="1900-01-01";   
            }
            //System.out.println( scheduleEndDate +" -- $$$$$$$$$$$$$$$$$$$$$$$$$");
            if( !scheduleEndDate.equalsIgnoreCase( "" )  )
            {
                //System.out.println("inside enddate if condition");

                dateCheck = 2;
            }
            else
            {   
                //System.out.println("inside enddate condition");
                //scheduleEndDate = "2050-01-01";
                //System.out.println("inside enddate condition");
            }
            if( !scheduleStartDate.equalsIgnoreCase( "" )  && !scheduleEndDate.equalsIgnoreCase( "" )  )
            {
                dateCheck = 3;
            }
           
            if( programId != null)
            {
                programCheck = 1;
            }
//            else
//            {
//                programId = 0;
//            }
            if( !inspectionType.equalsIgnoreCase( "" ) )
            { 
                inspectionCheck = 1;
            }
            else
            {
                //inspectionType="";
            }
            
            if( !assignedInspector.equalsIgnoreCase( "" ) )
            {
                inspectorCheck = 1;
            }
            else
            {
                //assignedInspector="";
            }
            if( !inspectionType.equalsIgnoreCase( "" ) && !assignedInspector.equalsIgnoreCase( "" ) )
            { 
               inspectionCheck = 1;
               inspectorCheck = 1;
            }
            
            if( !districtName.equalsIgnoreCase( "" )  )
            {
                distCommunityCheck = 2;
            }
            else
            {
                //districtName="";
            }
            if( !communityName.equalsIgnoreCase( "" )  )
            {
                distCommunityCheck = 1;
            }
            else
            {
                //communityName="";
            }
            
            if( !districtName.equalsIgnoreCase( "" )  &&  !communityName.equalsIgnoreCase( "" ) )
            {
                distCommunityCheck = 3;
            }
            
            System.out.println( " Start Date " + scheduleStartDate  );
            System.out.println( " End Date " + scheduleEndDate  );
            System.out.println( " inspectionCheck " + inspectionCheck  );
            System.out.println( " inspectorCheck " + inspectorCheck  );
            System.out.println( " programCheck " + programCheck  );
            System.out.println( " dateCheck " + dateCheck  );
            System.out.println( " distCommunityCheck " + distCommunityCheck  );
            
            
            
            // let's try this
             
             query = "SELECT psi.programstageinstanceid " +
                    "FROM programstageinstance psi " +
                    "INNER JOIN programinstance pi ON pi.programinstanceid = psi.programinstanceid ";
            
            if( !inspectionType.equalsIgnoreCase( "" ) )    // inspection filter
                query += "INNER JOIN (" +
                          "SELECT programstageinstanceid " +
                          "FROM trackedentitydatavalue " +
                          "WHERE dataelementid = 1953 " + // -- inspection type
                          "AND value ILIKE '" + inspectionType + "' " +
                          ") tdv1 on tdv1.programstageinstanceid = psi.programstageinstanceid ";

            if( !assignedInspector.equalsIgnoreCase( "" ) )     // inspector filter
                query += "INNER JOIN (" +
                         "SELECT programstageinstanceid " +
                         "FROM trackedentitydatavalue " +
                         "WHERE dataelementid = 1954 " +  // -- assigned inspector
                         "AND value ILIKE '" + assignedInspector + "' " +
                         ") tdv2 on tdv2.programstageinstanceid = psi.programstageinstanceid ";
            
            if( !districtName.equalsIgnoreCase( "" ) )    // district filter
                query += "INNER JOIN (" +
                         "SELECT trackedentityinstanceid " +
                         "FROM trackedentityattributevalue " +
                         "WHERE trackedentityattributeid = 698 " + //  --district
                         "AND value ILIKE '" + districtName + "' " +
                         ") tav1 on tav1.trackedentityinstanceid = pi.trackedentityinstanceid ";
            
            if( !communityName.equalsIgnoreCase( "" ) )   // community filter
                query += "INNER JOIN (" +
                         "SELECT trackedentityinstanceid " +
                         "FROM trackedentityattributevalue " +
                         "WHERE trackedentityattributeid = 699 " + //  -- community
                         "AND value ILIKE '" +communityName + "' " +
                         ") tav2 on tav2.trackedentityinstanceid = pi.trackedentityinstanceid ";
            
            String q_where = " WHERE psi.programstageid = 130 ";    // inspection program stage id
            
            if( !status.equalsIgnoreCase( "" ) )
                q_where += "AND psi.status = '" + status + "' ";  // status filter
                
            if( programId != null )
                q_where += "AND pi.programid = " + programId + " "; // program filter
            
            switch( dateCheck ) {                                   // date filter 
                case 1: q_where += "AND psi.duedate >= '"+ scheduleStartDate +"' "; break;
                case 2: q_where += "AND psi.duedate <= '"+ scheduleEndDate +"' "; break;
                case 3: q_where += "AND psi.duedate BETWEEN '" + scheduleStartDate + "' AND '" + scheduleEndDate +"' "; break;

            }
            
            query = query + q_where;
            
            System.out.println( " query -- " + query  );
            
            
            
//            if( districtName != null &&  communityName != null)
//            {
//                distCommunityCheck = 3;
//                
//            }
            
            /*
            if( scheduleStartDate != null && scheduleEndDate != null && programId != null && inspectionType != null && assignedInspector != null )
            {
                
                query = " SELECT query1.programstageinstanceid from ( SELECT programstageinstanceid " +
                    " FROM trackedentitydatavalue where value IN ( '" + inspectionType + "') )query1 " +
                    " inner join ( SELECT programstageinstanceid " +
                    " FROM trackedentitydatavalue where value IN ( '" + assignedInspector + "' ) ) query2 on query1.programstageinstanceid=query2.programstageinstanceid " +
                    " INNER JOIN programstageinstance psi ON query1.programstageinstanceid = psi.programstageinstanceid " +
                    " INNER JOIN programinstance pi ON pi.programinstanceid = psi.programinstanceid " +
                    " WHERE pi.programid = " + programId + 
                    " and psi.duedate between '"+ scheduleStartDate +"' and '"+ scheduleEndDate +"' ";
                
                System.out.println( " inside filter condition " + listAll  );
                System.out.println( " query " + query  );
                
                programStageInstances = new ArrayList<ProgramStageInstance>( getProgramStageInstanceList( query ) );
                
                this.paging = createPaging( programStageInstances.size() );
                
                programStageInstances = getFilterProgramStageInstancesBetween( programStageInstances, paging.getStartPos(), paging.getPageSize() );
                
            }
            */
                /*
                query = " SELECT distinct query1.programstageinstanceid from ( " +
                        " SELECT *  FROM trackedentitydatavalue where case when 1= " + inspectionCheck + " then value IN ( '" + inspectionType + "' ) else 1=1 end  and dataelementid in (1953) ) " +
                        " query1  inner join ( SELECT *  FROM trackedentitydatavalue where case when 1= " + inspectorCheck + " then value IN  ( '" + assignedInspector + "' ) else 1=1 end and dataelementid in (1954)) " +
                        " query2 on query1.programstageinstanceid=query2.programstageinstanceid " + 
                        " INNER JOIN programstageinstance psi ON query1.programstageinstanceid = psi.programstageinstanceid  " +
                        " INNER JOIN programinstance pi ON pi.programinstanceid = psi.programinstanceid " +
                        " INNER JOIN trackedentityattributevalue ta on ta.trackedentityinstanceid=pi.trackedentityinstanceid  " +
                        " WHERE case when 1= " + programCheck + " then pi.programid = " + programId + " else 1=1 end " + 
                        " and case when 1= " + dateCheck + " then psi.duedate >= '"+ scheduleStartDate +"' " +
                        " when 2= " + dateCheck + " then psi.duedate <= '"+ scheduleEndDate +"' " +
                        " when 3= " + dateCheck + " then  psi.duedate between '"+ scheduleStartDate +"' and '"+ scheduleEndDate +"' else 1=1 end " +
                        " and  trackedentityattributeid in (699,698) " +
                        " and case when 1= " + distCommunityCheck  + " then ta.value in  ( '" + communityName + "' ) " +
                        " when 2= " + distCommunityCheck + " then ta.value in  ( '" + districtName + "'  ) " +
                        " when 3= " + distCommunityCheck + " then ta.value in  ( '" + districtName + "' " + " ," + " '" + districtName + "' ) else 1=1 end; " ;
            
                System.out.println( " inside filter condition " + listAll  );
                System.out.println( " query " + query  );
                */
            
                programStageInstances = new ArrayList<ProgramStageInstance>( getProgramStageInstanceList( query ) );
                
                this.paging = createPaging( programStageInstances.size() );
                
                programStageInstances = getFilterProgramStageInstancesBetween( programStageInstances, paging.getStartPos(), paging.getPageSize() );
            

        }
        
        
//        if( listAll )
//        {
//            System.out.println( " inside list All " + listAll  );
//            programStageInstances = new ArrayList<ProgramStageInstance>( programStageInstanceStore.getAll() );
//            
//            this.paging = createPaging( programStageInstances.size() );
//            
//            programStageInstances = getProgramStageInstancesBetween( paging.getStartPos(), paging.getPageSize() );
//        }
//        
//        else
//        {
//            System.out.println( " inside filter condition " + listAll  );
//            System.out.println( " query " + query  );
//            
//            programStageInstances = new ArrayList<ProgramStageInstance>( getProgramStageInstanceList( query ) );
//            
//            this.paging = createPaging( programStageInstances.size() );
//            
//            programStageInstances = getProgramStageInstancesBetween( paging.getStartPos(), paging.getPageSize() );
//        }
//        
        
        /*
        String query1 = " SELECT query1.programstageinstanceid from ( SELECT programstageinstanceid " +
                        " FROM trackedentitydatavalue where value IN ( " + inspectionType + ") )query1 " +
                        " inner join ( SELECT programstageinstanceid, dataelementid, value, created, lastupdated, providedelsewhere, storedby " +
                        " FROM trackedentitydatavalue where value IN ( " + assignedInspector + ") ) query2 on query1.programstageinstanceid=query2.programstageinstanceid " +
                        " INNER JOIN programstageinstance psi ON query1.programstageinstanceid = psi.programstageinstanceid " +
                        " INNER JOIN programinstance pi ON pi.programinstanceid = psi.programinstanceid " +
                        " WHERE pi.programid = " + programId + 
                        " and psi.duedate between '"+ scheduleStartDate +"' '"+ scheduleEndDate +"' ";

       */
        
//        programStageInstances = new ArrayList<ProgramStageInstance>( programStageInstanceStore.getAll() );
//        
//        this.paging = createPaging( programStageInstances.size() );
//        
//        programStageInstances = getProgramStageInstancesBetween( paging.getStartPos(), paging.getPageSize() );
        
        
        
        
        String attributeIdsByComma = "89,698,699";
        String trackerDataElementIdsByComma = "1951,1952,1953,1954";
        Integer teNameAttributeId = 89;
        //ScheduledInspectionService  scheduledInspectionService = new ScheduledInspectionService();
        
        
        
        teiValueMap = new HashMap<String, String>( getTrackedEntityInstanceAttributeValuesByAttributeIds( attributeIdsByComma ));
        teInstanceDataValueMap = new HashMap<String, String>( getTrackedEntityDataValuesByDataElementIds( trackerDataElementIdsByComma ) );
        teiMap = new HashMap<Integer, String>( getTrackedEntityInstanceAttributeValuesByAttributeId( teNameAttributeId ) );
        teiIds = new ArrayList<Integer>( teiMap.keySet());
        
        programs = programService.getAllPrograms();
        OptionSet optionSet = optionService.getOptionSetByCode( INSPECTION_TYPE );
        
        //programStageService
        
        options = new ArrayList<Option>( optionSet.getOptions());
        
        //users = new ArrayList<User>( userService.getAllUsers() );
        
        UserGroup inspectorUserGroup = userGroupService.getUserGroupByName( INSPECTOR_USER_GROUP ).get( 0 );
        
        users = new ArrayList<User>( inspectorUserGroup.getMembers() );
        
        
        districtList = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitsAtLevel( 2 ) );
        
        
        
         //teiValueMap = new HashMap<String, String>( scheduledInspectionService.getTrackedEntityInstanceAttributeValues( ));
        
//        for( ProgramStageInstance programStageInstance : programStageInstances )
//        {
//           System.out.println( programStageInstance.getId() + " -- " + programStageInstance.getStatus().toString() );
//           System.out.println( programStageInstance.getId() + " -- tracked Entity Instance Name -- " + teiValueMap.get( programStageInstance.getProgramInstance().getEntityInstance().getId() + ":"+89 ));
//           System.out.println( programStageInstance.getId() + " -- tracked Entity Instance Community -- " +  teiValueMap.get( programStageInstance.getProgramInstance().getEntityInstance().getId() + ":"+699 ) );
//           System.out.println( programStageInstance.getId() + " -- tracked Entity Instance District -- " +teiValueMap.get(  programStageInstance.getProgramInstance().getEntityInstance().getId() + ":"+698 ) );
//           programStageInstance.getExecutionDate()
//       }
        
//        
//        TrackedEntityInstance TrackedEntityInstance = new TrackedEntityInstance();
//        
//        ProgramInstance programInstance = new ProgramInstance();
//        
//        programInstanceService
//        
//        programInstance.getProgram().getp
//        
//        programInstance.getEntityInstance().getOrganisationUnit();
//        
//        ProgramStageInstance programStageInstance = new ProgramStageInstance();
//        
//        programStageInstance.setProgramInstance( programInstance );
//        programStageInstance.setProgramStage( programStage );
//        programStageInstance.setProgramStageInstanceMembers( programStageInstanceMembers );
//        programStageInstance.setExecutionDate( new Date() );
//        programStageInstance.setDueDate( new Date() );
//        programStageInstance.setCreated( new Date() );
//        programStageInstance.setLastUpdated( new Date );
//        programStageInstance.setOrganisationUnit( programInstance.getEntityInstance().getOrganisationUnit() );
//        
//        programStageInstanceService.addProgramStageInstance( programStageInstance );
        
        //programStageInstance.setp
        
//        TrackedEntityDataValue trackedEntityDataValue = new TrackedEntityDataValue();
//        trackedEntityDataValue.setDataElement( dataElement );
//        trackedEntityDataValue.setProgramStageInstance( programStageInstance );
//        trackedEntityDataValue.setProvidedElsewhere( false );
//        trackedEntityDataValue.setValue( value );
//        trackedEntityDataValue.setLastUpdated( new Date() );
//        trackedEntityDataValue.setStoredBy( storedBy );
//        trackedEntityDataValue.setAutoFields();
//        trackedEntityDataValue.setCreated( new Date() );
//        trackedEntityDataValueService.saveTrackedEntityDataValue( trackedEntityDataValue );
        
        return SUCCESS;
    }


    // Supportive Methods
    public List<ProgramStageInstance> getProgramStageInstancesBetween( int min, int max )
    {
        return i18n( i18nService, programStageInstanceStore.getAll(  min, max ) );
    }
    
    // Supportive Methods
    public List<ProgramStageInstance> getFilterProgramStageInstancesBetween( List<ProgramStageInstance> programStageInstances, int min, int max )
    {
        String programInstanceIdsByComma = "-1";
        for( ProgramStageInstance programStageInstance : programStageInstances )
        {
            programInstanceIdsByComma += "," + programStageInstance.getId();
        }
        
        //System.out.println( "  filter size " + programStageInstances.size()  );
        //System.out.println( "  filter condition  " + programInstanceIdsByComma  );
        
        return i18n( i18nService, programStageInstanceStore.getFilteredProgramStageInstances( programInstanceIdsByComma,  min, max ) );
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
    public List<ProgramStageInstance> getProgramStageInstanceList( String query )
    {
        List<ProgramStageInstance> peiList = new ArrayList<ProgramStageInstance>();

        try
        {
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer psiId = rs.getInt( 1 );
                
                if ( psiId != null )
                {
                    ProgramStageInstance psi = programStageInstanceService.getProgramStageInstance( psiId );
                    peiList.add( psi );
                }
            }

            return peiList;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Program id", e );
        }
    }
    
    
    
    
    
}