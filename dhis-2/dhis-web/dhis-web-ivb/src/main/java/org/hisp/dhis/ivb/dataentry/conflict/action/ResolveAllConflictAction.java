package org.hisp.dhis.ivb.dataentry.conflict.action;

/*
 * Copyright (c) 2004-2012, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.datavalue.DataValueAuditService;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.CurrentUserService;

import com.opensymphony.xwork2.Action;

/**
 * @author Samta bajpai
 */
public class ResolveAllConflictAction
    implements Action
{
    private static final Log log = LogFactory.getLog( ResolveAllConflictAction.class );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

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

    private DataValueAuditService dataValueAuditService;

    public void setDataValueAuditService( DataValueAuditService dataValueAuditService )
    {
        this.dataValueAuditService = dataValueAuditService;
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private String selectedValue;

    public void setSelectedValue( String selectedValue )
    {
        this.selectedValue = selectedValue;
    }

    public String getSelectedValue()
    {
        return selectedValue;
    }

    private String value;

    public void setValue( String value )
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    private String dataElementId;

    public void setDataElementId( String dataElementId )
    {
        this.dataElementId = dataElementId;
    }

    public String getDataElementId()
    {
        return dataElementId;
    }

    public String getOptionComboId()
    {
        return optionComboId;
    }

    private int organisationUnitId;

    public void setOrganisationUnitId( int organisationUnitId )
    {
        this.organisationUnitId = organisationUnitId;
    }

    public int getOrganisationUnitId()
    {
        return organisationUnitId;
    }

    private String optionComboId;

    public void setOptionComboId( String optionComboId )
    {
        this.optionComboId = optionComboId;
    }

    private String periodId;

    public void setPeriodId( String periodId )
    {
        this.periodId = periodId;
    }

    public String getPeriodId()
    {
        return periodId;
    }

    private String status;

    public void setStatus( String status )
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    private String selectedPeriodId;

    public void setSelectedPeriodId( String selectedPeriodId )
    {
        this.selectedPeriodId = selectedPeriodId;
    }

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private int statusCode = 0;

    public int getStatusCode()
    {
        return statusCode;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
    {
        Period period = PeriodType.getPeriodFromIsoString( selectedPeriodId );
        
        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();

        if ( period == null )
        {
            return logError( "Illegal period identifier: " + selectedPeriodId, 1 );
        }

        String[] splitSelectValues = selectedValue.split( "\\|" );
        for ( String values : splitSelectValues )
        {

            String[] valuesType = values.split( "-" );

            OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( Integer.parseInt( valuesType[1] ) );
            
            DataElement dataElement = dataElementService.getDataElement( Integer.parseInt( valuesType[2] ) );
            
            //System.out.println( dataElement.getId() + " : " + organisationUnit.getId() + " : " + period.getId() );
            
            DataValue dataValue = dataValueService.getDataValue( dataElement, period, organisationUnit, optionCombo );

            Date now = new Date();
            if ( dataValue == null )
            {
                DataValue dataValue1 = dataValueService.getLatestDataValue( dataElement, optionCombo, organisationUnit );
                dataValue = new DataValue( dataElement, period, organisationUnit, optionCombo, optionCombo, dataValue1.getValue(), dataValue1.getStoredBy(), now, dataValue1.getComment() );
                dataValueService.addDataValue( dataValue );
            }
            
            if ( valuesType[0].equalsIgnoreCase( "latest" ) )
            {
                dataValue.setFollowup( false );
                dataValueService.updateDataValue( dataValue );

            }
            else if ( valuesType[0].equalsIgnoreCase( "history" ) )
            {
                String dataValueAuditId = valuesType[3];

                DataValueAudit dataValueAudit = dataValueAuditService.getDataValueAuditById( Integer.parseInt( dataValueAuditId ) );
                
                //dataValue = new DataValue();
                dataValue.setValue( dataValueAudit.getValue() );
                dataValue.setComment( dataValueAudit.getComment() );
                dataValue.setStoredBy( dataValueAudit.getModifiedBy() );
                dataValue.setFollowup( false );

                DataValue dataValue2 = dataValueService.getLatestDataValue( dataElement, optionCombo, organisationUnit );
                DataValueAudit dataValueAudit1 = new DataValueAudit( dataValue2, dataValue2.getValue(),
                    dataValue2.getStoredBy(), dataValue2.getLastUpdated(), dataValue2.getComment(),
                    DataValueAudit.DVA_CT_HISOTRY, DataValueAudit.DVA_STATUS_ACTIVE );
                
                dataValueAuditService.addDataValueAudit( dataValueAudit1 );

                dataValueService.updateDataValue( dataValue );

            }
        }

        return SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private String logError( String message )
    {
        return logError( message, 1 );
    }

    private String logError( String message, int statusCode )
    {
        log.info( message );

        this.statusCode = statusCode;

        return SUCCESS;
    }
}
