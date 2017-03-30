package org.hisp.dhis.ivb.dataentry.action;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.datavalue.DataValueAuditService;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.CurrentUserService;

import com.opensymphony.xwork2.Action;

public class CopyDataValueFromHistoryAction implements Action
{
    private static final Log log = LogFactory.getLog( CopyValueAndCommentAction.class );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private DataValueAuditService dataValueAuditService;

    public void setDataValueAuditService( DataValueAuditService dataValueAuditService )
    {
        this.dataValueAuditService = dataValueAuditService;
    }

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    private DataElementCategoryService categoryService;

    public void setCategoryService( DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }


    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private Integer dataValueAuditId;

    public void setDataValueAuditId( Integer dataValueAuditId )
    {
        this.dataValueAuditId = dataValueAuditId;
    }
    
    private String selectedPeriodId;
    
    public void setSelectedPeriodId( String selectedPeriodId )
    {
        this.selectedPeriodId = selectedPeriodId;
    }
    
    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private DataValue dataValue;
    
    public DataValue getDataValue() 
    {
		return dataValue;
	}

	private int statusCode = 0;

    public int getStatusCode()
    {
        return statusCode;
    }

    private SimpleDateFormat standardDateFormat;
    
    public SimpleDateFormat getStandardDateFormat() 
    {
		return standardDateFormat;
	}
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

	public String execute()
    {
    	standardDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
    	
        DataValueAudit dataValueAudit = dataValueAuditService.getDataValueAuditById( dataValueAuditId );

        Period period = PeriodType.getPeriodFromIsoString( selectedPeriodId );

        if ( period == null )
        {
            return logError( "Illegal period identifier: " + selectedPeriodId, 1 );
        }

        OrganisationUnit organisationUnit = dataValueAudit.getOrganisationUnit();
       
        DataElement dataElement = dataValueAudit.getDataElement();

        DataElementCategoryOptionCombo optionCombo = dataValueAudit.getCategoryOptionCombo();
        
        String value = dataValueAudit.getValue();
        
        if ( value != null && value.trim().length() == 0 )
        {
            value = null;
        }

        if ( value != null )
        {
            value = value.trim();
        }

        String comment = dataValueAudit.getComment();
        
        String storedBy = currentUserService.getCurrentUsername();

        if ( storedBy == null )
        {
            storedBy = "[unknown]";
        }

        Date now = new Date();


        // ---------------------------------------------------------------------
        // Check locked status
        // ---------------------------------------------------------------------

        if ( dataSetService.isLocked( dataElement, period, organisationUnit, null ) )
        {
            return logError( "Entry locked for combination: " + dataElement + ", " + period + ", " + organisationUnit, 2 );
        }

        // ---------------------------------------------------------------------
        // Update data
        // ---------------------------------------------------------------------

        dataValue = dataValueService.getDataValue( dataElement, period, organisationUnit, optionCombo );

        if ( dataValue == null )
        {
            if ( ( value != null && !value.trim().equals( "" ) ) || ( comment != null && !comment.trim().equals( "" ) ) )
            {    
                dataValue = new DataValue( dataElement, period, organisationUnit, optionCombo, optionCombo, value, storedBy, now, comment ); 
                dataValue.setStatus(1);
                dataValueService.addDataValue( dataValue );
                
                DataValue dataValue1 = dataValueService.getDataValue( dataElement, period, organisationUnit, optionCombo );
                DataValueAudit dataValueAudit1 = new DataValueAudit( dataValue1, dataValue1.getValue(), dataValue1.getStoredBy(), dataValue1.getLastUpdated(), dataValue1.getComment(), DataValueAudit.DVA_CT_HISOTRY, DataValueAudit.DVA_STATUS_ACTIVE );
                dataValueAuditService.addDataValueAudit( dataValueAudit1 );
            }
        }
        else
        {
            dataValue.setValue( value );
            dataValue.setComment( comment );
            dataValue.setLastUpdated( now );
            dataValue.setStoredBy( storedBy );
            dataValue.setStatus(1);
            dataValueService.updateDataValue( dataValue );
                
            DataValueAudit dataValueAudit1 = dataValueAuditService.getDataValueAuditByLastUpdated_StoredBy( dataElement, organisationUnit, now, storedBy, 1, DataValueAudit.DVA_CT_HISOTRY );
                
            if( dataValueAudit1 == null )
            {
                dataValueAudit1 = new DataValueAudit( dataValue, dataValue.getValue(), dataValue.getStoredBy(), dataValue.getLastUpdated(), dataValue.getComment(), DataValueAudit.DVA_CT_HISOTRY, DataValueAudit.DVA_STATUS_ACTIVE );     
                dataValueAuditService.addDataValueAudit( dataValueAudit1 );      
            }
            else
            {
                dataValueAudit1.setOrganisationUnit( dataValue.getSource( ) );
                dataValueAudit1.setDataElement( dataValue.getDataElement( ) );
                dataValueAudit1.setPeriod( dataValue.getPeriod( ) );
				dataValueAudit1.setCategoryOptionCombo( dataValue.getCategoryOptionCombo() );

                //dataValueAudit1.setDataValue( dataValue );
                dataValueAudit1.setValue( value );
                dataValueAudit1.setComment( comment );
                dataValueAudit1.setCommentType( DataValueAudit.DVA_CT_HISOTRY );
                dataValueAudit1.setModifiedBy( storedBy );
                dataValueAudit1.setTimestamp( now );
                dataValueAuditService.updateDataValueAudit( dataValueAudit1 );
            }
            dataValueService.updateDataValue( dataValue );
        }
        
        return SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private String logError( String message, int statusCode )
    {
        log.info( message );

        this.statusCode = statusCode;

        return SUCCESS;
    }

}
