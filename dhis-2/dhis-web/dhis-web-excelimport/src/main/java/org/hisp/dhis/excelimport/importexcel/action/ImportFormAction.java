package org.hisp.dhis.excelimport.importexcel.action;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSetStore;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class ImportFormAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private PeriodService periodService;

    @Autowired
    private OrganisationUnitGroupService organisationUnitGroupService;

    @Autowired
    private OrganisationUnitGroupSetStore organisationUnitGroupSetStore;

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    List<String> statusMsg;

    private List<Period> periods;

    public List<Period> getPeriods()
    {
        return periods;
    }

    private List<OrganisationUnit> orgunits;

    public List<OrganisationUnit> getSource()
    {
        return orgunits;
    }

    private List<String> yearList;

    private List<OrganisationUnitGroup> organisationUnitGroups;

    public List<OrganisationUnitGroup> getOrganisationUnitGroups()
    {
        return organisationUnitGroups;
    }

    public List<String> getStatusMsg()
    {
        return statusMsg;
    }

    public void setStatusMsg( List<String> statusMsg )
    {
        this.statusMsg = statusMsg;
    }

    public List<String> getYearList()
    {
        return yearList;
    }

    public void setYearList( String year )
    {
        System.out.println( year );
    }

    //private SimpleDateFormat simpleDateFormat;
    
    private Collection<PeriodType> periodTypes;

    public Collection<PeriodType> getPeriodTypes()
    {
        return periodTypes;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
       /* Period Info */
        
        periodTypes = periodService.getAllPeriodTypes();

        Iterator<PeriodType> alldeIterator = periodTypes.iterator();
        while ( alldeIterator.hasNext() )
        {
            PeriodType type = alldeIterator.next();
            
            if ( type.getName().equalsIgnoreCase("Weekly") || type.getName().equalsIgnoreCase("Monthly") 
                || type.getName().equalsIgnoreCase("quarterly") || type.getName().equalsIgnoreCase("yearly"))
            {
                //periods.addAll( periodService.getPeriodsByPeriodType(type) );
            }
            
            else
            {
               alldeIterator.remove();
            }
        }
        
        return SUCCESS;
    }

}
