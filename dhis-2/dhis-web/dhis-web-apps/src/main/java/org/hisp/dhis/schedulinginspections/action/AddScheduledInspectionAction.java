package org.hisp.dhis.schedulinginspections.action;

import static org.hisp.dhis.i18n.I18nUtils.i18n;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.jexl2.UnifiedJEXL.Exception;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageDataElement;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStageInstanceStore;
import org.hisp.dhis.program.ProgramStageService;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValue;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class AddScheduledInspectionAction
    implements Action
{
    public static final String PROGRAM_STATUS = "ACTIVE";

    public static final String PROGRAM_STAGE_INSTANCE_STATUS = "SCHEDULE";

    public static final String PREFIX_DATAELEMENT = "deps";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private ProgramStageService programStageService;

    @Autowired
    private ProgramStageInstanceService programStageInstanceService;

    @Autowired
    private ProgramStageInstanceStore programStageInstanceStore;

    @Autowired
    private ProgramInstanceService programInstanceService;

    @Autowired
    private TrackedEntityDataValueService trackedEntityDataValueService;
    
    @Autowired
    protected DataElementCategoryService categoryService;
    
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

    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private Integer trackedEntityInstanceId;

    public void setTrackedEntityInstanceId( Integer trackedEntityInstanceId )
    {
        this.trackedEntityInstanceId = trackedEntityInstanceId;
    }

    private Integer programId;
    
    public void setProgramId( Integer programId )
    {
        this.programId = programId;
    }

    private Integer programStageId;

    public void setProgramStageId( Integer programStageId )
    {
        this.programStageId = programStageId;
    }

    private String deps1951;
    
    public void setDeps1951( String deps1951 )
    {
        this.deps1951 = deps1951;
    }
    
    private Set<TrackedEntityInstance> programStageInstanceMembers = new HashSet<TrackedEntityInstance>();
    
    private SimpleDateFormat simpleDateFormat;
    
    public SimpleDateFormat getSimpleDateFormat()
    {
        return simpleDateFormat;
    }
    
   private boolean listAll = true;
    
    public boolean isListAll()
    {
        return listAll;
    }

    public void setListAll( boolean listAll )
    {
        this.listAll = listAll;
    }
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        HttpServletRequest request = ServletActionContext.getRequest();

        Date now = new Date();
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        String storedBy = currentUserService.getCurrentUsername();

        // User user = currentUserService.getCurrentUser();

        Program program = programService.getProgram( programId );

        ProgramStage programStage = programStageService.getProgramStage( programStageId );

        // Integer programInstanceId = getProgramInstanceId(
        // trackedEntityInstanceId, program.getId(), PROGRAM_STATUS );

        ProgramInstance programInstance = programInstanceService.getProgramInstance( getProgramInstanceId(
            trackedEntityInstanceId, program.getId(), PROGRAM_STATUS ) );

        // create programStageInstance or Event for SCHEDULE the Event
        
        programStageInstanceMembers = new HashSet<TrackedEntityInstance>();
        
        //programStageInstanceMembers.add( programInstance.getEntityInstance() );
        
        Integer programStageInstanceId = null;
        
        DataElementCategoryOptionCombo coc = null;
        coc = categoryService.getDefaultDataElementCategoryOptionCombo();
        
        EventStatus eventStatus = EventStatus.SCHEDULE;
        ProgramStageInstance programStageInstance = new ProgramStageInstance();
        
//        Date executionDate = new Date();
//
//        executionDate = DateUtils.parseDate( simpleDateFormat.format( now ) );
//        
        
//        if ( event.getEventDate() != null )
//        {
//            executionDate = DateUtils.parseDate( event.getEventDate() );
//            programStageInstance.setExecutionDate( executionDate );
//        }
        
        
        if ( programInstance != null )
        {
            programStageInstance = new ProgramStageInstance();

            programStageInstance.setProgramInstance( programInstance );
            programStageInstance.setProgramStage( programStage );
            //programStageInstance.setProgramStageInstanceMembers( programStageInstanceMembers );
            programStageInstance.setAttributeOptionCombo( coc );  
            programStageInstance.setOrganisationUnit( programInstance.getEntityInstance().getOrganisationUnit() );
            //programStageInstance.setExecutionDate( executionDate );
            programStageInstance.setStatus( eventStatus );
            programStageInstance.setDueDate( format.parseDate( deps1951 ) ); // format is yyyy-mm-dd
            programStageInstance.setCreated( now );
            programStageInstance.setStoredBy( storedBy );
            programStageInstance.setLastUpdated( now );

            programStageInstanceId = programStageInstanceService.addProgramStageInstance( programStageInstance );
        }

        // add trackedEntityDataValue for for SCHEDULE the
        // event/programStageInstance

       
        
        ProgramStageInstance tempProgramStageInstance = programStageInstanceService
            .getProgramStageInstance( programStageInstanceId );

        String value = null;

        List<ProgramStageDataElement> programStageDataElements = new ArrayList<ProgramStageDataElement>(
            programStage.getProgramStageDataElements() );

        if ( programStageDataElements != null && programStageDataElements.size() > 0 )
        {
//            if ( tempProgramStageInstance.getExecutionDate() == null )
//            {
//                tempProgramStageInstance.setExecutionDate( executionDate );
//                programStageInstanceService.updateProgramStageInstance( tempProgramStageInstance );
//            }

            for ( ProgramStageDataElement programStageDataElement : programStageDataElements )
            {
                value = request.getParameter( PREFIX_DATAELEMENT + programStageDataElement.getDataElement().getId() );

                // TrackedEntityDataValue trackedEntityDataValue = new
                // TrackedEntityDataValue( tempProgramStageInstance,
                // programStageDataElement.getDataElement(), value );

                TrackedEntityDataValue trackedEntityDataValue = trackedEntityDataValueService
                    .getTrackedEntityDataValue( tempProgramStageInstance, programStageDataElement.getDataElement() );

                if ( trackedEntityDataValue == null )
                {
                    if ( value != null && StringUtils.isNotBlank( value ) )
                    {
                        boolean providedElsewhere = false;

                        trackedEntityDataValue = new TrackedEntityDataValue( tempProgramStageInstance,
                            programStageDataElement.getDataElement(), value );
                        trackedEntityDataValue.setProgramStageInstance( programStageInstance );
                        trackedEntityDataValue.setProvidedElsewhere( providedElsewhere );
                        trackedEntityDataValue.setValue( value );
                        // trackedEntityDataValue.setAutoFields();
                        trackedEntityDataValue.setCreated( now );
                        trackedEntityDataValue.setLastUpdated( now );
                        trackedEntityDataValue.setStoredBy( storedBy );

                        trackedEntityDataValueService.saveTrackedEntityDataValue( trackedEntityDataValue );
                    }
                }
                else
                {
                    trackedEntityDataValue.setValue( value );
                    // trackedEntityDataValue.setAutoFields();
                    trackedEntityDataValue.setStoredBy( storedBy );
                    trackedEntityDataValue.setLastUpdated( now );

                    trackedEntityDataValueService.updateTrackedEntityDataValue( trackedEntityDataValue );
                }

            }
        }

        // TrackedEntityInstance trackedEntityInstance = new
        // TrackedEntityInstance();
        //
        // ProgramInstance programInstance = new ProgramInstance();
        //
        // programInstanceService
        //
        // programInstance.getProgram().getp
        //
        // programInstance.getEntityInstance().getOrganisationUnit();
        //
        // ProgramStageInstance programStageInstance = new
        // ProgramStageInstance();
        //
        // programStageInstance.setProgramInstance( programInstance );
        // programStageInstance.setProgramStage( programStage );
        // programStageInstance.setProgramStageInstanceMembers(
        // programStageInstanceMembers );
        // programStageInstance.setExecutionDate( new Date() );
        // programStageInstance.setDueDate( new Date() );
        // programStageInstance.setCreated( new Date() );
        // programStageInstance.setLastUpdated( new Date );
        // programStageInstance.setOrganisationUnit(
        // programInstance.getEntityInstance().getOrganisationUnit() );
        //
        // programStageInstanceService.addProgramStageInstance(
        // programStageInstance );

        // programStageInstance.setp

        // TrackedEntityDataValue trackedEntityDataValue = new
        // TrackedEntityDataValue();
        // trackedEntityDataValue.setDataElement( dataElement );
        // trackedEntityDataValue.setProgramStageInstance( programStageInstance
        // );
        // trackedEntityDataValue.setProvidedElsewhere( false );
        // trackedEntityDataValue.setValue( value );
        // trackedEntityDataValue.setLastUpdated( new Date() );
        // trackedEntityDataValue.setStoredBy( storedBy );
        // trackedEntityDataValue.setAutoFields();
        // trackedEntityDataValue.setCreated( new Date() );
        // trackedEntityDataValueService.saveTrackedEntityDataValue(
        // trackedEntityDataValue );

        return SUCCESS;
    }

    // Supportive Methods
    public List<ProgramStageInstance> getProgramStageInstancesBetween( int min, int max )
    {
        return i18n( i18nService, programStageInstanceStore.getAll( min, max ) );
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

    // get ProgramInstanceId from trackedEntityInstanceId and programId and
    // programStatus
    public Integer getProgramInstanceId( Integer trackedEntityInstanceId, Integer programId, String programStatus )
    {
        Integer programInstanceId = null;

        try
        {
            String query = "SELECT programinstanceid FROM programinstance WHERE trackedentityinstanceid = "
                + trackedEntityInstanceId + " AND " + " programid = " + programId + " AND status = '" + programStatus
                + "'";

            // SELECT programinstanceid from programinstance where
            // trackedentityinstanceid = 436 and programid = 115 and status =
            // 'ACTIVE';

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            if ( rs.next() )
            {
                programInstanceId = rs.getInt( 1 );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return programInstanceId;
    }

}
