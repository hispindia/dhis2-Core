package org.hisp.dhis.ivb.demapping;

import java.io.Serializable;

import org.hisp.dhis.common.BaseNameableObject;

@SuppressWarnings( "serial" )
public class DeMapping
    extends BaseNameableObject
    implements Serializable
{
    
    private String deid;
    private String mappeddeid;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public DeMapping()
    {

    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getDeid() {
        return deid;
    }

    public void setDeid(String deid) {
        this.deid = deid;
    }

    public String getMappeddeid() {
        return mappeddeid;
    }

    public void setMappeddeid(String mappeddeid) {
        this.mappeddeid = mappeddeid;
    }

}
