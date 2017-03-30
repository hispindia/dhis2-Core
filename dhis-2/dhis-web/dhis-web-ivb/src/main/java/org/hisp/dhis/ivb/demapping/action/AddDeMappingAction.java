package org.hisp.dhis.ivb.demapping.action;


import org.hisp.dhis.ivb.demapping.DeMapping;
import org.hisp.dhis.ivb.demapping.DeMappingService;

import com.opensymphony.xwork2.Action;

public class AddDeMappingAction
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
    // Input & output
    // -------------------------------------------------------------------------

    private String deid;
    private String mappeddeid;
    public void setDeid(String deid) {
        this.deid = deid;
    }

    public void setMappeddeid(String mappeddeid) {
        this.mappeddeid = mappeddeid;
    }


    // -------------------------------------------------------------------------
    // Action
    // -------------------------------------------------------------------------

 
    public String execute()
        throws Exception
    {
        DeMapping demapping = new DeMapping();

        demapping.setDeid( deid );
        demapping.setMappeddeid( mappeddeid );

        demappingService.addDeMapping( demapping );

        return SUCCESS;
    }

}
