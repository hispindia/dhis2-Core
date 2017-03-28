package org.hisp.dhis.ivb.useractivity;


import java.io.Serializable;
import java.util.Date;

import org.hisp.dhis.common.BaseNameableObject;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.User;


@SuppressWarnings( "serial" )
public class UserActivity
    extends BaseNameableObject
    implements Serializable
    {
    /**
     * 
     */
  
    private int id;
    
    //public String  userid;
    
    private User user;
    
    public Date loginTime;
    
    

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public UserActivity()
    {
        
    }

    public UserActivity( User user, Date loginTime )
    {
        this.user = user;
        this.loginTime = loginTime;
    }
    
    
    // -------------------------------------------------------------------------
    // hashCode and equals
    // -------------------------------------------------------------------------

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

        if ( !(o instanceof UserActivity) )
        {
            return false;
        }

        final UserActivity other = (UserActivity) o;

        return user.equals( other.getUser() );

    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;

        result = result * prime + user.hashCode();
        return result;
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

    public User getUser()
    {
        return user;
    }

    public void setUser( User user )
    {
        this.user = user;
    }
    
    public Date getLoginTime()
    {
        return loginTime;
    }

    public void setLoginTime( Date loginTime )
    {
        this.loginTime = loginTime;
    }



    
}


