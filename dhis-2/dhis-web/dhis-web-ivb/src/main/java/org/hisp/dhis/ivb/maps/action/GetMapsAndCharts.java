package org.hisp.dhis.ivb.maps.action;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementGroupSet;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Priyanka Bawa
 */
public class GetMapsAndCharts
    implements Action
{
    private static final String ORGUNIT_GROUP_SET = "tWUrSY3jGRh";
    //private static final String SHOW_ALL_COUNTRIES_ORGUNIT_GROUP = "P2EW5Afg4ay";
    private static final String VACCINE_INTRO_DE_GROUPSET = "w9nGuFiF3yh"; 
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    /**
     * TODO - should use constant and further to have paramter module..
     */
//    private final static int ORGUNIT_GROUP_SET = 3;
//
//    private final static int VACCINE_INTRO_DE_GROUPSET = 1;
//
//    private static final String SHOW_ALL_COUNTRIES_ORGUNIT_GROUP = "SHOW_ALL_COUNTRIES_ORGUNIT_GROUP";

    @Autowired
    private SelectionTreeManager selectionTreeManager;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private I18nService i18nService;

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private DataElementService dataElementService;

    @Autowired
    private OrganisationUnitGroupService organisationUnitGroupService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ConstantService constantService;

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

    private List<DataElementGroup> deGroupList = new ArrayList<DataElementGroup>();

    public List<DataElementGroup> getDeGroupList()
    {
        return deGroupList;
    }

    private List<DataElement> allDataElementList = new ArrayList<DataElement>();

    public List<DataElement> getAllDataElementList()
    {
        return allDataElementList;
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

    private DataElementGroup DEvaccineintro;

    public DataElementGroup getDEvaccineintro()
    {
        return DEvaccineintro;
    }

    public void setDEvaccineintro( DataElementGroup dEvaccineintro )
    {
        DEvaccineintro = dEvaccineintro;
    }

    public String getAdminStatus()
    {
        return adminStatus;
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
        //orgUnitGrpId = (int) show_all.getValue() + "";
        userName = currentUserService.getCurrentUser().getUsername();

        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }
        DataElementGroupSet deGroupSet = dataElementService.getDataElementGroupSet( VACCINE_INTRO_DE_GROUPSET );
        deGroupList.addAll( deGroupSet.getMembers() );
        Collections.sort( deGroupList, new IdentifiableObjectNameComparator() );
        for ( DataElementGroup ds : deGroupList )
        {
            if ( ds.getName().equals( "Intro Status" ) )
            {
                allDataElementList.addAll( ds.getMembers() );

            }
        }
        Collections.sort( allDataElementList );
//        OrganisationUnitGroupSet organisationUnitGroupSet = organisationUnitGroupService
//            .getOrganisationUnitGroupSet( ORGUNIT_GROUP_SET );
//
//        orgUnitGrpList = new ArrayList<OrganisationUnitGroup>( organisationUnitGroupSet.getOrganisationUnitGroups() );

        OrganisationUnitGroupSet organisationUnitGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( ORGUNIT_GROUP_SET );

        if( organisationUnitGroupSet != null )
        {
        	 orgUnitGrpList = new ArrayList<OrganisationUnitGroup>( organisationUnitGroupSet.getOrganisationUnitGroups() );
        }        
        Set<OrganisationUnit> currentUserOrgUnits = new HashSet<OrganisationUnit>( currentUserService.getCurrentUser()
            .getDataViewOrganisationUnits() );
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
        Collections.sort( orgUnitGrpList );

        return SUCCESS;
    }
}