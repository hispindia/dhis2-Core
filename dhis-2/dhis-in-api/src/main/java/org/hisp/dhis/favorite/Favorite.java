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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hisp.dhis.common.BaseAnalyticalObject;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroup;


/**
 * @author Samta Bajpai 
 */

@SuppressWarnings( "serial" )
public class Favorite extends BaseAnalyticalObject implements Serializable
{
    /**
     * The unique identifier for this Favorite.
     */
    private int id;

    /**
     * The name of this Favorite. Required and unique.
     */
    private String name;
    
    /**
     * The favType of this Favorite..
     */    
    private String favType;
    
    /**
     * The created Date of this Favorite.
     */    
    private Date created;
    
    /**
     * The lastUpdated Date of this Favorite.
     */    
    private Date lastUpdated;
    
    private List<User> users = new ArrayList<User>();
    /**
     * The showAll of this Favorite.
     */ 
    private int showAll; 
    
    private String storedBy;
    
	private List<DataElement> dataElements = new ArrayList<DataElement>();
	
	private List<String> datavalueTypes = new ArrayList<String>();
    
    private UserGroup userGroup;
    
    private int userGroupAccess;
    
    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Favorite()
    {
    }
    
    public Favorite(String name, List<DataElement> dataElements, List<OrganisationUnit> organisationUnits, List<User> users, List<String> datavalueTypes)
    {
        this.name = name;				
        this.dataElements = dataElements;        
        this.organisationUnits = organisationUnits;
        this.users = this.users;
        this.datavalueTypes = this.datavalueTypes;
    }

    // -------------------------------------------------------------------------
    // hashCode and equals
    // -------------------------------------------------------------------------
    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }

        if ( o == null )
        {
            return false;
        }

        if ( !(o instanceof Favorite) )
        {
            return false;
        }

        final Favorite other = (Favorite) o;

        return name.equals( other.getName() );
    }

    @Override
    public String toString()
    {
        return "[" + name + "]";
    }    
    
    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------
    
    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getFavType()
    {
        return favType;
    }

    public void setFavType( String favType )
    {
        this.favType = favType;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated( Date created )
    {
        this.created = created;
    }

    public Date getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated( Date lastUpdated )
    {
        this.lastUpdated = lastUpdated;
    }
    
    public int getShowAll()
    {
        return showAll;
    }

    public void setShowAll( int showAll )
    {
        this.showAll = showAll;
    }
    
    public List<User> getUsers()
    {
        return users;
    }

    public void setUsers( List<User> users )
    {
        this.users = users;
    }
    
    public UserGroup getUserGroup()
    {
        return userGroup;
    }

    public void setUserGroup( UserGroup userGroup )
    {
        this.userGroup = userGroup;
    }

    public int getUserGroupAccess()
    {
        return userGroupAccess;
    }

    public void setUserGroupAccess( int userGroupAccess )
    {
        this.userGroupAccess = userGroupAccess;
    }

    public String getStoredBy()
    {
        return storedBy;
    }

    public void setStoredBy( String storedBy )
    {
        this.storedBy = storedBy;
    }

    public List<String> getDatavalueTypes()
    {
        return datavalueTypes;
    }

    public void setDatavalueTypes( List<String> datavalueTypes )
    {
        this.datavalueTypes = datavalueTypes;
    }

    @Override
    public void populateAnalyticalProperties()
    {
        // TODO Auto-generated method stub        
    }

	@Override
	public void init( User user, Date date, OrganisationUnit organisationUnit, 
        List<OrganisationUnit> organisationUnitsAtLevel, List<OrganisationUnit> organisationUnitsInGroups, I18nFormat format  )
	{
		// TODO Auto-generated method stub
	}
	
	public List<DataElement> getDataElements() 
	{
		return dataElements;
	}

	public void setDataElements(List<DataElement> dataElements) 
	{
		this.dataElements = dataElements;
	}

	@Override
	public void clearTransientStateProperties() {
		// TODO Auto-generated method stub
		
	}

}
