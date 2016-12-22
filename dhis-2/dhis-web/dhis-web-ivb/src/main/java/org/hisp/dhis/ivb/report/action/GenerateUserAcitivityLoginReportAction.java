package org.hisp.dhis.ivb.report.action;

import java.util.ArrayList;
import java.util.List;

import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.ivb.util.UserDetails;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;

import com.opensymphony.xwork2.Action;

public class GenerateUserAcitivityLoginReportAction implements Action
{
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
    }

    private MessageService messageService;

    public void setMessageService( MessageService messageService )
    {
        this.messageService = messageService;
    }

    private ConfigurationService configurationService;

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    private IVBUtil ivbUtil;

    public void setIvbUtil( IVBUtil ivbUtil )
    {
        this.ivbUtil = ivbUtil;
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------
	
    private String language;

    private String userName;

    public String getLanguage()
    {
        return language;
    }

    public String getUserName()
    {
        return userName;
    }

    private int messageCount;

    public int getMessageCount()
    {
        return messageCount;
    }

    private String adminStatus;

    public String getAdminStatus()
    {
        return adminStatus;
    }

    private List<UserDetails> userDetails = new ArrayList<UserDetails>();
    
    public List<UserDetails> getUserDetails() 
    {
		return userDetails;
	}

	// --------------------------------------------------------------------------
    // Action implementation
    // --------------------------------------------------------------------------
    public String execute()
    {
        userName = currentUserService.getCurrentUser().getUsername();
        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }
        messageCount = (int) messageService.getUnreadMessageConversationCount();
        List<UserGroup> userGrps = new ArrayList<UserGroup>( currentUserService.getCurrentUser().getGroups() );
        if ( userGrps.contains( configurationService.getConfiguration().getFeedbackRecipients() ) )
        {
            adminStatus = "Yes";
        }
        else
        {
            adminStatus = "No";
        }

        userDetails.addAll( ivbUtil.getUserDetails() );
        
        return SUCCESS;
    }

}
