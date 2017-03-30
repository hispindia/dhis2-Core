package org.hisp.dhis.ivb.dataentry.action;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.datavalue.DataValueAuditService;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Samta bajpai
 */
public class GetIndicatorHelpAction
    implements Action
{
    private final static String KEY_DATAELEMENT = "KEYFLAG_DE_ATTRIBUTE_ID";
    
    private static String SOURCES = "SOURCES";

    private static String MORE_INFO = "MORE_INFO";
   
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }
    
    private DataElementService dataElementService;
    
    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }
    private ExpressionService expressionService;

    public void setExpressionService( ExpressionService expressionService )
    {
        this.expressionService = expressionService;
    }
    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
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
    
    private DataValueAuditService dataValueAuditService;
    
    public void setDataValueAuditService( DataValueAuditService dataValueAuditService )
    {
        this.dataValueAuditService = dataValueAuditService;
    }
    
    @Autowired
    private IVBUtil ivbUtil;
    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private String indicatorId;

    public void setIndicatorId( String indicatorId )
    {
        this.indicatorId = indicatorId;
    }
    
    private String countryUid;
    
    public void setCountryUid( String countryUid )
    {
        this.countryUid = countryUid;
    }
    
    public String getCountryUid()
    {
        return countryUid;
    }
    private OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }    
    
    private String userName;    

    public void setUserName( String userName )
    {
        this.userName = userName;
    }
    public String getUserName()
    {
        return userName;
    }
    private SimpleDateFormat simpleDateFormat1;

    public SimpleDateFormat getSimpleDateFormat1()
    {
        return simpleDateFormat1;
    }

    private SimpleDateFormat simpleDateFormat2;

    public SimpleDateFormat getSimpleDateFormat2()
    {
        return simpleDateFormat2;
    }
    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private List<DataValueAudit> historyList = new ArrayList<DataValueAudit>();

    public List<DataValueAudit> getHistoryList()
    {
        return historyList;
    }
     
    private OrganisationUnit oUnit;
    
    public OrganisationUnit getoUnit()
    {
        return oUnit;
    }

    private DataElement keyDataElement;
    
    public DataElement getKeyDataElement()
    {
        return keyDataElement;
    }

    private Indicator indicator;

    public Indicator getIndicator()
    {
        return indicator;
    }

    private String sources;

    public String getSources()
    {
        return sources;
    }

    private List<String> moreInfoList = new ArrayList<String>();

    public List<String> getMoreInfoList()
    {
        return moreInfoList;
    }

    private int statusCode = 0;

    public int getStatusCode()
    {
        return statusCode;
    }
    private List<DataElement> dataElementList;
    
    public List<DataElement> getDataElementList()
    {
        return dataElementList;
    }
    
    private Map<String,String> valueMap = new HashMap<String,String>();
    
    public Map<String, String> getValueMap()
    {
        return valueMap;
    }
    private Map<String,String> commentMap = new HashMap<String,String>();
    
    public Map<String, String> getCommentMap()
    {
        return commentMap;
    }
    private String selectedPeriod;

    public String getSelectedPeriod()
    {
        return selectedPeriod;
    }
    private String currentPeriod;
    
    public String getCurrentPeriod()
    {
        return currentPeriod;
    }

    private DataElementCategoryOptionCombo optionCombo;

    public DataElementCategoryOptionCombo getOptionCombo()
    {
        return optionCombo;
    }
    private String periodTypeName;

    public String getPeriodTypeName()
    {
        return periodTypeName;
    }

    public String execute()
    {        
        indicator = indicatorService.getIndicator( Integer.parseInt( indicatorId ) );
        Period period = new Period();
        
        PeriodType periodType = null;
        for(DataSet ds : indicator.getDataSets())
        {
            periodType = ds.getPeriodType();
        }
       
        period = ivbUtil.getCurrentPeriod( periodType, new Date() );
        selectedPeriod = period.getDescription();
        currentPeriod = period.getName();
        if ( period.getPeriodType().getName().equalsIgnoreCase( "Quarterly" ) )
        {
            simpleDateFormat1 = new SimpleDateFormat( "MMM" );
            simpleDateFormat2 = new SimpleDateFormat( "MMM yyyy" );
            periodTypeName = "Quarterly";
        }
        else
        {
            simpleDateFormat1 = new SimpleDateFormat( "yyyy-MM-dd" );
            periodTypeName = "Other";
        }
        
        optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
        
        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
        
        dataElementList = new ArrayList<DataElement>(dataElementService.getAllDataElements());
        
        List<DataElement> indicatorDEs = new ArrayList<DataElement>();
        indicatorDEs.addAll( expressionService.getDataElementsInExpression( indicator.getNumerator() ));        
        dataElementList.retainAll( indicatorDEs );
        
        List<String> orgUnitUids = new ArrayList<String>();
        orgUnitUids.add( countryUid );

        List<OrganisationUnit> orgUnit = organisationUnitService.getOrganisationUnitsByUid( orgUnitUids );
        
        organisationUnit = orgUnit.get( 0 );
        for(DataElement de : dataElementList)
        {
            DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, orgUnit.get( 0 ) );
            if(dv != null)
            {
               if(dv.getValue() != null) 
               {
                  valueMap.put( de.getUid(), dv.getValue() ); 
                  commentMap.put( de.getUid(), dv.getComment() );
               }
               else
               {
                   valueMap.put( de.getUid(), "" ); 
                   commentMap.put( de.getUid(), "" );
               }
            }
            else
            {
                valueMap.put( de.getUid(), "" ); 
                commentMap.put( de.getUid(), "" );
            }
        }
        
        Constant conSource = constantService.getConstantByName( SOURCES );
        Constant moreInformation = constantService.getConstantByName( MORE_INFO );
        Set<AttributeValue> attrValueSet = new HashSet<AttributeValue>( indicator.getAttributeValues() );
        oUnit = orgUnit.get( 0 );
        Pattern p = Pattern.compile("(http|https):\\/\\/[A-Za-z0-9\\.]*\\.(com|org|net|int)");
        sources = " ";
        for ( AttributeValue attValue : attrValueSet )
        {
            if ( attValue.getAttribute().getId() == conSource.getValue() )
            {
                String[] linkValues = attValue.getValue().split( "(\\{|\\(|\\[|\\,|\\]|\\)|\\})" );
                for(String linkValue : linkValues)
                {               
                    Matcher match = p.matcher(linkValue);                
                    if(match.find())
                    {
                        String link = match.group();
                        sources = " "+sources+" " + "<a href=\'"+link+"\' target=\'_blank\'>"+link+"</a>,"; 
                    }
                    else
                    {
                        sources = " "+sources+" " + linkValue;
                    }
                }
                
                
            }
            if ( attValue.getAttribute().getId() == moreInformation.getValue() )
            {
                moreInfoList.add( attValue.getValue() );
            }
        }
        Constant keyDEConst = constantService.getConstantByName( KEY_DATAELEMENT );
        List<DataSet> dataSets = new ArrayList<DataSet>(indicator.getDataSets());        
        List<DataElement> keyFlagDataElements = new ArrayList<DataElement>(dataSets.get( 0 ).getDataElements());
        for ( DataElement dataElement : keyFlagDataElements )
        {
            List<AttributeValue> deAttributeValues = new ArrayList<AttributeValue>(dataElement.getAttributeValues());
            for(AttributeValue da : deAttributeValues)
            {                
                if ( da.getAttribute().getId() == keyDEConst.getValue() &&  da.getValue().equalsIgnoreCase( "true" ))
                {
                    historyList.addAll( dataValueAuditService.getDataValueAuditByOrgUnit_DataElement( dataElement,
                        orgUnit.get( 0 ) ) );  
                    keyDataElement = dataElement;                
                }
            }
        }

        return SUCCESS;
    }
}