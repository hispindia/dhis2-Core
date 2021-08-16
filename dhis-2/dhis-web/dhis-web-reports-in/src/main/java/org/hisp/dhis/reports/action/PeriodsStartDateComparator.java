package org.hisp.dhis.reports.action;

import java.util.Comparator;

import org.hisp.dhis.period.Period;

/**
 * @author Mithilesh Kumar Thakur
 */
public class PeriodsStartDateComparator implements Comparator<Period>
{
    public int compare( Period period0, Period period1 )
    {
        return period0.getStartDate().compareTo( period1.getStartDate() );
    }
}


