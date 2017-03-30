package org.hisp.dhis.ivb.dataentry.offline.upload.action;

import java.util.ArrayList;
import java.util.List;

import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserGroup;

import com.opensymphony.xwork2.Action;

/**
 * @author BHARATH
 */

public class OfflineDataUploadFormAction implements Action
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
    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    
    private String userName;

    public String getUserName()
    {
        return userName;
    }

    private String language;

    public String getLanguage()
    {
        return language;
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
    private String conflictAuthority = "";

    public String getConflictAuthority()
    {
        return conflictAuthority;
    }
    
    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------
    public String execute()
    {
        User currentUser = currentUserService.getCurrentUser();
        
        userName = currentUser.getUsername();
        
        if( i18nService.getCurrentLocale() == null )
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
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( currentUser.getUserCredentials()
            .getUserAuthorityGroups() );

        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {            
            if ( userAuthorityGroup.getAuthorities().contains( "F_DATAVALUE_CONFLICT" ) )
            {
                conflictAuthority = "Yes";
            }
            else
            {
                conflictAuthority = "No";
            }            
        }
        return SUCCESS;
    }

}
