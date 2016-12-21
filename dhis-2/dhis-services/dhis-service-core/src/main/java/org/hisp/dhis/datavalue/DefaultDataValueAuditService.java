package org.hisp.dhis.datavalue;

/*
 * Copyright (c) 2004-2015, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
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

import java.util.Collection;
import java.util.List;
import java.util.Date;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Quang Nguyen
 * @author Halvdan Hoem Grelland
 */
public class DefaultDataValueAuditService
    implements DataValueAuditService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataValueAuditStore dataValueAuditStore;

    public void setDataValueAuditStore( DataValueAuditStore dataValueAuditStore )
    {
        this.dataValueAuditStore = dataValueAuditStore;
    }

    // -------------------------------------------------------------------------
    // DataValueAuditService implementation
    // -------------------------------------------------------------------------

    @Override
    public void addDataValueAudit( DataValueAudit dataValueAudit )
    {
        dataValueAuditStore.addDataValueAudit( dataValueAudit );
    }
    
    @Override
    public void deleteDataValueAudits( OrganisationUnit organisationUnit )
    {
        dataValueAuditStore.deleteDataValueAudits( organisationUnit );
    }

    @Override
    @Transactional
    public void deleteDataValueAudit( DataValueAudit dataValueAudit )
    {
        dataValueAuditStore.deleteDataValueAudit( dataValueAudit );
    }

    @Override
    public List<DataValueAudit> getDataValueAudits( DataValue dataValue )
    {
        return dataValueAuditStore.getDataValueAudits( dataValue );
    }

    @Override
    public List<DataValueAudit> getDataValueAudits( DataElement dataElement, Period period,
        OrganisationUnit organisationUnit, DataElementCategoryOptionCombo categoryOptionCombo, DataElementCategoryOptionCombo attributeOptionCombo )
    {
        return dataValueAuditStore.getDataValueAudits( dataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo );
    }

    @Override
    @Transactional
    public int deleteDataValueAuditByDataElement( DataElement dataElement )
    {
        return dataValueAuditStore.deleteDataValueAuditByDataElement( dataElement );
    }

    @Override
    @Transactional
    public int deleteDataValueAuditByPeriod( Period period )
    {
        return dataValueAuditStore.deleteDataValueAuditByPeriod( period );
    }

    @Override
    @Transactional
    public int deleteDataValueAuditByOrganisationUnit( OrganisationUnit organisationUnit )
    {
        return dataValueAuditStore.deleteDataValueAuditByOrganisationUnit( organisationUnit );
    }

    @Override
    @Transactional
    public int deleteDataValueAuditByCategoryOptionCombo( DataElementCategoryOptionCombo categoryOptionCombo )
    {
        return dataValueAuditStore.deleteDataValueAuditByCategoryOptionCombo( categoryOptionCombo );
    }
	
	    public DataValueAudit getDataValueAuditById( Integer id )
    {        
        return dataValueAuditStore.getDataValueAuditById( id );
    }

    public DataValueAudit getDataValueAuditByLastUpdated_StoredBy( DataElement dataElement, OrganisationUnit organisationUnit, Date timeStamp, String storedBy, Integer status, String commentType )
    {
        return dataValueAuditStore.getDataValueAuditByLastUpdated_StoredBy(  dataElement, organisationUnit,timeStamp, storedBy, status, commentType );
    }

    public void updateDataValueAudit( DataValueAudit dataValueAudit )
    {       
        dataValueAuditStore.updateDataValueAudit( dataValueAudit );       
    }

    public Collection<DataValueAudit> getDataValueAuditByOrgUnit_DataElement( DataElement dataElement, OrganisationUnit organisationUnit )
    {
        return dataValueAuditStore.getDataValueAuditByOrgUnit_DataElement( dataElement, organisationUnit );
    }
    
    public Collection<DataValueAudit> getActiveDataValueAuditByOrgUnit_DataElement( DataElement dataElement, OrganisationUnit organisationUnit )
    {
        return dataValueAuditStore.getActiveDataValueAuditByOrgUnit_DataElement( dataElement, organisationUnit );
    }
    
    public Collection<DataValueAudit> getActiveDataValueAuditByOrgUnit_DataElement_Period( DataElement dataElement, OrganisationUnit organisationUnit, Period period )
    {
    	return dataValueAuditStore.getActiveDataValueAuditByOrgUnit_DataElement_Period( dataElement, organisationUnit, period );
    }
    
    public Collection<DataValueAudit> getActiveDataValueAuditByOrgUnit_DataElement_Period_type( DataElement dataElement, OrganisationUnit organisationUnit, Period period, String commentType )
    {        
    	return dataValueAuditStore.getActiveDataValueAuditByOrgUnit_DataElement_Period_type( dataElement, organisationUnit, period, commentType );
    }
    
    public DataValueAudit getLatestDataValueAudit( DataElement dataElement, OrganisationUnit organisationUnit, String commentType )
    {
        return dataValueAuditStore.getLatestDataValueAudit( dataElement, organisationUnit, commentType );
    }
    
    public DataValueAudit getLatestDataValueAudit( DataElement dataElement, OrganisationUnit organisationUnit, String commentType, String value, String comment )
    {
        return dataValueAuditStore.getLatestDataValueAudit( dataElement, organisationUnit, commentType, value, comment );
    }

    public Collection<DataValueAudit> getDataValueAuditByDataValue( DataValue dataValue )
    {
        return dataValueAuditStore.getDataValueAuditByDataValue( dataValue );
    }

    public Collection<DataValueAudit> getActiveDataValueAuditByDataValue( DataValue dataValue )
    {
        return dataValueAuditStore.getActiveDataValueAuditByDataValue( dataValue );
    }

}
