package org.hisp.dhis.excelimport.api;

import java.io.Serializable;

/**
 * @author Mithilesh Kumar Thakur
 */
@SuppressWarnings("serial")
public class AttributeCategoryOptionCombo implements Serializable
{
    
    /**
     * categoryoptioncomboid
     */
    private int cocId;
    
    /**
     * categoryoptioncomboUID
     */
    private String cocUid;
    
    /**
     * categoryoptioncomboName
     */
    private String cocName;
    
    // -------------------------------------------------------------------------
    // Contructors
    // -------------------------------------------------------------------------
    
    public AttributeCategoryOptionCombo()
    {
        
    }
 
    public AttributeCategoryOptionCombo( int cocId, String cocUid )
    {
        this.cocId = cocId;
        this.cocUid = cocUid;     
    }    
    
    public AttributeCategoryOptionCombo( int cocId, String cocUid, String cocName )
    {
        this.cocId = cocId;
        this.cocUid = cocUid;
        this.cocName = cocName;       
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------
    
    public int getCocId()
    {
        return cocId;
    }

    public void setCocId( int cocId )
    {
        this.cocId = cocId;
    }

    public String getCocUid()
    {
        return cocUid;
    }

    public void setCocUid( String cocUid )
    {
        this.cocUid = cocUid;
    }

    public String getCocName()
    {
        return cocName;
    }

    public void setCocName( String cocName )
    {
        this.cocName = cocName;
    }

}
