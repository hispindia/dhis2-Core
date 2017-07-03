package org.hisp.dhis.ivb.report.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.user.CurrentUserService;
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

public class ToolBoxReportFormAction implements Action
{
    /**
     * TODO - should use constant and further to have paramter module..
     */
    //private final static int ORGUNIT_GROUP_SET = 3;
    //private static final String SHOW_ALL_COUNTRIES_ORGUNIT_GROUP = "SHOW_ALL_COUNTRIES_ORGUNIT_GROUP";
    //private final static int REGION_LEVEL = 2;

    private static final String ORGUNIT_GROUP_SET = "tWUrSY3jGRh";

    //private static final String SHOW_ALL_COUNTRIES_ORGUNIT_GROUP = "P2EW5Afg4ay";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
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
    
    private DataSetService dataSetService;
    
    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }
    
    private OrganisationUnitGroupService organisationUnitGroupService;

    public void setOrganisationUnitGroupService( OrganisationUnitGroupService organisationUnitGroupService )
    {
        this.organisationUnitGroupService = organisationUnitGroupService;
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
    
    @Autowired
    private SelectionTreeManager selectionTreeManager;

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    
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

    private List<DataSet> datasetList = new ArrayList<DataSet>();
    
    public List<DataSet> getDatasetList()
    {
        return datasetList;
    }

    private List<OrganisationUnitGroup> orgUnitGrpList = new ArrayList<OrganisationUnitGroup>();

    public List<OrganisationUnitGroup> getOrgUnitGrpList()
    {
        return orgUnitGrpList;
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
    
    private String currentQuarter;
    
    public String getCurrentQuarter()
    {
        return currentQuarter;
    }
    
//    private String orgUnitGrpId = "";
//
//    public String getOrgUnitGrpId()
//    {
//        return orgUnitGrpId;
//    }
    

    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------
    public String execute()
    {
        //Constant show_all = constantService.getConstantByName( SHOW_ALL_COUNTRIES_ORGUNIT_GROUP );
        //orgUnitGrpId = (int)show_all.getValue()+"";
        userName = currentUserService.getCurrentUser().getUsername();

        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }

        datasetList = new ArrayList<DataSet>( dataSetService.getAllDataSets() );
        List<DataSet> datasets = new ArrayList<DataSet>();
        Iterator<DataSet> dataSetIterator = datasetList.iterator();
        while( dataSetIterator.hasNext() )
        {
            DataSet ds = dataSetIterator.next();
            if( ds.getSources() != null && ds.getSources().size() > 0 )
            {
                datasets.add( ds );
            }
        }
        datasetList.retainAll( datasets );
        
        Collections.sort( datasetList, new IdentifiableObjectNameComparator() ); 

//        OrganisationUnitGroupSet organisationUnitGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( ORGUNIT_GROUP_SET );
//
//       orgUnitGrpList = new ArrayList<OrganisationUnitGroup>( organisationUnitGroupSet.getOrganisationUnitGroups() );
        OrganisationUnitGroupSet organisationUnitGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( ORGUNIT_GROUP_SET );

        if( organisationUnitGroupSet != null )
        {
        	 orgUnitGrpList = new ArrayList<OrganisationUnitGroup>( organisationUnitGroupSet.getOrganisationUnitGroups() );
        }
        
        Set<OrganisationUnit> currentUserOrgUnits = new HashSet<OrganisationUnit>( currentUserService.getCurrentUser().getDataViewOrganisationUnits() );
        selectionTreeManager.setRootOrganisationUnits( currentUserOrgUnits );

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

        Date curDate = new Date();
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( curDate );
        int currentMonth = calendar.get( Calendar.MONTH );
        
        if ( currentMonth >= 0 && currentMonth <= 2 )
        {
            currentQuarter = calendar.get( Calendar.YEAR ) + "-Q1";
        }
        else if ( currentMonth >= 3 && currentMonth <= 5 )
        {
            currentQuarter = calendar.get( Calendar.YEAR ) + "-Q2";
        }
        else if ( currentMonth >= 6 && currentMonth <= 8 )
        {
            currentQuarter = calendar.get( Calendar.YEAR ) + "-Q3";
        }
        else
        {
            currentQuarter = calendar.get( Calendar.YEAR ) + "-Q4";
        }
        
        return SUCCESS;
    }

}
