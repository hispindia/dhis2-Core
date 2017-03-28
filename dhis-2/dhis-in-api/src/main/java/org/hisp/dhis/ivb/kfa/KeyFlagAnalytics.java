package org.hisp.dhis.ivb.kfa;

import java.io.Serializable;
import java.util.Date;

import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.organisationunit.OrganisationUnit;

/**
 * @author Ganesh
 * 
 */
public class KeyFlagAnalytics
    implements Serializable
{

    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /**
     * organisationUnit id
     */
    private OrganisationUnit organisationUnit;

    /**
     * Indicator id
     */
    private Indicator indicator;

    /**
     * key flag value
     */
    private String keyFlagValue;

    /**
     * combine Comment for indicator
     */
    private String comment;

    /**
     * source filed to keep track source of entry
     */
    private String source;

    /**
     * user name
     */
    private String user;

    /**
     * period
     */
    private String period;

    /**
     * color code to indicate each entry of indicator
     */
    private String color;

    private String deValue;
    
    private String lastUpdated;
    
    private Date lastScheduled;
    
    public String getDeValue()
    {
        return deValue;
    }

    public void setDeValue( String deValue )
    {
        this.deValue = deValue;
    }

    public String getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated( String lastUpdated )
    {
        this.lastUpdated = lastUpdated;
    }

    public Date getLastScheduled()
    {
        return lastScheduled;
    }

    public void setLastScheduled( Date lastScheduled )
    {
        this.lastScheduled = lastScheduled;
    }

    
    
    // -------------------------------------------------------------------------
    // Getters/Setters
    // -------------------------------------------------------------------------

    /**
     * @return the organisationUnit
     */
    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    /**
     * @param organisationUnit the organisationUnit to set
     */
    public void setOrganisationUnit( OrganisationUnit organisationUnit )
    {
        this.organisationUnit = organisationUnit;
    }

    /**
     * @return the indicator
     */
    public Indicator getIndicator()
    {
        return indicator;
    }

    /**
     * @param indicator the indicator to set
     */
    public void setIndicator( Indicator indicator )
    {
        this.indicator = indicator;
    }

    /**
     * @return the keyFlagValue
     */
    public String getKeyFlagValue()
    {
        return keyFlagValue;
    }

    /**
     * @param keyFlagValue the keyFlagValue to set
     */
    public void setKeyFlagValue( String keyFlagValue )
    {
        this.keyFlagValue = keyFlagValue;
    }

    /**
     * @return the comment
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment( String comment )
    {
        this.comment = comment;
    }

    /**
     * @return the source
     */
    public String getSource()
    {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource( String source )
    {
        this.source = source;
    }

    /**
     * @return the user
     */
    public String getUser()
    {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser( String user )
    {
        this.user = user;
    }

    /**
     * @return the period
     */
    public String getPeriod()
    {
        return period;
    }

    /**
     * @param period the period to set
     */
    public void setPeriod( String period )
    {
        this.period = period;
    }

    /**
     * @return the color
     */
    public String getColor()
    {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor( String color )
    {
        this.color = color;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public KeyFlagAnalytics()
    {

    }

    public KeyFlagAnalytics( OrganisationUnit organisationUnit, Indicator indicator, String keyFlagValue, String comment,
        String source, String user, String period, String color )
    {
        super();
        this.organisationUnit = organisationUnit;
        this.indicator = indicator;
        this.keyFlagValue = keyFlagValue;
        this.comment = comment;
        this.source = source;
        this.user = user;
        this.period = period;
        this.color = color;
    }

}
