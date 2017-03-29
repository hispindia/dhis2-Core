package org.hisp.dhis.ivb.kfa;

import java.util.Collection;
import java.util.Map;

import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.ivb.kfa.KeyFlagAnalytics;
import org.hisp.dhis.ivb.kfa.KeyFlagAnalyticsService;
import org.hisp.dhis.ivb.kfa.KeyFlagAnalyticsStore;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultKeyFlagAnalyticsService
    implements KeyFlagAnalyticsService
{

    // ---------------------------------------------------------------------
    // Dependencies
    // ---------------------------------------------------------------------

    @Autowired
    private OrganisationUnitService organisationUnitService;

    private KeyFlagAnalyticsStore regionalReportStore;

    public void setRegionalReportStore( KeyFlagAnalyticsStore regionalReportStore )
    {
        this.regionalReportStore = regionalReportStore;
    }

    // ---------------------------------------------------------------------
    // Methods
    // ---------------------------------------------------------------------

    @Override
    public void saveRegionalReport( KeyFlagAnalytics regionalReport )
    {
        regionalReportStore.saveRegionalReport( regionalReport );
    }

    @Override
    public void updateRegionalReport( KeyFlagAnalytics regionalReport )
    {
        regionalReportStore.updateRegionalReport( regionalReport );
    }

    @Override
    public void deleteRegionalReport( KeyFlagAnalytics regionalReport )
    {
        regionalReportStore.deleteRegionalReport( regionalReport );

    }

    @Override
    public Collection<KeyFlagAnalytics> getAllRegionalReports()
    {
        return regionalReportStore.getAllRegionalReports();
    }

    @Override
    public Collection<KeyFlagAnalytics> getAlRegionalReportsByOrgUnit( OrganisationUnit organisationUnit )
    {
        return regionalReportStore.getAlRegionalReportsByOrgUnit( organisationUnit );
    }

    @Override
    public Collection<KeyFlagAnalytics> getAlRegionalReportsByIndicator( Indicator indicator )
    {
        return regionalReportStore.getAlRegionalReportsByIndicator( indicator );
    }

    @Override
    public KeyFlagAnalytics getRegionalReportByOrgUnitId( Integer orgunitId )
    {
        return regionalReportStore.getRegionalReportByOrgUnitId( orgunitId );
    }

    @Override
    public KeyFlagAnalytics getRegionalReportByIndicatorId( Integer indicatorId )
    {
        return regionalReportStore.getRegionalReportByIndicatorId( indicatorId );
    }

    @Override
    public Collection<KeyFlagAnalytics> getAllReginalReportsByIndicatorandOrgUnit( Indicator indicator,
        OrganisationUnit organisationUnit )
    {

        return regionalReportStore.getAllReginalReportsByIndicatorandOrgUnit( indicator, organisationUnit );
    }
    
    @Override
    public KeyFlagAnalytics getRegionalReport( Integer orgunitId, Integer indicatorId )
    {
        return regionalReportStore.getRegionalReport( orgunitId, indicatorId );
    }
   
    public Map<String, KeyFlagAnalytics> getKeyFlagAnalyticsMap()
    {
        return regionalReportStore.getKeyFlagAnalyticsMap();
    }

}
