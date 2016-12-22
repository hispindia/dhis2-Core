package org.hisp.dhis.ivb.aggregation.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hisp.dhis.caseaggregation.CaseAggregationCondition;
import org.hisp.dhis.caseaggregation.CaseAggregationConditionService;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.paging.ActionPagingSupport;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserGroup;

import com.opensymphony.xwork2.Action;

public class AggregationQueryListAction 
        extends ActionPagingSupport<CaseAggregationCondition>
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private CaseAggregationConditionService aggregationConditionService;

    public void setAggregationConditionService( CaseAggregationConditionService aggregationConditionService )
    {
        this.aggregationConditionService = aggregationConditionService;
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
    // -------------------------------------------------------------------------
    // Input/ Output
    // -------------------------------------------------------------------------

    private Collection<CaseAggregationCondition> aggregationConditions;

    public Collection<CaseAggregationCondition> getAggregationConditions()
    {
        return aggregationConditions;
    }
    private String language;

    public String getLanguage()
    {
        return language;
    }

    private String userName;

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
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
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
        this.paging = createPaging(aggregationConditionService.getAggConditionCount());        
        
        aggregationConditions = new ArrayList<CaseAggregationCondition>(aggregationConditionService.getAggConditionsBetween(paging.getStartPos(),
            paging.getPageSize() ));
        
        return SUCCESS;
    }

}
