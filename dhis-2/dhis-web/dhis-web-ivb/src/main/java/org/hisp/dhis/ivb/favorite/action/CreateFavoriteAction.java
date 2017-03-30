package org.hisp.dhis.ivb.favorite.action;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.favorite.DataValueType;
import org.hisp.dhis.favorite.Favorite;
import org.hisp.dhis.favorite.FavoriteService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroupService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Samta bajpai
 */
public class CreateFavoriteAction implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
	@Autowired
    private SelectionTreeManager selectionTreeManager;
   
    private FavoriteService favoriteService;
    
    public void setFavoriteService( FavoriteService favoriteService )
    {
        this.favoriteService = favoriteService;
    }
    
    private DataElementService dataElementService;
    
    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }
    
    private OrganisationUnitService organisationUnitService;
    
    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }   
    private UserGroupService userGroupService;
    
    public void setUserGroupService( UserGroupService userGroupService )
    {
        this.userGroupService = userGroupService;
    }
    
    private CurrentUserService currentUserService;
    
    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }
    
    private IVBUtil ivbUtil;
    
    public void setIvbUtil( IVBUtil ivbUtil )
    {
        this.ivbUtil = ivbUtil;
    }
    
    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private String name;

    public void setName( String name )
    {
        this.name = name;
    }

    private String dataElements;
    
    private String organisationUnits;
    
    public void setDataElements( String dataElements )
    {
        this.dataElements = dataElements;
    }
    private String values;

    public void setValues( String values )
    {
        this.values = values;
    }

    private String comments;
    
    public void setComments( String comments )
    {
        this.comments = comments;
    }

    public void setOrganisationUnits( String organisationUnits )
    {
        this.organisationUnits = organisationUnits;
    } 
     
    private String userGrpId;
    
    public void setUserGrpId( String userGrpId )
    {
        this.userGrpId = userGrpId;
    }
    
    private String favoriteId;
    
    public void setFavoriteId( String favoriteId )
    {
        this.favoriteId = favoriteId;
    }

    private String showAll;
    
    public void setShowAll( String showAll )
    {
        this.showAll = showAll;
    }
    
    private String userGrpAccess;

    public void setUserGrpAccess( String userGrpAccess )
    {
        this.userGrpAccess = userGrpAccess;
    }
   
    private String favoriteType;
    
    public void setFavoriteType(String favoriteType) {
		this.favoriteType = favoriteType;
	}
    

    
    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getOrgUnitList()
    {
        return orgUnitList;
    }

	// -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------
    public String execute()
    {
        User curUser = currentUserService.getCurrentUser();
        System.out.println( dataElements + " --- " + organisationUnits );
        Favorite favorite;
        if(favoriteId != null )
        {
            //System.out.println( favoriteId );
            favorite = favoriteService.getFavorite( Integer.parseInt( favoriteId ) );            
        }
        else
        {
            favorite = new Favorite();
            favorite.setCreated( new Date() );
        }
          
        
        favorite.setName( name );                
        favorite.setFavType( favoriteType );
        
        List<DataElement> dataElementList = new ArrayList<DataElement>();
        Map<Integer,String> datavalueTypeMap = new HashMap<Integer, String>();
        String deIds[] = dataElements.split( "," );
        if(values != null || comments != null)
        {
            String deValues[] = values.split( "," );
            String commentValues[] = comments.split( "," );
            for( int i = 0; i <  deIds.length; i++ )
            {
                try
                { 
                    dataElementList.add( dataElementService.getDataElement( Integer.parseInt( deIds[i] ) ) );
                    if(deValues[i].equalsIgnoreCase( "true" ) && commentValues[i].equalsIgnoreCase( "false" ))
                    {
                        
                        datavalueTypeMap.put(Integer.parseInt( deIds[i] ), "V" );
                    }
                    else if(deValues[i].equalsIgnoreCase( "false" ) && commentValues[i].equalsIgnoreCase( "true" ))
                    {
                       
                        datavalueTypeMap.put(Integer.parseInt( deIds[i] ), "C" );
                    }
                    else if(deValues[i].equalsIgnoreCase( "true" ) && commentValues[i].equalsIgnoreCase( "true" ))
                    {
                        datavalueTypeMap.put(Integer.parseInt( deIds[i] ), "VC" );                 
                    }
                    
                }
                catch( Exception e )
                {
                    System.out.println( e.getMessage() );
                }
            } 
        }
        else
        {
            for( int i = 0; i <  deIds.length; i++ )
            {
                try
                { 
                    dataElementList.add( dataElementService.getDataElement( Integer.parseInt( deIds[i] ) ) );
                }
                catch( Exception e )
                {
                    System.out.println( e.getMessage() );
                }
            } 
        }
        
        favorite.setDataElements( dataElementList );
        //favorite.setDatavalueTypes( datavalueTypeList );
       
        orgUnitList = new ArrayList<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() );
        if(orgUnitList.size()>0){
       
        favorite.setOrganisationUnits( orgUnitList );
        }
        else{
        favorite.setOrganisationUnits( null );	
        }
        favorite.setStoredBy( currentUserService.getCurrentUsername() );
        
        if(showAll.equalsIgnoreCase( "false" ))
        {         
            favorite.setShowAll( 0);
        }
        else
        {
           favorite.setShowAll( 1 ); 
        }
        if(favoriteId != null)
        {
            favoriteService.updateFavorite( favorite );            
        }
        else
        {
            favoriteService.saveFavorite( favorite );
        }
        if(datavalueTypeMap.size() > 0)
        {
            ivbUtil.saveFavoriteDataValueType( favorite.getId(), dataElementList, datavalueTypeMap );
        }
        
        
        return SUCCESS;
    }
}