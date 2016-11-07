package org.hisp.dhis.birtreports.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.reports.ReportService;
import org.hisp.dhis.reports.Report_in;
import org.hisp.dhis.reports.comparator.Report_inNameComparator;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class GetBirtReportsAction implements Action
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

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    // -------------------------------------------------------------------------
    // Input & output
    // -------------------------------------------------------------------------

    private String periodType;

    public void setPeriodType( String periodType )
    {
        this.periodType = periodType;
    }

    private String orgUnitId;
    
    public void setOrgUnitId( String orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }

    private String reportType;
    
    public void setReportType( String reportType )
    {
        this.reportType = reportType;
    }

    private List<Report_in> reportList;

    public List<Report_in> getReportList()
    {
        return reportList;
    }

    private String ouName;

    public String getOuName()
    {
        return ouName;
    }
    
    private String orgUnitUid;
    
    public String getOrgUnitUid()
    {
        return orgUnitUid;
    }

    private Integer ouLavel;
    
    public Integer getOuLavel()
    {
        return ouLavel;
    }
    private int selectedOrgUnitLevel;
    
    public int getSelectedOrgUnitLevel()
    {
        return selectedOrgUnitLevel;
    }
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute() throws Exception
    {   
        //System.out.println( " OutSide orgUnitId  : " + orgUnitId + "---- periodType  : " + periodType + "------reportType----- " + reportType );
        
        if ( orgUnitId != null )
        {
            try
            {
                OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitId );
                
                ouName = orgUnit.getShortName();
                ouLavel = orgUnit.getLevel();
                orgUnitUid = orgUnit.getUid();
                
                //selectedOrgUnitLevel = organisationUnitService.getLevelOfOrganisationUnit( orgUnit.getId() );
                //selectedOrgUnitLevel = organisationUnitService.getOrganisationUnitLevel( orgUnit.getId() ).getLevel();
                selectedOrgUnitLevel = orgUnit.getLevel();
                
                //System.out.println( "ouName  : " + orgUnit.getShortName() + "---- ouLavel  : " + orgUnit.getLevel() + "------LAVEL----- " + selectedOrgUnitLevel );
                
                PeriodType periodTypeObj = periodService.getPeriodTypeByName( periodType );
                
                
                //System.out.println( " In Side ouName  : " + orgUnit.getName() + "---- periodTypeObj  : " + periodTypeObj.getName() + "------reportType----- " + reportType );
                
                reportList = new ArrayList<Report_in>( reportService.getReportsByPeriodSourceAndReportType( periodTypeObj, orgUnit, reportType ) );
                
                Collections.sort( reportList, new Report_inNameComparator() );
            }
            catch ( Exception e )
            {
                System.out.println( "Exception while getting Reports List : " + e.getMessage() );
            }
        }

        return SUCCESS;
    }
}
