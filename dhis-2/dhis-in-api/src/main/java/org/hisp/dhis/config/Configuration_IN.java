package org.hisp.dhis.config;

import java.io.Serializable;

import org.hisp.dhis.common.DxfNamespaces;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.common.base.MoreObjects;


/*
 * Copyright (c) 2004-2009, University of Oslo
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
@JacksonXmlRootElement( localName = "metadataAudit", namespace = DxfNamespaces.DXF_2_0 )
public class Configuration_IN implements Serializable
{
    /**
     * Determines if a de-serialized file is compatible with this class.
     */
    private static final long serialVersionUID = -6551567526188061690L;

    public static final String KEY_MYSQLPATH = "mysqlpath";
    public static final String KEY_BACKUPDATAPATH = "backupdatapath";
    public static final String KEY_REPORTFOLDER = "reportfolder";
    
    public static final String DEFAULT_MYSQLPATH = "C:/DHIS2/mysql/bin/";
    public static final String DEFAULT_BACKUPDATAPATH = "C:/dhisdata/";
    public static final String DEFAULT_REPORTFOLDER = "ra_national";
    
    public static final String DEFAULT_TEMPFOLDER = "temp";

    private int id;
    
    private String key;
    
    private String value;
    
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
    
    public Configuration_IN()
    {
        
    }
    
    public Configuration_IN( String key, String value )
    {
        this.key = key;
        this.value = value;
    }
    
    
    // -------------------------------------------------------------------------
    // hashCode and equals
    // -------------------------------------------------------------------------

    @Override
    public int hashCode()
    {
        return key.hashCode();
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

        if ( !(o instanceof Configuration_IN) )
        {
            return false;
        }
        
        final Configuration_IN other = (Configuration_IN) o;
        
        return key.equals( other.getKey() );
    }

    /*
    @Override
    public String toString()
    {
        return "[" + key + "]";
    }
    */
    
    //---------------------------------------------------------------
    // Getters and Setters
    //---------------------------------------------------------------
    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getKey()
    {
        return key;
    }

    public void setKey( String key )
    {
        this.key = key;
    }

    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getValue()
    {
        return value;
    }

    public void setValue( String value )
    {
        this.value = value;
    }
    
    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this )
            .add( "key", key )
            .add( "value", value )
            .toString();
    }
    
}
