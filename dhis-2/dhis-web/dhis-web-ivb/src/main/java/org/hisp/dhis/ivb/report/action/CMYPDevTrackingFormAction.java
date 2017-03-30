package org.hisp.dhis.ivb.report.action;

import java.util.HashSet;
import java.util.Set;

import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class CMYPDevTrackingFormAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private SelectionTreeManager selectionTreeManager;

    @Autowired
    private OrganisationUnitSelectionManager selectionManager;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private I18nService i18nService;
    
    @Autowired
    private OrganisationUnitService organisationUnitService;

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

        selectionManager.clearSelectedOrganisationUnits();
        Set<OrganisationUnit> currentUserOrgUnits = new HashSet<OrganisationUnit>( currentUserService.getCurrentUser().getDataViewOrganisationUnits() );
        selectionTreeManager.setRootOrganisationUnits( currentUserOrgUnits );
        
        Set<OrganisationUnit> selectOus = new HashSet<OrganisationUnit>();
        for( OrganisationUnit rootOrgUnit : currentUserOrgUnits )
        {
            //selectOus.addAll( organisationUnitService.getOrganisationUnitWithChildren( rootOrgUnit.getId() ) );
            
            selectOus.addAll(  organisationUnitService.getOrganisationUnitsAtLevel( 3, rootOrgUnit ) );
        }
        
        selectionTreeManager.clearSelectedOrganisationUnits();
      //  selectionTreeManager.setSelectedOrganisationUnits( selectOus );

        return SUCCESS;
    }
}