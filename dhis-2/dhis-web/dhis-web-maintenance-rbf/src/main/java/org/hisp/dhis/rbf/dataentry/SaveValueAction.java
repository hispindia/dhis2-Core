package org.hisp.dhis.rbf.dataentry;

/*
 * Copyright (c) 2004-2012, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.hisp.dhis.option.Option;
import org.hisp.dhis.option.OptionService;
import org.hisp.dhis.option.OptionSet;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodStore;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.rbf.api.PBFDataValue;
import org.hisp.dhis.rbf.api.PBFDataValueService;
import org.hisp.dhis.system.util.ValidationUtils;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;


public class SaveValueAction
    implements Action
{
    private static final Log log = LogFactory.getLog( SaveValueAction.class );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    private DataElementCategoryService categoryService;

    public void setCategoryService( DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    private PBFDataValueService pbfDataValueService;
    
    public void setPbfDataValueService(PBFDataValueService pbfDataValueService) 
    {
	this.pbfDataValueService = pbfDataValueService;
    }
    
    @Autowired
    private OptionService optionService;
    
    private PeriodStore periodStore;
    
    public void setPeriodStore(PeriodStore periodStore) 
    {
		this.periodStore = periodStore;
	}

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

	private String tariffAmt;
    
    public void setTariffAmt( String tariffAmt )
    {
        this.tariffAmt = tariffAmt;
    }

    private String value;

    public void setValue( String value )
    {
        this.value = value;
    }

    private String valueType;
    
    public void setValueType(String valueType) 
    {
    	this.valueType = valueType;
    }

    private String totalValue;
    
    public void setTotalValue(String totalValue) 
    {
		this.totalValue = totalValue;
	}

	private String dataElementId;

    public void setDataElementId( String dataElementId )
    {
        this.dataElementId = dataElementId;
    }

    private String organisationUnitId;

    public void setOrganisationUnitId( String organisationUnitId )
    {
        this.organisationUnitId = organisationUnitId;
    }

    /*
    private String optionComboId;

    public void setOptionComboId( String optionComboId )
    {
        this.optionComboId = optionComboId;
    }
    */

    private String periodId;

    public void setPeriodId( String periodId )
    {
        this.periodId = periodId;
    }
    
    private String periodIso;
    
    public void setPeriodIso(String periodIso) 
    {
	this.periodIso = periodIso;
    }
    
    private String dataSetId;
    
    public void setDataSetId(String dataSetId) 
    {
	this.dataSetId = dataSetId;
    }
    
    private Map<Integer, List<Option>> optionSetOptions = new HashMap<Integer, List<Option>>();
    
	public Map<Integer, List<Option>> getOptionSetOptions() 
	{
		return optionSetOptions;
	}
    
    private List<Option> options = new ArrayList<Option>();
    
    public List<Option> getOptions()
    {
        return options;
    }
    
    private DataElement aggDataElement;
    private DataElement aggTriffDataElement;
    private DataElement aggTotalDataElement;
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
    	Period period = PeriodType.getPeriodFromIsoString(periodIso);
    	//System.out.println( "periodIso -- "   + periodIso + "   Period Id -- " + period );
       // Period period = PeriodType.createPeriodExternalId( periodId );
    	period = periodStore.reloadForceAddPeriod( period );
    	//System.out.println( "periodIso -- "   + periodIso + "   Period Id -- " + period.getId() );
        if ( period == null )
        {
            return logError( "Illegal period identifier: " + periodIso );
        }
        
        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );

        if ( organisationUnit == null )
        {
            return logError( "Invalid organisation unit identifier: " + organisationUnitId );
        }
        
        DataElement dataElement = dataElementService.getDataElement( Integer.parseInt(dataElementId) );

        if ( dataElement == null )
        {
            return logError( "Invalid data element identifier: " + dataElementId );
        }
    
        /*
        DataElementCategoryOptionCombo optionCombo = categoryService.getDataElementCategoryOptionCombo( Integer.parseInt(optionComboId) );

        if ( optionCombo == null )
        {
            return logError( "Invalid category option combo identifier: " + optionComboId );
        }
        */
        
        DataSet dataSet = dataSetService.getDataSet( Integer.parseInt( dataSetId ) );
        if ( dataSet == null )
        {
            return logError( "Invalid dataset identifier: " + dataSetId );
        }
        
        String storedBy = currentUserService.getCurrentUsername();

        Date now = new Date();

        if ( storedBy == null )
        {
            storedBy = "[unknown]";
        }

        if ( value != null && value.trim().length() == 0 )
        {
            value = null;
        }

        if ( value != null )
        {
            value = value.trim();
        }

        // tariffAmt
        if ( tariffAmt != null && tariffAmt.trim().length() == 0 )
        {
        	tariffAmt = null;
        }

        if ( tariffAmt != null )
        {
        	tariffAmt = tariffAmt.trim();
        }
        
        // totalValue
        if ( totalValue != null && totalValue.trim().length() == 0 )
        {
        	totalValue = null;
        }

        if ( totalValue != null )
        {
        	totalValue = totalValue.trim();
        }
        DataElementCategoryOptionCombo categoryOptionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
        DataElementCategoryOptionCombo attributeOptionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
        
        /*
        List<OptionSet> optionSets = new ArrayList<OptionSet>( optionService.getAllOptionSets());
        
        for( OptionSet optionSet : optionSets )
        {
        	if( optionSet != null && optionSet.getCode() != null && !optionSet.getCode().equalsIgnoreCase(""))
        	{
        		optionSetOptions.put( Integer.parseInt(optionSet.getCode() ), optionSet.getOptions());
        	}
        }
        */
        
        options = new ArrayList<Option>( );
        OptionSet optionSet = optionService.getOptionSetByCode( dataElementId );
        List<Integer> optionIds = new ArrayList<Integer>();
        if( optionSet != null )
    	{
        	options = new ArrayList<Option>( optionSet.getOptions() );
        	if( options != null && options.size() > 0 )
        	{
        		for( Option option : options )
        		{
        			if( option.getCode() != null )
        			{
        				optionIds.add(Integer.parseInt(option.getCode() ));
        				//System.out.println( "OptionSet Name -- " + optionSet.getName() + " -- " + option.getName() + " -- " + option.getCode());
        			}
        		}
        	}
    	}
        
        
        System.out.println( "value -- " + value + " -- " + tariffAmt + " -- " + totalValue );
        // ---------------------------------------------------------------------
        // Validate value according to type from data element
        // ---------------------------------------------------------------------

        String valid = ValidationUtils.dataValueIsValid( value, dataElement );
        
        if ( valid != null )
        {
            return logError( valid, 3 );
        }

        // ---------------------------------------------------------------------
        // Check locked status
        // ---------------------------------------------------------------------

        /*
        if ( dataSetService.isLocked( dataElement, period, organisationUnit, null ) )
        {
            return logError( "Entry locked for combination: " + dataElement + ", " + period + ", " + organisationUnit, 2 );
        }
*/
        // ---------------------------------------------------------------------
        // Update data
        // ---------------------------------------------------------------------

        /*
        DataValue dataValue = dataValueService.getDataValue(  dataElement, period,organisationUnit,optionCombo );

        if ( dataValue == null )
        {
            if ( value != null )
            {
                dataValue = new DataValue( );
                dataValue.setDataElement(dataElement);
                dataValue.setPeriod(period);
                dataValue.setSource(organisationUnit);
                dataValue.setValue(value);
                dataValue.setStoredBy(storedBy);
                dataValue.setTimestamp(now);
                dataValue.setCategoryOptionCombo(optionCombo);
                dataValueService.addDataValue( dataValue );
                
                System.out.println("Value Added");
            }
        }
        else
        {
            dataValue.setValue( value );
            dataValue.setTimestamp( now );
            dataValue.setStoredBy( storedBy );

            dataValueService.updateDataValue( dataValue );
            System.out.println("Value Updated");
        }

*/
        if( optionIds != null && optionIds.size() > 0 )
        {
        	aggTriffDataElement = dataElementService.getDataElement( optionIds.get(3) );
         	aggTotalDataElement = dataElementService.getDataElement( optionIds.get(4) );
        }
       
        PBFDataValue pbfDataValue = pbfDataValueService.getPBFDataValue(organisationUnit, dataSet, period, dataElement);
        
        if ( pbfDataValue == null )
        {
            if ( value != null )
            {
            	pbfDataValue = new PBFDataValue( );
            	pbfDataValue.setDataSet(dataSet);
            	pbfDataValue.setDataElement(dataElement);
            	pbfDataValue.setPeriod(period);
            	pbfDataValue.setOrganisationUnit(organisationUnit);
                
            	if( valueType.equals("1") )
            	{
            	    pbfDataValue.setQuantityReported( Integer.parseInt( value ) );
            	    if( optionIds != null && optionIds.size() > 0 )
            	    {
            	    	aggDataElement = dataElementService.getDataElement( optionIds.get(0) );
            	    }
            	    try
            	    {
            	        pbfDataValue.setTariffAmount( Double.parseDouble( tariffAmt ) );
            	    }
            	    catch( Exception e )
            	    {
            	    }
            	}
            	else if( valueType.equals("2") )
            	{
            	    pbfDataValue.setQuantityValidated( Integer.parseInt( value ) );
            	    if( optionIds != null && optionIds.size() > 0 )
            	    {
            	    	aggDataElement = dataElementService.getDataElement( optionIds.get(1) );
            	    }
            	    try
            	    {
            	        pbfDataValue.setTariffAmount( Double.parseDouble( tariffAmt ) );
            	    }
            	    catch( Exception e )
            	    {
            	    }
            	}
            	
            	else if( valueType.equals("3") )
                {
                    pbfDataValue.setQuantityExternalVerification( Integer.parseInt( value ) );
                    if( optionIds != null && optionIds.size() > 0 )
            	    {
            	    	aggDataElement = dataElementService.getDataElement( optionIds.get(2) );
            	    }
                    try
                    {
                        pbfDataValue.setTariffAmount( Double.parseDouble( tariffAmt ) );
                    }
                    catch( Exception e )
                    {
                    }
                }
            	
            	pbfDataValue.setStoredBy(storedBy);
            	pbfDataValue.setTimestamp(now);
                pbfDataValueService.addPBFDataValue(pbfDataValue);
                
                System.out.println(" PBF Value Added");
                
                // add/update in aggregated data value;
                DataValue aggDataValue = dataValueService.getDataValue( aggDataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo );
                
                if ( aggDataValue == null )
                {
                	aggDataValue = new DataValue();
                    
                	aggDataValue.setPeriod(period);
                	aggDataValue.setDataElement(aggDataElement);
                	aggDataValue.setSource(organisationUnit);
                	aggDataValue.setCategoryOptionCombo(categoryOptionCombo);
                	aggDataValue.setAttributeOptionCombo(attributeOptionCombo);
                    
                	aggDataValue.setValue( value.trim() );
                	aggDataValue.setLastUpdated( now );
                	aggDataValue.setStoredBy( storedBy );
                    
                    dataValueService.addDataValue( aggDataValue );
                    
                    System.out.println( " Value Addedd in dataValue Table de: "+ aggDataElement.getId() + " org unit Id : " + organisationUnit.getId() + "  Period Id : " + period.getId() + "  Value : " + value );
                }
                else
                {
                	aggDataValue.setValue( value.trim() );
                	aggDataValue.setLastUpdated( now );
                	aggDataValue.setStoredBy( storedBy );                
                    dataValueService.updateDataValue( aggDataValue );

                    System.out.println( " Value Updated in dataValue Table de: " + aggDataElement.getId() + " org unit Id : " + organisationUnit.getId() + "  Period Id : " + period.getId() + "  Value : " + value );  
                    
                }
                
                DataValue aggTriffDataValue = dataValueService.getDataValue( aggTriffDataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo );
                if( tariffAmt != null && !tariffAmt.trim().equals( "" ) )
                {
                	if ( aggTriffDataValue == null )
                    {
                    	aggTriffDataValue = new DataValue();
                        
                    	aggTriffDataValue.setPeriod(period);
                    	aggTriffDataValue.setDataElement(aggTriffDataElement);
                    	aggTriffDataValue.setSource(organisationUnit);
                    	aggTriffDataValue.setCategoryOptionCombo(categoryOptionCombo);
                    	aggTriffDataValue.setAttributeOptionCombo(attributeOptionCombo);
                        
                    	aggTriffDataValue.setValue( tariffAmt.trim() );
                    	aggTriffDataValue.setLastUpdated( now );
                    	aggTriffDataValue.setStoredBy( storedBy );
                        
                        dataValueService.addDataValue( aggTriffDataValue );
                        
                        System.out.println( " Triff Value Addedd in dataValue Table de: "+ aggTriffDataElement.getId() + " org unit Id : " + organisationUnit.getId() + "  Period Id : " + period.getId() + "  Value : " + value );
                    }
                    else
                    {
                    	aggTriffDataValue.setValue( tariffAmt.trim() );
                    	aggTriffDataValue.setLastUpdated( now );
                    	aggTriffDataValue.setStoredBy( storedBy );                
                        dataValueService.updateDataValue( aggTriffDataValue );

                        System.out.println( " Triff Value Updated in dataValue Table de: " + aggTriffDataElement.getId() + " org unit Id : " + organisationUnit.getId() + "  Period Id : " + period.getId() + "  Value : " + value );  
                        
                    }
                    
                }
                
                DataValue aggTotalDataValue = dataValueService.getDataValue( aggTotalDataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo );
                if( totalValue != null && !totalValue.trim().equals( "" ) )
                {
                	if ( aggTotalDataValue == null )
                    {
                    	aggTotalDataValue = new DataValue();
                        
                    	aggTotalDataValue.setPeriod(period);
                    	aggTotalDataValue.setDataElement(aggTotalDataElement);
                    	aggTotalDataValue.setSource(organisationUnit);
                    	aggTotalDataValue.setCategoryOptionCombo(categoryOptionCombo);
                    	aggTotalDataValue.setAttributeOptionCombo(attributeOptionCombo);
                    	aggTotalDataValue.setValue( totalValue.trim() );
                    	aggTotalDataValue.setLastUpdated( now );
                    	aggTotalDataValue.setStoredBy( storedBy );
                        
                        dataValueService.addDataValue( aggTotalDataValue );
                        
                        System.out.println( " Total Value Addedd in dataValue Table de: "+ aggTotalDataElement.getId() + " org unit Id : " + organisationUnit.getId() + "  Period Id : " + period.getId() + "  Value : " + value );
                    }
                    else
                    {
                    	aggTotalDataValue.setValue( totalValue.trim() );
                    	aggTotalDataValue.setLastUpdated( now );
                    	aggTotalDataValue.setStoredBy( storedBy );                
                        dataValueService.updateDataValue( aggTotalDataValue );

                        System.out.println( " Value Updated in dataValue Table de: " + aggTotalDataElement.getId() + " org unit Id : " + organisationUnit.getId() + "  Period Id : " + period.getId() + "  Value : " + value );  
                        
                    }
                }
            }
        }
        else
        {
            if( valueType.equals("1") )
            {
            	if( optionIds != null && optionIds.size() > 0 )
         	    {
            		aggDataElement = dataElementService.getDataElement( optionIds.get(0) );
         	    }
            	Integer intVal = null;
                if( value != null && !value.trim().equals( "" ) )
                {
                    intVal = Integer.parseInt( value );
                }
                pbfDataValue.setQuantityReported( intVal );
        	
                try
                {
                    pbfDataValue.setTariffAmount( Double.parseDouble( tariffAmt ) );
                }
                catch( Exception e )
                {
                }
        	
                System.out.println(" PBF Value 1 " + intVal );
            }
            else if( valueType.equals("2") )
            {
            	if( optionIds != null && optionIds.size() > 0 )
         	    {
            		aggDataElement = dataElementService.getDataElement( optionIds.get(1) );
         	    }
                Integer intVal = null;
                if( value != null && !value.trim().equals( "" ) )
                {
                    intVal = Integer.parseInt( value );
                }
                
                System.out.println(" PBF Value 2 " + intVal );
                
                pbfDataValue.setQuantityValidated( intVal );
        	
                try
                {
                    pbfDataValue.setTariffAmount( Double.parseDouble( tariffAmt ) );
                }
                catch( Exception e )
                {
                }
        	
            }
            
            else if( valueType.equals("3") )
            {
            	if( optionIds != null && optionIds.size() > 0 )
         	    {
            		aggDataElement = dataElementService.getDataElement( optionIds.get(2) );
         	    }
            	
            	Integer intVal = null;
                if( value != null && !value.trim().equals( "" ) )
                {
                    intVal = Integer.parseInt( value );
                }
                System.out.println(" PBF Value 3 " + intVal );
                
                pbfDataValue.setQuantityExternalVerification( intVal );
                
                try
                {
                    pbfDataValue.setTariffAmount( Double.parseDouble( tariffAmt ) );
                }
                catch( Exception e )
                {
                }
            }
            
            pbfDataValue.setStoredBy(storedBy);
        	
            pbfDataValue.setTimestamp(now);

            pbfDataValueService.updatePBFDataValue( pbfDataValue );
        	
            System.out.println(" PBF Value Updated");
            
            // add/update in aggregated data value;
            DataValue aggDataValue = dataValueService.getDataValue( aggDataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo );
            
            if ( aggDataValue == null )
            {
            	aggDataValue = new DataValue();
                
            	aggDataValue.setPeriod(period);
            	aggDataValue.setDataElement(aggDataElement);
            	aggDataValue.setSource(organisationUnit);
            	aggDataValue.setCategoryOptionCombo(categoryOptionCombo);
            	aggDataValue.setAttributeOptionCombo(attributeOptionCombo);
                
            	aggDataValue.setValue( value.trim() );
            	aggDataValue.setLastUpdated( now );
            	aggDataValue.setStoredBy( storedBy );
                
                dataValueService.addDataValue( aggDataValue );
                
                System.out.println( " Value Addedd in dataValue Table de: "+ aggDataElement.getId() + " org unit Id : " + organisationUnit.getId() + "  Period Id : " + period.getId() + "  Value : " + value );
            }
            else
            {
            	aggDataValue.setValue( value.trim() );
            	aggDataValue.setLastUpdated( now );
            	aggDataValue.setStoredBy( storedBy );                
                dataValueService.updateDataValue( aggDataValue );

                System.out.println( " Value Updated in dataValue Table de: " + aggDataElement.getId() + " org unit Id : " + organisationUnit.getId() + "  Period Id : " + period.getId() + "  Value : " + value );  
                
            }
            
            DataValue aggTriffDataValue = dataValueService.getDataValue( aggTriffDataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo );
            if( tariffAmt != null && !tariffAmt.trim().equals( "" ) )
            {
            	if ( aggTriffDataValue == null )
                {
                	aggTriffDataValue = new DataValue();
                    
                	aggTriffDataValue.setPeriod(period);
                	aggTriffDataValue.setDataElement(aggTriffDataElement);
                	aggTriffDataValue.setSource(organisationUnit);
                	aggTriffDataValue.setCategoryOptionCombo(categoryOptionCombo);
                	aggTriffDataValue.setAttributeOptionCombo(attributeOptionCombo);
                    
                	aggTriffDataValue.setValue( tariffAmt.trim() );
                	aggTriffDataValue.setLastUpdated( now );
                	aggTriffDataValue.setStoredBy( storedBy );
                    
                    dataValueService.addDataValue( aggTriffDataValue );
                    
                    System.out.println( " Triff Value Addedd in dataValue Table de: "+ aggTriffDataElement.getId() + " org unit Id : " + organisationUnit.getId() + "  Period Id : " + period.getId() + "  Value : " + value );
                }
                else
                {
                	aggTriffDataValue.setValue( tariffAmt.trim() );
                	aggTriffDataValue.setLastUpdated( now );
                	aggTriffDataValue.setStoredBy( storedBy );                
                    dataValueService.updateDataValue( aggTriffDataValue );

                    System.out.println( " Triff Value Updated in dataValue Table de: " + aggTriffDataElement.getId() + " org unit Id : " + organisationUnit.getId() + "  Period Id : " + period.getId() + "  Value : " + value );  
                    
                }
            }
            
            DataValue aggTotalDataValue = dataValueService.getDataValue( aggTotalDataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo );
            if( totalValue != null && !totalValue.trim().equals( "" ) )
            {
            	if ( aggTotalDataValue == null )
                {
                	aggTotalDataValue = new DataValue();
                    
                	aggTotalDataValue.setPeriod(period);
                	aggTotalDataValue.setDataElement(aggTotalDataElement);
                	aggTotalDataValue.setSource(organisationUnit);
                	aggTotalDataValue.setCategoryOptionCombo(categoryOptionCombo);
                	aggTotalDataValue.setAttributeOptionCombo(attributeOptionCombo);
                	aggTotalDataValue.setValue( totalValue.trim() );
                	aggTotalDataValue.setLastUpdated( now );
                	aggTotalDataValue.setStoredBy( storedBy );
                    
                    dataValueService.addDataValue( aggTotalDataValue );
                    
                    System.out.println( " Total Value Addedd in dataValue Table de: "+ aggTotalDataElement.getId() + " org unit Id : " + organisationUnit.getId() + "  Period Id : " + period.getId() + "  Value : " + value );
                }
                else
                {
                	aggTotalDataValue.setValue( totalValue.trim() );
                	aggTotalDataValue.setLastUpdated( now );
                	aggTotalDataValue.setStoredBy( storedBy );                
                    dataValueService.updateDataValue( aggTotalDataValue );

                    System.out.println( " Value Updated in dataValue Table de: " + aggTotalDataElement.getId() + " org unit Id : " + organisationUnit.getId() + "  Period Id : " + period.getId() + "  Value : " + value );  
                    
                }
            }
            
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
