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
package org.hisp.dhis.favorite;

import static org.hisp.dhis.i18n.I18nUtils.i18n;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.user.User;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Samta Bajpai
 */

@Transactional
public class DefaultFavoriteService
    implements FavoriteService
{
    // ---------------------------------------------------------------------
    // Dependencies
    // ---------------------------------------------------------------------

    private FavoriteStore favoriteStore;

    public void setFavoriteStore( FavoriteStore favoriteStore )
    {
        this.favoriteStore = favoriteStore;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
    }

    // ---------------------------------------------------------------------
    // Methods
    // ---------------------------------------------------------------------

    public void deleteFavorite( Favorite favorite )
    {
    	System.out.println("Inside delete method of favorite in hibernate class ");
        favoriteStore.deleteFavorite( favorite );
    }

    public int saveFavorite( Favorite favorite )
    {
        return favoriteStore.saveFavorite( favorite );
    }

    public void updateFavorite( Favorite favorite )
    {
        favoriteStore.updateFavorite( favorite );
    }

    public List<Favorite> getAllFavorite()
    {
        return favoriteStore.getAllFavorite();
    }

    public Favorite getFavorite( int id )
    {
        return i18n( i18nService, favoriteStore.getFavorite( id ) );
    }

    public Favorite getFavoriteByName( String name )
    {
        return i18n( i18nService, favoriteStore.getFavoriteByName( name ) );
    }
    
    public Map<Favorite, String> getFavoritesByUser( User user, String favoriteType )
    {
        Map<Favorite, String> favorites = new HashMap<Favorite, String>();
        for ( Favorite fv : favoriteStore.getAllFavoriteByFavoriteType( favoriteType ) )
        {
            if ( fv.getStoredBy() != null && fv.getStoredBy().equals( user.getUsername() ) )
            {
                favorites.put( fv, "Can View and Edit" );
            }
            /*
             * else if(fv.getShowAll() == 0 &&
             * fv.getUserGroup().getMembers().contains( user )) {
             * if(fv.getUserGroupAccess() == 1) { favorites.put( fv, "Can View"
             * ); } else if(fv.getUserGroupAccess() == 2) { favorites.put( fv,
             * "Can View and Edit" ); } }
             */
            else if ( fv.getShowAll() == 1 )
            {
                favorites.put( fv, "Can View" );
            }
            else if ( fv.getShowAll() == 2 )
            {
                favorites.put( fv, "Can View and Edit" );
            }
        }
        return favorites;
    }

    @Override
    public List<Favorite> getAllFavoriteByFavoriteType( String favoriteType )
    {

        return favoriteStore.getAllFavoriteByFavoriteType( favoriteType );
    }

}
