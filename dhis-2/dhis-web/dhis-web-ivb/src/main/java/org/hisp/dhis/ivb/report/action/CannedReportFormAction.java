package org.hisp.dhis.ivb.report.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.favorite.Favorite;
import org.hisp.dhis.favorite.FavoriteService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
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
public class CannedReportFormAction
    implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
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

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
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

    private FavoriteService favoriteService;

    public void setFavoriteService( FavoriteService favoriteService )
    {
        this.favoriteService = favoriteService;
    }

    @Autowired
    private IVBUtil ivbUtil;
    
    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

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

    private List<DataSet> datasetList = new ArrayList<DataSet>();

    public List<DataSet> getDatasetList()
    {
        return datasetList;
    }

    private List<String> countryList = new ArrayList<String>();

    public List<String> getCountryList()
    {
        return countryList;
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

    private Favorite prowgReport;
    
    public void setProwgReport( Favorite prowgReport )
    {
        this.prowgReport = prowgReport;
    }
    public Favorite getProwgReport()
    {
        return prowgReport;
    }
    
    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------


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

        
        Set<OrganisationUnit> currentUserOrgUnits = new HashSet<OrganisationUnit>( currentUserService.getCurrentUser().getDataViewOrganisationUnits() );
        selectionTreeManager.setRootOrganisationUnits( currentUserOrgUnits );
        
       // selectionTreeManager.clearSelectedOrganisationUnits();

        List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>(
            organisationUnitService.getAllOrganisationUnits() );
        for ( OrganisationUnit org : orgUnitList )
        {
            for ( OrganisationUnit o : ivbUtil.getLeafOrganisationUnits( org.getId() ) )
            {
                if ( !(countryList.contains( "\"" + o.getUid() + "\"" )) )
                {
                    countryList.add( "\"" + o.getUid() + "\"" );
                }
            }
        }

        datasetList = new ArrayList<DataSet>( dataSetService.getAllDataSets() );
       
        if(favoriteService.getFavoriteByName( "PROWG REPORT" ) != null)
        {
            prowgReport = favoriteService.getFavoriteByName( "PROWG REPORT" );
        }
        
        Iterator<DataSet> dataSetIterator = datasetList.iterator();
        while ( dataSetIterator.hasNext() )
        {
            DataSet dataSet = dataSetIterator.next();
            if ( dataSet.getSources() != null && dataSet.getSources().size() > 0 )
            {
                dataSetIterator.remove();
            }
        }
        Collections.sort( datasetList, new IdentifiableObjectNameComparator() );

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

        return SUCCESS;
    }
}
