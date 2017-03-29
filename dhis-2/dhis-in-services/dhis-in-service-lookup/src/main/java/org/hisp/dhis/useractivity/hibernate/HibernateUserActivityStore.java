package org.hisp.dhis.useractivity.hibernate;

import java.util.Collection;
import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.common.hibernate.HibernateIdentifiableObjectStore;
import org.hisp.dhis.ivb.useractivity.UserActivity;
import org.hisp.dhis.ivb.useractivity.UserActivityStore;
import org.hisp.dhis.reports.Report_in;
import org.hisp.dhis.user.User;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class HibernateUserActivityStore
    extends HibernateIdentifiableObjectStore<UserActivity>
    implements UserActivityStore
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

    //@Override
    public void addUserActivity( UserActivity useractivity )
    {
        Session session = sessionFactory.getCurrentSession();
        session.save( useractivity );
        
        session.flush();        
    }

    //@Override
    public void updateUserActivity( UserActivity useractivity )
    {
        Session session = sessionFactory.getCurrentSession();
        
        session.update( useractivity );
        
        session.flush();
    }

    //@Override
    public void deleteUserActivity( UserActivity useractivity )
    {
        Session session = sessionFactory.getCurrentSession();

        session.delete( useractivity );
    }

    //@Override
    public UserActivity getUserActivity( String userid )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( UserActivity.class );
        criteria.add( Restrictions.eq( "userid", userid ) );
        
        return (UserActivity)criteria.uniqueResult();
    }

    @SuppressWarnings( "unchecked" )
    //@Override
    public Collection<UserActivity> getAllUserActivitys()
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( UserActivity.class );

        return criteria.list();
    }
    
    @SuppressWarnings( "unchecked" )
    public Collection<UserActivity> getUserActivityByDate( Date startdate, Date endDate )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( UserActivity.class );

       // criteria.add( Restrictions.between( "loginTime", startdate, endDate ) );
        criteria.add(Restrictions.ge("loginTime", startdate)); 
        criteria.add(Restrictions.le("loginTime", endDate));
        
        return criteria.list();

    }

    @SuppressWarnings( "unchecked" )
    public Collection<UserActivity> getUserActivityByUserAndDate( User user, Date startdate, Date endDate )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( UserActivity.class );

        criteria.add( Restrictions.eq( "user", user ) );
        
      //  criteria.add( Restrictions.between( "loginTime", startdate, endDate ) );
        
        criteria.add(Restrictions.ge("loginTime", startdate)); 
        criteria.add(Restrictions.le("loginTime", endDate));
        

        return criteria.list();
    }

    public void deleteUser( User arg0 )
    {
        // TODO Auto-generated method stub
        
    }

}


