package org.hisp.dhis.ivb.aggregation.action;

import org.hisp.dhis.caseaggregation.CaseAggregationCondition;
import org.hisp.dhis.caseaggregation.CaseAggregationConditionService;

import com.opensymphony.xwork2.Action;

public class DeleteAggregationQueryAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private CaseAggregationConditionService aggregationConditionService;

    public void setAggregationConditionService( CaseAggregationConditionService aggregationConditionService )
    {
        this.aggregationConditionService = aggregationConditionService;
    }

    // -------------------------------------------------------------------------
    // Input/ Output
    // -------------------------------------------------------------------------

    private String aggregationCaseId;

    public void setAggregationCaseId( String aggregationCaseId )
    {
        this.aggregationCaseId = aggregationCaseId;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
    {
        
        CaseAggregationCondition caseAggregationCondition = aggregationConditionService
            .getCaseAggregationCondition( Integer.parseInt( aggregationCaseId ) );
        
        aggregationConditionService.deleteCaseAggregationCondition( caseAggregationCondition );

        return SUCCESS;
    }

}
