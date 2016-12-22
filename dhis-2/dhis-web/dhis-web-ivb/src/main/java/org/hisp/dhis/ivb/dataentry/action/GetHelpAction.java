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

import java.util.ArrayList;
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
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;

import com.opensymphony.xwork2.Action;

/**
 * @author Samta bajpai
 */
public class GetHelpAction
    implements Action
{
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

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

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

    public String execute()
    {
        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
        indicator = indicatorService.getIndicator( Integer.parseInt( indicatorId ) );
        dataElementList = new ArrayList<DataElement>(dataElementService.getAllDataElements());
        
        List<DataElement> indicatorDEs = new ArrayList<DataElement>();
        indicatorDEs.addAll( expressionService.getDataElementsInExpression( indicator.getNumerator() ));        
        dataElementList.retainAll( indicatorDEs );
        
        List<String> orgUnitUids = new ArrayList<String>();
        orgUnitUids.add( countryUid );

        List<OrganisationUnit> orgUnit = organisationUnitService.getOrganisationUnitsByUid( orgUnitUids );
        
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
        Pattern p = Pattern.compile("(http|https):\\/\\/[A-Za-z0-9\\.]*\\.(com|org|net|int)");
        sources = " ";
        for ( AttributeValue attValue : attrValueSet )
        {
            if ( attValue.getAttribute().getId() == conSource.getValue() )
            {              
                {                   
                    Matcher match = p.matcher(attValue.getValue());                
                    if(match.find())
                    {
                        String link = attValue.getValue();
                        link = link.replaceAll( match.group(), "<a href=\'"+match.group()+"\' target=\'_blank\'>"+match.group()+"</a>" );
                        sources = link; 
                    }
                    else
                    {
                        sources = attValue.getValue();
                    }
                    
                }
                
                
            }
            if ( attValue.getAttribute().getId() == moreInformation.getValue() )
            {
                moreInfoList.add( attValue.getValue() );
            }
        }

        return SUCCESS;
    }
}