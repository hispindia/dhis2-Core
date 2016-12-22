package org.hisp.dhis.ivb.report.action;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.favorite.Favorite;
import org.hisp.dhis.favorite.FavoriteService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

/**
 * @author Samta bajpai
 */
public class GetTabularReportFormAction
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

    private FavoriteService favoriteService;

    public void setFavoriteService( FavoriteService favoriteService )
    {
        this.favoriteService = favoriteService;
    }

    private ConfigurationService configurationService;

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    @Autowired
    private DataElementService dataElementService;

    // -------------------------------------------------------------------------
    // Input / Output
    // -------------------------------------------------------------------------

    private String favoriteId;

    public void setFavoriteId( String favoriteId )
    {
        this.favoriteId = favoriteId;
    }

    public String getFavoriteId()
    {
        return favoriteId;
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

    private List<DataElement> dataElementsList = new ArrayList<DataElement>();

    public void setDataElementsList( List<DataElement> dataElementsList )
    {
        this.dataElementsList = dataElementsList;
    }

    public List<DataElement> getDataElementsList()
    {
        return dataElementsList;
    }

    private Collection<OrganisationUnit> selectedUnits = new HashSet<OrganisationUnit>();

    public Collection<OrganisationUnit> getSelectedUnits()
    {
        return selectedUnits;
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

    private String accessAuthority;

    public void setAccessAuthority( String accessAuthority )
    {
        this.accessAuthority = accessAuthority;
    }

    private String favoriteType;

    public String getFavoriteType()
    {
        return favoriteType;
    }

    private Map<Integer, String> valueMap = new HashMap<Integer, String>();

    public void setValueMap( Map<Integer, String> valueMap )
    {
        this.valueMap = valueMap;
    }

    public Map<Integer, String> getValueMap()
    {
        return valueMap;
    }

    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------
    public String execute()
    {
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

        favoriteType = IVBUtil.TABULAR_REPORT;

        favoriteList = favoriteService.getAllFavoriteByFavoriteType( favoriteType );
        if ( favoriteService.getFavoritesByUser( currentUserService.getCurrentUser(), favoriteType ) != null )
        {
            favoriteMap = favoriteService.getFavoritesByUser( currentUserService.getCurrentUser(), favoriteType );
        }

        favoriteList.retainAll( favoriteMap.keySet() );
        /*
        if ( ActionContext.getContext().getSession().get( "tabulardataElementList" ) != null )
        {
            List<Integer> tabulardataElementsList = (List<Integer>) ActionContext.getContext().getSession().get( "tabulardataElementList" );
            List<Boolean> values = (List<Boolean>)ActionContext.getContext().getSession().get( "values" );
            List<Boolean> comments = (List<Boolean>)ActionContext.getContext().getSession().get( "comments" );
            
            for ( int i = 0; i < tabulardataElementsList.size(); i++ )
            {
                DataElement de = dataElementService.getDataElement( tabulardataElementsList.get( i ));
                dataElementsList.add( de );
                if( values.get( i ) == true && comments.get( i ) == false )
                { 
                      valueMap.put(de.getId() , "V" );
                }
               else if( values.get( i ) == false && comments.get( i ) == true )
               {
                       
                   valueMap.put(de.getId() , "C" );
               }
               else if( values.get( i ) == true && comments.get( i ) == true )
               {
                   valueMap.put(de.getId() , "VC" );                 
               }
                
            }
            
        }
        if ( ActionContext.getContext().getSession().get( "favoriteId" ) != null )
        {
            favoriteId = (String) ActionContext.getContext().getSession().get( "favoriteId" );
            String[] favoriteAccess = favoriteId.split( ":" );
            favoriteId = favoriteAccess[0];
            System.out.println( favoriteId );
        }
        */
        
        Set<OrganisationUnit> currentUserOrgUnits = new HashSet<OrganisationUnit>( currentUserService.getCurrentUser().getDataViewOrganisationUnits() );
        selectionTreeManager.setRootOrganisationUnits( currentUserOrgUnits );
        
        ActionContext.getContext().getSession().put( "adminStatus", adminStatus );
        ActionContext.getContext().getSession().put( "messageCount", messageCount );
        return SUCCESS;
    }
}