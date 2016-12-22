package org.hisp.dhis.ivb.dataentry.action;

import java.util.Date;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.CurrentUserService;

import com.opensymphony.xwork2.Action;

public class LoadTechnicalAssistanceAction implements Action
{
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
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

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private DataElementCategoryService categoryService;

    public void setCategoryService( DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }


    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    private String dataValueObj;
    
    public void setDataValueObj( String dataValueObj )
    {
        this.dataValueObj = dataValueObj;
    }

    private String isSave;
    
    public void setIsSave( String isSave )
    {
        this.isSave = isSave;
    }
    
    
    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------



    public String execute()
    {
        String periodId = dataValueObj.split( "-" )[0];
        Integer organisationUnitId = Integer.parseInt( dataValueObj.split( "-" )[1] );
        Integer dataElementId = Integer.parseInt( dataValueObj.split( "-" )[2] );
        Integer optionComboId = Integer.parseInt( dataValueObj.split( "-" )[3] );
        
        Period period = PeriodType.getPeriodFromIsoString( periodId );
        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );
        DataElement dataElement = dataElementService.getDataElement( dataElementId );
        DataElementCategoryOptionCombo optionCombo = categoryService.getDataElementCategoryOptionCombo( optionComboId );
        String storedBy = currentUserService.getCurrentUsername();
        Date now = new Date();

        if ( storedBy == null )
        {
            storedBy = "[unknown]";
        }

        DataValue dataValue = dataValueService.getDataValue( dataElement, period, organisationUnit, optionCombo );

        return SUCCESS;
    }

}
