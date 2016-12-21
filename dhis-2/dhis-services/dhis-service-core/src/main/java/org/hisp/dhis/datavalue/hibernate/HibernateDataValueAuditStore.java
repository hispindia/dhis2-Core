package org.hisp.dhis.datavalue.hibernate;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.datavalue.DataValueAuditStore;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodStore;

/**
 * @author Quang Nguyen
 * @author Halvdan Hoem Grelland
 */
public class HibernateDataValueAuditStore
    implements DataValueAuditStore
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SessionFactory sessionFactory;

    public void setSessionFactory( SessionFactory sessionFactory )
    {
        this.sessionFactory = sessionFactory;
    }

    private PeriodStore periodStore;

    public void setPeriodStore( PeriodStore periodStore )
    {
        this.periodStore = periodStore;
    }

    // -------------------------------------------------------------------------
    // DataValueAuditStore implementation
    // -------------------------------------------------------------------------

    @Override
    public void addDataValueAudit( DataValueAudit dataValueAudit )
    {
        Session session = sessionFactory.getCurrentSession();

        session.save( dataValueAudit );
    }

    @Override
    public void deleteDataValueAudits( OrganisationUnit organisationUnit )
    {
        String hql = "delete from DataValueAudit d where d.organisationUnit = :unit";
        
        sessionFactory.getCurrentSession().createQuery( hql ).
            setEntity( "unit", organisationUnit ).executeUpdate();
    }

    @Override
    public List<DataValueAudit> getDataValueAudits( DataValue dataValue )
    {
        return getDataValueAudits( dataValue.getDataElement(), dataValue.getPeriod(),
            dataValue.getSource(), dataValue.getCategoryOptionCombo(), dataValue.getAttributeOptionCombo() );
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DataValueAudit> getDataValueAudits( DataElement dataElement, Period period,
        OrganisationUnit organisationUnit, DataElementCategoryOptionCombo categoryOptionCombo, DataElementCategoryOptionCombo attributeOptionCombo )
    {
        Session session = sessionFactory.getCurrentSession();

        Period storedPeriod = periodStore.reloadPeriod( period );

        if( storedPeriod == null )
        {
            return new ArrayList<>();
        }

        Criteria criteria = session.createCriteria( DataValueAudit.class )
            .add( Restrictions.eq( "dataElement", dataElement ) )
            .add( Restrictions.eq( "period", storedPeriod ) )
            .add( Restrictions.eq( "organisationUnit", organisationUnit ) )
            .add( Restrictions.eq( "categoryOptionCombo", categoryOptionCombo ) )
            .add( Restrictions.eq( "attributeOptionCombo", attributeOptionCombo ) )
            .addOrder( Order.desc( "timestamp" ) );

        return criteria.list();
    }

    @Override
    public void deleteDataValueAudit( DataValueAudit dataValueAudit )
    {
        Session session = sessionFactory.getCurrentSession();

        session.delete( dataValueAudit );
    }

    @Override
    public int deleteDataValueAuditByDataElement( DataElement dataElement )
    {
        Query query = sessionFactory.getCurrentSession()
            .createQuery( "delete DataValueAudit where dataElement = :dataElement" )
            .setEntity( "dataElement", dataElement );

        return query.executeUpdate();
    }

    @Override
    public int deleteDataValueAuditByPeriod( Period period )
    {
        Period storedPeriod = periodStore.reloadPeriod( period );

        Query query = sessionFactory.getCurrentSession()
            .createQuery( "delete DataValueAudit where period = :period" )
            .setEntity( "period", storedPeriod );

        return query.executeUpdate();
    }

    @Override
    public int deleteDataValueAuditByOrganisationUnit( OrganisationUnit organisationUnit )
    {
        Query query = sessionFactory.getCurrentSession()
            .createQuery( "delete DataValueAudit where organisationUnit = :organisationUnit" )
            .setEntity( "organisationUnit", organisationUnit );

        return query.executeUpdate();
    }

    @Override
    public int deleteDataValueAuditByCategoryOptionCombo( DataElementCategoryOptionCombo categoryOptionCombo )
    {
        Query query = sessionFactory.getCurrentSession()
            .createQuery( "delete DataValueAudit where categoryOptionCombo = :categoryOptionCombo or attributeOptionCombo = :categoryOptionCombo" )
            .setEntity( "categoryOptionCombo", categoryOptionCombo );

        return query.executeUpdate();
    }
	
	public DataValueAudit getDataValueAuditById( Integer id )
    {        
        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery( "SELECT dva FROM DataValueAudit dva WHERE dva.id ="+id );       
        
        return (DataValueAudit)query.uniqueResult();
    }
	
    public DataValueAudit getDataValueAuditByLastUpdated_StoredBy( DataElement dataElement, OrganisationUnit organisationUnit, Date timestamp, String modifiedBy, Integer status, String commentType )
    {
        final String hsql = "SELECT dva FROM DataValueAudit dva WHERE  dva.modifiedBy =:modifiedBy "
            + " AND dva.dataElement =:dataElement AND dva.organisationUnit=:organisationUnit " 
            + " AND dva.timestamp=:timestamp AND dva.status =:status AND dva.commentType =:commentType ORDER BY dva.timestamp DESC";

        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery( hsql );

        query.setParameter( "dataElement", dataElement );
        query.setParameter( "organisationUnit", organisationUnit );
        query.setParameter( "timestamp", timestamp );
        query.setParameter( "modifiedBy", modifiedBy );
        query.setParameter( "status", status );
        query.setParameter( "commentType", commentType );
        
        return (DataValueAudit) query.uniqueResult();
    }
    
    public void updateDataValueAudit( DataValueAudit dataValueAudit )
    {
    	dataValueAudit.setPeriod( periodStore.reloadForceAddPeriod( dataValueAudit.getPeriod() ) );

        Session session = sessionFactory.getCurrentSession();
        
        session.update( dataValueAudit );         
        
        session.flush();
    }
    
    @SuppressWarnings( "unchecked" )
    public Collection<DataValueAudit> getDataValueAuditByOrgUnit_DataElement( DataElement dataElement, OrganisationUnit organisationUnit )
    {
        final String hsql = "SELECT dva FROM DataValueAudit dva WHERE  dva.dataElement =:dataElement "
            + " AND dva.organisationUnit=:organisationUnit ORDER BY dva.timestamp,dva.period.startDate DESC";

        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery( hsql );

        query.setParameter( "dataElement", dataElement );
        query.setParameter( "organisationUnit", organisationUnit );
        
        return query.list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataValueAudit> getActiveDataValueAuditByOrgUnit_DataElement( DataElement dataElement, OrganisationUnit organisationUnit )
    {
        final String hsql = "SELECT dva FROM DataValueAudit dva WHERE  dva.dataElement =:dataElement "
            + " AND dva.organisationUnit=:organisationUnit AND dva.status = 1 ORDER BY dva.timestamp,dva.period.startDate DESC";

        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery( hsql );

        query.setParameter( "dataElement", dataElement );
        query.setParameter( "organisationUnit", organisationUnit );
        
        return query.list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataValueAudit> getActiveDataValueAuditByOrgUnit_DataElement_Period( DataElement dataElement, OrganisationUnit organisationUnit, Period period )
    {
        final String hsql = "SELECT dva FROM DataValueAudit dva WHERE  dva.dataElement =:dataElement "
            + " AND dva.organisationUnit=:organisationUnit AND dva.period=:period AND dva.status = 1 ORDER BY dva.timestamp DESC";

        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery( hsql );

        query.setParameter( "dataElement", dataElement );
        query.setParameter( "organisationUnit", organisationUnit );
        query.setParameter( "period", period );
        
        return query.list();
    }
    
    @SuppressWarnings( "unchecked" )
    public Collection<DataValueAudit> getActiveDataValueAuditByOrgUnit_DataElement_Period_type( DataElement dataElement, OrganisationUnit organisationUnit, Period period ,String commentType )
    {
        final String hsql = "SELECT dva FROM DataValueAudit dva WHERE  dva.dataElement =:dataElement "
            + " AND dva.organisationUnit=:organisationUnit AND dva.period=:period AND dva.status = 1 AND dva.commentType=:commentType AND ( dva.value IS NOT NULL OR dva.comment IS NOT NULL )  ORDER BY dva.timestamp DESC";

        //System.out.println( hsql );
        
        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery( hsql );

        query.setParameter( "dataElement", dataElement );
        query.setParameter( "organisationUnit", organisationUnit );
        query.setParameter( "period", period );
        query.setParameter( "commentType", commentType );
        
        return query.list();
    }
    
    public DataValueAudit getLatestDataValueAudit( DataElement dataElement, OrganisationUnit organisationUnit, String commentType )
    {
        final String hsql = "SELECT dva FROM DataValueAudit dva, Period p WHERE  dva.dataElement =:dataElement "
            + " AND dva.period=p AND dva.commentType=:commentType AND dva.organisationUnit=:organisationUnit ORDER BY p.endDate,dva.timestamp DESC";

        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery( hsql );

        query.setParameter( "dataElement", dataElement );
        query.setParameter( "commentType", commentType );
        query.setParameter( "organisationUnit", organisationUnit );
        
        query.setFirstResult( 0 );
        query.setMaxResults( 1 );

        return (DataValueAudit) query.uniqueResult();
    }

    public DataValueAudit getLatestDataValueAudit( DataElement dataElement, OrganisationUnit organisationUnit, String commentType, String dv_value, String dv_comment )
    {
        final String hsql = "SELECT dva FROM DataValueAudit dva, Period p WHERE dva.dataElement =:dataElement "
            + " AND dva.period=p AND dva.commentType=:commentType AND dva.organisationUnit=:organisationUnit "
            + " AND dva.value=:value AND dva.comment=:comment ORDER BY p.endDate,dva.timestamp DESC";

        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery( hsql );

        query.setParameter( "dataElement", dataElement );
        query.setParameter( "commentType", commentType );
        query.setParameter( "organisationUnit", organisationUnit );
        query.setParameter( "value", dv_value );
        query.setParameter( "comment", dv_comment );
        
        query.setFirstResult( 0 );
        query.setMaxResults( 1 );

        return (DataValueAudit) query.uniqueResult();
    }	

    @SuppressWarnings( "unchecked" )
    public Collection<DataValueAudit> getDataValueAuditByDataValue( DataValue dataValue )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataValueAudit.class );
        criteria.add( Restrictions.eq( "dataElement", dataValue.getDataElement() ) );
        criteria.add( Restrictions.eq( "organisationUnit", dataValue.getSource() ) );
        criteria.add( Restrictions.eq( "period", dataValue.getPeriod() ) );
        criteria.add( Restrictions.eq( "categoryOptionCombo", dataValue.getCategoryOptionCombo() ) );

        return criteria.list();
    }
	
    @SuppressWarnings( "unchecked" )
    public Collection<DataValueAudit> getActiveDataValueAuditByDataValue( DataValue dataValue )
    {
        Integer status = 1;
        
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataValueAudit.class );
        criteria.add( Restrictions.eq( "dataElement", dataValue.getDataElement() ) );
        criteria.add( Restrictions.eq( "organisationUnit", dataValue.getSource() ) );
        criteria.add( Restrictions.eq( "period", dataValue.getPeriod() ) );
        criteria.add( Restrictions.eq( "categoryOptionCombo", dataValue.getCategoryOptionCombo() ) );
        criteria.add( Restrictions.eq( "status", status ) );

        return criteria.list();
    }
	
}
