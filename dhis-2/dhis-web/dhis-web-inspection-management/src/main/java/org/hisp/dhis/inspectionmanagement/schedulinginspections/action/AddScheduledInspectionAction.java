package org.hisp.dhis.inspectionmanagement.schedulinginspections.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dxf2.events.event.DataValue;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.dxf2.events.event.EventService;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.eventdatavalue.EventDataValue;
import org.hisp.dhis.i18n.I18nFormat;
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
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class AddScheduledInspectionAction  implements Action
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

    /*
    @Autowired
    private TrackedEntityDataValueService trackedEntityDataValueService;
    */
    
    @Autowired
    protected CategoryService categoryService;
    
    @Autowired
    private EventService eventService;
    
    /*
    @Autowired
    private I18nService i18nService;
    */
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    // scheduleDate duedate
    private String depsFFgsHumW2e4;
    
    public void setDepsFFgsHumW2e4( String depsFFgsHumW2e4 )
    {
        this.depsFFgsHumW2e4 = depsFFgsHumW2e4;
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

        ProgramInstance programInstance = programInstanceService.getProgramInstance( getProgramInstanceId( trackedEntityInstanceId, (int)program.getId(), PROGRAM_STATUS ) );

        // create programStageInstance or Event for SCHEDULE the Event
        
        programStageInstanceMembers = new HashSet<TrackedEntityInstance>();
        
        //programStageInstanceMembers.add( programInstance.getEntityInstance() );
        
        Integer programStageInstanceId = null;
        
        CategoryOptionCombo coc = null;
        coc = categoryService.getDefaultCategoryOptionCombo();
        
        System.out.println(  " due date -- " + format.parseDate( depsFFgsHumW2e4 ) );
        
        EventStatus eventStatus = EventStatus.SCHEDULE;
        ProgramStageInstance programStageInstance = new ProgramStageInstance();
           
        Map<DataElement, EventDataValue> dataElementEventDataValueMap = new HashMap<DataElement, EventDataValue>();
        boolean providedElsewhere = false;
        String value = null;
        List<ProgramStageDataElement> programStageDataElements = new ArrayList<ProgramStageDataElement>( programStage.getProgramStageDataElements() );
        
        if ( programStageDataElements != null && programStageDataElements.size() > 0 )
        {
            for ( ProgramStageDataElement programStageDataElement : programStageDataElements )
            {
                EventDataValue eventDataValue = new EventDataValue();
                value = request.getParameter( PREFIX_DATAELEMENT + programStageDataElement.getDataElement().getUid() );
                
                eventDataValue.setDataElement( programStageDataElement.getDataElement().getUid() );
                eventDataValue.setValue( value ); // for Expired
                eventDataValue.setProvidedElsewhere( providedElsewhere );
                eventDataValue.setStoredBy( storedBy );
                dataElementEventDataValueMap.put( programStageDataElement.getDataElement(), eventDataValue );
            }
        }
        
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
            programStageInstance.setDueDate( format.parseDate( depsFFgsHumW2e4 ) ); // for dueDate format is yyyy-mm-dd
            programStageInstance.setCreated( now );
            programStageInstance.setStoredBy( storedBy );
            programStageInstance.setLastUpdated( now );

            //programStageInstanceId = (int)programStageInstanceService.addProgramStageInstance( programStageInstance );
            
            programStageInstanceService.saveEventDataValuesAndSaveProgramStageInstance( programStageInstance, dataElementEventDataValueMap );
        }

        // add trackedEntityDataValue for for SCHEDULE the
        // event/programStageInstance

        
        //ProgramStageInstance tempProgramStageInstance = programStageInstanceService.getProgramStageInstance( programStageInstanceId );
        /*
        Set<DataValue> updatedEventDataValues = new HashSet<>();
        Event updatedEvent = eventService.getEvent( tempProgramStageInstance );
        
        if ( programStageDataElements != null && programStageDataElements.size() > 0 )
        {

            for ( ProgramStageDataElement programStageDataElement : programStageDataElements )
            {
                //value = request.getParameter( PREFIX_DATAELEMENT + programStageDataElement.getDataElement().getId() );
                value = request.getParameter( PREFIX_DATAELEMENT + programStageDataElement.getDataElement().getUid() );
                
                EventDataValue eventDataValue = new EventDataValue();
                eventDataValue.setDataElement( programStageDataElement.getDataElement().getUid() );
                eventDataValue.setValue( value ); // for Expired
                eventDataValue.setProvidedElsewhere( providedElsewhere );
                eventDataValue.setStoredBy( storedBy );
                dataElementEventDataValueMap.put( programStageDataElement.getDataElement(), eventDataValue );
                
                DataValue updateEventDataValue = new DataValue();
                updateEventDataValue.setDataElement( programStageDataElement.getDataElement().getUid() );
                updateEventDataValue.setValue( value );
                updateEventDataValue.setProvidedElsewhere( providedElsewhere );
                
                updatedEventDataValues.add( updateEventDataValue );
            }
            
            //updatedEvent.setStatus( eventStatus );
            updatedEvent.setDataValues( updatedEventDataValues );
            
            ImportSummary importSummary = eventService.updateEvent( updatedEvent, true, null, false );
            System.out.println(  " importSummary -- " + importSummary.toString());
            
            //importSummaries.getImportSummaries().get( 0 );
        }
        */
        

        return SUCCESS;
    }

    // Supportive Methods
    /*
    public List<ProgramStageInstance> getProgramStageInstancesBetween( int min, int max )
    {
        return i18n( i18nService, programStageInstanceStore.getAll( min, max ) );
    }
    */
    

    // --------------------------------------------------------------------------------
    // Get Tracked Entity Instance Attribute Values by attributeIds
    // --------------------------------------------------------------------------------
    /*
    public Map<String, String> getTrackedEntityInstanceAttributeValuesByAttributeIds( String attributeIdsByComma )
    {
        Map<String, String> teiValueMap = new HashMap<String, String>();

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
*/
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
