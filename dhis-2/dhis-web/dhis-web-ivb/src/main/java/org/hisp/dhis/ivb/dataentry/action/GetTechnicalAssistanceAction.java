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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.datavalue.DataValueAuditService;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.datavalue.comparator.DataValueAuditLastUpdatedComparator;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;

import com.opensymphony.xwork2.Action;

/**
 * @author Samta bajpai
 */
public class GetTechnicalAssistanceAction implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
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
    
    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }
    
    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------
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
    
    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private int statusCode = 0;

    public int getStatusCode()
    {
        return statusCode;
    }
    private List<DataValueAudit> historyList = new ArrayList<DataValueAudit>();    

    public List<DataValueAudit> getHistoryList()
    {
        return historyList;
    }

    private String dataElementName;
    
    public String getDataElementName()
    {
        return dataElementName;
    }
    
    private SimpleDateFormat simpleDateFormat1;
    
    public SimpleDateFormat getSimpleDateFormat1()
    {
        return simpleDateFormat1;
    }

    private SimpleDateFormat simpleDateFormat2;
    
    public SimpleDateFormat getSimpleDateFormat2()
    {
        return simpleDateFormat2;
    }
    
    private String periodTypeName;
    
    public String getPeriodTypeName()
    {
        return periodTypeName;
    }

    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------
    public String execute()
    {
        Period period = PeriodType.getPeriodFromIsoString( periodId );
        
        if( period.getPeriodType().getName().equalsIgnoreCase( "Quarterly" ) )
        {
            simpleDateFormat1 = new SimpleDateFormat( "MMM" );
            simpleDateFormat2 = new SimpleDateFormat( "MMM yyyy" );
            periodTypeName = "Quarterly";
        }
        else
        {
            simpleDateFormat1 = new SimpleDateFormat( "yyyy-MM-dd");
            periodTypeName = "Other";
        }

        DataElement dataElement = dataElementService.getDataElement( Integer.parseInt( dataElementId ) );
        
        dataElementName = dataElement.getName();
        
        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );
        
        DataElementCategoryOptionCombo optionCombo = categoryService.getDataElementCategoryOptionCombo( Integer.parseInt( optionComboId ) );
        
        historyList.addAll( dataValueAuditService.getActiveDataValueAuditByOrgUnit_DataElement( dataElement, organisationUnit ) );
        
        Collections.sort( historyList, new DataValueAuditLastUpdatedComparator() );
        
        return SUCCESS;
    }
}