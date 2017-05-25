package org.hisp.dhis.escalations.action;

import java.util.Date;

import org.apache.commons.jexl2.UnifiedJEXL.Exception;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValue;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
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
    
    @Autowired
    private TrackedEntityDataValueService trackedEntityDataValueService;
    
    @Autowired
    private CurrentUserService currentUserService;
    
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
        System.out.println(  " Final authoritie -- " + currentUserService.currenUserIsAuthorized( ESCALATION_STATUS_UPDATE_AUTHORITIES ) );
        
        Date now = new Date();
        // update program stage instance
        if( currentUserService.currenUserIsAuthorized( ESCALATION_STATUS_UPDATE_AUTHORITIES ) )
        {
            if ( programStageInstance != null )
            {
                EventStatus eventStatus = EventStatus.ACTIVE;
                programStageInstance.setStatus( eventStatus );
                programStageInstanceService.updateProgramStageInstance( programStageInstance );
                
                DataElement psDataElement = dataElementService.getDataElement( ESCALATION_STATUS_DATAELEMENT_ID );
                if( psDataElement != null)
                {
                    TrackedEntityDataValue trackedEntityDataValue = trackedEntityDataValueService.getTrackedEntityDataValue( programStageInstance, psDataElement );
                
                    if( trackedEntityDataValue != null && trackedEntityDataValue.getValue().equalsIgnoreCase("1")  )
                    {
                     trackedEntityDataValue.setValue( "2" );
                        trackedEntityDataValue.setStoredBy( storedBy );
                        trackedEntityDataValue.setLastUpdated( now );

                        trackedEntityDataValueService.updateTrackedEntityDataValue( trackedEntityDataValue );
                    }
                    
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

