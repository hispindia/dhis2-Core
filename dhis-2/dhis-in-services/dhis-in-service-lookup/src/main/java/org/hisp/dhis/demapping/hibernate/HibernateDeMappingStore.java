package org.hisp.dhis.demapping.hibernate;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.common.hibernate.HibernateIdentifiableObjectStore;
import org.hisp.dhis.ivb.demapping.DeMapping;
import org.hisp.dhis.ivb.demapping.DeMappingStore;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class HibernateDeMappingStore
    extends HibernateIdentifiableObjectStore<DeMapping>
    implements DeMappingStore
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SessionFactory sessionFactory;

    public void setSessionFactory( SessionFactory sessionFactory )
    {
        this.sessionFactory = sessionFactory;
    }

    // -------------------------------------------------------------------------
    // DeMapping implementation
    // -------------------------------------------------------------------------

    @Override
    public void addDeMapping( DeMapping demapping )
    {
        Session session = sessionFactory.getCurrentSession();
        session.save( demapping );
        
        session.flush();        
    }

    @Override
    public void updateDeMapping( DeMapping demapping )
    {
        Session session = sessionFactory.getCurrentSession();
        
        session.update( demapping );
        
        session.flush();
    }

    @Override
    public void deleteDeMapping( DeMapping demapping )
    {
        Session session = sessionFactory.getCurrentSession();

        session.delete( demapping );
    }

    @Override
    public DeMapping getDeMapping( String deid )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DeMapping.class );
        criteria.add( Restrictions.eq( "deid", deid ) );
        
        return (DeMapping)criteria.uniqueResult();
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Collection<DeMapping> getAllDeMappings()
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DeMapping.class );

        return criteria.list();
    }
}
