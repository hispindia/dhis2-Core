package org.hisp.dhis.de.action;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
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
public class SaveClosingBalancForNextPeriodAction implements Action 
{
    private static final Log log = LogFactory.getLog( SaveClosingBalancForNextPeriodAction.class );
    
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
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private DataElementCategoryService dataElementCategoryService;

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

    public String execute() 
    {
        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId.trim() );
        
        DataSet dataSet = dataSetService.getDataSet( dataSetUid.trim() );
        
        String storedBy = currentUserService.getCurrentUsername();

        Date now = new Date();

        String[] nextdataElementId = closingBalDeId.split("-");
        DataElement nextdataElement = dataElementService.getDataElement( nextdataElementId[0].trim() );
        DataElementCategoryOptionCombo nextoptionCombo = dataElementCategoryService.getDataElementCategoryOptionCombo( nextdataElementId[1].trim() );
        
        String nextISOPeriod = null;
        
        //Integer i = 12;
        
        if ( dataSet.getPeriodType().getName().equalsIgnoreCase( "Monthly" ) )
        {
            System.out.println( " Inside Monthly Period " + nextdataElement.getId() + " -- " + periodId );
            
            String year = periodId.substring( 0, 4 ).trim();
            
            String month = periodId.substring( 4, 6 );
            
            //Integer yearInt = Integer.parseInt( year );
            //Integer monthInt = Integer.parseInt( month );
            
            //System.out.println( " Month " + month + " -- year " + year );
            
            if( Integer.parseInt( month ) >= 1 && Integer.parseInt( month ) < 12 )
            {               
                int nextMonth=Integer.parseInt( month ) + 1;
                
                if( nextMonth >= 1 && nextMonth <= 9 )
                {
                    nextISOPeriod = year + "0"+nextMonth;
                }
                else if( nextMonth >= 10 && nextMonth <= 12 )
                {
                    nextISOPeriod = year + nextMonth;
                }
            }
            else if( Integer.parseInt( month ) == 12  )
            {
                //System.out.println( " nextISOPeriod - 1 " + nextISOPeriod );
                
                int nextYear=Integer.parseInt( year ) + 1;
                nextISOPeriod = nextYear + "0"+1;
                //System.out.println( " nextISOPeriod - 2 " + nextISOPeriod );
            }
            
            //System.out.println( " nextISOPeriod - 2 " + nextISOPeriod );
        }
        
        else if ( dataSet.getPeriodType().getName().equalsIgnoreCase( "Weekly" ) )
        {
            String year =  periodId.substring( 0, 4 ).trim();
            
            String week = periodId.substring( 5 ).trim();
            
            String weekConstant = periodId.substring( 4, 5 ).trim();
            
            if( Integer.parseInt( week ) >= 1 && Integer.parseInt( week ) < 52 )
            {
                int nextWeek = Integer.parseInt( week ) + 1;
                nextISOPeriod = year + weekConstant + nextWeek;
            }
            
            else if( Integer.parseInt( week ) == 52 )
            {
                int nextYear=Integer.parseInt( year ) + 1;
                nextISOPeriod = nextYear + weekConstant + 1;
            }
        }
        
        else if ( dataSet.getPeriodType().getName().equalsIgnoreCase( "Yearly" ) )
        {
            String year =  periodId.trim();
            
            int nextYear = Integer.parseInt( year ) + 1;
            nextISOPeriod = ""+nextYear;
        }             
        
        Period nextperiod = new Period();
        
        //System.out.println( " nextISOPeriod 3 " + nextISOPeriod );
        
        nextperiod = PeriodType.getPeriodFromIsoString( nextISOPeriod );
        
        nextperiod = periodService.reloadPeriod( nextperiod );
        
        if ( nextperiod == null )
        {
            return logError( "Illegal period identifier: " + nextISOPeriod );
        }
        
        DataElementCategoryOptionCombo defaultAttributeOptionCombo = dataElementCategoryService.getDefaultDataElementCategoryOptionCombo();
        
        DataValue nextdataValue = dataValueService.getDataValue( nextdataElement, nextperiod, organisationUnit, nextoptionCombo );
        
        System.out.println( " nextISOPeriod ID " + nextperiod.getId() );
        
        if ( nextdataValue == null )
        {                       
            nextdataValue = new  DataValue( nextdataElement, nextperiod, organisationUnit, nextoptionCombo, defaultAttributeOptionCombo, closingBalance, storedBy, now, null );
            dataValueService.addDataValue( nextdataValue );
        }
        else
        {
            nextdataValue.setValue( closingBalance );
            nextdataValue.setLastUpdated( now );
            nextdataValue.setStoredBy( storedBy );
            dataValueService.updateDataValue( nextdataValue );
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
