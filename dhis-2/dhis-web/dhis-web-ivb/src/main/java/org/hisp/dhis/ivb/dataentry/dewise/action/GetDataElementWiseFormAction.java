package org.hisp.dhis.ivb.dataentry.dewise.action;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.favorite.Favorite;
import org.hisp.dhis.favorite.FavoriteService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserGroupService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

/**
 * @author Samta bajpai
 */
public class GetDataElementWiseFormAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
	
	@Autowired
    private SelectionTreeManager selectionTreeManager;

    
    private MessageService messageService;

    public void setMessageService( MessageService messageService )
    {
        this.messageService = messageService;
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

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    private UserGroupService userGroupService;

    public void setUserGroupService( UserGroupService userGroupService )
    {
        this.userGroupService = userGroupService;
    }

    private ConfigurationService configurationService;

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    private FavoriteService favoriteService;

    public void setFavoriteService( FavoriteService favoriteService )
    {
        this.favoriteService = favoriteService;
    }

    @Autowired
    private LookupService lookupService;
    
    @Autowired
    private IVBUtil ivbUtil;
    
    // -------------------------------------------------------------------------
    // Output
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

    private String message;

    public String getMessage()
    {
        return message;
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

    private Map<String, List<Section>> dataSetSectionMap = new HashMap<String, List<Section>>();

    public Map<String, List<Section>> getDataSetSectionMap()
    {
        return dataSetSectionMap;
    }

    private List<DataElement> dataElementsList;

    public void setDataElementsList( List<DataElement> dataElementsList )
    {
        this.dataElementsList = dataElementsList;
    }

    public List<DataElement> getDataElementsList()
    {
        return dataElementsList;
    }

    public List<Favorite> favoriteList = new ArrayList<Favorite>();

    public void setFavoriteList( List<Favorite> favoriteList )
    {
        this.favoriteList = favoriteList;
    }

    public Map<Favorite, String> favoriteMap = new HashMap<Favorite, String>();

    public void setFavoriteMap( Map<Favorite, String> favoriteMap )
    {
        this.favoriteMap = favoriteMap;
    }

    private String favoriteType;

    public String getFavoriteType()
    {
        return favoriteType;
    }
    
    private Favorite favorite;
    
    public Favorite getFavorite()
    {
        return favorite;
    }
    
    private String favoriteId;
    
    public void setFavoriteId( String favoriteId )
    {
        this.favoriteId = favoriteId;
    }
    
    private List<String> favoriteListt = new ArrayList<String>();
    
    public List<String> getFavoriteListt()
    {
        return favoriteListt;
    }

    public String execute()
    {
    	
    	Lookup lookup = lookupService.getLookupByName( Lookup.RESTRICTED_DE_ATTRIBUTE_ID );
        int restrictedDeAttributeId = Integer.parseInt( lookup.getValue() );
        Set<DataElement> restrictedDeList = ivbUtil.getRestrictedDataElements( restrictedDeAttributeId );

        favoriteType = IVBUtil.CUSTOM_DATAENTRY;

        if ( favoriteService.getFavoritesByUser( currentUserService.getCurrentUser(), IVBUtil.CUSTOM_DATAENTRY ) != null )
        {
            favoriteMap = favoriteService.getFavoritesByUser( currentUserService.getCurrentUser(), IVBUtil.CUSTOM_DATAENTRY );
        }
        favoriteList = new ArrayList<Favorite>( favoriteMap.keySet() );
        userName = currentUserService.getCurrentUser().getUsername();

        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }

        if ( ActionContext.getContext().getSession().get( "SessionMsg" ) == null )
        {
            message = "Not Exist";
        }
        else
        {
            message = "Exist";
        }

        Set<DataSet> dataSets = new HashSet<DataSet>( dataSetService.getAllDataSets() );
        List<DataSet> datasets = new ArrayList<DataSet>();
        Iterator<DataSet> dataSetIterator = dataSets.iterator();
        while ( dataSetIterator.hasNext() )
        {
            DataSet ds = dataSetIterator.next();
            if ( ds.getSources() != null && ds.getSources().size() > 0 )
            {
                datasets.add( ds );
            }
        }
        dataSets.retainAll( datasets );
        for ( DataSet dataSet : dataSets )
        {
            if ( dataSet.getSections() != null )
            {
                List<Section> sectionList = new ArrayList<Section>( dataSet.getSections() );
                Collections.sort( sectionList, new Comparator<Section>()
                {
                    public int compare( Section one, Section other )
                    {
                        return one.getDisplayName().trim().compareTo( other.getDisplayName().trim() );
                    }
                } );
                dataSetSectionMap.put( dataSet.getUid(), sectionList );
            }
        }
        
        
        Set<OrganisationUnit> currentUserOrgUnits = new HashSet<OrganisationUnit>( currentUserService.getCurrentUser().getOrganisationUnits() );
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

        if ( ActionContext.getContext().getSession().get( "dataElementList" ) != null )
        {
            dataElementsList = (List<DataElement>) ActionContext.getContext().getSession().get( "dataElementList" );
        }

        ActionContext.getContext().getSession().put( "adminStatus", adminStatus );
        ActionContext.getContext().getSession().put( "messageCount", messageCount );
        
        List<Favorite> favorites = favoriteService.getAllFavoriteByFavoriteType( IVBUtil.CUSTOM_DATAENTRY );
        
        for(Favorite f :favorites)
        {
            favoriteListt.add( "\""+ f.getName()+"\"" );
        }
        
        if( favoriteId != null && !favoriteId.equalsIgnoreCase( "-1" ))
        {
            String[] favoriteAccess = favoriteId.split( ":" );
            if(favoriteAccess[1].equalsIgnoreCase( "Can View and Edit" ))
            {
                favorite = favoriteService.getFavorite( Integer.parseInt( favoriteAccess[0] ) );
            }
        }
        
        return SUCCESS;
    }
}