package org.hisp.dhis.ivb.dashboard.action;

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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
//import org.hisp.dhis.dataset.comparator.DataSetSortOrderComparator;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.i18n.I18nManager;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.ivb.kfa.KeyFlagAnalytics;
import org.hisp.dhis.ivb.kfa.KeyFlagAnalyticsService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.ivb.util.KeyFlagCalculation;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

/**
 * @author Samta bajpai
 */
public class GetKeyIndicatorsAction
    implements Action
{

    private final static String DE_GROUP = "Dashboard Header";
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }
    
    private UserService userService;
    
    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
    }

    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
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

    private ExpressionService expressionService;

    public void setExpressionService( ExpressionService expressionService )
    {
        this.expressionService = expressionService;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }
    private MessageService messageService;

    public void setMessageService( MessageService messageService )
    {
        this.messageService = messageService;
    }
    
    private ConfigurationService configurationService;

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }
    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }

    private I18nManager i18nManager;
    
    public void setI18nManager( I18nManager i18nManager )
    {
        this.i18nManager = i18nManager;
    }
    
    private KeyFlagCalculation keyFlagCalculation;
    
    public void setKeyFlagCalculation( KeyFlagCalculation keyFlagCalculation )
    {
        this.keyFlagCalculation = keyFlagCalculation;
    }
    
    private KeyFlagAnalyticsService keyFlagAnalyticsService;
    
    public void setKeyFlagAnalyticsService( KeyFlagAnalyticsService keyFlagAnalyticsService )
    {
        this.keyFlagAnalyticsService = keyFlagAnalyticsService;
    }
    
    @Autowired
    private IVBUtil ivbUtil;

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    private String dataSetId;

    public void setDataSetId( String dataSetId )
    {
        this.dataSetId = dataSetId;
    }

    public String getDataSetId()
    {
        return dataSetId;
    }

    private String orgUnitUid;

    public void setOrgUnitUid( String orgUnitUid )
    {
        this.orgUnitUid = orgUnitUid;
    }

    public String getOrgUnitUid()
    {
        return orgUnitUid;
    }

    public OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    private String flagFolderPath;

    public String getFlagFolderPath()
    {
        return flagFolderPath;
    }

    private String language;

    public String getLanguage()
    {
        return language;
    }

    private String userName;

    public String getUserName()
    {
        return userName;
    }

    private List<Indicator> indicatorList = new ArrayList<Indicator>();

    public List<Indicator> getIndicatorList()
    {
        return indicatorList;
    }

    Map<String, String> dataValue = new HashMap<String, String>();

    public Map<String, String> getDataValue()
    {
        return dataValue;
    }

    Map<String, String> dataValueTimeStampMap = new HashMap<String, String>();

    public Map<String, String> getDataValueTimeStampMap()
    {
        return dataValueTimeStampMap;
    }

    private Map<String, String> commentMap = new HashMap<String, String>();

    public Map<String, String> getCommentMap()
    {
        return commentMap;
    }

    private Map<String, String> sourceMap = new HashMap<String, String>();

    public Map<String, String> getSourceMap()
    {
        return sourceMap;
    }
    
    private Map<String, String> userInfoMap = new HashMap<String, String>();

    public Map<String, String> getUserInfoMap()
    {
        return userInfoMap;
    }

    private Map<String, String> keyIndicatorValueMap = new HashMap<String, String>();

    public Map<String, String> getKeyIndicatorValueMap()
    {
        return keyIndicatorValueMap;
    }

    private Map<String, String> thresholdMap = new HashMap<String, String>();

    public Map<String, String> getThresholdMap()
    {
        return thresholdMap;
    }

    private String lastUpdated;

    public String getLastUpdated()
    {
        return lastUpdated;
    }

    private Map<String, List<DataSet>> datasetMap = new HashMap<String, List<DataSet>>();

    public Map<String, List<DataSet>> getDatasetMap()
    {
        return datasetMap;
    }
    private int messageCount;
    
    public int getMessageCount()
    {
        return messageCount;
    }

    private String adminStatus;
    
    public String getAdminStatus()
    {
        return adminStatus;
    }
 private String countryName;
    
    public String getCountryName()
    {
        return countryName;
    }

    public void setCountryName( String countryName )
    {
        this.countryName = countryName;
    }
    private List<String> countryList = new ArrayList<String>();
    
    public List<String> getCountryList()
    {
        return countryList;
    }
    public void setCountryList( List<String> countryList )
    {
        this.countryList = countryList;
    }    
    
    private Map<String,String> headerMap = new HashMap<String,String>();
    
    public Map<String, String> getHeaderMap()
    {
        return headerMap;
    }
   
    private Map<String, String> valueMap = new HashMap<String, String>();

    public Map<String, String> getValueMap()
    {
        return valueMap;
    }
    public Map<String, String> colorMap = new HashMap<String, String>();
    
    public Map<String, String> getColorMap()
    {
        return colorMap;
    }

    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------

    public String execute()
    {
        List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>(organisationUnitService.getAllOrganisationUnits());
        for(OrganisationUnit org : orgUnitList)
        {
            for(OrganisationUnit o: ivbUtil.getLeafOrganisationUnits( org.getId() ))
            {
                if(!(countryList.contains( "\""+o.getShortName()+"\"" )))
                {
                    countryList.add( "\""+o.getShortName()+"\"" );
                }
            }           
        }
        Collections.sort( countryList );
        lastUpdated = "";
        if(countryName != null)
        {
            List<OrganisationUnit> orgUnits = new ArrayList<OrganisationUnit>( organisationUnitService
                .getOrganisationUnitByShortName( countryName ));
            organisationUnit = orgUnits.get( 0 );
            ActionContext.getContext().getSession().put( "CountryId", organisationUnit.getId() );
        }
        else if ( ActionContext.getContext().getSession().get( "CountryId" ) == null
            && (orgUnitUid == null || orgUnitUid.trim().equals( "" )) )
        {
            return SUCCESS;
        }
        else if ( ActionContext.getContext().getSession().get( "CountryId" ) == null && orgUnitUid != null )
        {
            Collection<String> orgUnitUids = new ArrayList<String>();
            orgUnitUids.add( orgUnitUid );

            List<OrganisationUnit> orgUnits = new ArrayList<OrganisationUnit>( organisationUnitService
                .getOrganisationUnitsByUid( orgUnitUids ) );
            organisationUnit = orgUnits.get( 0 );
        }
        else
        {
            if ( orgUnitUid == null || orgUnitUid.trim().equals( "" ) )
            {
                String orgUnitId = ActionContext.getContext().getSession().get( "CountryId" ).toString();
                organisationUnit = organisationUnitService.getOrganisationUnit( Integer.parseInt( orgUnitId ) );
            }
            
            else
            {
                Collection<String> orgUnitUids = new ArrayList<String>();
                orgUnitUids.add( orgUnitUid );

                List<OrganisationUnit> orgUnits = new ArrayList<OrganisationUnit>( organisationUnitService
                    .getOrganisationUnitsByUid( orgUnitUids ) );
                organisationUnit = orgUnits.get( 0 );
            }
        }
        flagFolderPath = System.getenv( "DHIS2_HOME" ) + File.separator + "flags" + File.separator;

        userName = currentUserService.getCurrentUser().getUsername();

        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }
        DataElementGroup dataElementGroup = dataElementService.getDataElementGroupByName( DE_GROUP );
        List<DataElement> dataelementList = new ArrayList<DataElement>(dataElementService.getAllDataElements());
        List<DataElement> dataelementMemberList = new ArrayList<DataElement>( dataElementGroup.getMembers() );
        dataelementList.retainAll( dataelementMemberList );
        DataElementCategoryOptionCombo optionCombo = categoryService.getDataElementCategoryOptionCombo( 1 );
        Date tempDate = null;
        for ( DataElement de : dataelementList )
        {
            headerMap.put( de.getUid(), de.getFormName() );
            DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, organisationUnit );

            if ( dv == null )
            {
                dataValue.put( de.getName(), "" );
                dataValueTimeStampMap.put( de.getName(), "" );
            }
            else
            {
                dataValue.put( de.getUid(), dv.getValue() );
                Calendar calendar = Calendar.getInstance();
                calendar.setTime( dv.getPeriod().getStartDate() );
                if ( calendar.get( Calendar.MONTH ) >= 0 && calendar.get( Calendar.MONTH ) <= 2 )
                {
                    dataValueTimeStampMap.put( de.getUid(), "" + calendar.get( Calendar.YEAR ) + " Q1" );
                }
                else if ( calendar.get( Calendar.MONTH ) >= 3 && calendar.get( Calendar.MONTH ) <= 5 )
                {
                    dataValueTimeStampMap.put( de.getUid(), "" + calendar.get( Calendar.YEAR ) + " Q2" );
                }
                else if ( calendar.get( Calendar.MONTH ) >= 6 && calendar.get( Calendar.MONTH ) <= 8 )
                {
                    dataValueTimeStampMap.put( de.getUid(), "" + calendar.get( Calendar.YEAR ) + " Q3" );
                }
                else if ( calendar.get( Calendar.MONTH ) >= 9 && calendar.get( Calendar.MONTH ) <= 11 )
                {
                    dataValueTimeStampMap.put( de.getUid(), "" + calendar.get( Calendar.YEAR ) + " Q4" );
                }
                else
                {
                    dataValueTimeStampMap.put( de.getUid(), "" );
                }

                if ( tempDate == null || tempDate.before( dv.getLastUpdated() ) )
                {
                    tempDate = dv.getLastUpdated();
                }
            }
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        if ( tempDate != null )
        {
            lastUpdated = simpleDateFormat.format( tempDate );
        }

       List<DataSet> datasetList = new ArrayList<DataSet>(dataSetService.getAllDataSets());   
       
       
       Iterator<DataSet> dataSetIterator = datasetList.iterator();
       List<DataSet> datasets = new ArrayList<DataSet>();
       while( dataSetIterator.hasNext() )
       {
           DataSet ds = dataSetIterator.next();
           if( ds.getSources() != null && ds.getSources().size() > 0 )
           {
               datasets.add( ds );
           }
       }
       datasetList.retainAll( datasets );
      // Collections.sort( datasetList, new IdentifiableObjectNameComparator() );
       
       
       
       
       //Collections.sort( datasetList, new DataSetSortOrderComparator() );
       List<Indicator> allIndicators = new ArrayList<Indicator>(indicatorService.getAllIndicators());
       
       Map<String, KeyFlagAnalytics> keyFlagAnalyticMap = new HashMap<String, KeyFlagAnalytics>();
       keyFlagAnalyticMap = new HashMap<String, KeyFlagAnalytics>( keyFlagAnalyticsService.getKeyFlagAnalyticsMap() );
       
       for(DataSet dataSet : datasetList)
       {
           for ( Indicator indicator : dataSet.getIndicators())
           {
               if( !indicatorList.contains( indicator ) )
               {
                   indicatorList.add( indicator );
                   datasetMap.put( indicator.getUid(),new ArrayList<DataSet>(indicator.getDataSets()));
               }
               
               indicatorList.retainAll( allIndicators );
               KeyFlagAnalytics kfa = keyFlagAnalyticMap.get( organisationUnit.getId() + ":" + indicator.getId() );
               
               String mapKey =  indicator.getUid() + "-" + organisationUnit.getUid();
                /*   
                String keyIndicatorValue = "";
                String exString = indicator.getNumerator(); 
                if(exString.contains(KeyFlagCalculation.NESTED_OPERATOR_AND) || exString.contains( KeyFlagCalculation.NESTED_OPERATOR_OR ))
                { 
                    keyIndicatorValue = keyFlagCalculation.getNestedKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator.getUid(), organisationUnit ); 										
                }
                else
                {
                    keyIndicatorValue = keyFlagCalculation.getKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator.getUid(),organisationUnit );
                }

                //System.out.println("Before KeyIndicatorValue for "+indicator.getName()+ " is " + keyIndicatorValue );
                
                if( keyFlagCalculation.getIsthresoldrev() && keyIndicatorValue != null && keyIndicatorValue.equalsIgnoreCase("Yes") )
		{
                    keyIndicatorValue = "No";
		}
		else if( keyFlagCalculation.getIsthresoldrev() && keyIndicatorValue != null && keyIndicatorValue.equalsIgnoreCase("No") )
		{	
		    keyIndicatorValue = "Yes";
		}
                
                //System.out.println("After KeyIndicatorValue for "+indicator.getName()+ " is " + keyIndicatorValue + "  -- " + keyFlagCalculation.getIsthresoldrev() );
                
                try 
                {
                    Integer factor = indicator.getIndicatorType().getFactor();
                    double keyIndicatorVal = Double.parseDouble( keyIndicatorValue );
                    keyIndicatorVal = keyIndicatorVal * factor;
                    keyIndicatorVal = Math.round(keyIndicatorVal * 100);
                    keyIndicatorVal = keyIndicatorVal/100;
                    keyIndicatorValue = ""+keyIndicatorVal;
                } 
                catch (Exception e) 
                {                    
                }
                
                keyIndicatorValueMap.put( indicator.getUid()+"-"+organisationUnit.getUid(), keyIndicatorValue );                               
                colorMap = keyFlagCalculation.getColorMap();
                commentMap = keyFlagCalculation.getCommentMap();
                sourceMap = keyFlagCalculation.getSourceMap();                
                userInfoMap = keyFlagCalculation.getUserInfoMap();
                
                */
                
                if( kfa != null )
                {
                    if( kfa.getDeValue() != null )
                    {
                        keyIndicatorValueMap.put( mapKey, kfa.getDeValue() );
                    }
                    
                    if( kfa.getColor() != null )
                    {
                        colorMap.put( mapKey, kfa.getColor() );
                    }
                   
                    
                    if( kfa.getComment() != null )
                    {
                        commentMap.put( mapKey, kfa.getComment() );
                    }
                    
                    if( kfa.getSource() != null )
                    {
                        sourceMap.put( mapKey, kfa.getSource() );
                    }
                    
                    if( kfa.getUser() != null )
                    {
                        userInfoMap.put( mapKey, kfa.getUser() );
                    }
                    
                    /*
                    if( kfa.getLastUpdated() != null )
                    {
                        lastUpdated = kfa.getLastUpdated();
                    }
                    */
                    
                }
           }
       }
       
       /*
       indicatorList = new ArrayList<Indicator>( keyFlagCalculation.getIndicatorList() )  ;
       datasetMap = keyFlagCalculation.getDatasetMap();    
       indicatorList.retainAll( allIndicators );
       */
       
       
       
       ActionContext.getContext().getSession().put( "CountryId", organisationUnit.getId() );        
       messageCount = (int) messageService.getUnreadMessageConversationCount();
       List<UserGroup> userGrps = new ArrayList<UserGroup>( currentUserService.getCurrentUser().getGroups() );
       if ( userGrps.contains( configurationService.getConfiguration().getFeedbackRecipients() ) )
       {
            adminStatus = "Yes";
       }
       else
       {
           adminStatus = "No";
       }
        return SUCCESS;
    }
   
}
