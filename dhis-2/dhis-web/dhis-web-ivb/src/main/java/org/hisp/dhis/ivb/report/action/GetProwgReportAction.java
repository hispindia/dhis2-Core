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

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.favorite.Favorite;
import org.hisp.dhis.favorite.FavoriteService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Samta Bajpai
 */
public class GetProwgReportAction
    implements Action
{
    private static final int MAX_PER_OBJECT = 15;
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private SelectionTreeManager selectionTreeManager;

    @Autowired
    private IVBUtil ivbUtil;
    
    @Autowired
    private CurrentUserService currentUserService;
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private List<DataElement> dataElementsList = new ArrayList<DataElement>();

    public void setDataElementsList( List<DataElement> dataElementsList )
    {
        this.dataElementsList = dataElementsList;
    }

    public List<DataElement> getDataElementsList()
    {
        return dataElementsList;
    }
    private String favoriteName;

    public String getFavoriteName()
    {
        return favoriteName;
    }

    public void setFavoriteName( String favoriteName )
    {
        this.favoriteName = favoriteName;
    }

    private Collection<OrganisationUnit> selectedUnits = new HashSet<OrganisationUnit>();

    public Collection<OrganisationUnit> getSelectedUnits()
    {
        return selectedUnits;
    }
    
    private String message;

    public String getMessage()
    {
        return message;
    }
    private Map<Integer,String> valueMap = new HashMap<Integer, String>();

    public void setValueMap( Map<Integer, String> valueMap )
    {
        this.valueMap = valueMap;
    }
    public Map<Integer, String> getValueMap()
    {
        return valueMap;
    }
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------
    
    @Override
    public String execute() throws Exception
    {
        
        Favorite favorite = favoriteService.getFavoriteByName( favoriteName ) ;
        if ( favorite.getDataElements().size() > 0 )
        {
            for ( DataElement de : favorite.getDataElements() )
            {
                if ( de != null && !dataElementsList.contains( de ) )
                {
                    dataElementsList.add( de );
                }
            }
            valueMap = ivbUtil.getFavoriteDataValueType(favorite.getId());
        }
        // System.out.println("Favorite Name: "+favorite.getStoredBy());
        // System.out.println("Favorite Size: "+favorite.getOrganisationUnits().size());
        if ( favorite.getOrganisationUnits().size() > 0 )
        {
            for ( OrganisationUnit org : favorite.getOrganisationUnits() )
            {
                if ( org != null )
                {
                    selectedUnits.add( org );
                }
            }
            
            Set<OrganisationUnit> currentUserOrgUnits = new HashSet<OrganisationUnit>( currentUserService.getCurrentUser().getDataViewOrganisationUnits() );
            selectionTreeManager.setRootOrganisationUnits( currentUserOrgUnits );

            selectionTreeManager.clearSelectedOrganisationUnits();
            selectionTreeManager.setSelectedOrganisationUnits( selectedUnits );

        }
        
        message = "true";
        
        return SUCCESS;
    }
}
