package com.seabrief.Monitor.Jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.seabrief.Services.MQTT.Pattern.MessageBuilder;
import com.seabrief.Services.Tools.Logger;


public class Heartbeat implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Long time = System.currentTimeMillis();

            new MessageBuilder()
                    .withTopic("external/sysmon/heartbeat")
                    .withPayload(time.toString().getBytes())
                    .publish();

            Logger.log("Published heartbeat message");

        } catch (Exception ex) {
            Logger.error("Failed to publish heartbeat message");
            ex.printStackTrace();
        }
    }
}
