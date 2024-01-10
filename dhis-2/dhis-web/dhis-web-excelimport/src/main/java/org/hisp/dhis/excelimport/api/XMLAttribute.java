package org.hisp.dhis.excelimport.api;

import java.io.Serializable;

/**
 * @author Mithilesh Kumar Thakur
 */
@SuppressWarnings("serial")
public class XMLAttribute implements Serializable
{
    /**
     * Sheet number
     */
    private int sheetno;
    
    /**
     * Row number
     */
    private int rowno;
    
    /**
     * Column number
     */
    private int colno;
    
    /**
     * dataElement
     */
    private String dataElement;
    
    /**
     *  categoryOptionCombo
     */
    private String categoryOptionCombo;
    
    /**
     *  orgUnit
     */
    private String orgUnit;
    
    /**
     * Formula to calculate the values.
     */
    private String expression;
    
    // -------------------------------------------------------------------------
    // Contructors
    // -------------------------------------------------------------------------
    
    public XMLAttribute()
    {
        
    }
 
    public XMLAttribute( int sheetno, int rowno, int colno, String dataElement, String categoryOptionCombo, String orgUnit )
    {
        this.sheetno = sheetno;
        this.rowno = rowno;
        this.colno = colno;
        this.dataElement = dataElement;
        this.categoryOptionCombo = categoryOptionCombo;
        this.orgUnit = orgUnit;      
    }    
    
    public XMLAttribute( int sheetno, int rowno, int colno, String dataElement, String categoryOptionCombo, String orgUnit, String expression )
    {
        this.sheetno = sheetno;
        this.rowno = rowno;
        this.colno = colno;
        this.dataElement = dataElement;
        this.categoryOptionCombo = categoryOptionCombo;
        this.orgUnit = orgUnit;
        this.expression = expression;        
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------
    
    public int getSheetno()
    {
        return sheetno;
    }

    public void setSheetno( int sheetno )
    {
        this.sheetno = sheetno;
    }

    public int getRowno()
    {
        return rowno;
    }

    public void setRowno( int rowno )
    {
        this.rowno = rowno;
    }

    public int getColno()
    {
        return colno;
    }

    public void setColno( int colno )
    {
        this.colno = colno;
    }

    public String getDataElement()
    {
        return dataElement;
    }

    public void setDataElement( String dataElement )
    {
        this.dataElement = dataElement;
    }

    public String getCategoryOptionCombo()
    {
        return categoryOptionCombo;
    }

    public void setCategoryOptionCombo( String categoryOptionCombo )
    {
        this.categoryOptionCombo = categoryOptionCombo;
    }

    public String getOrgUnit()
    {
        return orgUnit;
    }

    public void setOrgUnit( String orgUnit )
    {
        this.orgUnit = orgUnit;
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression( String expression )
    {
        this.expression = expression;
    }    
    
    
    
}



