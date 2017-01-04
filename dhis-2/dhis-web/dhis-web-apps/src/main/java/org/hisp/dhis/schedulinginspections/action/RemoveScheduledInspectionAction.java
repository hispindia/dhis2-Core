package org.hisp.dhis.schedulinginspections.action;

import org.apache.commons.jexl2.UnifiedJEXL.Exception;
import org.hisp.dhis.common.DeleteNotAllowedException;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class RemoveScheduledInspectionAction implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private ProgramStageInstanceService programStageInstanceService;

    @Autowired
    private TrackedEntityDataValueService trackedEntityDataValueService;
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private int id;

    public void setId( int id )
    {
        this.id = id;
    }

    private I18n i18n;

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
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
        //System.out.println( " ID -- " + id );
        programStageInstance = programStageInstanceService.getProgramStageInstance( id );
        
        //System.out.println( programStageInstance.getId() + " -- " + programStageInstance.getStatus().toString() );
        // update program stage instance
        if ( programStageInstance != null )
        {
            try
            {
                trackedEntityDataValueService.deleteTrackedEntityDataValue( programStageInstance );
                programStageInstanceService.deleteProgramStageInstance( programStageInstance );
                
                //System.out.println( programStageInstance.getId() + " deleted -- "  );
            }
            catch ( DeleteNotAllowedException ex )
            {
                if ( ex.getErrorCode().equals( DeleteNotAllowedException.ERROR_ASSOCIATED_BY_OTHER_OBJECTS ) )
                {
                    //System.out.println( programStageInstance.getId() + " not deleted -- "  + ex.getMessage());
                    message = i18n.getString( "object_not_deleted_associated_by_objects" ) + " " + ex.getMessage();

                    return ERROR;
                }
            }
        }
        
        return SUCCESS;
    }
}
