package org.hisp.dhis.dataadmin.action.test;
/**
 * @author Mithilesh Kumar Thakur
 */
import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
@Service
public class MyScheduler
{
    @Scheduled(fixedRate = 2000)
    public void process() {
        System.out.println("Invoking testTask at " + new Date());
    }
}
