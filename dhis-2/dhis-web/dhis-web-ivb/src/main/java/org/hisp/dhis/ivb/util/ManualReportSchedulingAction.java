package org.hisp.dhis.ivb.util;

import java.util.Date;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class ManualReportSchedulingAction implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private ReportScheduler reportScheduler;
    
    public void setReportScheduler( ReportScheduler reportScheduler )
    {
        this.reportScheduler = reportScheduler;
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

 
    // --------------------------------------------------------------------------
    // Action implementation
    // --------------------------------------------------------------------------

    public String execute() throws Exception
    {
        System.out.println(" Manual Job Scheduler Started at : " + new Date() );
        
        reportScheduler.scheduledReport();
        
        System.out.println(" Manual Job Scheduler End at : " + new Date() );
        
        return SUCCESS;

    }

}
