package org.hisp.dhis.ivb.kfa;

import java.util.Collection;
import java.util.Map;

import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.organisationunit.OrganisationUnit;

/**
 * @author Ganesh
 * 
 */
public interface KeyFlagAnalyticsService
{

    String ID = KeyFlagAnalyticsService.class.getName();

    void saveRegionalReport( KeyFlagAnalytics regionalReport );

    void updateRegionalReport( KeyFlagAnalytics regionalReport );

    void deleteRegionalReport( KeyFlagAnalytics regionalReport );

    Collection<KeyFlagAnalytics> getAllRegionalReports();

    Collection<KeyFlagAnalytics> getAlRegionalReportsByOrgUnit( OrganisationUnit organisationUnit );

    Collection<KeyFlagAnalytics> getAlRegionalReportsByIndicator( Indicator indicator );

    Collection<KeyFlagAnalytics> getAllReginalReportsByIndicatorandOrgUnit( Indicator indicator,
        OrganisationUnit organisationUnit );

    KeyFlagAnalytics getRegionalReportByOrgUnitId( Integer orgunitId );

    KeyFlagAnalytics getRegionalReportByIndicatorId( Integer indicatorId );
    
    KeyFlagAnalytics getRegionalReport( Integer orgunitId, Integer indicatorId );
    
    Map<String, KeyFlagAnalytics> getKeyFlagAnalyticsMap();

}
