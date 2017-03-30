package org.hisp.dhis.ivb.hiddendeorgunit;

import java.util.HashSet;
import java.util.Set;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class FilterOrganisationUnitsAction implements Action {
	private String selectedDE;

	private Set<OrganisationUnit> filteredOrgUnits = new HashSet<OrganisationUnit>();

	public String getSelectedDE() {
		return selectedDE;
	}

	public void setSelectedDE(String selectedDE) {
		this.selectedDE = selectedDE;
	}

	public Set<OrganisationUnit> getFilteredOrgUnits() {
		return filteredOrgUnits;
	}

	public void setFilteredOrgUnits(Set<OrganisationUnit> filteredOrgUnits) {
		this.filteredOrgUnits = filteredOrgUnits;
	}

	// -------------------------------------------------------------------------
	// Dependencies
	// -------------------------------------------------------------------------

	@Autowired
	private DataElementService dataElementService;

	@Autowired
	private SelectionTreeManager selectionTreeManager;

	public static Boolean failed = false;

	// -------------------------------------------------------------------------
	// Action
	// -------------------------------------------------------------------------

	public String execute() throws Exception {
		selectionTreeManager.clearSelectedOrganisationUnits();

		if (selectedDE != null) {
			failed = true;
			DataElement hiddenDE = new DataElement();
			hiddenDE = dataElementService.getDataElement(selectedDE);

			Set<OrganisationUnit> orgUnitsForHiddenDE = new HashSet<OrganisationUnit>();
			orgUnitsForHiddenDE = hiddenDE.getOrgUnits();
			selectionTreeManager.setSelectedOrganisationUnits(orgUnitsForHiddenDE);
		} 
		return SUCCESS;
	}
}
