package org.hisp.dhis.excelimport.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.period.WeeklyPeriodType;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;


/**
 * @author Mithilesh Kumar Thakur
 */

public class GetQuarterlyPeriodsAction implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private PeriodService periodService;

    /*
    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    } 
    */
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
    private String year;
    
    public void setYear( String year )
    {
        this.year = year;
    }

    private List<Period> periods;

    public List<Period> getPeriods()
    {
        return periods;
    }
    
    private List<String> periodNameList;

    public List<String> getPeriodNameList()
    {
        return periodNameList;
    }
    
    private String quarterlyPeriodTypeName;
    
    private SimpleDateFormat simpleDateFormat1;
    private SimpleDateFormat simpleDateFormat2;
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute() throws Exception
    {
        //quarterlyPeriodTypeName = QuarterlyPeriodType.NAME;
        
        /*
        Collection<PeriodType> periodTypes = periodService.getAllPeriodTypes();

        periods = new ArrayList<Period>();

        for ( PeriodType type : periodTypes )
        {
            System.out.println("period Type  1 is ------" + type.getName() );
            
            PeriodType periodType = periodService.getPeriodTypeByName( type.getName() );
            
            System.out.println("period Type 2 is ------" + periodType.getName() );
            
            
        }
        */
        //quarterlyPeriodTypeName = QuarterlyPeriodType.NAME;
        //System.out.println("\n\n quarterlyPeriodTypeName : " + quarterlyPeriodTypeName );
        
        //PeriodType periodType = periodService.getPeriodTypeByName( quarterlyPeriodTypeName );
        PeriodType periodType = periodService.getPeriodTypeByName( "Quarterly" );
        
        periods = new ArrayList<Period>();
        periodNameList = new ArrayList<String>();
        
        if( year != null )
        {
            String isoPeriodString = year;
            
            //System.out.println("\n\n Iso Period : " + isoPeriodString );
            
            //Period period = periodService.getPeriod( isoPeriodString );
            Period period = periodService.reloadIsoPeriod( isoPeriodString );
                
            if( period != null )
            {
                //periods = new ArrayList<Period>( periodService.getIntersectingPeriodsByPeriodType( periodType, period.getStartDate(), period.getEndDate() ) );
                periods = new ArrayList<Period>( periodService.getPeriodsBetweenDates( periodType, period.getStartDate(), period.getEndDate() ) );
            }
        }
        else
        {
            periods = new ArrayList<Period>( periodService.getPeriodsByPeriodType( periodType ) );
        }
        
       //Period pp = periods.get(1);
       //System.out.println("period pp is ------"+pp);
        
        
        // remove future period
        
        if( periods != null && periods.size() > 0 )
        {
            Iterator<Period> periodIterator = periods.iterator();
            while ( periodIterator.hasNext() )
            {
                Period p1 = periodIterator.next();
                
                //p1.getIsoDate();
               
                
                if ( p1.getStartDate().compareTo( new Date() ) > 0 )
                {
                    periodIterator.remove();
                }

            }
            //Collections.sort( periods, new PeriodComparator() );
            Collections.sort( periods );
            
            simpleDateFormat1 = new SimpleDateFormat( "MMMM" );
            simpleDateFormat2 = new SimpleDateFormat( "MMMM yyyy" );
            
            for ( Period p1 : periods )
            {
                String tempPeriodName = simpleDateFormat1.format( p1.getStartDate() ) + " - "
                    + simpleDateFormat2.format( p1.getEndDate() );
                periodNameList.add( tempPeriodName );
                
            }
        }
        
        
        return SUCCESS;
    }
}
