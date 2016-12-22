package org.hisp.dhis.ivb.report.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;


/**
 * @author BHARATH
 */
public class BWAActivitiesResultAction implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private LookupService lookupService;
    
    @Autowired
    private IVBUtil ivbUtil;
    
    @Autowired
    private I18nService i18nService;
    
    @Autowired
    private CurrentUserService currentUserService;
    
    @Autowired
    private ConfigurationService configurationService;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private OrganisationUnitService organisationUnitService;
    
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

    private Map<String, DataValue> dataValueMap = new HashMap<String, DataValue>();

    public Map<String, DataValue> getDataValueMap()
    {
        return dataValueMap;
    }
    
    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getOrgUnitList()
    {
        return orgUnitList;
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

        Lookup lookup = lookupService.getLookupByName( Lookup.BWA_ACTIVITY_REPORT_PARAMS );
        String bwa_activity_report_params = lookup.getValue();
        
        String rootOrgUnitUId = bwa_activity_report_params.split( "-" )[0];

        if ( rootOrgUnitUId != null )
        {
            orgUnitList = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitWithChildren( rootOrgUnitUId ) );
            
            List<OrganisationUnit> lastLevelOrgUnit = new ArrayList<OrganisationUnit>();
            List<OrganisationUnit> userOrgUnits = new ArrayList<OrganisationUnit>( currentUserService.getCurrentUser().getDataViewOrganisationUnits() );
            for ( OrganisationUnit orgUnit : userOrgUnits )
            {
                if ( orgUnit.getHierarchyLevel() == 3 )
                {
                    lastLevelOrgUnit.add( orgUnit );
                }
                else
                {
                    lastLevelOrgUnit.addAll( organisationUnitService.getOrganisationUnitsAtLevel( 3, orgUnit ) );
                }
            }
            orgUnitList.retainAll( lastLevelOrgUnit );
        }
        
        Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );
        Collection<Integer> organisationUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );

        String orgUnitIdsByComma = "-1";
        if ( orgUnitList.size() > 0 )
        {
            orgUnitIdsByComma = getCommaDelimitedString( organisationUnitIds );
        }
        
        String dataElementIdsByComma = bwa_activity_report_params.split( "-" )[1];
        
        dataValueMap = ivbUtil.getLatestDataValuesForTabularReport( dataElementIdsByComma, orgUnitIdsByComma );
        
        return SUCCESS;
    }
}
