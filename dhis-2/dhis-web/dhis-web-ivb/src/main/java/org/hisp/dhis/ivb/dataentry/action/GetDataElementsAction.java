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

import static org.hisp.dhis.i18n.I18nUtils.i18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.paging.ActionPagingSupport;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;

/**
 * @author Samta bajpai
 */
public class GetDataElementsAction
    extends ActionPagingSupport<DataElement>
{
    private final static String KEY_DATAELEMENT = "KEYFLAG_DE_ATTRIBUTE_ID";
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
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

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
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

    private List<DataElement> dataElementList = new ArrayList<DataElement>();

    public List<DataElement> getDataElementList()
    {
        return dataElementList;
    }

    private List<DataSet> dataSetList;

    public List<DataSet> getDataSetList()
    {
        return dataSetList;
    }

    private Map<DataElement, String> off_pri_de_map;

    public Map<DataElement, String> getOff_pri_de_map()
    {
        return off_pri_de_map;
    }

    private Map<String, List<String>> optionSetMap = new HashMap<String, List<String>>();

    public Map<String, List<String>> getOptionSetMap()
    {
        return optionSetMap;
    }

    private List<DataValue> dataValueList = new ArrayList<DataValue>();

    public List<DataValue> getDataValueList()
    {
        return dataValueList;
    }

    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
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

    Map<String, Integer> dataValueStatusMap = new HashMap<String, Integer>();
    
    public Map<String, Integer> getDataValueStatusMap()
    {
        return dataValueStatusMap;
    }

    Map<String, String> dataValue = new HashMap<String, String>();

    Map<String, String> dataComments = new HashMap<String, String>();

    Map<String, String> dataStoredBy = new HashMap<String, String>();

    public Map<String, String> getDataValue()
    {
        return dataValue;
    }

    public Map<String, String> getDataComments()
    {
        return dataComments;
    }

    public Map<String, String> getDataStoredBy()
    {
        return dataStoredBy;
    }

    Map<String,String> conflictMap = new HashMap<String, String>();
    
    public Map<String, String> getConflictMap()
    {
        return conflictMap;
    }
    Map<String,String> copyRightMap = new HashMap<String, String>();    

    public Map<String, String> getCopyRightMap()
    {
        return copyRightMap;
    }

    private PeriodType periodT;

    public PeriodType getPeriod()
    {
        return periodT;
    }

    private OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    private DataElementCategoryOptionCombo dataElementCategoryOptionCombo;

    public DataElementCategoryOptionCombo getDataElementCategoryOptionCombo()
    {
        return dataElementCategoryOptionCombo;
    }

    private Map<String, String> periodMap = new HashMap<String, String>();

    public Map<String, String> getPeriodMap()
    {
        return periodMap;
    }

    private String statusMessage;

    public String getStatusMessage()
    {
        return statusMessage;
    }

    public String dataSetName;    
    
    public String getDataSetName()
    {
        return dataSetName;
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
    
    Set<DataElement> userDataElements;
    
    public Set<DataElement> getUserDataElements()
    {
        return userDataElements;
    }
    
    private Set<OrganisationUnit> userOrgUnit = new  HashSet<OrganisationUnit>();
    
    public Set<OrganisationUnit> getUserOrgUnit()
    {
        return userOrgUnit;
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
    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------
    public String execute()
    { 
        userDataElements = new HashSet<DataElement>();
        
        User curUser = currentUserService.getCurrentUser();
        
        userName = curUser.getUsername();
        
        List<OrganisationUnit> organUnit = new ArrayList<OrganisationUnit>(currentUserService.getCurrentUser().getOrganisationUnits());
      
        for(OrganisationUnit orgU : organUnit)
        {           
            userOrgUnit.addAll( ivbUtil.getLeafOrganisationUnits( orgU.getId() ));
        }       
        
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials().getUserAuthorityGroups() );
        
        for( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
            userDataElements.addAll( userAuthorityGroup.getDataElements() );
        }
        
        if( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }
       
        statusMessage = "NONE";
        
        if ( ActionContext.getContext().getSession().get( "CountryId" ) == null && ( orgUnitUid == null || orgUnitUid.trim().equals( "" ) ))
        {
            statusMessage = "Please select organisationunit";            
            return SUCCESS;
        }
        else if ( ActionContext.getContext().getSession().get( "CountryId" ) != null )
        {
            String orgUnitId = ActionContext.getContext().getSession().get( "CountryId" ).toString();
            organisationUnit = organisationUnitService.getOrganisationUnit( Integer.parseInt( orgUnitId ) );
            orgUnitUid = organisationUnit.getUid();
        }  
        else if (ActionContext.getContext().getSession().get( "DataSetId" ) == null && ( dataSetId == null || dataSetId.trim().equals( "" ) ))
        {
            statusMessage = "Please select key indicator";            
            return SUCCESS;
        }
        else if(ActionContext.getContext().getSession().get( "DataSetId" ) != null)
        {
            String datasetid = ActionContext.getContext().getSession().get( "DataSetId" ).toString();        	
            DataSet dataset = dataSetService.getDataSet(Integer.parseInt(datasetid));
            dataSetId = dataset.getUid();  
        }
        
        DataValue dValue = new DataValue();

        int monthDays[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

        List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();

        List<String> orgUnitUids = new ArrayList<String>();
        orgUnitUids.add( orgUnitUid );

        List<OrganisationUnit> orgUnit = organisationUnitService.getOrganisationUnitsByUid( orgUnitUids );

        organisationUnit = organisationUnitService.getOrganisationUnit( orgUnit.get( 0 ).getId() );

        dataElementCategoryOptionCombo = categoryService.getDataElementCategoryOptionCombo( 1 );

        dataSetList = new ArrayList<DataSet>( dataSetService.getAllDataSets() );

        DataElementCategoryOptionCombo optionCombo = categoryService.getDataElementCategoryOptionCombo( 1 );

        Collections.sort( dataSetList, new IdentifiableObjectNameComparator() );

        Period period = new Period();

        List<String> datasetList = new ArrayList<String>();
        datasetList.add( dataSetId );
        List<DataSet> dataSets = dataSetService.getDataSetsByUid( datasetList );

        if ( dataSets == null || dataSets.isEmpty() )
        {
            statusMessage = "Please select key indicator";
            
            return SUCCESS;
        }
        
        periodT = dataSets.get( 0 ).getPeriodType();
        
        PeriodType periodType = dataSets.get( 0 ).getPeriodType();
        
        dataSetName = dataSets.get( 0 ).getName();
        
        period = ivbUtil.getCurrentPeriod( periodType, new Date() );
        selectedPeriod = period.getName();
        currentPeriod = period.getDescription();
        
        List<DataElement> tempDEList =  new ArrayList<DataElement>();
        for ( Section section : dataSets.get( 0 ).getSections() )
        {
            tempDEList.addAll( section.getDataElements() );
        }
        dataElementList = new ArrayList<DataElement>( );
        dataElementList.addAll( tempDEList );
        
        i18n( i18nService, dataElementList );
        for ( DataElement de : dataElementList )
        {
            dataValueService.getDataValues( de, period, orgUnit );
            DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, orgUnit.get( 0 ) );
            
            if ( dv == null )
            {
                dataValue.put( (orgUnit.get( 0 ).getUid() + "-" + de.getUid()).trim(), "" );
                dataComments.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "" );
                dataStoredBy.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "" );
                conflictMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "No" );
                copyRightMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "No" );
                dataValueStatusMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), 1 );
            }
            else
            {                
                dataValue.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), dv.getValue() );
                dataComments.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), dv.getComment() );
                dataStoredBy.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), dv.getStoredBy() + "(" + dv.getLastUpdated() + ")" );
                dataValueStatusMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), dv.getStatus() );
               
                if( dv.getLastUpdated().getTime() < period.getStartDate().getTime())
                {
                    copyRightMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "Yes" );
                }
                else
                {
                    copyRightMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "No" );
                }
                if(dv.getFollowup() != null && dv.getFollowup() == true)
                {
                    conflictMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "Yes" ); 
                              
                }
                else
                {
                    conflictMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "No" ); 
                }
            }
        }

        Constant off_pri_de_attribute = constantService.getConstantByName( "OFFICIAL/PRIVATE_DE_ATTRIBUTE_ID" );
        int off_pri_de_attribute_id = 1;
        if ( off_pri_de_attribute != null )
        {
            off_pri_de_attribute_id = (int) off_pri_de_attribute.getValue();
        }

        off_pri_de_map = new HashMap<DataElement, String>();
        for ( DataElement dataelemnt : dataElementList )
        {
        	/*
            if ( dataelemnt.getOptionSet() != null )
            {
                List<String> optionsetList = dataelemnt.getOptionSet().getOptions();
                optionSetMap.put( dataelemnt.getOptionSet().getId() + "", optionsetList );
            }
            */
            Constant keyDEConst = constantService.getConstantByName( KEY_DATAELEMENT );
            Set<AttributeValue> dataElementAttributeValues = dataelemnt.getAttributeValues();
            if ( dataElementAttributeValues != null && dataElementAttributeValues.size() > 0 )
            {
                for ( AttributeValue deAttributeValue : dataElementAttributeValues )
                {
                    if ( deAttributeValue.getAttribute().getId() == keyDEConst.getValue() &&  deAttributeValue.getValue().equalsIgnoreCase( "true" ))
                    {
                        dataElementList.remove( dataelemnt );
                    }
                    if ( deAttributeValue.getAttribute().getId() == off_pri_de_attribute_id
                        && deAttributeValue.getValue() != null
                        && deAttributeValue.getValue().equalsIgnoreCase( "Official" ) )
                    {
                        off_pri_de_map.put( dataelemnt, "official" );
                    }
                    else if ( deAttributeValue.getAttribute().getId() == off_pri_de_attribute_id
                        && deAttributeValue.getValue() != null
                        && deAttributeValue.getValue().equalsIgnoreCase( "Private" ) )
                    {
                        off_pri_de_map.put( dataelemnt, "private" );
                    }
                    else
                    {
                        off_pri_de_map.put( dataelemnt, "other" );
                    }
                }
            }
            else
            {
                off_pri_de_map.put( dataelemnt, "other" );
            }
        }
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
        ActionContext.getContext().getSession().put("OrgUnitId", orgUnit.get( 0 ).getParent().getId());
        ActionContext.getContext().getSession().put("DataSetId", dataSets.get( 0 ).getId());
       
        return SUCCESS;
    }

}
