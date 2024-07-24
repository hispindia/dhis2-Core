package org.hisp.dhis.excelimport.api;

import java.util.List;

/**
 * @author Mithilesh Kumar Thakur
 */
public class XMLRootAttribute
{
    /**
     *  orgUnit
     */
    private String rootElement;
    
    
    /**
     *  orgUnit
     */
    private List<XMLAttribute> xmlAttribute;
    
    // -------------------------------------------------------------------------
    // Contructors
    // -------------------------------------------------------------------------


    public XMLRootAttribute()
    {
        
    }
    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------
    
    
    public String getRootElement()
    {
        return rootElement;
    }

    public void setRootElement( String rootElement )
    {
        this.rootElement = rootElement;
    }


    public List<XMLAttribute> getXmlAttribute()
    {
        return xmlAttribute;
    }


    public void setXmlAttribute( List<XMLAttribute> xmlAttribute )
    {
        this.xmlAttribute = xmlAttribute;
    }

    

    
}
