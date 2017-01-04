package org.hisp.dhis.schedulinginspections.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.jexl2.UnifiedJEXL.Exception;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.program.ProgramStageDataElement;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStageInstanceStore;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValue;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class UpdateScheduledInspectionAction implements Action
{
    public static final String PREFIX_DATAELEMENT = "deps";
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private ProgramStageInstanceService programStageInstanceService;

    @Autowired
    private ProgramStageInstanceStore programStageInstanceStore;
    
    @Autowired
    private CurrentUserService currentUserService;
    
    @Autowired
    private TrackedEntityDataValueService trackedEntityDataValueService;
    
    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }
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
    
    private String deps1951;
    
    public void setDeps1951( String deps1951 )
    {
        this.deps1951 = deps1951;
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
    private ProgramStageInstance programStageInstance;
    
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------
    @Override
    public String execute()throws Exception
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        
        programStageInstance = programStageInstanceService.getProgramStageInstance( programStageInstanceId );
        
        //programStageInstance.getProgramInstance().getProgram().getName();
        //programStageInstance.getProgramStage().getName();
        String storedBy = currentUserService.getCurrentUsername();
        Date now = new Date();
        
        // update program stage instance
        if ( programStageInstance != null )
        {
            //programStageInstance.setProgramInstance( programStageInstance.getProgramInstance() );
            //programStageInstance.setProgramStage( programStageInstance.getProgramStage() );
            
            programStageInstance.setDueDate( format.parseDate( deps1951 ) ); // format is yyyy-mm-dd
            
            programStageInstance.setStoredBy( storedBy );
            programStageInstance.setLastUpdated( now );

            programStageInstanceService.updateProgramStageInstance( programStageInstance );
        }

        // update trackedEntityDataValue for for SCHEDULE the event/programStageInstance
        
        String value = null;

        List<ProgramStageDataElement> programStageDataElements = new ArrayList<ProgramStageDataElement>(
            programStageInstance.getProgramStage().getProgramStageDataElements() );

        if ( programStageDataElements != null && programStageDataElements.size() > 0 )
        {

            for ( ProgramStageDataElement programStageDataElement : programStageDataElements )
            {
                value = request.getParameter( PREFIX_DATAELEMENT + programStageDataElement.getDataElement().getId() );

                TrackedEntityDataValue trackedEntityDataValue = trackedEntityDataValueService
                    .getTrackedEntityDataValue( programStageInstance, programStageDataElement.getDataElement() );

                if ( trackedEntityDataValue == null )
                {
                    if ( value != null && StringUtils.isNotBlank( value ) )
                    {
                        boolean providedElsewhere = false;

                        trackedEntityDataValue = new TrackedEntityDataValue( programStageInstance,
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
        
        return SUCCESS;
    }
}
