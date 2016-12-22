package org.hisp.dhis.ivb.aggregation.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.option.OptionService;
import org.hisp.dhis.option.OptionSet;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;

import com.opensymphony.xwork2.Action;

public class GetAggregationParameterAction
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

    private OptionService optionService;

    public void setOptionService( OptionService optionService )
    {
        this.optionService = optionService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
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
    private String aggTypeId;

    public void setAggTypeId( String aggTypeId )
    {
        this.aggTypeId = aggTypeId;
    }

    public String getAggTypeId()
    {
        return aggTypeId;
    }

    private List<OptionSet> optionSets;

    public List<OptionSet> getOptionSets()
    {
        return optionSets;
    }

    private List<DataElement> dataElements;

    public List<DataElement> getDataElements()
    {
        return dataElements;
    }

    private List<Indicator> indicators;

    public List<Indicator> getIndicators()
    {
        return indicators;
    }

    List<Lookup> lookups;

    public List<Lookup> getLookups()
    {
        return lookups;
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

        if ( aggTypeId.equals( Lookup.AGG_TYPE_BY_OPTION ) )
        {
            optionSets = new ArrayList<OptionSet>( optionService.getAllOptionSets() );

            // dataElements = new ArrayList<DataElement>(
            // dataElementService.getAllActiveDataElements() );

            /**
             * TODO - filter dataelements by creating attribute called 'is auto
             * populate by aggregation engine' 6
             * 
             */
            dataElements = new ArrayList<DataElement>( dataElementService.getAllDataElements() );

            Constant ivbAggDEConst = constantService.getConstantByName( "IS_IVB_AGGREGATED_DE_ATTRIBUTE_ID" );
            Iterator<DataElement> iterator = dataElements.iterator();
            List<DataElement> dataElementList = new ArrayList<DataElement>();
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
                            iterator.remove();
                        }
                    }
                }
            }

            Collections.sort( dataElements, new IdentifiableObjectNameComparator() );
        }
        else if ( aggTypeId.equals( Lookup.AGG_TYPE_BY_OPTION_SCORE ) )
        {
            // dataElements = new ArrayList<DataElement>(
            // dataElementService.getAllActiveDataElements() );
            dataElements = new ArrayList<DataElement>( dataElementService.getAllDataElements() );

            Constant ivbAggDEConst = constantService.getConstantByName( "IS_IVB_AGGREGATED_DE_ATTRIBUTE_ID" );
            Iterator<DataElement> iterator = dataElements.iterator();
            List<DataElement> dataElementList = new ArrayList<DataElement>();
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
                            iterator.remove();
                        }
                    }
                }
            }
            
            Collections.sort( dataElements, new IdentifiableObjectNameComparator() );
        }
        else if ( aggTypeId.equals( Lookup.AGG_TYPE_BY_KEYFLAG ) )
        {
            indicators = new ArrayList<Indicator>( indicatorService.getAllIndicators() );
            
            Collections.sort( indicators, new IdentifiableObjectNameComparator() );
        }

        return SUCCESS;
    }
}
