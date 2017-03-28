package org.hisp.dhis.ivb.useractivity;



import java.util.Collection;
import java.util.Date;

import org.hisp.dhis.user.User;

public interface UserActivityService
{
    String ID = UserActivityService.class.getName();

    // -------------------------------------------------------------------------
    // DeMapping
    // -------------------------------------------------------------------------

    void addUserActivity( UserActivity useractivity );

    void updateUserActivity( UserActivity useractivity );

    void deleteUserActivity( UserActivity useractivity );

    UserActivity getUserActivity( String userid );

    Collection<UserActivity> getAllUserActivitys();
    
    Collection<UserActivity> getUserActivityByDate( Date startdate, Date endDate );
    Collection<UserActivity> getUserActivityByUserAndDate( User user, Date startdate, Date endDate );
    
    
}
