package org.hisp.dhis.ivb.util;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.organisationunit.OrganisationUnit;

public class ExpressionValue
{
    private DataElement de;

    private OrganisationUnit ou;
    
    private String expValue;

    
    public ExpressionValue()
    {
        
    }
    
    public DataElement getDe()
    {
        return de;
    }

    public void setDe( DataElement de )
    {
        this.de = de;
    }

    public OrganisationUnit getOu()
    {
        return ou;
    }

    public void setOu( OrganisationUnit ou )
    {
        this.ou = ou;
    }

    public String getExpValue()
    {
        return expValue;
    }

    public void setExpValue( String expValue )
    {
        this.expValue = expValue;
    }
    
    
}
