package org.hisp.dhis.escalations.action;

import static org.hisp.dhis.i18n.I18nUtils.i18n;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.UnifiedJEXL.Exception;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.option.Option;
import org.hisp.dhis.option.OptionService;
import org.hisp.dhis.option.OptionSet;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.paging.ActionPagingSupport;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStageInstanceStore;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.hisp.dhis.user.CurrentUserService;
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
public class GetEscalationsListAction
    extends ActionPagingSupport<ProgramStageInstance>
{
    private final String INSPECTOR_USER_GROUP = "Inspector";

    private final String ESCALATION_STATUS = "ESCALATION_STATUS";

    private final String FS_ESTABLISHMENT_TYPE = "FS_ESTABLISHMENT_TYPE";

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
    private CurrentUserService currentUserService;

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

    // @Autowired
    // private ScheduledInspectionService scheduledInspectionService;
    //
    // public void setScheduledInspectionService( ScheduledInspectionService
    // scheduledInspectionService )
    // {
    // this.scheduledInspectionService = scheduledInspectionService;
    // }

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

    private List<Option> establishmentTypes = new ArrayList<Option>();

    public List<Option> getEstablishmentTypes()
    {
        return establishmentTypes;
    }

    public void setEstablishmentTypes( List<Option> establishmentTypes )
    {
        this.establishmentTypes = establishmentTypes;
    }

    private List<User> users = new ArrayList<User>();

    public List<User> getUsers()
    {
        return users;
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

    private String escalationStartDate;

    public String getEscalationStartDate()
    {
        return escalationStartDate;
    }

    public void setEscalationStartDate( String escalationStartDate )
    {
        this.escalationStartDate = escalationStartDate;
    }

    private String escalationEndDate;

    public String getEscalationEndDate()
    {
        return escalationEndDate;
    }

    public void setEscalationEndDate( String escalationEndDate )
    {
        this.escalationEndDate = escalationEndDate;
    }

    private String establishmentName;

    public String getEstablishmentName()
    {
        return establishmentName;
    }

    public void setEstablishmentName( String establishmentName )
    {
        this.establishmentName = establishmentName;
    }

    private String establishmentType;

    public String getEstablishmentType()
    {
        return establishmentType;
    }

    public void setEstablishmentType( String establishmentType )
    {
        this.establishmentType = establishmentType;
    }

    private String ownerName;

    public String getOwnerName()
    {
        return ownerName;
    }

    public void setOwnerName( String ownerName )
    {
        this.ownerName = ownerName;
    }

    private String decisionStartDate;

    public String getDecisionStartDate()
    {
        return decisionStartDate;
    }

    public void setDecisionStartDate( String decisionStartDate )
    {
        this.decisionStartDate = decisionStartDate;
    }

    private String decisionEndDate;

    public String getDecisionEndDate()
    {
        return decisionEndDate;
    }

    public void setDecisionEndDate( String decisionEndDate )
    {
        this.decisionEndDate = decisionEndDate;
    }

    private String escalationStatus;

    public String getEscalationStatus()
    {
        return escalationStatus;
    }

    public void setEscalationStatus( String escalationStatus )
    {
        this.escalationStatus = escalationStatus;
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

    private Map<String, String> teiAttributeValueMap = new HashMap<String, String>();

    public Map<String, String> getTeiAttributeValueMap()
    {
        return teiAttributeValueMap;
    }

    private Map<String, String> escalationStatusMap = new HashMap<String, String>();

    public Map<String, String> getEscalationStatusMap()
    {
        return escalationStatusMap;
    }

    public void setEscalationStatusMap( Map<String, String> escalationStatusMap )
    {
        this.escalationStatusMap = escalationStatusMap;
    }

    public String programUid;

    public void setProgramUid( String programUid )
    {
        this.programUid = programUid;
    }

    // private String listAllCondition;
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        list = "List of Escalations";

        System.out.println( "listAll -- " + listAll );
        String programIdsByComma = "-1";
        List<Program> userPrograms = new ArrayList<Program>( currentUserService.getCurrentUser().getUserCredentials().getAllPrograms() );
        for( Program userProgram : userPrograms )
        {
            int programId = userProgram.getId();
            programIdsByComma += "," + programId;
        }
        
        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );

        // System.out.println( "scheduleStartDate -- " + scheduleStartDate);
        // System.out.println( "scheduleEndDate -- " + scheduleEndDate);
        // System.out.println( "programId -- " + programId );
        // System.out.println( "inspectionType -- " + inspectionType);
        // System.out.println( "assignedInspector -- " + assignedInspector);
        //
        if ( listAll )
        {
            // String query =
            // "SELECT programstageinstanceid FROM programstageinstance where programstageid in ( 3461 ) order by executiondate;";
            // System.out.println( " inside list All " + listAll );
            // programStageInstances = new ArrayList<ProgramStageInstance>(
            // programStageInstanceStore.getAll() );

            String query = "";

            if ( programUid != null && programUid.length() > 0 )
            {
                query = " SELECT programstageinstanceid FROM programstageinstance psi "
                    + " INNER JOIN programstage ps ON ps.programstageid = psi.programstageid "
                    + " INNER JOIN program p ON p.programid = ps.programid " + " WHERE p.uid = '" + programUid
                    + "' AND ps.name ILIKE 'Escalation' order by psi.executiondate ";
            }

            else
            {
                query = "SELECT programstageinstanceid FROM programstageinstance psi "
                    + " INNER JOIN programstage ps ON ps.programstageid = psi.programstageid "
                    + " INNER JOIN program p ON p.programid = ps.programid "
                    + " WHERE  ps.name ILIKE 'Escalation' AND ps.programid IN ( " + programIdsByComma + " ) order by psi.executiondate;";
            }

            System.out.println( " inside list All " + listAll + " programUid  " + programUid );
            System.out.println( " programIdsByComma  "+ programIdsByComma );
            
            programStageInstances = new ArrayList<ProgramStageInstance>( getProgramStageInstanceList( query ) );

            this.paging = createPaging( programStageInstances.size() );

            // programStageInstances = getProgramStageInstancesBetween(
            // paging.getStartPos(), paging.getPageSize() );
            programStageInstances = getFilterProgramStageInstancesBetween( programStageInstances, paging.getStartPos(),
                paging.getPageSize() );

        }

        /*
         * for( ProgramStageInstance programStageInstance :
         * programStageInstances ) { System.out.println(
         * programStageInstance.getId() + " -- " +
         * programStageInstance.getStatus().toString() ); System.out.println(
         * programStageInstance.getId() + " -- tracked Entity Instance Name -- "
         * + teiValueMap.get(
         * programStageInstance.getProgramInstance().getEntityInstance().getId()
         * + ":"+89 )); System.out.println( programStageInstance.getId() +
         * " -- tracked Entity Instance Community -- " + teiValueMap.get(
         * programStageInstance.getProgramInstance().getEntityInstance().getId()
         * + ":"+699 ) ); System.out.println( programStageInstance.getId() +
         * " -- tracked Entity Instance District -- " +teiValueMap.get(
         * programStageInstance.getProgramInstance().getEntityInstance().getId()
         * + ":"+698 ) ); programStageInstance.getExecutionDate();
         * 
         * programStageInstance.getCompletedDate();
         * programStageInstance.getProgramInstance().getProgram().getName();
         * 
         * programStageInstance.getOrganisationUnit().getUid();
         * programStageInstance
         * .getProgramInstance().getEntityInstance().getUid();
         * 
         * programStageInstance.getProgramInstance().getProgram().getUid(); }
         */

        else
        {
            /*
             * System.out.println( " Escalation start Date " +
             * escalationStartDate.length() ); System.out.println(
             * " Escalation end date " + escalationEndDate.length() );
             * System.out.println( " decision start Date " +
             * decisionStartDate.length() ); System.out.println(
             * " decision start Date " + decisionEndDate.length() );
             */

            int escalationDateCheck = 0;
            int decisionDateCheck = 0;
            /*
             * if( !"".equals(escalationStartDate) ) { escalationDateCheck = 1;
             * }
             * 
             * if( !"".equals(escalationEndDate) ) { escalationDateCheck = 2; }
             * 
             * if( !("".equals(escalationStartDate) &&
             * !"".equals(escalationEndDate)) ) { escalationDateCheck = 3; }
             */
            if ( escalationStartDate.length() > 0 )
            {
                escalationDateCheck = 1;
            }

            if ( escalationEndDate.length() > 0 )
            {
                escalationDateCheck = 2;
            }

            if ( escalationStartDate.length() > 0 && escalationEndDate.length() > 0 )
            {
                escalationDateCheck = 3;
            }

            /*
             * if( !escalationStartDate.equalsIgnoreCase( "" ) ) {
             * escalationDateCheck = 1; }
             * 
             * if( !escalationEndDate.equalsIgnoreCase( "" ) ) {
             * escalationDateCheck = 2; }
             * 
             * if( !(escalationStartDate.equalsIgnoreCase( "" ) &&
             * !escalationEndDate.trim().equalsIgnoreCase( "" )) ) {
             * escalationDateCheck = 3; }
             */

            /*
             * if( escalationEndDate == null) { escalationEndDate =
             * simpleDateFormat.format( new Date() ); }
             */

            if ( decisionStartDate.length() > 0 )
            {
                decisionDateCheck = 1;
            }

            if ( decisionEndDate.length() > 0 )
            {
                decisionDateCheck = 2;
            }

            if ( decisionStartDate.length() > 0 && decisionEndDate.length() > 0 )
            {
                decisionDateCheck = 3;
            }

            /*
             * if( !"".equals(decisionStartDate) ) { decisionDateCheck = 1; }
             * 
             * if( !"".equals(decisionEndDate) ) { decisionDateCheck = 2; }
             * 
             * if( !("".equals(decisionStartDate) &&
             * !"".equals(decisionEndDate)) ) { decisionDateCheck = 3; }
             */

            /*
             * 
             * if( !decisionStartDate.equalsIgnoreCase( "" ) ) {
             * decisionDateCheck = 1; }
             * 
             * if( !decisionEndDate.equalsIgnoreCase( "" ) ) { decisionDateCheck
             * = 2; }
             * 
             * if( !(decisionStartDate.equalsIgnoreCase( "" ) &&
             * !decisionEndDate.equalsIgnoreCase( "" )) ) { decisionDateCheck =
             * 3; }
             */

            /*
             * if( decisionEndDate == null) { decisionEndDate =
             * simpleDateFormat.format( new Date() ); }
             */

            String query = "";
            /*
             * System.out.println( " program Id " + programId );
             * System.out.println( " Escalation Date Check " +
             * escalationDateCheck ); System.out.println( " Establishment Name "
             * + establishmentName ); System.out.println( " Establishment Type "
             * + establishmentType ); System.out.println( " owner Name " +
             * ownerName ); System.out.println( " decision Date Check " +
             * decisionDateCheck ); System.out.println( " Escalation Status " +
             * escalationStatus );
             */

            query = "SELECT psi.programstageinstanceid " + "FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN programstage ps ON ps.programstageid = psi.programstageid ";

            if ( !escalationStatus.equalsIgnoreCase( "" ) ) // escalation Status
                                                            // filter
                query += "INNER JOIN (" + "SELECT programstageinstanceid " + "FROM trackedentitydatavalue "
                    + "WHERE dataelementid = 3460 "
                    + // -- escalation Status dataelement id
                    "AND value ILIKE '" + escalationStatus + "' "
                    + ") tdv1 on tdv1.programstageinstanceid = psi.programstageinstanceid ";

            if ( !establishmentName.equalsIgnoreCase( "" ) ) // establishment
                                                             // Name filter
                query += "INNER JOIN (" + "SELECT trackedentityinstanceid " + "FROM trackedentityattributevalue "
                    + "WHERE trackedentityattributeid = 89 "
                    + // -- establishment name attribute id
                    "AND value ILIKE '%" + establishmentName + "%' "
                    + ") tav1 on tav1.trackedentityinstanceid = pi.trackedentityinstanceid ";

            if ( !establishmentType.equalsIgnoreCase( "" ) ) // establishment
                                                             // Type filter
                query += "INNER JOIN (" + "SELECT trackedentityinstanceid " + "FROM trackedentityattributevalue "
                    + "WHERE trackedentityattributeid = 88 "
                    + // -- establishment type attribute id
                    "AND value ILIKE '" + establishmentType + "' "
                    + ") tav2 on tav2.trackedentityinstanceid = pi.trackedentityinstanceid ";

            if ( !ownerName.equalsIgnoreCase( "" ) ) // owner Name filter
                query += "INNER JOIN ("
                    + "SELECT trackedentityinstanceid, value "
                    + "FROM trackedentityattributevalue "
                    + "WHERE trackedentityattributeid = 94 "
                    + // -- Attribute "Operator/Owner UID"
                    ") ta_UID on ta_UID.trackedentityinstanceid = pi.trackedentityinstanceid "
                    + "INNER JOIN ("
                    + "SELECT op.uid "
                    + // -- operator uids
                    "FROM trackedentityinstance op " + "INNER JOIN ("
                    + "SELECT trackedentityinstanceid "
                    + // -- select operators with attribute name like '%search
                      // pattern%'
                    "FROM trackedentityattributevalue " + "WHERE trackedentityattributeid = 1112 AND value ILIKE '%"
                    + ownerName + "%' " + ") opname ON op.trackedentityinstanceid = opname.trackedentityinstanceid "
                    + "WHERE op.trackedentityid = 104 " + // -- Operator
                                                          // trackedentity
                    ") opuid ON opuid.uid = ta_UID.value ";
            /*
             * if( !ownerName.equalsIgnoreCase( "" ) ) // owner Name filter
             * query += "INNER JOIN (" + "SELECT trackedentityinstanceid " +
             * "FROM trackedentityattributevalue " +
             * "WHERE trackedentityattributeid = 1112 " + // -- owner Name
             * attribute id "AND value ILIKE '%" + ownerName + "%' " +
             * ") tav3 on tav3.trackedentityinstanceid = pi.trackedentityinstanceid "
             * ;
             */

            // String q_where = "WHERE psi.programstageid in ( 3461 ) "; //
            // Escalation program stage ids
            String q_where = " WHERE ps.name ILIKE 'Escalation' ";

            if ( programId != null )
                q_where += "AND pi.programid = " + programId + " "; // program
                                                                    // filter

            /*
             * if( !escalationDate.equalsIgnoreCase( "" ) ) q_where +=
             * "AND psi.executiondate = '" + escalationDate + "' "; //
             * escalation/execution Date filter
             * 
             * if( !decisionDate.equalsIgnoreCase( "" ) ) q_where +=
             * "AND psi.completeddate = '" + decisionDate + "' "; //
             * decision/complete Date filter
             */

            // escalation/execution Date filter
            switch ( escalationDateCheck )
            {
            case 1:
                q_where += "AND psi.executiondate >= '" + escalationStartDate + "' ";
                break;
            case 2:
                q_where += "AND psi.executiondate <= '" + escalationEndDate + "' ";
                break;
            case 3:
                q_where += "AND psi.executiondate BETWEEN '" + escalationStartDate + "' AND '" + escalationEndDate
                    + "' ";
                break;
            }
            // decision/complete Date filter
            switch ( decisionDateCheck )
            {
            case 1:
                q_where += "AND psi.completeddate >= '" + decisionStartDate + "' ";
                break;
            case 2:
                q_where += "AND psi.completeddate <= '" + decisionEndDate + "' ";
                break;
            case 3:
                q_where += "AND psi.completeddate BETWEEN '" + decisionStartDate + "' AND '" + decisionEndDate + "' ";
                break;
            }

            query = query + q_where;

            System.out.println( " query -- " + query );

            programStageInstances = new ArrayList<ProgramStageInstance>( getProgramStageInstanceList( query ) );

            this.paging = createPaging( programStageInstances.size() );

            programStageInstances = getFilterProgramStageInstancesBetween( programStageInstances, paging.getStartPos(),
                paging.getPageSize() );

        }

        String attributeIdsByComma = "88,89,94,698,699";
        String trackerDataElementIdsByComma = "1951,1952,1953,1954,3460";
        Integer teNameAttributeId = 89;
        String teOperatorNameAttributeId = "1112";

        teiValueMap = new HashMap<String, String>(
            getTrackedEntityInstanceAttributeValuesByAttributeIds( attributeIdsByComma ) );
        teInstanceDataValueMap = new HashMap<String, String>(
            getTrackedEntityDataValuesByDataElementIds( trackerDataElementIdsByComma ) );
        teiMap = new HashMap<Integer, String>( getTrackedEntityInstanceAttributeValuesByAttributeId( teNameAttributeId ) );

        teiAttributeValueMap = new HashMap<String, String>( getTrackedEntityAttributeValue( teOperatorNameAttributeId ) );

        teiIds = new ArrayList<Integer>( teiMap.keySet() );

        // programs = programService.getAllPrograms();

        String programStageName = "Escalation";
        programs = new ArrayList<Program>( getProgramsByProgramStageName( programStageName ) );

        // filter program user Role Wise
        Iterator<Program> programIterator = programs.iterator();
        while ( programIterator.hasNext() )
        {
            Program prg = programIterator.next();

            if ( !userPrograms.contains( prg ) )
            {
                programIterator.remove();
            }
        }
        
        OptionSet optionSet = optionService.getOptionSetByCode( ESCALATION_STATUS );

        options = new ArrayList<Option>( optionSet.getOptions() );

        OptionSet optionSetEstablishmentType = optionService.getOptionSetByCode( FS_ESTABLISHMENT_TYPE );

        // programStageService

        escalationStatusMap = new HashMap<String, String>();
        establishmentTypes = new ArrayList<Option>( optionSetEstablishmentType.getOptions() );
        for ( Option option : optionSet.getOptions() )
        {
            escalationStatusMap.put( option.getCode(), option.getName() );
        }

        // users = new ArrayList<User>( userService.getAllUsers() );

        UserGroup inspectorUserGroup = userGroupService.getUserGroupByName( INSPECTOR_USER_GROUP ).get( 0 );

        users = new ArrayList<User>( inspectorUserGroup.getMembers() );

        return SUCCESS;
    }

    // Supportive Methods
    public List<ProgramStageInstance> getProgramStageInstancesBetween( int min, int max )
    {
        return i18n( i18nService, programStageInstanceStore.getAll( min, max ) );
    }

    // Supportive Methods
    public List<ProgramStageInstance> getFilterProgramStageInstancesBetween(
        List<ProgramStageInstance> programStageInstances, int min, int max )
    {
        String programInstanceIdsByComma = "-1";
        for ( ProgramStageInstance programStageInstance : programStageInstances )
        {
            programInstanceIdsByComma += "," + programStageInstance.getId();
        }

        // System.out.println( "  filter size " + programStageInstances.size()
        // );
        // System.out.println( "  filter condition  " +
        // programInstanceIdsByComma );

        return i18n( i18nService,
            programStageInstanceStore.getFilteredProgramStageInstances( programInstanceIdsByComma, min, max ) );
    }

    // --------------------------------------------------------------------------------
    // Get Tracked Entity Instance Attribute Values by attributeIds
    // --------------------------------------------------------------------------------
    public Map<String, String> getTrackedEntityInstanceAttributeValuesByAttributeIds( String attributeIdsByComma )
    {
        Map<String, String> teiValueMap = new HashMap<String, String>();

        // SELECT trackedentityinstanceid, trackedentityattributeid, created,
        // lastupdated,
        // value, encryptedvalue
        // FROM trackedentityattributevalue where trackedentityattributeid IN(
        // 89,698,699) order by trackedentityattributeid;

        try
        {
            String query = "SELECT trackedentityinstanceid, trackedentityattributeid, value FROM trackedentityattributevalue "
                + "WHERE trackedentityattributeid IN ( " + attributeIdsByComma + ")";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiId = rs.getInt( 1 );
                Integer teiAttributeId = rs.getInt( 2 );
                String teiAttributeValue = rs.getString( 3 );

                if ( teiAttributeValue != null )
                {
                    teiValueMap.put( teiId + ":" + teiAttributeId, teiAttributeValue );
                }
            }

            return teiValueMap;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }

    // --------------------------------------------------------------------------------
    // Get Tracked Entity Instance Attribute Values by attributeIds
    // --------------------------------------------------------------------------------
    public Map<Integer, String> getTrackedEntityInstanceAttributeValuesByAttributeId( Integer attributeId )
    {
        Map<Integer, String> teiValueMap = new HashMap<Integer, String>();

        // SELECT trackedentityinstanceid, value FROM
        // trackedentityattributevalue where trackedentityattributeid IN( 89 )
        // order by value;

        try
        {
            String query = "SELECT trackedentityinstanceid, value FROM trackedentityattributevalue "
                + "WHERE trackedentityattributeid IN ( " + attributeId + ")";

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

    // --------------------------------------------------------------------------------
    // Get Tracked Entity Instance Attribute Values by attributeIds
    // --------------------------------------------------------------------------------
    public Map<String, String> getTrackedEntityDataValuesByDataElementIds( String dataElementIdsByComma )
    {
        Map<String, String> teiDataValueMap = new HashMap<String, String>();

        // SELECT programstageinstanceid, dataelementid, value from
        // trackedentitydatavalue WHERE dataelementid in
        // (1951,1952,1953,1954,1955);
        try
        {
            String query = "SELECT programstageinstanceid, dataelementid, value FROM trackedentitydatavalue  "
                + "WHERE dataelementid IN ( " + dataElementIdsByComma + ")";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer psiId = rs.getInt( 1 );
                Integer teiDataElementId = rs.getInt( 2 );
                String teiDataValue = rs.getString( 3 );

                if ( teiDataValue != null )
                {
                    teiDataValueMap.put( psiId + ":" + teiDataElementId, teiDataValue );
                }
            }

            return teiDataValueMap;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal DataElement id", e );
        }
    }

    // --------------------------------------------------------------------------------
    // Get Active Tracked Entity Instance List By Program
    // --------------------------------------------------------------------------------
    public List<ProgramStageInstance> getProgramStageInstanceList( String query )
    {
        List<ProgramStageInstance> peiList = new ArrayList<ProgramStageInstance>();
        System.out.println( " query -- " + query );
        try
        {
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer psiId = rs.getInt( 1 );

                if ( psiId != null )
                {
                    // System.out.println( " psiId -- " + psiId );
                    ProgramStageInstance psi = programStageInstanceService.getProgramStageInstance( psiId );
                    peiList.add( psi );
                    // System.out.println( " psi--Id -- " + psi.getId() );
                }
            }

            return peiList;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Program id", e );
        }
    }

    // --------------------------------------------------------------------------------
    // Get Tracked Entity Instance Attribute Values by attributeIds
    // --------------------------------------------------------------------------------
    public Map<String, String> getTrackedEntityAttributeValue( String attributeId )
    {
        Map<String, String> teiAttributeValueMap = new HashMap<String, String>();

        // SELECT programstageinstanceid, dataelementid, value from
        // trackedentitydatavalue WHERE dataelementid in
        // (1951,1952,1953,1954,1955);
        try
        {
            String query = "SELECT tei.uid, teav.value FROM trackedentityattributevalue teav "
                + " INNER JOIN trackedentityinstance tei ON tei.trackedentityinstanceid = teav.trackedentityinstanceid "
                + " WHERE teav.trackedentityattributeid IN ( " + attributeId + ")";

            /*
             * SELECT tei.uid, teav.value FROM trackedentityattributevalue teav
             * INNER JOIN trackedentityinstance tei ON
             * tei.trackedentityinstanceid = teav.trackedentityinstanceid where
             * teav.trackedentityattributeid in (1112 );
             */

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String teiUid = rs.getString( 1 );
                String teiAttrValue = rs.getString( 2 );

                if ( teiAttrValue != null )
                {
                    teiAttributeValueMap.put( teiUid, teiAttrValue );
                }
            }

            return teiAttributeValueMap;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }

    // --------------------------------------------------------------------------------
    // Get Program List By Program ProgramStage Name
    // --------------------------------------------------------------------------------
    public List<Program> getProgramsByProgramStageName( String programStageName )
    {
        List<Program> programiList = new ArrayList<Program>();

        try
        {
            String query = "SELECT programid from programstage  " + " WHERE name ILIKE '%" + programStageName + "%' ";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer pId = rs.getInt( 1 );

                if ( pId != null )
                {
                    // System.out.println( " psiId -- " + psiId );
                    Program program = programService.getProgram( pId );
                    programiList.add( program );
                }
            }

            return programiList;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Program id", e );
        }
    }

}