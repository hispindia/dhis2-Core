package org.hisp.dhis.useractivity;

import java.util.Collection;
import java.util.Date;

import org.hisp.dhis.ivb.useractivity.UserActivity;
import org.hisp.dhis.ivb.useractivity.UserActivityService;
import org.hisp.dhis.ivb.useractivity.UserActivityStore;
import org.hisp.dhis.user.User;



public class DefaultUserActivityService
implements UserActivityService
{
// -------------------------------------------------------------------------
// Dependencies
// -------------------------------------------------------------------------

private UserActivityStore useractivityStore;



// -------------------------------------------------------------------------
// DeMapping
// -------------------------------------------------------------------------



    public void setUseractivityStore( UserActivityStore useractivityStore )
{
    this.useractivityStore = useractivityStore;
}

    //@Override
    public void addUserActivity( UserActivity useractivity )
    {
        useractivityStore.addUserActivity( useractivity );
    }

    //@Override
    public void updateUserActivity( UserActivity useractivity )
    {
        useractivityStore.updateUserActivity( useractivity );
    }

    //@Override
    public void deleteUserActivity( UserActivity useractivity )
    {
        useractivityStore.deleteUserActivity( useractivity );
    }

    //@Override
    public UserActivity getUserActivity( String userid )
    {
        return useractivityStore.getUserActivity( userid );
    }

    //@Override
    public Collection<UserActivity> getAllUserActivitys()
    {
        return useractivityStore.getAllUserActivitys();
    }

    
    //@Override
    public Collection<UserActivity> getUserActivityByDate( Date startdate, Date endDate )
    {
        return useractivityStore.getUserActivityByDate( startdate, endDate );
    }
    
    //@Override
    public Collection<UserActivity> getUserActivityByUserAndDate( User user, Date startdate, Date endDate )
    {
        return useractivityStore.getUserActivityByUserAndDate( user, startdate, endDate);
    }

    public void deleteUser( User arg0 )
    {
        // TODO Auto-generated method stub
        
    }
}

