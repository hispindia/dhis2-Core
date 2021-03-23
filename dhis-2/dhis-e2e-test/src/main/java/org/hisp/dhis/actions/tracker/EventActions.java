

package org.hisp.dhis.actions.tracker;

import org.hisp.dhis.actions.MaintenanceActions;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.dto.ApiResponse;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class EventActions
    extends RestApiActions
{
    public EventActions()
    {
        super( "/events" );
    }

    /**
     * Hard deletes event.
     *
     * @param eventId
     * @return
     */
    @Override
    public ApiResponse delete( String eventId )
    {
        ApiResponse response = super.delete( eventId );
        new MaintenanceActions().removeSoftDeletedEvents();

        return response;
    }

    public ApiResponse softDelete( String eventId )
    {
        ApiResponse response = super.delete( eventId );

        response.validate().statusCode( 200 );

        return response;
    }

    public void softDelete( List<String> eventIds )
    {
        for ( String id : eventIds
        )
        {
            softDelete( id );
        }
    }
}
