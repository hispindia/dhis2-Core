package org.hisp.dhis.reports.ed.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorGroup;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.comparator.PeriodComparator;

import com.opensymphony.xwork2.Action;

public class EDReportFormAction
    implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }
    
    private IndicatorService indicatorService ;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }
    // -------------------------------------------------------------------------
    // Getter & Setter
    // -------------------------------------------------------------------------

    private List<Period> periods;

    public List<Period> getPeriods()
    {
        return periods;
    }

    private SimpleDateFormat simpleDateFormat;

    public SimpleDateFormat getSimpleDateFormat()
    {
        return simpleDateFormat;
    }
    
    private List<IndicatorGroup> indicatorGroups;
    
    public List<IndicatorGroup> getIndicatorGroups()
    {
        return indicatorGroups;
    }
    
    private List<Indicator> indicators;
    
    public List<Indicator> getIndicators()
    {
        return indicators;
    }
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        
        /* Indicator and Indicator Group */
        indicators = new ArrayList<Indicator>( indicatorService.getAllIndicators());
        indicatorGroups = new ArrayList<IndicatorGroup>( indicatorService.getAllIndicatorGroups()) ;
        Collections.sort( indicatorGroups, new IdentifiableObjectNameComparator() );
        Collections.sort( indicators, new IdentifiableObjectNameComparator() );

        //period information
        periods = new ArrayList<Period>( periodService.getPeriodsByPeriodType( new MonthlyPeriodType() ) );

        Iterator<Period> periodIterator = periods.iterator();
        while ( periodIterator.hasNext() )
        {
            Period p1 = periodIterator.next();

            if ( p1.getStartDate().compareTo( new Date() ) > 0 )
            {
                periodIterator.remove();
            }

        }
        simpleDateFormat = new SimpleDateFormat( "MMM-yy" );

        Collections.sort( periods, new PeriodComparator() );

        return SUCCESS;
    }
}
