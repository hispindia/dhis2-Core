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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.hisp.dhis.common.comparator.IdentifiableObjectCodeComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

/**
 * @author Samta bajpai
 */
public class GetOrgInfoAction
    implements Action
{
    private static final String ORGUNIT_GROUP_SET = "tWUrSY3jGRh";

    private  static final int REGION_LEVEL = 2;
    
    //private final static int CONSTANT_ID = 7;
    
    private static final String SHOW_ALL_COUNTRIES_ORGUNIT_GROUP = "P2EW5Afg4ay";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

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
    private IVBUtil ivbUtil;
    
    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getOrgUnitList()
    {
        return orgUnitList;
    }

    private Map<OrganisationUnit, List<OrganisationUnit>> orgUnitMap = new TreeMap<OrganisationUnit, List<OrganisationUnit>>();

    public Map<OrganisationUnit, List<OrganisationUnit>> getOrgUnitMap()
    {
        return orgUnitMap;
    }

    private OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    private List<OrganisationUnitGroup> orgUnitGrpList = new ArrayList<OrganisationUnitGroup>();

    public List<OrganisationUnitGroup> getOrgUnitGrpList()
    {
        return orgUnitGrpList;
    }

    private Map<String, List<Section>> dataSetSectionMap = new HashMap<String, List<Section>>();
    
    public Map<String, List<Section>> getDataSetSectionMap()
    {
        return dataSetSectionMap;
    }

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private String key;

    public String getKey()
    {
        return key;
    }

    public void setKey( String key )
    {
        this.key = key;
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

    private String orgUnitGrpId = "";

    public String getOrgUnitGrpId()
    {
        return orgUnitGrpId;
    }

    public void setOrgUnitGrpId( String orgUnitGrpId )
    {
        this.orgUnitGrpId = orgUnitGrpId;
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
    private List<String> countryList = new ArrayList<String>();
    
    public List<String> getCountryList()
    {
        return countryList;
    }
    private List<String> regionList = new ArrayList<String>();
    
    public List<String> getRegionList()
    {
        return regionList;
    }
    
    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {

    	Constant show_all = constantService.getConstant( SHOW_ALL_COUNTRIES_ORGUNIT_GROUP);
    	
    	//System.out.println( "show all----"+SHOW_ALL_COUNTRIES_ORGUNIT_GROUP );
    	
    	
        //Constant show_all = constantService.getConstantByName( SHOW_ALL_COUNTRIES_ORGUNIT_GROUP.trim() );
        
        //System.out.println("show_all------"+constantService.getConstantByName( SHOW_ALL_COUNTRIES_ORGUNIT_GROUP ));
        
        //System.out.println("show----"+show_all);
        
        flagFolderPath = System.getenv( "DHIS2_HOME" ) + File.separator + "flags" + File.separator;
        
        //System.out.println( "org group----"+ORGUNIT_GROUP_SET );
        
        OrganisationUnitGroupSet organisationUnitGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( ORGUNIT_GROUP_SET );
        
    	//System.out.println( "org group----"+ORGUNIT_GROUP_SET );
    	
    	orgUnitGrpList = new ArrayList<OrganisationUnitGroup>( organisationUnitGroupService.getAllOrganisationUnitGroups() );
    	if( organisationUnitGroupSet != null )
    	{
    		//orgUnitGrpList.addAll( organisationUnitGroupService.getAllOrganisationUnitGroups() );
    		orgUnitGrpList.retainAll( organisationUnitGroupSet.getOrganisationUnitGroups() );
    	}
    	

        //orgUnitGrpList.addAll( organisationUnitGroupService.getAllOrganisationUnitGroups() );        
        
        //orgUnitGrpList.retainAll( organisationUnitGroupSet.getOrganisationUnitGroups() );
        
        organisationUnit = currentUserService.getCurrentUser().getOrganisationUnit();
        
        if( orgUnitGrpId != "" )
        { 
        }
        else if( ActionContext.getContext().getSession().get( "orgUnitGrpId") != null )
        {
        	
        	//System.out.println("orgunit----"+orgUnitGrpId);
            orgUnitGrpId = (String)ActionContext.getContext().getSession().get( "orgUnitGrpId");
        	//System.out.println("orgunit----"+orgUnitGrpId);

        }
        else 
        {
        	//System.out.println("orgunit----"+orgUnitGrpId);
        	
        	if( show_all != null )
        	{
        		orgUnitGrpId = ""+(int)show_all.getValue();
            	//System.out.println("orgunit----"+orgUnitGrpId);
        	}
            
        	
        }
        
        if ( orgUnitGrpId != "")
        {
            OrganisationUnitGroup organisationUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( Integer.parseInt( orgUnitGrpId ) );

            ActionContext.getContext().getSession().put( "orgUnitGrpId", orgUnitGrpId );
            List<OrganisationUnit> countryList = new ArrayList<OrganisationUnit>( organisationUnitGroup.getMembers() );

            List<Map<OrganisationUnit, OrganisationUnit>> tableList = new ArrayList<Map<OrganisationUnit, OrganisationUnit>>();
            for ( OrganisationUnit org1 : countryList )
            {
                Map<OrganisationUnit, OrganisationUnit> numberOfOrgUnits = new TreeMap<OrganisationUnit, OrganisationUnit>();
                numberOfOrgUnits.put( org1.getParent(), org1 );
                tableList.add( numberOfOrgUnits );
            }

            for ( Map<OrganisationUnit, OrganisationUnit> map : tableList )
            {
                for ( OrganisationUnit parentOrgUnit : map.keySet() )
                {
                    List<OrganisationUnit> childList = new ArrayList<OrganisationUnit>();
                    if ( orgUnitMap.containsKey( parentOrgUnit ) )
                    {
                        childList.addAll( orgUnitMap.get( parentOrgUnit ) );
                        childList.add( map.get( parentOrgUnit ) );
                        Collections.sort( childList );
                        orgUnitMap.put( parentOrgUnit, childList );
                    }
                    else
                    {
                        orgUnitList.add( parentOrgUnit );
                        childList.add( map.get( parentOrgUnit ) );
                        orgUnitMap.put( parentOrgUnit, childList );
                    }
                }
            }
        }        
        else
        {
            List<OrganisationUnit> orgUnits = new ArrayList<OrganisationUnit>( organisationUnitService
                .getOrganisationUnitsAtLevel( REGION_LEVEL ) );

            for ( OrganisationUnit orgUnit : orgUnits )
            {
                orgUnitList.add( orgUnit );
                List<OrganisationUnit> childList = new ArrayList<OrganisationUnit>();
                childList.addAll( orgUnit.getChildren() );
                Collections.sort( childList, new IdentifiableObjectCodeComparator() );
                orgUnitMap.put( orgUnit, childList );
            }

        }
        
        
        System.out.println("orgUnitList"+orgUnitList);
        Collections.sort( orgUnitList, new IdentifiableObjectCodeComparator() );
        Collections.sort( orgUnitGrpList, new IdentifiableObjectCodeComparator() );        

        userName = currentUserService.getCurrentUser().getUsername();

        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }
        
        Set<DataSet> dataSets = new HashSet<DataSet>( dataSetService.getAllDataSets() );
        for( DataSet dataSet : dataSets )
        {
            if( dataSet.getSections() != null )
            {
                List<Section> sectionList = new ArrayList<Section>( dataSet.getSections() );
                Collections.sort( sectionList, new Comparator<Section>() {
                    public int compare(Section one, Section other) {
                        return one.getDisplayName().trim() .compareTo(other.getDisplayName().trim());
                    }
                });
                dataSetSectionMap.put( dataSet.getUid(), sectionList );
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
        
        ActionContext.getContext().getSession().put( "SessionMsg", "Session Message Exist" );
        
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
        for(OrganisationUnit region : new ArrayList<OrganisationUnit>( organisationUnitService
            .getOrganisationUnitsAtLevel( REGION_LEVEL ) ))
        {
               regionList.add( "\""+region.getShortName()+"\"" ) ;           
        }
        Collections.sort( countryList );
        Collections.sort( regionList );
        return SUCCESS;
    }
}