package org.hisp.dhis.ivb.hiddendeorgunit;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.oust.manager.SelectionTreeManager;

import com.opensymphony.xwork2.Action;

public class HideDataElementAction
    implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
    }

    // -------------------------------------------------------------------------
    // Input & output
    // -------------------------------------------------------------------------

    private String deSelected;

    public void setDeSelected( String deSelected )
    {
        this.deSelected = deSelected;
    }

    private Set<OrganisationUnit> ouSelected = new HashSet<OrganisationUnit>();;

    public void setOuSelected( Set<OrganisationUnit> ouSelected )
    {
        this.ouSelected = ouSelected;
    }
    // -------------------------------------------------------------------------
    // Action
    // -------------------------------------------------------------------------

    

    public String execute()
        throws Exception
    {
        ouSelected = new HashSet<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() );
        if(deSelected != null )
        {
            
            DataElement deToHide = new DataElement();
            deToHide = dataElementService.getDataElement( deSelected );
            
            
            if ( deToHide != null )
            {
                if ( deToHide.getOrgUnits().size() != 0 )
                    deToHide.getOrgUnits().clear();

                deToHide.setOrgUnits( ouSelected );
                
                dataElementService.updateDataElement( deToHide );
            }
        }
        return SUCCESS;
    }
}
