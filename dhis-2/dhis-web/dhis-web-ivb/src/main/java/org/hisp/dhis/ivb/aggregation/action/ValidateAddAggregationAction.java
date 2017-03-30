package org.hisp.dhis.ivb.aggregation.action;

import java.util.ArrayList;
import java.util.List;

import org.hisp.dhis.caseaggregation.CaseAggregationCondition;
import org.hisp.dhis.caseaggregation.CaseAggregationConditionService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;

import com.opensymphony.xwork2.Action;

public class ValidateAddAggregationAction
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

    private CaseAggregationConditionService aggregationConditionService;

    public void setAggregationConditionService( CaseAggregationConditionService aggregationConditionService )
    {
        this.aggregationConditionService = aggregationConditionService;
    }

    // -------------------------------------------------------------------------
    // Input/ Output
    // -------------------------------------------------------------------------

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
        DataElement dataElement = dataElementService.getDataElement( Integer.parseInt( dataElementId ) );
        List<CaseAggregationCondition> caseAggregationConditions = new ArrayList<CaseAggregationCondition>(
            aggregationConditionService.getCaseAggregationCondition( dataElement ) );
        if ( caseAggregationConditions.size() > 0 )
        {
            message = "true";
        }
        else
        {
            message = "false";
        }
        return SUCCESS;
    }
}
