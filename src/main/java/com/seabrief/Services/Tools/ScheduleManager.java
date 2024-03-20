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

/**
 * Schedule Manager Class
 * <p>
 * Cron-like class that takes in a runnable Job,
 * and schedules it at a set interval
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * 
 * ScheduleManager manager = ScheduleManager.getInstance()
 * 
 * }
 * </pre>
 */
public class ScheduleManager {
    private static ScheduleManager instance = null;
    private SchedulerFactory schedulerFactory;
    private Scheduler scheduler;

    public static ScheduleManager getInstance() throws SchedulerException {
        if (instance == null) {
            synchronized (ScheduleManager.class) {
                if (instance == null) {
                    instance = new ScheduleManager();
                }
            }
        }
        return instance;
    }
    
    private ScheduleManager() throws SchedulerException {
        Logger.getAnonymousLogger().setLevel(Level.OFF);
        schedulerFactory = new StdSchedulerFactory();
        scheduler = schedulerFactory.getScheduler();
        scheduler.start();
    }
    
    public <T extends Job> ScheduleManager scheduleJob(Class<T> jobClass, Duration interval) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(jobClass).build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever((int) interval.toMinutes()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);

        return this;
    }
    
    public void stop() throws SchedulerException {
        scheduler.shutdown();
    }
}
