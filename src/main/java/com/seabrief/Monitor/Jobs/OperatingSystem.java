package com.seabrief.Monitor.Jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.seabrief.Models.MonitorData.OSPayload;
import com.seabrief.Monitor.Parsers.OSRelease;
import com.seabrief.Monitor.Parsers.OSReleaseExtractor;
import com.seabrief.Services.MQTT.Pattern.MessageBuilder;
import com.seabrief.Services.Tools.Logger;


public class OperatingSystem implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            OSRelease data = OSReleaseExtractor.extract("/etc/os-release");

            OSPayload.Builder builder = OSPayload.newBuilder();

            builder.setName(data.name).setVersion(data.version);

            new MessageBuilder()
                    .withTopic("external/sysmon/os")
                    .withPayload(builder.build().toByteArray())
                    .publish();

            Logger.log("Published os message");
        } catch (Exception ex) {
            Logger.error("Failed to publish os message");
            ex.printStackTrace();
        }
    }
    
}
