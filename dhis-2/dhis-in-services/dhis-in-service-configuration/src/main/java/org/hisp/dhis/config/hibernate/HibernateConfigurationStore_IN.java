package org.hisp.dhis.config.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.config.ConfigurationStore_IN;
import org.hisp.dhis.config.Configuration_IN;
import org.springframework.beans.factory.annotation.Autowired;

//public class HibernateConfigurationStore_IN extends HibernateGenericStore<Configuration_IN> 
public class HibernateConfigurationStore_IN implements ConfigurationStore_IN
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private SessionFactory sessionFactory;
   
    
    /*
    private SessionFactory sessionFactory;

    public void setSessionFactory( SessionFactory sessionFactory )
    {
        this.sessionFactory = sessionFactory;
    }
    */
    
    // -------------------------------------------------------------------------
    // Config
    // -------------------------------------------------------------------------
    
    @Override
    public int addConfiguration( Configuration_IN con )
    {        
        Session session = sessionFactory.getCurrentSession();
        
        return (Integer) session.save( con );        
    }
    @Override
    public void updateConfiguration( Configuration_IN con )
    {
        Session session = sessionFactory.getCurrentSession();
        
        session.update( con );
    }

    @Override
    public void deleteConfiguration( Configuration_IN con )
    {
        Session session = sessionFactory.getCurrentSession();
        
        session.delete( con );
    }
    @Override
    public Configuration_IN getConfiguration( int id )
    {
        Session session = sessionFactory.getCurrentSession();

        return (Configuration_IN) session.get( Configuration_IN.class, id );       
    }
    @Override
    public Configuration_IN getConfigurationByKey( String ckey )
    {        
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( Configuration_IN.class );
        criteria.add( Restrictions.eq( "key", ckey ) );
        
        return (Configuration_IN) criteria.uniqueResult();
    }    
}
