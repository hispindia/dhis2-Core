package org.hisp.dhis.ivb.report.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

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

/**
 * @author BHARATH
 */
public class GenerateToolBoxReportAction
    implements Action
{

    private static final String COUNTRY_ORGUNIT_GROUP = "COUNTRY_ORGUNIT_GROUP";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private OrganisationUnitGroupService organisationUnitGroupService;

    public void setOrganisationUnitGroupService( OrganisationUnitGroupService organisationUnitGroupService )
    {
        this.organisationUnitGroupService = organisationUnitGroupService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
    }

    private IVBUtil ivbUtil;

    public void setIvbUtil( IVBUtil ivbUtil )
    {
        this.ivbUtil = ivbUtil;
    }

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
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
    private LookupService lookupService;

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    private String introStartDate;

    private Integer orgUnitGroupId;

    private String toolBoxOption;

    private String orgUnitId;

    private List<Integer> selectedListDataset;

    public void setIntroStartDate( String introStartDate )
    {
        this.introStartDate = introStartDate;
    }

    public void setOrgUnitGroupId( Integer orgUnitGroupId )
    {
        this.orgUnitGroupId = orgUnitGroupId;
    }

    public void setToolBoxOption( String toolBoxOption )
    {
        this.toolBoxOption = toolBoxOption;
    }

    public String getToolBoxOption()
    {
        return toolBoxOption;
    }

    public void setOrgUnitId( String orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }

    public void setSelectedListDataset( List<Integer> selectedListDataset )
    {
        this.selectedListDataset = selectedListDataset;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    private String language;

    private String userName;

    private List<DataElement> dataElementList = new ArrayList<DataElement>();

    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();

    private Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> dataValueAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>();

    private Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> technicalAssistanceAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>();

    private Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> conflictAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>();

    private Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> closeDiscussionAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>();

    private Map<String, String> colsedDiscussionMap = new HashMap<String, String>();

    public Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> getDataValueAuditMap()
    {
        return dataValueAuditMap;
    }

    public Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> getTechnicalAssistanceAuditMap()
    {
        return technicalAssistanceAuditMap;
    }

    public Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> getConflictAuditMap()
    {
        return conflictAuditMap;
    }

    public Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> getCloseDiscussionAuditMap()
    {
        return closeDiscussionAuditMap;
    }

    public Map<String, String> getColsedDiscussionMap()
    {
        return colsedDiscussionMap;
    }

    private String headerText;

    public String getHeaderText()
    {
        return headerText;
    }

    private Period period;

    public Period getPeriod()
    {
        return period;
    }

    public List<DataElement> getDataElementList()
    {
        return dataElementList;
    }

    public List<OrganisationUnit> getOrgUnitList()
    {
        return orgUnitList;
    }

    public String getLanguage()
    {
        return language;
    }

    public String getUserName()
    {
        return userName;
    }

    private SimpleDateFormat simpleDateFormat;

    public SimpleDateFormat getSimpleDateFormat()
    {
        return simpleDateFormat;
    }

    private List<Integer> orgUnitIds = new ArrayList<Integer>();

    public void setOrgUnitIds( List<Integer> orgUnitIds )
    {
        this.orgUnitIds = orgUnitIds;
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

    // --------------------------------------------------------------------------
    // Action implementation
    // --------------------------------------------------------------------------
    public String execute()
    {
        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );

        userName = currentUserService.getCurrentUser().getUsername();

        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }

        // PERIOD
        Date sDate = ivbUtil.getStartDateByString( introStartDate );
        Period period = ivbUtil.getCurrentPeriod( new QuarterlyPeriodType(), sDate );
        period = periodService.reloadPeriod( period );

        if ( toolBoxOption.equalsIgnoreCase( DataValueAudit.DVA_CT_TA ) )
        {
            headerText = "Tool Box - Technical Assistance - " + period.getName();
        }
        else if ( toolBoxOption.equalsIgnoreCase( DataValueAudit.DVA_CT_DISCUSSION ) )
        {
            headerText = "Tool Box - Discussion - " + period.getName();
        }
        else if ( toolBoxOption.equalsIgnoreCase( "C" ) )
        {
            headerText = "Tool Box - Conflicts - " + period.getName();
        }
        else if ( toolBoxOption.equalsIgnoreCase( "ALL" ) )
        {
            headerText = "Tool Box - Technical Assistance, Discussion, Conflicts - " + period.getName();
        }

        // ORGUNIT
        if ( orgUnitIds.size() > 1 )
        {
            for ( Integer id : orgUnitIds )
            {
                OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( id );
                orgUnitList.add( orgUnit );
            }
        }
        else if ( selectionTreeManager.getReloadedSelectedOrganisationUnits() != null )
        {
            orgUnitList = new ArrayList<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() );
            List<OrganisationUnit> lastLevelOrgUnit = new ArrayList<OrganisationUnit>();
            List<OrganisationUnit> userOrgUnits = new ArrayList<OrganisationUnit>( currentUserService.getCurrentUser().getDataViewOrganisationUnits()
                 );
            for ( OrganisationUnit orgUnit : userOrgUnits )
            {
                if ( orgUnit.getHierarchyLevel() == 3 )
                {
                    lastLevelOrgUnit.add( orgUnit );
                }
                else
                {
                    lastLevelOrgUnit.addAll( organisationUnitService.getOrganisationUnitsAtLevel( 3, orgUnit ) );
                }
            }
            orgUnitList.retainAll( lastLevelOrgUnit );
        }
        Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );

        Collection<Integer> orgUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );

        String orgUnitIdsByComma = "-1";
        if ( orgUnitIds.size() > 0 )
        {
            orgUnitIdsByComma = getCommaDelimitedString( orgUnitIds );
        }

        // DATAELEMENT
        dataElementList = new ArrayList<DataElement>();
        for ( Integer dataSetId : selectedListDataset )
        {
            DataSet dataSet = dataSetService.getDataSet( dataSetId );
            dataElementList.addAll( dataSet.getDataElements() );
        }

        Lookup lookup = lookupService.getLookupByName( Lookup.RESTRICTED_DE_ATTRIBUTE_ID );
        int restrictedDeAttributeId = Integer.parseInt( lookup.getValue() );
        Set<DataElement> restrictedDes = new HashSet<DataElement>( ivbUtil.getRestrictedDataElements( restrictedDeAttributeId ) );
        
        User curUser = currentUserService.getCurrentUser();
        Set<DataElement> userDes = new HashSet<DataElement>();
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials().getUserAuthorityGroups() );
        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
        	userDes.addAll( userAuthorityGroup.getDataElements() );
        }
        
        restrictedDes.removeAll( userDes );
        dataElementList.removeAll( restrictedDes );
        
        Collection<Integer> dataElementIds = new ArrayList<Integer>( getIdentifiers( dataElementList ) );

        String dataElementIdsByComma = "-1";
        if ( dataElementIds.size() > 0 )
        {
            dataElementIdsByComma = getCommaDelimitedString( dataElementIds );
        }

        // DATA RESULT

        if ( toolBoxOption.equalsIgnoreCase( DataValueAudit.DVA_CT_TA ) )
        {
            dataValueAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>(
                ivbUtil.getDataValueAuditMap( dataElementIdsByComma, orgUnitIdsByComma, period.getId(), toolBoxOption ) );
        }
        else if ( toolBoxOption.equalsIgnoreCase( DataValueAudit.DVA_CT_DISCUSSION ) )
        {
            dataValueAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>(
                ivbUtil.getDataValueAuditMap( dataElementIdsByComma, orgUnitIdsByComma, period.getId(), toolBoxOption ) );

            colsedDiscussionMap = new HashMap<String, String>( ivbUtil.getClosedDiscussions( dataElementIdsByComma,
                orgUnitIdsByComma, period.getId() ) );
        }
        else if ( toolBoxOption.equalsIgnoreCase( "C" ) )
        {
            dataValueAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>(
                ivbUtil.getDataValueAuditMapForConflicts( dataElementIdsByComma, orgUnitIdsByComma, period.getId() ) );
        }
        else if ( toolBoxOption.equalsIgnoreCase( "ALL" ) )
        {
            technicalAssistanceAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>(
                ivbUtil.getDataValueAuditMap( dataElementIdsByComma, orgUnitIdsByComma, period.getId(),
                    DataValueAudit.DVA_CT_TA ) );
            System.out.println( technicalAssistanceAuditMap.size() );
            closeDiscussionAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>(
                ivbUtil.getDataValueAuditMap( dataElementIdsByComma, orgUnitIdsByComma, period.getId(),
                    DataValueAudit.DVA_CT_DISCUSSION ) );
            System.out.println( closeDiscussionAuditMap.size() );
            colsedDiscussionMap = new HashMap<String, String>( ivbUtil.getClosedDiscussions( dataElementIdsByComma,
                orgUnitIdsByComma, period.getId() ) );
            System.out.println( colsedDiscussionMap.size() );
            conflictAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>(
                ivbUtil.getDataValueAuditMapForConflicts( dataElementIdsByComma, orgUnitIdsByComma, period.getId() ) );
            System.out.println( conflictAuditMap.size() );
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
        return SUCCESS;
    }
}
