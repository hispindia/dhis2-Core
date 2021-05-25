package org.hisp.dhis.inspectionmanagement.escalations.action;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.jexl2.UnifiedJEXL.Exception;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dxf2.events.event.DataValue;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.dxf2.events.event.EventService;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.eventdatavalue.EventDataValue;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class UpdateEscalationsStatusAction implements Action
{
    public static final int ESCALATION_STATUS_DATAELEMENT_ID = 3460;
    public static final String ESCALATION_STATUS_UPDATE_AUTHORITIES = "F_ESCALATION_STATUS_UPDATE";
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private ProgramStageInstanceService programStageInstanceService;

    @Autowired
    private DataElementService dataElementService;
    
    /*
    @Autowired
    private TrackedEntityDataValueService trackedEntityDataValueService;
    */
    
    @Autowired
    private CurrentUserService currentUserService;
    
    @Autowired
    private EventService eventService;
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private String psiUid;

    public void setPsiUid( String psiUid )
    {
        this.psiUid = psiUid;
    }

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private String message;

    public String getMessage()
    {
        return message;
    }
    
    private ProgramStageInstance programStageInstance;
    
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------
    @Override
    public String execute()throws Exception
    {
        programStageInstance = programStageInstanceService.getProgramStageInstance( psiUid );
        
        System.out.println( programStageInstance.getId() + " -- " + programStageInstance.getStatus().toString() );
        
        String storedBy = currentUserService.getCurrentUsername();
        //User currentUser = currentUserService.getCurrentUser();
        
        /*
        for( String authoritie : currentUser.getUserCredentials().getAllAuthorities())
        {
            System.out.println( " authoritie -- " + authoritie );
        }
        */
        System.out.println(  " Final authoritie -- " + currentUserService.currentUserIsAuthorized( ESCALATION_STATUS_UPDATE_AUTHORITIES ) );
        
        Date now = new Date();
        // update program stage instance
        if( currentUserService.currentUserIsAuthorized( ESCALATION_STATUS_UPDATE_AUTHORITIES ) )
        {
            if ( programStageInstance != null )
            {
                EventStatus eventStatus = EventStatus.ACTIVE;
                programStageInstance.setStatus( eventStatus );
                //programStageInstanceService.updateProgramStageInstance( programStageInstance );
                
                DataElement psDataElement = dataElementService.getDataElement( ESCALATION_STATUS_DATAELEMENT_ID );
                if( psDataElement != null)
                {
                    DataValue updateEventDataValue = new DataValue();
                    Event updatedEvent = eventService.getEvent( programStageInstance );
                    Set<DataValue> eventDataValues = new HashSet<>( updatedEvent.getDataValues() );
                    Set<DataValue> updatedEventDataValues = new HashSet<>();
                    for( DataValue edv : eventDataValues )
                    {
                        System.out.println( edv.getDataElement() + " value -- " + edv.getValue() );
                        if( edv.getDataElement().equalsIgnoreCase( psDataElement.getUid() ) && edv.getValue().equalsIgnoreCase( "1" ))
                        {
                            System.out.println( edv.getDataElement() + " inside update value -- " + edv.getValue() );
                            updateEventDataValue.setDataElement( edv.getDataElement() );
                            updateEventDataValue.setValue( "2" );
                        }
                    }
                    
                    
                    updatedEventDataValues.add( updateEventDataValue );
                    
                    updatedEvent.setStatus( eventStatus );
                    updatedEvent.setDataValues( updatedEventDataValues );
                    
                    ImportSummary importSummary = eventService.updateEvent( updatedEvent, true, null, false );
                    
                    System.out.println(  " importSummary -- " + importSummary);
                    
                    //TrackedEntityDataValue trackedEntityDataValue = trackedEntityDataValueService.getTrackedEntityDataValue( programStageInstance, psDataElement );
                
                    //Set<EventDataValue> tempEventDataValues = new HashSet<EventDataValue>( programStageInstance.getEventDataValues());
                    
                    
                    
                    //List<EventDataValue> tempEventDataValues = new CopyOnWriteArrayList<>( programStageInstance.getEventDataValues() );
                    
                    /*
                    CopyOnWriteArraySet<EventDataValue> tempEventDataValues = new CopyOnWriteArraySet<EventDataValue>( programStageInstance.getEventDataValues() );
                    
                    System.out.println(  " tempEventDataValues -- " + tempEventDataValues.size() );
                    */
                    
                    /*
                    
                    for( Iterator<EventDataValue> edv = tempEventDataValues.iterator(); edv.hasNext(); )
                    {
                        EventDataValue tempEdv = edv.next(); 
                        System.out.println( tempEdv.getDataElement() + " tempEdv value -- " + tempEdv.getValue() );
                        if( tempEdv.getDataElement().equalsIgnoreCase( psDataElement.getUid() ) && tempEdv.getValue().equalsIgnoreCase( "1" ))
                        {
                            System.out.println( tempEdv.getDataElement() + " inside update value -- " + tempEdv.getValue() );
                            edv.remove();
                            EventDataValue updateEdv = new EventDataValue();
                            updateEdv.setDataElement( tempEdv.getDataElement() );
                            updateEdv.setValue( "2" );
                            updateEdv.setStoredBy( storedBy );
                            updateEdv.setLastUpdated( now );
                            tempEventDataValues.add( updateEdv );
                            
                            
                        }
                        
                    }
                    */
                    
                   
                    /*
                    for( EventDataValue edv : tempEventDataValues )
                    {
                        //edv.getDataElement();
                        //edv.getValue();
                        System.out.println( edv.getDataElement() + " value -- " + edv.getValue() );
                        if( edv.getDataElement().equalsIgnoreCase( psDataElement.getUid() ) && edv.getValue().equalsIgnoreCase( "1" ))
                        {
                            System.out.println( edv.getDataElement() + " inside update value -- " + edv.getValue() );
                            EventDataValue updateEdv = new EventDataValue();
                            updateEdv.setDataElement( edv.getDataElement() );
                            updateEdv.setValue( "2" );
                            updateEdv.setStoredBy( storedBy );
                            updateEdv.setLastUpdated( now );
                            tempEventDataValues.add( updateEdv );
                        }
                    }
                    
                    
                    for( EventDataValue edv : tempEventDataValues )
                    {
                        //edv.getDataElement();
                        //edv.getValue();
                        System.out.println( edv.getDataElement() + " after update value -- " + edv.getValue() );
                        
                    }                    
                    programStageInstance.setEventDataValues( tempEventDataValues );
                    programStageInstanceService.updateProgramStageInstance( programStageInstance );
                    */
                    
                    /*
                    if( trackedEntityDataValue != null && trackedEntityDataValue.getValue().equalsIgnoreCase("1")  )
                    {
                        trackedEntityDataValue.setValue( "2" );
                        trackedEntityDataValue.setStoredBy( storedBy );
                        trackedEntityDataValue.setLastUpdated( now );

                        trackedEntityDataValueService.updateTrackedEntityDataValue( trackedEntityDataValue );
                    }
                    */
                    
                    /*
                    if ( trackedEntityDataValue == null )
                    {
                        boolean providedElsewhere = false;
                        trackedEntityDataValue = new TrackedEntityDataValue( programStageInstance, psDataElement, "2" );
                        
                        trackedEntityDataValue.setProgramStageInstance( programStageInstance );
                        trackedEntityDataValue.setProvidedElsewhere( providedElsewhere );
                        trackedEntityDataValue.setValue( "2" );
                        trackedEntityDataValue.setCreated( now );
                        trackedEntityDataValue.setLastUpdated( now );
                        trackedEntityDataValue.setStoredBy( storedBy );

                        trackedEntityDataValueService.saveTrackedEntityDataValue( trackedEntityDataValue );
                    }
                    else
                    {
                        trackedEntityDataValue.setValue( "2" );
                        trackedEntityDataValue.setStoredBy( storedBy );
                        trackedEntityDataValue.setLastUpdated( now );

                        trackedEntityDataValueService.updateTrackedEntityDataValue( trackedEntityDataValue );
                    }
                    */
                    
                }
                            
            }
        }
        
        return SUCCESS;
    }
}

