package com.seabrief;

import java.time.Duration;

import com.seabrief.Logger.Endpoints.GetBounds;
import com.seabrief.Logger.Endpoints.GetDatabases;
import com.seabrief.Logger.Endpoints.GetDefinitions;
import com.seabrief.Logger.Endpoints.GetHistoric;
import com.seabrief.Logger.Parser.Parser;
import com.seabrief.Monitor.Jobs.AppCheck;
import com.seabrief.Monitor.Jobs.DiskSpace;
import com.seabrief.Monitor.Jobs.Heartbeat;
import com.seabrief.Monitor.Jobs.OperatingSystem;
import com.seabrief.Services.MQTT.MQTTClient;
import com.seabrief.Services.Tools.Logger;
import com.seabrief.Services.Tools.ScheduleManager;

public class App {
    public static void main(String[] args) throws Exception {
        try {
            Parser.getInstance().load(System.getProperty("COMPONENTS_DIR"));

            MQTTClient.getInstance()
                    .subscribe("local/health")
                    .subscribe("external/log/+/req")
                    .subscribe("external/log/+/+/req");

            MQTTClient.getInstance()
                    .addMessageReceiver("external/log/bounds/req", new GetBounds())
                    .addMessageReceiver("external/log/defs/req", new GetDefinitions())
                    .addMessageReceiver("external/log/database/req", new GetDatabases())
                    .addMessageReceiver("external/log/hist/.+/req", new GetHistoric());

            ScheduleManager.getInstance()
                    .scheduleJob(Heartbeat.class, Duration.ofMinutes(10))
                    .scheduleJob(DiskSpace.class, Duration.ofHours(6))
                    .scheduleJob(AppCheck.class, Duration.ofHours(10))
                    .scheduleJob(OperatingSystem.class, Duration.ofHours(10));
        } catch (Exception ex) {
            Logger.error("Application Failure:");
            ex.printStackTrace();
        } finally {
            ScheduleManager.getInstance().stop();
            MQTTClient.getInstance().close();
        }
    }
}
