package org.hisp.dhis.reports.md.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.reports.ReportService;

import com.opensymphony.xwork2.Action;

public class GetOrgUnitsAction implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    
    private ReportService reportService;

    public void setReportService( ReportService reportService )
    {
        this.reportService = reportService;
    }
    
    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    
    /*
    private Integer orgUnitId;

    public void setOrgUnitId( Integer orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }
    */
    
    private String orgUnitId;
    
    public void setOrgUnitId( String orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }

    private String type;
    
    public void setType( String type )
    {
        this.type = type;
    }

    private OrganisationUnit orgUnit;

    public OrganisationUnit getOrgUnit()
    {
        return orgUnit;
    }

    private Integer orgUnitLevel;

    public Integer getOrgUnitLevel()
    {
        return orgUnitLevel;
    }

    private Integer maxOrgUnitLevel;
    
    public Integer getMaxOrgUnitLevel()
    {
        return maxOrgUnitLevel;
    }
    
    private List<OrganisationUnitLevel> levels;

    public List<OrganisationUnitLevel> getLevels()
    {
        return levels;
    }
    
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------


    public String execute()
        throws Exception
    {
        if ( orgUnitId != null )
        {
            orgUnit = organisationUnitService.getOrganisationUnit( orgUnitId );
        }
        
       // System.out.println(" orgUnit Id is : " + orgUnit.getId() + " , orgUnit Name is : " + orgUnit.getName() );
        //orgUnitLevel = organisationUnitService.getLevelOfOrganisationUnit( orgUnit );
        orgUnitLevel = organisationUnitService.getOrganisationUnitLevel( orgUnit.getUid() ).getLevel();
        maxOrgUnitLevel = organisationUnitService.getNumberOfOrganisationalLevels();
            
        List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitWithChildren( orgUnit.getId() ) );
        Map<Integer, Integer> orgunitLevelMap = new HashMap<Integer, Integer>( reportService.getOrgunitLevelMap() );
    
        maxOrgUnitLevel = 1;
        Iterator<OrganisationUnit> ouIterator = orgUnitList.iterator();
        while ( ouIterator.hasNext() )
        {
            OrganisationUnit orgU = ouIterator.next();
            
            Integer level = orgunitLevelMap.get( orgU.getId() );
            if( level == null )
                level = organisationUnitService.getOrganisationUnitLevel( orgU.getUid() ).getLevel();
            if ( level > maxOrgUnitLevel )
            {
                maxOrgUnitLevel = level;
            }
        }
        
        levels = organisationUnitService.getFilledOrganisationUnitLevels();
        
        /*
        for( OrganisationUnitLevel organisationUnitLevel : levels  )
        {
            System.out.println(" Level : " + organisationUnitLevel.getLevel() + " , Level id : " + organisationUnitLevel.getId() + " , Level Name : " + organisationUnitLevel.getName() + " , Level Display Name : " + organisationUnitLevel.getDisplayName() );
        }
        */
        return SUCCESS;
    }

}
