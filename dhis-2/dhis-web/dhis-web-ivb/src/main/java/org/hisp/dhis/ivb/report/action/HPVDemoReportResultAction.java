package org.hisp.dhis.ivb.report.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author BHARATH
 */
public class HPVDemoReportResultAction implements Action
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

    @Autowired
    private SelectionTreeManager selectionTreeManager;

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
    
    private Map<Integer, List<String>> countryDemoYearMap = new HashMap<Integer, List<String>>();
    
    public Map<Integer, List<String>> getCountryDemoYearMap()
    {
        return countryDemoYearMap;
    }

    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getOrgUnitList()
    {
        return orgUnitList;
    }      

    private List<String> orgUnitIds = new ArrayList<String>();

    public void setOrgUnitIds( List<String> orgUnitIds )
    {
        this.orgUnitIds = orgUnitIds;
    }

    private String demoYear;

    public void setDemoYear( String demoYear )
    {
        this.demoYear = demoYear;
    }

    private String eoi;
    
    public void setEoi( String eoi )
    {
        this.eoi = eoi;
    }
    
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------
    @Override
    public String execute() throws Exception
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
        
        if ( orgUnitIds.size() > 1 )
        {
            for ( String id : orgUnitIds )
            {
                OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( Integer.parseInt( id ) );
                if ( orgUnit.getHierarchyLevel() == 3 )
                {
                    orgUnitList.add( orgUnit );
                }
            }
        }
        else if ( selectionTreeManager.getReloadedSelectedOrganisationUnits() != null )
        {
            orgUnitList = new ArrayList<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() );
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
        
        Lookup lookup = lookupService.getLookupByName( Lookup.HPVDEMO_REPORT_PARAMS );
        String hpvdemo_report_params = lookup.getValue();
        
        Integer hpvDemoYearId = Integer.parseInt( hpvdemo_report_params.split( ":" )[0] );

        String hpvDemoYearDes = hpvdemo_report_params.split( ":" )[1];
        
        Integer hpvDemoGaviStatusId = Integer.parseInt( hpvdemo_report_params.split( ":" )[2] );
        
        String dataElementIdsByComma = hpvDemoGaviStatusId + "," + hpvDemoYearId;

        String orgUnitIdsByComma = "-1";
        if ( orgUnitList.size() > 0 )
        {
            Collection<Integer> organisationUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );
            orgUnitIdsByComma = getCommaDelimitedString( organisationUnitIds );
        }

        dataValueMap = ivbUtil.getLatestDataValuesForTabularReport( dataElementIdsByComma, orgUnitIdsByComma );
        
        countryDemoYearMap = ivbUtil.getHPVDemoYearsByCountry( orgUnitIdsByComma, hpvDemoYearId+"" );
        
        Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );
        
        Iterator<OrganisationUnit> ouIterator = orgUnitList.iterator();
        while( ouIterator.hasNext() )
        {
            OrganisationUnit orgUnit = ouIterator.next();
            
            DataValue dv = dataValueMap.get( orgUnit.getId()+":"+hpvDemoYearId );            
            if( dv != null )
            {
                String value = dv.getValue();
                
                if( value != null )
                {
                    if(  value.contains( "Year" ) && value.trim().equalsIgnoreCase( demoYear.trim().toLowerCase() ) )
                    {
                        if( eoi != null )
                        {
                            DataValue dv1 = dataValueMap.get( orgUnit.getId()+":"+hpvDemoGaviStatusId );
                            if( dv1 != null && dv1.getValue() != null && dv1.getValue().equalsIgnoreCase( "EOI" ) )
                            {
                                
                            }
                            else
                            {
                                ouIterator.remove();
                            }
                        }
                    }
                    else if( demoYear.trim().toLowerCase().equalsIgnoreCase( "ALL" ) && value.contains( "Year" ) )
                    { 
                        if( eoi != null )
                        {
                            DataValue dv1 = dataValueMap.get( orgUnit.getId()+":"+hpvDemoGaviStatusId );
                            if( dv1 != null && dv1.getValue() != null && dv1.getValue().equalsIgnoreCase( "EOI" ) )
                            {
                                
                            }
                            else
                            {
                                ouIterator.remove();
                            }
                        }                        
                    }
                    else
                    {
                        ouIterator.remove();
                    }
                }
                else
                {
                    ouIterator.remove();
                }
            }
            else
            {
                ouIterator.remove();
            }
        }

        Collection<Integer> organisationUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );
        if( organisationUnitIds.size() > 0 )
        {
            orgUnitIdsByComma = getCommaDelimitedString( organisationUnitIds );
        }
        else
        {
            orgUnitIdsByComma = "-1";
        }

        dataValueMap.putAll( ivbUtil.getLatestDataValuesForHPVDemoReport( hpvDemoYearDes, orgUnitIdsByComma, hpvDemoYearId+"" ) );
        
        return SUCCESS;
    }
}
