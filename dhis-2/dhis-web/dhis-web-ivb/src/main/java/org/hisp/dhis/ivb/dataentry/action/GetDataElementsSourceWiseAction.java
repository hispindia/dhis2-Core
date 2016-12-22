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

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.util.ArrayList;
import java.util.Collection;
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
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserService;

import com.opensymphony.xwork2.ActionContext;

/**
 * @author Samta bajpai
 */
public class GetDataElementsSourceWiseAction
    extends ActionPagingSupport<DataElement>
{
    private final static String KEY_DATAELEMENT = "KEYFLAG_DE_ATTRIBUTE_ID";
    private final static int REGION = 2;

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

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

    private UserService userService;

    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
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
    
    private DataSetService dataSetService;
    
    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }
    
    private IVBUtil ivbUtil;
    
    public void setIvbUtil( IVBUtil ivbUtil )
    {
        this.ivbUtil = ivbUtil;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
   
    private String dataSetId = "0";

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

    private List<DataSet> dataSetList = new ArrayList<DataSet>();

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

    private OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    private String oranisationUnitId;

    public String getOranisationUnitId()
    {
        return oranisationUnitId;
    }

    public void setOranisationUnitId( String oranisationUnitId )
    {
        this.oranisationUnitId = oranisationUnitId;
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

    private List<OrganisationUnit> userOrgUnitList = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getUserOrgUnitList()
    {
        return userOrgUnitList;
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

    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getOrgUnitList()
    {
        return orgUnitList;
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

    private Map<String, DataSet> dataSetMap = new HashMap<String, DataSet>();

    public Map<String, DataSet> getDataSetMap()
    {
        return dataSetMap;
    }
    Map<String,String> copyRightMap = new HashMap<String, String>();    

    public Map<String, String> getCopyRightMap()
    {
        return copyRightMap;
    }
    
    public Map<String, DataValue> getDataValueMap()
    {
        return dataValueMap;
    }

    private Map<String, DataValue> dataValueMap = new HashMap<String, DataValue>();
    
    private Period period;
    
    public Period getPeriod()
    {
        return period;
    }
    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------

    public String execute()
    {
        User curUser = currentUserService.getCurrentUser();
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
        userName = curUser.getUsername();

        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }

        dataElementCategoryOptionCombo = categoryService.getDataElementCategoryOptionCombo( 1 );
        Constant off_pri_de_attribute = constantService.getConstantByName( "OFFICIAL/PRIVATE_DE_ATTRIBUTE_ID" );
        int off_pri_de_attribute_id = 1;
        if ( off_pri_de_attribute != null )
        {
            off_pri_de_attribute_id = (int) off_pri_de_attribute.getValue();
        }

        off_pri_de_map = new HashMap<DataElement, String>();

        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();

        List<OrganisationUnit> userOrgUnits = new ArrayList<OrganisationUnit>( currentUserService.getCurrentUser()
            .getOrganisationUnits() );

        for ( OrganisationUnit org : userOrgUnits )
        {
            if ( org.getChildren().isEmpty() )
            {
                if ( !(userOrgUnitList.contains( org.getParent() )) )
                {
                    userOrgUnitList.addAll( organisationUnitService.getOrganisationUnitsAtLevel( REGION, org
                        .getParent() ) );
                }
            }
            else
            {
                if ( !(userOrgUnitList.contains( org )) )
                {
                    userOrgUnitList.addAll( organisationUnitService.getOrganisationUnitsAtLevel( REGION, org ) );
                }
            }
        }
        Collections.sort( userOrgUnitList, new IdentifiableObjectNameComparator() );

        dataElementList = new ArrayList<DataElement>( dataElementService.getAllDataElements() );

        Set<DataElement> userDataElements = new HashSet<DataElement>();

        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials()
            .getUserAuthorityGroups() );

        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
            userDataElements.addAll( userAuthorityGroup.getDataElements() );
        }

        if ( userDataElements == null || userDataElements.isEmpty() )
        {
            period = ivbUtil.getCurrentPeriod( new QuarterlyPeriodType(), new Date() );

            selectedPeriod = period.getName();

            return SUCCESS;
        }
        Constant keyDEConst = constantService.getConstantByName( KEY_DATAELEMENT );
        for(DataElement de : userDataElements)
        { 
            Set<AttributeValue> dataElementAttributeValues = de.getAttributeValues();
            if ( dataElementAttributeValues != null && dataElementAttributeValues.size() > 0 )
            {
                for ( AttributeValue deAttributeValue : dataElementAttributeValues )
                {
                    if (deAttributeValue.getValue() != null && deAttributeValue.getAttribute().getId() == keyDEConst.getValue() &&  deAttributeValue.getValue().equalsIgnoreCase( "true" ))
                    {
                        userDataElements.remove( de );
                    }
                }
            }
        }

        dataElementList.retainAll( userDataElements );
        if(dataElementList.size() <= 0)
        {
            return SUCCESS;
        }
        for ( DataElement de : dataElementList )
        {
            if ( de.getDataSets().size() != 0 )
            {
                dataSetMap.put( de.getUid(), new ArrayList<DataSet>( de.getDataSets() ).get( 0 ) );
                if(!(dataSetList.contains( new ArrayList<DataSet>( de.getDataSets() ).get( 0 ) )))
                {
                    dataSetList.add( new ArrayList<DataSet>( de.getDataSets() ).get( 0 ) );
                }
            }
        }
        DataSet ds = new DataSet();
        if(dataSetId != "0" || dataSetId != "-1" )
        {
            ds = dataSetService.getDataSet( Integer.parseInt( dataSetId ) ); 
        }
        else if(ActionContext.getContext().getSession().get( "dataSetId") != null)
        {
            dataSetId = (String)ActionContext.getContext().getSession().get( "dataSetId");
            ds = dataSetService.getDataSet( Integer.parseInt( dataSetId ) );
        }
        else
        {
            ds = dataSetService.getDataSet( Integer.parseInt( dataSetId ) ); 
        }        
        if(ds != null)
        {            
            List<DataElement> deList = new ArrayList<DataElement>(ds.getDataElements());
            dataElementList.retainAll( deList );
            ActionContext.getContext().getSession().put( "dataSetId",dataSetId );
        }

        PeriodType periodType = dataElementList.get( 0 ).getPeriodType();

        period = ivbUtil.getCurrentPeriod( periodType, new Date() );

        selectedPeriod = period.getName();

        currentPeriod = period.getIsoDate();

        if ( ActionContext.getContext().getSession().get( "OrgUnitId" ) == null
            && (oranisationUnitId == null || oranisationUnitId.trim().equals( "" ) || oranisationUnitId.equals( "-1" )) )
        {
            return SUCCESS;
        }
        else if ( ActionContext.getContext().getSession().get( "OrgUnitId" ) == null && oranisationUnitId != null )
        {
            organisationUnit = organisationUnitService.getOrganisationUnit( Integer.parseInt( oranisationUnitId ) );
            orgUnitList = new ArrayList<OrganisationUnit>( ivbUtil.getLeafOrganisationUnits( Integer.parseInt( oranisationUnitId ) ) );
            ActionContext.getContext().getSession().put( "OrgUnitId", oranisationUnitId );
        }
        else
        {
            if ( oranisationUnitId == null || oranisationUnitId.trim().equals( "" ) || oranisationUnitId.equals( "-1" ) )
            {
                String orgUnitId = ActionContext.getContext().getSession().get( "OrgUnitId" ).toString();
                organisationUnit = organisationUnitService.getOrganisationUnit( Integer.parseInt( orgUnitId ) );
                orgUnitList = new ArrayList<OrganisationUnit>( ivbUtil.getLeafOrganisationUnits( Integer.parseInt( orgUnitId ) ) );
            }
            else
            {
                organisationUnit = organisationUnitService.getOrganisationUnit( Integer.parseInt( oranisationUnitId ) );
                orgUnitList = new ArrayList<OrganisationUnit>( ivbUtil.getLeafOrganisationUnits( Integer.parseInt( oranisationUnitId ) ) );

                ActionContext.getContext().getSession().put( "OrgUnitId", oranisationUnitId );
            }
        }
        List<OrganisationUnit> lastLevelOrgUnit = new ArrayList<OrganisationUnit>();
        for ( OrganisationUnit orgUnit : userOrgUnits )
        {
            if ( orgUnit.getChildren().isEmpty() )
            {
                lastLevelOrgUnit.add( orgUnit );
            }
            else
            {
                lastLevelOrgUnit.addAll( organisationUnitService.getOrganisationUnitsAtLevel( 3, orgUnit ) );
            }
        }
        orgUnitList.retainAll( lastLevelOrgUnit );
        
        Collection<Integer> orgUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );        
        String orgUnitIdByComma = getCommaDelimitedString( orgUnitIds );
        
        Collection<Integer> dataElementIds = new ArrayList<Integer>( getIdentifiers( dataElementList ) );        
        String dataElementIdsByComma = getCommaDelimitedString( dataElementIds );
        
        dataValueMap = ivbUtil.getLatestDataValues( dataElementIdsByComma, orgUnitIdByComma);
        
        for ( DataElement de : dataElementList )
        {            
            Set<AttributeValue> dataElementAttributeValues = de.getAttributeValues();
            if ( dataElementAttributeValues != null && dataElementAttributeValues.size() > 0 )
            {
                for ( AttributeValue deAttributeValue : dataElementAttributeValues )
                {                    
                    if ( deAttributeValue.getAttribute().getId() == off_pri_de_attribute_id
                        && deAttributeValue.getValue() != null
                        && deAttributeValue.getValue().equalsIgnoreCase( "Official" ) )
                    {
                        off_pri_de_map.put( de, "official" );
                    }
                    else if ( deAttributeValue.getAttribute().getId() == off_pri_de_attribute_id
                        && deAttributeValue.getValue() != null
                        && deAttributeValue.getValue().equalsIgnoreCase( "Private" ) )
                    {
                        off_pri_de_map.put( de, "private" );
                    }
                    else
                    {
                        off_pri_de_map.put( de, "other" );
                    }
                }
            }
            else
            {
                off_pri_de_map.put( de, "other" );
            }
        }
        Collections.sort( dataElementList, new IdentifiableObjectNameComparator() );
        
        return SUCCESS;
    }

}
