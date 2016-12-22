package org.hisp.dhis.ivb.report.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dataset.SectionService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class IPVImplementationResultAction implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SectionService sectionService;

    public void setSectionService( SectionService sectionService )
    {
        this.sectionService = sectionService;
    }

    private OrganisationUnitGroupService organisationUnitGroupService;

    public void setOrganisationUnitGroupService( OrganisationUnitGroupService organisationUnitGroupService )
    {
        this.organisationUnitGroupService = organisationUnitGroupService;
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

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    @Autowired 
    private LookupService lookupService;

    @Autowired
    private IVBUtil ivbUtil;

    // -------------------------------------------------------------------------
    // Setters & Getters
    // -------------------------------------------------------------------------

    private List<Integer> selectedListTier;
    
    public void setSelectedListTier( List<Integer> selectedListTier )
    {
        this.selectedListTier = selectedListTier;
    }
    
    private String includeComments;
    
    public String getIncludeComments()
    {
        return includeComments;
    }

    public void setIncludeComments( String includeComments )
    {
        this.includeComments = includeComments;
    }

    private Integer ipvSectionId;

    public Integer getIpvSectionId()
    {
        return ipvSectionId;
    }

    public void setIpvSectionId( Integer ipvSectionId )
    {
        this.ipvSectionId = ipvSectionId;
    }

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

    private Map<String, DataValue> headerDataValueMap = new HashMap<String, DataValue>();

    public Map<String, DataValue> getHeaderDataValueMap()
    {
        return headerDataValueMap;
    }

    private Map<String, DataValue> dataValueMap = new HashMap<String, DataValue>();

    public Map<String, DataValue> getDataValueMap()
    {
        return dataValueMap;
    }

    private OrganisationUnitGroupSet unicefRegionsGroupSet;
    
    public OrganisationUnitGroupSet getUnicefRegionsGroupSet()
    {
        return unicefRegionsGroupSet;
    }

    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();
    
    public List<OrganisationUnit> getOrgUnitList()
    {
        return orgUnitList;
    }

    private OrganisationUnitGroupSet tierGroupSet;
    
    public OrganisationUnitGroupSet getTierGroupSet()
    {
        return tierGroupSet;
    }
    
    private OrganisationUnitGroupSet whoRegionsGroupSet;
    
    public OrganisationUnitGroupSet getWhoRegionsGroupSet()
    {
        return whoRegionsGroupSet;
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
    
        Lookup lookup = lookupService.getLookupByName( Lookup.UNICEF_REGIONS_GROUPSET );
        
        unicefRegionsGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( Integer.parseInt( lookup.getValue() ) );
        
        lookup = lookupService.getLookupByName( Lookup.TIER_GROUPSET );
        
        tierGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( Integer.parseInt( lookup.getValue() ) );

        lookup = lookupService.getLookupByName( Lookup.WHO_REGIONS_GROUPSET );
        
        whoRegionsGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( Integer.parseInt( lookup.getValue() ) );
        
        
        if( includeComments == null )
        {
            includeComments = "NO";
        }
        else
        {
            includeComments = "YES";
        }
        
        for( Integer orgUnitGroupId : selectedListTier )
        {
            orgUnitList.addAll( organisationUnitGroupService.getOrganisationUnitGroup( orgUnitGroupId ).getMembers() );
        }
        
        String orgUnitIdsByComma = "-1";
        List<Integer> orgunitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );
        if ( orgUnitList.size() > 0 )
        {
            orgUnitIdsByComma = getCommaDelimitedString( orgunitIds );
        }

        Section ivpSection = sectionService.getSection( ipvSectionId );
        System.out.println("ipv section--"+ivpSection);
        System.out.println(" ipvSectionId--"+ipvSectionId);
        Collection<DataElement> dataElements = ivpSection.getDataElements( );
        List<Integer> dataElementIds = new ArrayList<Integer>( getIdentifiers( dataElements ) );
        String dataElementIdsByComma = "-1";
        if ( dataElementIds.size() > 0 )
        {
            dataElementIdsByComma = getCommaDelimitedString( dataElementIds );
        }
        dataValueMap = ivbUtil.getLatestDataValuesForTabularReport( dataElementIdsByComma, orgUnitIdsByComma );

        Constant tabularDataElementGroupId = constantService.getConstantByName( Lookup.TABULAR_REPORT_DATAELEMENTGROUP_ID );
        dataElements = new ArrayList<DataElement>( dataElementService.getDataElementsByGroupId( (int) tabularDataElementGroupId.getValue() ) );
        List<Integer >headerDataElementIds = new ArrayList<Integer>( getIdentifiers( dataElements ) );
        String headerDataElementIdsByComma = "-1";
        if ( headerDataElementIds.size() > 0 )
        {
            headerDataElementIdsByComma = getCommaDelimitedString( headerDataElementIds );
        }

        headerDataValueMap = ivbUtil.getLatestDataValuesForTabularReport( headerDataElementIdsByComma, orgUnitIdsByComma );
        
        dataValueMap.putAll( headerDataValueMap );

        return SUCCESS;
    }
}
