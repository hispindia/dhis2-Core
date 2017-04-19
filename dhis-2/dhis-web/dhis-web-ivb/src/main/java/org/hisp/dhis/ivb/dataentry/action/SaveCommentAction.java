package org.hisp.dhis.ivb.dataentry.action;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.common.AuditType;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.datavalue.DataValueAuditService;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.ivb.util.ReportScheduler;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.CurrentUserService;

import com.opensymphony.xwork2.Action;

/**
 * @author Samta bajpai
 */
public class SaveCommentAction
    implements Action
{
    private static final Log log = LogFactory.getLog( SaveCommentAction.class );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

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

    private DataValueAuditService dataValueAuditService;

    public void setDataValueAuditService( DataValueAuditService dataValueAuditService )
    {
        this.dataValueAuditService = dataValueAuditService;
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

    private ReportScheduler reportScheduler;

    public void setReportScheduler( ReportScheduler reportScheduler )
    {
        this.reportScheduler = reportScheduler;
    }

    private ExpressionService expressionService;

    public void setExpressionService( ExpressionService expressionService )
    {
        this.expressionService = expressionService;
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private String value;

    public void setValue( String value )
    {
        this.value = value;
    }

    private String comment;

    public void setComment( String comment )
    {
        this.comment = comment;
    }

    private String dataElementId;

    public void setDataElementId( String dataElementId )
    {
        this.dataElementId = dataElementId;
    }

    private int organisationUnitId;

    public void setOrganisationUnitId( int organisationUnitId )
    {
        this.organisationUnitId = organisationUnitId;
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

    private String conflict;

    public void setConflict( String conflict )
    {
        this.conflict = conflict;
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
        Period period = PeriodType.getPeriodFromIsoString( periodId );
        // period.setId(Integer.parseInt(periodId));

        if ( period == null )
        {
            return logError( "Illegal period identifier: " + periodId );
        }

        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );

        if ( organisationUnit == null )
        {
            return logError( "Invalid organisation unit identifier: " + organisationUnitId );
        }

        DataElement dataElement = dataElementService.getDataElement( Integer.parseInt( dataElementId ) );

        if ( dataElement == null )
        {
            return logError( "Invalid data element identifier: " + dataElementId );
        }

        DataElementCategoryOptionCombo optionCombo = categoryService.getDataElementCategoryOptionCombo( Integer
            .parseInt( optionComboId ) );

        if ( optionCombo == null )
        {
            return logError( "Invalid category option combo identifier: " + optionComboId );
        }

        String storedBy = currentUserService.getCurrentUsername();

        Date now = new Date();

        if ( storedBy == null )
        {
            storedBy = "[unknown]";
        }

        if ( value != null && value.trim().length() == 0 )
        {
            value = null;
        }

        if ( value != null )
        {
            value = value.trim();
        }

        // ---------------------------------------------------------------------
        // Check locked status
        // ---------------------------------------------------------------------

        if ( dataSetService.isLocked( dataElement, period, organisationUnit, null ) )
        {
            return logError( "Entry locked for combination: " + dataElement + ", " + period + ", " + organisationUnit,
                2 );
        }

        // ---------------------------------------------------------------------
        // Update data
        // ---------------------------------------------------------------------

        DataValue dataValue = dataValueService.getDataValue( dataElement, period, organisationUnit, optionCombo );

        if ( dataValue == null )
        {
            if ( comment != null && !comment.trim().equals( "" ) )
            {
                dataValue = new DataValue( dataElement, period, organisationUnit, optionCombo, optionCombo, value,
                    storedBy, now, comment.trim() );
                dataValue.setStatus( 1 );
                dataValueService.addDataValue( dataValue );
                
                DataValue dataValue1 = dataValueService.getDataValue( dataElement, period, organisationUnit,
                        optionCombo );
                DataValueAudit dataValueAudit = new DataValueAudit();
                dataValueAudit.setOrganisationUnit( dataValue1.getSource() );
                dataValueAudit.setCategoryOptionCombo( dataValue1.getCategoryOptionCombo() );
                dataValueAudit.setPeriod( dataValue1.getPeriod() );
                dataValueAudit.setDataElement( dataValue1.getDataElement() );
                dataValueAudit.setAttributeOptionCombo(dataValue1.getCategoryOptionCombo());
                
                // dataValueAudit.setDataValue( dataValue );
                dataValueAudit.setValue( dataValue1.getValue() );
                dataValueAudit.setComment( dataValue1.getComment() );
                dataValueAudit.setCommentType( DataValueAudit.DVA_CT_HISOTRY );
                dataValueAudit.setModifiedBy( storedBy );
                dataValueAudit.setStatus(1);
                dataValueAudit.setTimestamp( now );
                dataValueAudit.setAuditType(AuditType.UPDATE);
             
                dataValueAuditService.addDataValueAudit( dataValueAudit );
            }
        }
        else
        {
            if ( comment == null )
                comment = "";
            if ( !comment.trim().equalsIgnoreCase( dataValue.getComment() ) )
            {
                if ( conflict != null && conflict.equalsIgnoreCase( "null" )
                    && !(dataValue.getStoredBy().equalsIgnoreCase( storedBy )) )
                {
                    dataValue.setFollowup( true );
                }
                else
                {
                    dataValue.setFollowup( false );
                }
                dataValue.setComment( comment.trim() );
                dataValue.setValue( value );
                dataValue.setLastUpdated( now );
                dataValue.setStoredBy( storedBy );
                dataValue.setStatus( 1 );
                dataValueService.updateDataValue( dataValue );

                DataValueAudit dataValueAudit = dataValueAuditService.getDataValueAuditByLastUpdated_StoredBy(
                    dataElement, organisationUnit, now, storedBy, 1, DataValueAudit.DVA_CT_HISOTRY );

                if ( dataValueAudit == null )
                {
                    /*dataValueAudit = new DataValueAudit( dataValue, dataValue.getValue(), dataValue.getStoredBy(),
                        dataValue.getLastUpdated(), dataValue.getComment(), DataValueAudit.DVA_CT_HISOTRY,
                        DataValueAudit.DVA_STATUS_ACTIVE );*/
                	
                	   dataValueAudit = new DataValueAudit();
                       dataValueAudit.setOrganisationUnit( dataValue.getSource() );
                       dataValueAudit.setCategoryOptionCombo( dataValue.getCategoryOptionCombo() );
                       dataValueAudit.setPeriod( dataValue.getPeriod() );
                       dataValueAudit.setDataElement( dataValue.getDataElement() );
                       dataValueAudit.setAttributeOptionCombo(dataValue.getCategoryOptionCombo());
                       
                       // dataValueAudit.setDataValue( dataValue );
                       dataValueAudit.setValue( dataValue.getValue() );
                       dataValueAudit.setComment( dataValue.getComment() );
                       dataValueAudit.setCommentType( DataValueAudit.DVA_CT_HISOTRY );
                       dataValueAudit.setModifiedBy( storedBy );
                       dataValueAudit.setStatus(1);
                       dataValueAudit.setTimestamp( now );
                       dataValueAudit.setAuditType(AuditType.UPDATE);
                       
                    
                       dataValueAuditService.addDataValueAudit( dataValueAudit );
                	
                }
                else
                {
                    dataValueAudit.setComment( comment.trim() );
                    dataValueAudit.setCommentType( DataValueAudit.DVA_CT_HISOTRY );
                    dataValueAuditService.updateDataValueAudit( dataValueAudit );
                }

            }
        }

        // Saving in Key Flag Analytic
        Set<DataSet> datasets = new HashSet<DataSet>();

        datasets = new HashSet<DataSet>( dataElement.getDataSets() );

        Set<Indicator> indicatorList = new HashSet<Indicator>();

        if ( datasets != null && datasets.size() > 0 )
        {
            for ( DataSet dataSet : datasets )
            {
                indicatorList.addAll( dataSet.getIndicators() );
            }

            if ( indicatorList != null && indicatorList.size() > 0 )
            {
                List<DataElement> deList = new ArrayList<DataElement>();

                deList = new ArrayList<DataElement>( expressionService.getDataElementsInIndicators( indicatorList ) );

                //System.out.println( " Indicator Size  " + indicatorList.size() + "-- DataElement Size : "   + deList.size() );

                for ( Indicator indicator : indicatorList )
                {
                    if ( deList.contains( dataElement ) )
                    {
                        //System.out.println( " OrgUnit Id " + organisationUnit.getId() + "-- Indicator Id : "  + indicator.getId() );
                        reportScheduler.updateSingleKeyFlagAnalytic( organisationUnit, indicator );
                    }
                }

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
