package com.seabrief.Monitor.Jobs;

import java.io.File;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.seabrief.Models.MonitorData.Disk;
import com.seabrief.Models.MonitorData.DiskPayload;
import com.seabrief.Services.MQTT.Pattern.MessageBuilder;
import com.seabrief.Services.Tools.Logger;

public class DiskSpace implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            DiskPayload.Builder builder = DiskPayload.newBuilder();

            File[] roots = File.listRoots();

            for (File root : roots) {
                String path = root.getAbsolutePath();
                long totalSpace = root.getTotalSpace();
                long freeSpace = root.getFreeSpace();
                builder.addDisks(Disk.newBuilder().setName(path).setTotal(totalSpace).setFree(freeSpace));
            }

            DiskPayload payload = builder.build();

            new MessageBuilder()
                    .withTopic("external/sysmon/disk")
                    .withPayload(payload.toByteArray())
                    .publish();

            Logger.log("Published disk message");
        } catch (Exception ex) {
            Logger.error("Failed to publish disk message");
            ex.printStackTrace();
        }
    }

}
