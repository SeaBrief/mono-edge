package com.seabrief.Services.Tools;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class ScheduleManager {
    private SchedulerFactory schedulerFactory;
    private Scheduler scheduler;
    
    public ScheduleManager() throws SchedulerException {
        Logger.getAnonymousLogger().setLevel(Level.OFF);
        schedulerFactory = new StdSchedulerFactory();
        scheduler = schedulerFactory.getScheduler();
        scheduler.start();
    }
    
    public <T extends Job> void scheduleJob(Class<T> jobClass, Duration interval) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(jobClass).build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever((int) interval.toMinutes()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }
    
    public void stop() throws SchedulerException {
        scheduler.shutdown();
    }
}
