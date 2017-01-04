package org.hisp.dhis.schedulinginspections.action;

import java.util.ArrayList;
import java.util.List;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class GetChildOrgUnitListAction implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private OrganisationUnitService organisationUnitService;
    
    
    // -------------------------------------------------------------------------
    // Input & output
    // -------------------------------------------------------------------------

    private String parentOrgUnitName;
    
    public void setParentOrgUnitName( String parentOrgUnitName )
    {
        this.parentOrgUnitName = parentOrgUnitName;
    }

    private List<OrganisationUnit> organisationUnits = new ArrayList<OrganisationUnit>();
   
    public List<OrganisationUnit> getOrganisationUnits()
    {
        return organisationUnits;
    }



    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
    {
        organisationUnits = new ArrayList<OrganisationUnit>();
        
        if ( parentOrgUnitName != null && !parentOrgUnitName.equalsIgnoreCase( "" ) )
        {
            OrganisationUnit parentOrgUnit = organisationUnitService.getOrganisationUnitByNameIgnoreCase( parentOrgUnitName ).get( 0 );
            
            if( parentOrgUnit != null )
            {
                organisationUnits = new ArrayList<OrganisationUnit>( parentOrgUnit.getChildren() );
            }
        }

        return SUCCESS;
    }
}
