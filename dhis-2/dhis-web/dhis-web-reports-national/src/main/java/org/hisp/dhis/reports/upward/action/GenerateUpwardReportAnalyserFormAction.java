package org.hisp.dhis.reports.upward.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.reports.ReportType;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class GenerateUpwardReportAnalyserFormAction
    implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }
    
    @Autowired
    private OrganisationUnitGroupService organisationUnitGroupService;

    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    private Collection<PeriodType> periodTypes;

    public Collection<PeriodType> getPeriodTypes()
    {
        return periodTypes;
    }

    private String reportTypeName;

    public String getReportTypeName()
    {
        return reportTypeName;
    }
    
    private List<OrganisationUnitGroup> orgUnitGroups;
    
    public List<OrganisationUnitGroup> getOrgUnitGroups()
    {
        return orgUnitGroups;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
    {
        reportTypeName = ReportType.RT_GOI;

        periodTypes = periodService.getAllPeriodTypes();

        // Filtering Periodtypes other than Monthly, Quarterly and Yearly
        Iterator<PeriodType> periodTypeIterator = periodTypes.iterator();
        while ( periodTypeIterator.hasNext() )
        {
            PeriodType type = periodTypeIterator.next();
            if ( type.getName().equalsIgnoreCase( "Monthly" ) || type.getName().equalsIgnoreCase( "quarterly" )
                || type.getName().equalsIgnoreCase( "yearly" ) )
            {
            }
            else
            {
                periodTypeIterator.remove();
            }
        }

        List<OrganisationUnitGroupSet> organisationUnitGroupSet1List = new ArrayList<OrganisationUnitGroupSet>( organisationUnitGroupService.getOrganisationUnitGroupSetByName( "Private GOI Report" ) );
        OrganisationUnitGroupSet organisationUnitGroupSet = organisationUnitGroupSet1List.get( 0 );
        orgUnitGroups = new ArrayList<OrganisationUnitGroup>( organisationUnitGroupSet.getOrganisationUnitGroups() );
        
        return SUCCESS;
    }
}
