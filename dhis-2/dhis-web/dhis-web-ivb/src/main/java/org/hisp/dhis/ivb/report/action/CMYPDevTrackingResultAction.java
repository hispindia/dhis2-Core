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
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
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
public class CMYPDevTrackingResultAction implements Action
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
    
    @Autowired
    private DataElementService dataElementService;
    
    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------
    
    private Integer cMYPyear;
    
    public Integer getCMYPyear()
    {
        return cMYPyear;
    }

    public void setCMYPyear( Integer cMYPyear )
    {
        this.cMYPyear = cMYPyear;
    }

    private List<String> orgUnitIds = new ArrayList<String>();

    public void setOrgUnitIds( List<String> orgUnitIds )
    {
        this.orgUnitIds = orgUnitIds;
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
   
    private List<DataElement> headerDes = new ArrayList<DataElement>();
    
    public List<DataElement> getHeaderDes()
    {
        return headerDes;
    }

    private List<DataElement> dataElements = new ArrayList<DataElement>();
    
    public List<DataElement> getDataElements()
    {
        return dataElements;
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
        Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );
        Collection<Integer> organisationUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );

        String orgUnitIdsByComma = "-1";
        if ( orgUnitList.size() > 0 )
        {
            orgUnitIdsByComma = getCommaDelimitedString( organisationUnitIds );
        }
        
        Lookup lookup = lookupService.getLookupByName( Lookup.CMYP_DEV_TRACKING_REPORT_PARAMS );
        String cmyp_dev_tracking_report_params = lookup.getValue();
        
        String headerDataelments = cmyp_dev_tracking_report_params.split( "-" )[0];
        String bodyDataelements = cmyp_dev_tracking_report_params.split( "-" )[1];
        String cmypEndDateDeId = cmyp_dev_tracking_report_params.split( "-" )[2];
        
        String dataElementIdsByComma = headerDataelments + "," + bodyDataelements;
        
        for( String deId : headerDataelments.split( "," ) )
        {
            DataElement de = dataElementService.getDataElement(  Integer.parseInt(deId) );
            headerDes.add( de );
        }
        
        for( String deId : bodyDataelements.split( "," ) )
        {
            DataElement de = dataElementService.getDataElement(  Integer.parseInt(deId) );
            dataElements.add( de );
        }
        
        dataValueMap = ivbUtil.getLatestDataValuesForTabularReport( dataElementIdsByComma, orgUnitIdsByComma );
        
        //System.out.println( "cMYPyear : " + cMYPyear );
        
        if( cMYPyear != null )
        {
            Iterator<OrganisationUnit> it = orgUnitList.iterator();
            while( it.hasNext() )
            {
                OrganisationUnit ou = it.next();
                
                DataValue dv = dataValueMap.get( ou.getId()+":"+cmypEndDateDeId );
                if( dv != null && dv.getValue() != null )
                {
                    try
                    {
                        Integer dvYear = Integer.parseInt(  dv.getValue().split( "-" )[0] );
                        
                        //System.out.println( ou.getName() + " : " + dvYear );
                        if( dvYear > cMYPyear )
                        {
                            it.remove();
                        }
                    }
                    catch( Exception e )
                    {
                        it.remove();
                    }
                }
                else
                {
                    it.remove();
                }            
            }
        }
        else
        {
            cMYPyear = 0;
        }
        
        return SUCCESS;
    }
}
