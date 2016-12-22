package org.hisp.dhis.ivb.demapping.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hisp.dhis.caseaggregation.CaseAggregationCondition;
import org.hisp.dhis.caseaggregation.CaseAggregationConditionService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.ivb.demapping.DeMapping;
import org.hisp.dhis.ivb.demapping.DeMappingService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class ValidateDEMappingAction
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

    private String mappeddeid;

    public String getMappeddeid()
    {
        return mappeddeid;
    }

    public void setMappeddeid( String mappeddeid )
    {
        this.mappeddeid = mappeddeid;
    }

    private String selectedDE;

    // -------------------------------------------------------------------------
    // Input/ Output
    // -------------------------------------------------------------------------

    public String getSelectedDE()
    {
        return selectedDE;
    }

    public void setSelectedDE( String selectedDE )
    {
        this.selectedDE = selectedDE;
    }

    private DeMapping demapping;

    public DeMapping getDemapping()
    {
        return demapping;
    }

    public void setDemapping( DeMapping demapping )
    {
        this.demapping = demapping;
    }

    @Autowired
    private DeMappingService demappingService;

    public DeMappingService getDemappingService()
    {
        return demappingService;
    }

    public void setDemappingService( DeMappingService demappingService )
    {
        this.demappingService = demappingService;
    }

    private String dataElementId;

    public void setDataElementId( String dataElementId )
    {
        this.dataElementId = dataElementId;
    }

    private String message;

    public String getMessage()
    {
        return message;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
    {
        // DataElement dataElement = dataElementService.getDataElement(
        // Integer.parseInt( dataElementId ) );
        // System.out.println("DE are--" +dataElement);

        System.out.println("selectedDE--"+selectedDE);
        demapping = demappingService.getDeMapping( selectedDE );
        if ( demapping == null )
        {
            message = "false";
        }
        else
        {
            message = "true";
        }
        return SUCCESS;
    }
}
