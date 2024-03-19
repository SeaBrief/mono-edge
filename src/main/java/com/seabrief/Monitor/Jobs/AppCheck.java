package com.seabrief.Monitor.Jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.seabrief.Models.MonitorData.AppPayload;
import com.seabrief.Models.MonitorData.Application;
import com.seabrief.Monitor.Parsers.CDPExtractor;
import com.seabrief.Monitor.Parsers.LogExtractor;
import com.seabrief.Services.MQTT.Pattern.MessageBuilder;
import com.seabrief.Services.Tools.Logger;

public class AppCheck implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            String logFile = System.getProperty("CDP_LOG_FILE");

            if (logFile == null) {
                logFile = LogExtractor.getFile();
            }

            CDPExtractor extractor = new CDPExtractor(logFile);

            extractor.extract();

            AppPayload.Builder builder = AppPayload.newBuilder();

            for (com.seabrief.Monitor.Parsers.CDPApplication application : extractor.getApplications()) {
                builder.addApps(Application.newBuilder()
                        .setName(application.name)
                        .setVersion(application.version)
                        .setAddress(application.address).build());
            }

            AppPayload status = builder.build();

            new MessageBuilder()
                    .withTopic("external/sysmon/apps")
                    .withPayload(status.toByteArray())
                    .publish();

            Logger.log("Published apps message");
        } catch (Exception ex) {
            Logger.error("Failed to publish app message");
            ex.printStackTrace();
        }
    }

}