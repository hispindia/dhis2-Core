package org.hisp.dhis.ivb.aggregation.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.caseaggregation.CaseAggregationCondition;
import org.hisp.dhis.caseaggregation.CaseAggregationConditionService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.ivb.util.AggregationManager;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;

import com.opensymphony.xwork2.Action;

public class UpdateAggregationQueryAction
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

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private LookupService lookupService;

    public void setLookupService( LookupService lookupService )
    {
        this.lookupService = lookupService;
    }

    private AggregationManager aggregationManager;

    public void setAggregationManager( AggregationManager aggregationManager )
    {
        this.aggregationManager = aggregationManager;
    }

    private DataElementCategoryService dataElementCategoryService;

    public void setDataElementCategoryService( DataElementCategoryService dataElementCategoryService )
    {
        this.dataElementCategoryService = dataElementCategoryService;
    }

    // -------------------------------------------------------------------------
    // Input/ Output
    // -------------------------------------------------------------------------

    private Integer queryDeId;

    public void setQueryDeId( Integer queryDeId )
    {
        this.queryDeId = queryDeId;
    }

    private String optionSetOption;

    public void setOptionSetOption( String optionSetOption )
    {
        this.optionSetOption = optionSetOption;
    }

    private String name;

    public void setName( String name )
    {
        this.name = name;
    }

    private String aggType;

    public void setAggType( String aggType )
    {
        this.aggType = aggType;
    }

    private Integer dataElementId;

    public void setDataElementId( Integer dataElementId )
    {
        this.dataElementId = dataElementId;
    }

    private Integer keyFlagIndicatorId;
    
    public void setKeyFlagIndicatorId( Integer keyFlagIndicatorId )
    {
        this.keyFlagIndicatorId = keyFlagIndicatorId;
    }
    
    private String aggregationCaseId ;
    
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
        
        CaseAggregationCondition caseAggregationCondition = aggregationConditionService.getCaseAggregationCondition( Integer.parseInt( aggregationCaseId ) );
        
        DataElement aggDataElement = dataElementService.getDataElement( dataElementId );
        
        //DataElement queryDataElement = dataElementService.getDataElement( queryDeId );

        if( aggType.equals( Lookup.AGG_TYPE_BY_OPTION ) )
        {
            String optionSetId = optionSetOption.split( "#@#" )[0];
            
            String optionValue = optionSetOption.split( "#@#" )[1];
            
            String query = aggregationManager.prepareQueryForByOption( queryDeId, optionValue );
            
            caseAggregationCondition.setName( aggDataElement.getName() );
            caseAggregationCondition.setOperator( aggType );
            caseAggregationCondition.setAggregationExpression( query );
            caseAggregationCondition.setAggregationDataElement( aggDataElement );
            
            caseAggregationCondition.setOptionCombo( dataElementCategoryService.getDefaultDataElementCategoryOptionCombo() );
            aggregationConditionService.updateCaseAggregationCondition( caseAggregationCondition );
            
        }
        else if( aggType.equals( Lookup.AGG_TYPE_BY_OPTION_SCORE ) )
        {
            String query = aggregationManager.prepareQueryForByOptionScore( queryDeId );

            caseAggregationCondition.setName( aggDataElement.getName() );
            caseAggregationCondition.setOperator( aggType );
            caseAggregationCondition.setAggregationExpression( query );
            caseAggregationCondition.setAggregationDataElement( aggDataElement );
            
            caseAggregationCondition.setOptionCombo( dataElementCategoryService.getDefaultDataElementCategoryOptionCombo() );
            aggregationConditionService.updateCaseAggregationCondition( caseAggregationCondition );
        }
        else if( aggType.equals( Lookup.AGG_TYPE_BY_KEYFLAG ) )
        {
            String query = ""+keyFlagIndicatorId;
            
            caseAggregationCondition.setName( aggDataElement.getName() );
            caseAggregationCondition.setOperator( aggType );
            caseAggregationCondition.setAggregationExpression( query );
            caseAggregationCondition.setAggregationDataElement( aggDataElement );
            
            caseAggregationCondition.setOptionCombo( dataElementCategoryService.getDefaultDataElementCategoryOptionCombo() );
            aggregationConditionService.updateCaseAggregationCondition( caseAggregationCondition );
        }
        else if( aggType.equals( Lookup.AGG_TYPE_BY_KEYFLAG_SCORE ) )
        {
            String query = "NONE";
            
            caseAggregationCondition.setName( aggDataElement.getName() );
            caseAggregationCondition.setOperator( aggType );
            caseAggregationCondition.setAggregationExpression( query );
            caseAggregationCondition.setAggregationDataElement( aggDataElement );
            
            caseAggregationCondition.setOptionCombo( dataElementCategoryService.getDefaultDataElementCategoryOptionCombo() );
            aggregationConditionService.updateCaseAggregationCondition( caseAggregationCondition );
        }
            
        return SUCCESS;
    }

}
