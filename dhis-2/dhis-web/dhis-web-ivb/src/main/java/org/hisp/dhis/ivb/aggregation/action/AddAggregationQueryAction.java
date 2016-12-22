package org.hisp.dhis.ivb.aggregation.action;

import java.util.ArrayList;
import java.util.List;

//import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.caseaggregation.CaseAggregationCondition;
import org.hisp.dhis.caseaggregation.CaseAggregationConditionService;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.AggregationManager;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;

import com.opensymphony.xwork2.Action;

public class AddAggregationQueryAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private CaseAggregationConditionService aggregationConditionService;

    public void setAggregationConditionService( CaseAggregationConditionService aggregationConditionService )
    {
        this.aggregationConditionService = aggregationConditionService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private LookupService lookupService;

    public void setLookupService( LookupService lookupService )
    {
        this.lookupService = lookupService;
    }

    private AggregationManager aggregationManager;

    public void setAggregationManager( AggregationManager aggregationManager )
    {
        this.aggregationManager = aggregationManager;
    }

    private DataElementCategoryService dataElementCategoryService;

    public void setDataElementCategoryService( DataElementCategoryService dataElementCategoryService )
    {
        this.dataElementCategoryService = dataElementCategoryService;
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

    private Integer queryDeId;

    public void setQueryDeId( Integer queryDeId )
    {
        this.queryDeId = queryDeId;
    }

    private String optionSetOption;

    public void setOptionSetOption( String optionSetOption )
    {
        this.optionSetOption = optionSetOption;
    }

    private String name;

    public void setName( String name )
    {
        this.name = name;
    }

    private String aggType;

    public void setAggType( String aggType )
    {
        this.aggType = aggType;
    }

    private Integer dataElementId;

    public void setDataElementId( Integer dataElementId )
    {
        this.dataElementId = dataElementId;
    }

    private Integer keyFlagIndicatorId;
    
    public void setKeyFlagIndicatorId( Integer keyFlagIndicatorId )
    {
        this.keyFlagIndicatorId = keyFlagIndicatorId;
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
        DataElement aggDataElement = dataElementService.getDataElement( dataElementId );
        
        //DataElement queryDataElement = dataElementService.getDataElement( queryDeId );

        if( aggType.equals( Lookup.AGG_TYPE_BY_OPTION ) )
        {
            String optionSetId = optionSetOption.split( "#@#" )[0];
            
            String optionValue = optionSetOption.split( "#@#" )[1];
            
            String query = aggregationManager.prepareQueryForByOption( queryDeId, optionValue );
            
            CaseAggregationCondition condition = new CaseAggregationCondition( aggDataElement.getName(), aggType, query, aggDataElement, dataElementCategoryService.getDefaultDataElementCategoryOptionCombo() );
            
            aggregationConditionService.addCaseAggregationCondition( condition );
        }
        else if( aggType.equals( Lookup.AGG_TYPE_BY_OPTION_SCORE ) )
        {
            String query = aggregationManager.prepareQueryForByOptionScore( queryDeId );

            CaseAggregationCondition condition = new CaseAggregationCondition( aggDataElement.getName(), aggType, query, aggDataElement, dataElementCategoryService.getDefaultDataElementCategoryOptionCombo() );
            
            aggregationConditionService.addCaseAggregationCondition( condition );
        }
        else if( aggType.equals( Lookup.AGG_TYPE_BY_KEYFLAG ) )
        {
            String query = ""+keyFlagIndicatorId;
            
            CaseAggregationCondition condition = new CaseAggregationCondition( aggDataElement.getName(), aggType, query, aggDataElement, dataElementCategoryService.getDefaultDataElementCategoryOptionCombo() );
            
            aggregationConditionService.addCaseAggregationCondition( condition );
        }
        else if( aggType.equals( Lookup.AGG_TYPE_BY_KEYFLAG_SCORE ) )
        {
            String query = "NONE";
            
            CaseAggregationCondition condition = new CaseAggregationCondition( aggDataElement.getName(), aggType, query, aggDataElement, dataElementCategoryService.getDefaultDataElementCategoryOptionCombo() );
            
            aggregationConditionService.addCaseAggregationCondition( condition );
        }
           
        return SUCCESS;
    }

}
