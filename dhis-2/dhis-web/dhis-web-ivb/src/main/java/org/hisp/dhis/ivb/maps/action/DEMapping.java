package org.hisp.dhis.ivb.maps.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.caseaggregation.CaseAggregationCondition;
import org.hisp.dhis.caseaggregation.CaseAggregationConditionService;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.i18n.I18nService;

import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;

import com.opensymphony.xwork2.Action;

public class DEMapping
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    private LookupService lookupService;

    public void setLookupService( LookupService lookupService )
    {
        this.lookupService = lookupService;
    }

    private List<DataElement> allDataElementList = new ArrayList<DataElement>();

    public List<DataElement> getAllDataElementList()
    {
        return allDataElementList;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }

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
    List<Lookup> lookups;

    public List<Lookup> getLookups()
    {
        return lookups;
    }

    List<DataElement> dataElementList;

    public List<DataElement> getDataElementList()
    {
        return dataElementList;
    }

    private String language;

    public String getLanguage()
    {
        return language;
    }
public String message;
    public String getMessage()
{
    return message;
}

public void setMessage( String message )
{
    this.message = message;
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
        lookups = new ArrayList<Lookup>( lookupService.getAllLookupsByType( Lookup.AGGREGATION_TYPE ) );

        List<CaseAggregationCondition> caseAggregationConditions = new ArrayList<CaseAggregationCondition>(
            aggregationConditionService.getCaseAggregationConditionByOperator( Lookup.AGG_TYPE_BY_KEYFLAG_SCORE ) );

        if ( caseAggregationConditions.size() > 0 )
        {
            Lookup lookup = lookupService.getLookupByName( Lookup.AGG_TYPE_BY_KEYFLAG_SCORE );
            lookups.remove( lookup );
        }
        /**
         * TODO - filter dataelements by creating attribute called 'is auto
         * populate by aggregation engine' 6
         * 
         */
        dataElementList = new ArrayList<DataElement>( dataElementService.getAllDataElements() );

        Constant ivbAggDEConst = constantService.getConstantByName( "IS_IVB_AGGREGATED_DE_ATTRIBUTE_ID" );
        Iterator<DataElement> iterator = dataElementList.iterator();
        List<DataElement> dataElements = new ArrayList<DataElement>();
        while ( iterator.hasNext() )
        {
            DataElement dataElement = iterator.next();

            Set<AttributeValue> dataElementAttributeValues = dataElement.getAttributeValues();
            if ( dataElementAttributeValues != null && dataElementAttributeValues.size() > 0 )
            {
                for ( AttributeValue deAttributeValue : dataElementAttributeValues )
                {
                    if ( deAttributeValue.getAttribute().getId() == ivbAggDEConst.getValue()
                        && deAttributeValue.getValue().equalsIgnoreCase( "true" ) )
                    {
                        dataElements.add( dataElement );
                    }
                }
            }
        }
        dataElementList.retainAll( dataElements );

        Collections.sort( dataElementList, new IdentifiableObjectNameComparator() );
        
        allDataElementList = (List<DataElement>) dataElementService.getAllDataElements();
       // System.out.println( "allDataElementList" + allDataElementList );
        Collections.sort( allDataElementList );
      
     
        return SUCCESS;
    }
}
