package org.hisp.dhis.ivb.demapping.action;


import org.hisp.dhis.ivb.demapping.DeMapping;
import org.hisp.dhis.ivb.demapping.DeMappingService;
import com.opensymphony.xwork2.Action;

public class GetDeMappingAction
    implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DeMappingService demappingService;

    public void setDemappingService(DeMappingService demappingService) {
        this.demappingService = demappingService;
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private String deid;

    public void setDeid(String deid) {
        this.deid = deid;
    }

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private DeMapping demapping;

    public DeMapping getDemapping() {
        return demapping;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        demapping = demappingService.getDeMapping( deid );

        return SUCCESS;
    }

}
