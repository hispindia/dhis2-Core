package org.hisp.dhis.ivb.kfa.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.datavalue.hibernate.HibernateDataValueStore;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.ivb.kfa.KeyFlagAnalytics;
import org.hisp.dhis.ivb.kfa.KeyFlagAnalyticsStore;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class HibernateKeyFlagAnalyticsStore
    implements KeyFlagAnalyticsStore
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private IndicatorService indicatorService;

    @SuppressWarnings( "unused" )
    private static final Log log = LogFactory.getLog( HibernateDataValueStore.class );

    private SessionFactory sessionFactory;

    public void setSessionFactory( SessionFactory sessionFactory )
    {
        this.sessionFactory = sessionFactory;
    }
    
    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    @Override
    public void saveRegionalReport( KeyFlagAnalytics regionalReport )
    {
        Session session = sessionFactory.getCurrentSession();
        session.save( regionalReport );
    }

    @Override
    public void updateRegionalReport( KeyFlagAnalytics regionalReport )
    {
        Session session = sessionFactory.getCurrentSession();
        session.update( regionalReport );
    }

    @Override
    public void deleteRegionalReport( KeyFlagAnalytics regionalReport )
    {
        Session session = sessionFactory.getCurrentSession();
        session.delete( regionalReport );

    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Collection<KeyFlagAnalytics> getAllRegionalReports()
    {

        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria( KeyFlagAnalytics.class );
        return criteria.list();
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Collection<KeyFlagAnalytics> getAlRegionalReportsByOrgUnit( OrganisationUnit organisationUnit )
    {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria( KeyFlagAnalytics.class );
        criteria.add( Restrictions.eq( "organisationUnit", organisationUnit ) );
        return criteria.list();
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Collection<KeyFlagAnalytics> getAlRegionalReportsByIndicator( Indicator indicator )
    {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria( KeyFlagAnalytics.class );
        criteria.add( Restrictions.eq( "indicator", indicator ) );
        return criteria.list();
    }

    @Override
    public KeyFlagAnalytics getRegionalReportByOrgUnitId( Integer orgunitId )
    {

        Session session = sessionFactory.getCurrentSession();
        OrganisationUnit orgunit = organisationUnitService.getOrganisationUnit( orgunitId );

        Criteria criteria = session.createCriteria( KeyFlagAnalytics.class );
        criteria.add( Restrictions.eq( "organisationUnit", orgunit ) );

        return (KeyFlagAnalytics) criteria.uniqueResult();

    }

    @Override
    public KeyFlagAnalytics getRegionalReportByIndicatorId( Integer indicatorId )
    {
        Session session = sessionFactory.getCurrentSession();
        Indicator indicator = indicatorService.getIndicator( indicatorId );

        Criteria criteria = session.createCriteria( KeyFlagAnalytics.class );
        criteria.add( Restrictions.eq( "indicator", indicator ) );

        return (KeyFlagAnalytics) criteria.uniqueResult();

    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Collection<KeyFlagAnalytics> getAllReginalReportsByIndicatorandOrgUnit( Indicator indicator,
        OrganisationUnit organisationUnit )
    {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria( KeyFlagAnalytics.class );
        criteria.add( Restrictions.eq( "indicator", indicator ) );

        criteria.add( Restrictions.eq( "organisationUnit", organisationUnit ) );
        return criteria.list();
    }
    
    @Override
    public KeyFlagAnalytics getRegionalReport( Integer orgunitId, Integer indicatorId )
    {

        Session session = sessionFactory.getCurrentSession();
        OrganisationUnit orgunit = organisationUnitService.getOrganisationUnit( orgunitId );
        Indicator indicator = indicatorService.getIndicator( indicatorId );
        
        Criteria criteria = session.createCriteria( KeyFlagAnalytics.class );
        criteria.add( Restrictions.eq( "organisationUnit", orgunit ) );
        criteria.add( Restrictions.eq( "indicator", indicator ) );

        return (KeyFlagAnalytics) criteria.uniqueResult();

    }
    
    // get KeyFlagAnalyticsMap
    public Map<String, KeyFlagAnalytics> getKeyFlagAnalyticsMap()
    {
        Map<String, KeyFlagAnalytics> KeyFlagAnalyticMap = new HashMap<String, KeyFlagAnalytics>();
        
        Collection<KeyFlagAnalytics> keyFlagAnalyticObjs = new ArrayList<KeyFlagAnalytics>( getAllRegionalReports() );
        
        for( KeyFlagAnalytics keyFlagAnalyticObj : keyFlagAnalyticObjs )
        {
            KeyFlagAnalyticMap.put( keyFlagAnalyticObj.getOrganisationUnit().getId()+":"+keyFlagAnalyticObj.getIndicator().getId(), keyFlagAnalyticObj );
        }
        
        /*try
        {                       
            String query = "SELECT orgunitid, indicatorid,  FROM keyflag_analytics ";
                            
            //System.out.println("Query: " + query );
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            while ( rs.next() )
            {
                Integer organisationUnitId = rs.getInt( 1 );
                Integer indicatorId = rs.getInt( 2 );
                
                if( organisationUnitId != null && indicatorId != null )
                {
                    
                    KeyFlagAnalytics keyFlagAnalytics = new KeyFlagAnalytics();
                    
                    //getRegionalReport( organisationUnitId,  indicatorId );
                    
                    if( keyFlagAnalytics != null )
                    {
                        KeyFlagAnalyticMap.put( organisationUnitId +":"+ indicatorId, keyFlagAnalytics );
                    }
                }
                
            }
        }
        catch( Exception e )
        {
            System.out.println("In KeyFlagAnalyticsMap Exception :"+ e.getMessage() );
        }*/
    
        return KeyFlagAnalyticMap;
    }    
        
}
