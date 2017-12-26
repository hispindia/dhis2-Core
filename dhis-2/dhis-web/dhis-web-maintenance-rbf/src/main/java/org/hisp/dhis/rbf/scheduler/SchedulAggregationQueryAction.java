package org.hisp.dhis.rbf.scheduler;

import java.util.HashMap;
import java.util.Map;

import org.hisp.dhis.system.scheduling.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * @author Mithilesh Kumar Thakur
 */
public class SchedulAggregationQueryAction 
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private RunAggregationQueryActionByScheduler scheduleRunAggregationQueryTask;
    
    private Scheduler scheduler;

    public void setScheduler( Scheduler scheduler )
    {
        this.scheduler = scheduler;
    }
    
    private Map<String, Runnable> tasks = new HashMap<>();

    public void setTasks( Map<String, Runnable> tasks )
    {
        this.tasks = tasks;
    }
    
    
    @EventListener
    public void handleContextRefresh( ContextRefreshedEvent contextRefreshedEvent )
    {
        scheduleAggregationTasks();
    }
    
    private void scheduleAggregationTasks()
    {
        scheduler.scheduleTask( RunAggregationQueryActionByScheduler.KEY_TASK, scheduleRunAggregationQueryTask, Scheduler.CRON_DAILY_10PM );
    }
}
