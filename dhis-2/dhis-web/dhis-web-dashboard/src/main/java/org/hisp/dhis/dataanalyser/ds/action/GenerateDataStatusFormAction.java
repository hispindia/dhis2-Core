package org.hisp.dhis.dataanalyser.ds.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.comparator.PeriodComparator;

import com.opensymphony.xwork2.Action;

public class GenerateDataStatusFormAction
    implements Action
{

    /* Dependencies */
    @SuppressWarnings("unused")
    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    /* Output Parameters */
    private List<DataSet> dataSetList;

    public List<DataSet> getDataSetList()
    {
        return dataSetList;
    }

    private List<Period> monthlyPeriods;

    public List<Period> getMonthlyPeriods()
    {
        return monthlyPeriods;
    }

    private SimpleDateFormat simpleDateFormat;

    public SimpleDateFormat getSimpleDateFormat()
    {
        return simpleDateFormat;
    }
    
    private List<Period> periods;

    public List<Period> getPeriods()
    {
        return periods;
    }
    
    private List<String> yearList;
    
    public List<String> getYearList()
    {
        return yearList;
    }

    
    public String execute()
        throws Exception
    {
        /* DataSet List */
        
        dataSetList = new ArrayList<DataSet>( dataSetService.getAllDataSets() );
        
        //dataSetList = new ArrayList<DataSet>( dataSetService.getDataSetsForMobile( ));
        
        Iterator<DataSet> dataSetListIterator = dataSetList.iterator();
        
        while(dataSetListIterator.hasNext())
        {
             DataSet d = (DataSet) dataSetListIterator.next();

            
            if ( d.getSources().size() <= 0 )
			{
                dataSetListIterator.remove();
			}
			else
			{			
				// -------------------------------------------------------------------------
				// Added to remove Indian Linelisting datasets
				// -------------------------------------------------------------------------
				
				if ( d.getId() == 8 || d.getId() == 9 || d.getId() == 10 || d.getId() == 14
					|| d.getId() == 15 || d.getId() == 35 || d.getId() == 36 || d.getId() == 37
					|| d.getId() == 38 )
				{
					dataSetListIterator.remove();
				}	
			}
        }
        
        //Collections.sort( dataSetList, new IdentifiableObjectNameComparator() );
        Collections.sort( dataSetList );
        /* Monthly Periods */
        //monthlyPeriods = new ArrayList<Period>( periodService.getPeriodsByPeriodType( new MonthlyPeriodType() ) );
        //Collections.sort( monthlyPeriods, new PeriodStartDateComparator() );
        //simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
     // for IPPF
        PeriodType periodType = periodService.getPeriodTypeByName( "Yearly" );
        
        periods = new ArrayList<Period>( periodService.getPeriodsByPeriodType( periodType ) );
        
        Iterator<Period> periodIterator = periods.iterator();
        while ( periodIterator.hasNext() )
        {
            Period p1 = periodIterator.next();

            if ( p1.getStartDate().compareTo( new Date() ) > 0 )
            {
                periodIterator.remove();
            }

        }
        
        Collections.sort( periods, new PeriodComparator() );
        
        yearList = new ArrayList<String>();
        simpleDateFormat = new SimpleDateFormat( "yyyy" );
        for ( Period p1 : periods )
        {
            yearList.add( simpleDateFormat.format( p1.getStartDate() ) );
        }
        
        
        return SUCCESS;
    }
 

}// class end
