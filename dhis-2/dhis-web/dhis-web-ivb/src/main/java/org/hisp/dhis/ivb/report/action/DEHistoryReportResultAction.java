package org.hisp.dhis.ivb.report.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;

import com.opensymphony.xwork2.Action;


/**
 * @author BHARATH
 */
public class DEHistoryReportResultAction implements Action
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

    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    private MessageService messageService;

    public void setMessageService( MessageService messageService )
    {
        this.messageService = messageService;
    }

    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
    }
    
    private IVBUtil ivbUtil;
    
    public void setIvbUtil( IVBUtil ivbUtil )
    {
        this.ivbUtil = ivbUtil;
    }
    
    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    
    private ConfigurationService configurationService;

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }
    
    private DataElementService dataElementService;
    
    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }
    
    // -------------------------------------------------------------------------
    // Getter / Setters
    // -------------------------------------------------------------------------

    private List<Integer> orgUnitIds = new ArrayList<Integer>();
    
    public void setOrgUnitIds(List<Integer> orgUnitIds) 
    {
        this.orgUnitIds = orgUnitIds;
    }
    
    private List<Integer> dataElementsSelectedList;
    
    public void setDataElementsSelectedList(List<Integer> dataElementsSelectedList) 
    {
        this.dataElementsSelectedList = dataElementsSelectedList;
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
    
    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();
    
    public List<OrganisationUnit> getOrgUnitList()
    {
        return orgUnitList;
    }
    
    private Map<String, List<DataValueAudit>> dataValueAuditMap;
    
    public Map<String, List<DataValueAudit>> getDataValueAuditMap()
    {
        return dataValueAuditMap;
    }
    
    private List<DataElement> dataElements = new ArrayList<DataElement>();
    
    public List<DataElement> getDataElements()
    {
        return dataElements;
    }
    private SimpleDateFormat simpleDateFormat;
    
    public SimpleDateFormat getSimpleDateFormat()
    {
        return simpleDateFormat;
    }

    private SimpleDateFormat simpleDateFormat1;
    
    public SimpleDateFormat getSimpleDateFormat1()
    {
        return simpleDateFormat1;
    }
    private SimpleDateFormat simpleDateFormat2;
    
    public SimpleDateFormat getSimpleDateFormat2()
    {
        return simpleDateFormat2;
    }
    
    private List<Period> periods = new ArrayList<Period>();
    
    public List<Period> getPeriods()
    {
        return periods;
    }
    
    // --------------------------------------------------------------------------
    // Action implementation
    // --------------------------------------------------------------------------    
    public String execute()
    {
        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        simpleDateFormat1 = new SimpleDateFormat( "MMM" );
        simpleDateFormat2 = new SimpleDateFormat( "MMM yyyy" );
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
        
        // ORGUNIT
        if(orgUnitIds.size() > 0)
        {
            for(Integer id : orgUnitIds)
            {
                OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( id );
                orgUnitList.add( orgUnit );
            }
        }
        else if(selectionTreeManager.getReloadedSelectedOrganisationUnits() != null)
        {             
            orgUnitList =  new ArrayList<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() ) ;            
            List<OrganisationUnit> lastLevelOrgUnit = new ArrayList<OrganisationUnit>();
            List<OrganisationUnit> userOrgUnits = new ArrayList<OrganisationUnit>( currentUserService.getCurrentUser().getDataViewOrganisationUnits() );
            for ( OrganisationUnit orgUnit : userOrgUnits )
            {
                if ( orgUnit.getHierarchyLevel() == 3  )
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
        
        String orgUnitIdsByComma = "-1";
        if(orgUnitList.size() > 0) 
        {
           Collection<Integer> orgUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );        
           orgUnitIdsByComma = getCommaDelimitedString( orgUnitIds );
        }       

        //DATAELEMENTS
        String deIdsByComma = "-1";
        
        for( int deId : dataElementsSelectedList )
        {
            DataElement de = dataElementService.getDataElement( deId );
                
            dataElements.add( de );
        }   
        
        Collection<Integer> deIds = new ArrayList<Integer>( getIdentifiers( dataElements ) );  
        if(deIds.size() > 0)
        {
            deIdsByComma = getCommaDelimitedString( deIds );
        }
        
        //PERIODS
        periods.addAll( ivbUtil.getDistinctPeriodsFromHisotry( deIdsByComma, orgUnitIdsByComma, DataValueAudit.DVA_CT_HISOTRY ) );
        
        
        //DATAVALUE AUDIT
        dataValueAuditMap = ivbUtil.getDataValueAuditMapByUser_UserActivity( deIdsByComma, orgUnitIdsByComma, null, null, DataValueAudit.DVA_CT_HISOTRY, null );
        
        return SUCCESS;
    }
}
