package org.hisp.dhis.de.action;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class SaveClosingBalancAction implements Action 
{
    private static final Log log = LogFactory.getLog( SaveClosingBalancAction.class );
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private DataElementService dataElementService;
    
    @Autowired
    private DataValueService dataValueService;
    
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private DataSetService dataSetService;

    // -------------------------------------------------------------------------
    // Input / Output
    // -------------------------------------------------------------------------
            
    private String closingBalDeId;
    
    public void setClosingBalDeId( String closingBalDeId )
    {
        this.closingBalDeId = closingBalDeId;
    }

    private String periodId;
    
    public void setPeriodId(String periodId) 
    {
        this.periodId = periodId;
    }

    private String organisationUnitId;
    
    public void setOrganisationUnitId(String organisationUnitId) 
    {
        this.organisationUnitId = organisationUnitId;
    }       
    
    private String closingBalance;

    public void setClosingBalance(String closingBalance) 
    {
        this.closingBalance = closingBalance;
    }               
    
    private String dataSetUid;
    
    public void setDataSetUid( String dataSetUid )
    {
        this.dataSetUid = dataSetUid;
    }


    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private int statusCode = 0;

    public int getStatusCode()
    {
        return statusCode;
    }

    
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute() throws Exception
    {

        System.out.println( "Inside ClosingBalance");
        
        System.out.println( closingBalDeId + "-- " + periodId + "-- " + organisationUnitId + "-- " + closingBalance );
        
        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId.trim() );
        
        DataSet dataSet = dataSetService.getDataSet( dataSetUid.trim() );
        
        String storedBy = currentUserService.getCurrentUsername();

        Date now = new Date();

        String[] closingBaldataElementId = closingBalDeId.split("-");
        DataElement closingBaldataElement = dataElementService.getDataElement( closingBaldataElementId[0].trim() );
        CategoryOptionCombo closingBalCategoryOptionCombo = categoryService.getCategoryOptionCombo( closingBaldataElementId[1].trim() );
        
        String nextISOPeriod = null;
        
        if ( dataSet.getPeriodType().getName().equalsIgnoreCase( "Monthly" ) )
        {
            String year = periodId.substring( 0, 4 ).trim();
            
            String month = periodId.substring( 4, 6 );
            
            //System.out.println( "year " + year +" -- month " + month );
            if( Integer.parseInt( month ) > 1 || Integer.parseInt( month ) < 12 )
            {               
                int nextMonth=Integer.parseInt( month ) + 1;
                
                if( nextMonth >= 1 && nextMonth <= 9 )
                {
                    nextISOPeriod = year + "0"+nextMonth;
                    //System.out.println( " nextISOPeriod In 1 " + nextISOPeriod);
                }
                else if( nextMonth >= 10 && nextMonth <= 12 )
                {
                    nextISOPeriod = year + nextMonth;
                    //System.out.println( " nextISOPeriod In 2 " + nextISOPeriod);
                }
            }
            
            else if( Integer.parseInt( month ) == 12 )
            {
                int nextYear=Integer.parseInt( year ) + 1;
                nextISOPeriod = nextYear + "0"+1;
                
                //System.out.println( " nextISOPeriod In 3 " + nextISOPeriod);
            }
        }
        
        else if ( dataSet.getPeriodType().getName().equalsIgnoreCase( "Weekly" ) )
        {
            
            String year =  periodId.substring( 0, 4 ).trim();
            
            String week = periodId.substring( 5 ).trim();
            
            String weekConstant = periodId.substring( 4, 5 ).trim();
            
            //System.out.println( "year-- " + year + " week-- " + week +" weekConstant -- " + weekConstant );
            
            if( Integer.parseInt( week ) >= 1 && Integer.parseInt( week ) < 52 )
            {
                int nextWeek = Integer.parseInt( week ) + 1;
                nextISOPeriod = year + weekConstant + nextWeek;
                //System.out.println( " nextISOPeriod In 1 " + nextISOPeriod);
            }
            
            else if( Integer.parseInt( week ) == 52 )
            {
                int nextYear=Integer.parseInt( year ) + 1;
                nextISOPeriod = nextYear + weekConstant + 1;
                //System.out.println( " nextISOPeriod In 2 " + nextISOPeriod);
            }
            
        }       
        
        //System.out.println( "Next ISO Period 4 " + nextISOPeriod );
        
        Period nextperiod = new Period();
        
        nextperiod = PeriodType.getPeriodFromIsoString( nextISOPeriod );
        
        nextperiod = periodService.reloadPeriod( nextperiod );
        
        if ( nextperiod == null )
        {
            return logError( "Illegal period identifier: " + nextISOPeriod );
        }
        
        //System.out.println( "Next ISO Period Id " + nextperiod.getId() );
        
        CategoryOptionCombo defaultAttributeOptionCombo = categoryService.getDefaultCategoryOptionCombo();
        
        DataValue nextdataValue = dataValueService.getDataValue( closingBaldataElement, nextperiod, organisationUnit, closingBalCategoryOptionCombo );
        
        if ( nextdataValue == null )
        {                       
            nextdataValue = new  DataValue( closingBaldataElement, nextperiod, organisationUnit, closingBalCategoryOptionCombo, defaultAttributeOptionCombo, closingBalance, storedBy, now, null );
            dataValueService.addDataValue( nextdataValue );
            
            //System.out.println( "Data Added for " + nextperiod.getId() );
        }
        else
        {
            nextdataValue.setValue( closingBalance );
            nextdataValue.setLastUpdated( now );
            //nextdataValue.setTimestamp( now );
            nextdataValue.setStoredBy( storedBy );
            dataValueService.updateDataValue( nextdataValue );
            
            //System.out.println( "Data updated " + nextperiod.getId() );
        }
        
        return SUCCESS;
    }
    
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private String logError( String message )
    {
        return logError( message, 1 );
    }

    private String logError( String message, int statusCode )
    {
        log.info( message );

        this.statusCode = statusCode;

        return SUCCESS;
    }
}

