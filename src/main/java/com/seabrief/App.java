package com.seabrief;

import java.time.Duration;

import com.seabrief.Logger.Endpoints.GetBounds;
import com.seabrief.Logger.Endpoints.GetDatabases;
import com.seabrief.Logger.Endpoints.GetDefinitions;
import com.seabrief.Logger.Endpoints.GetHistoric;
import com.seabrief.Monitor.Jobs.AppCheck;
import com.seabrief.Monitor.Jobs.DiskSpace;
import com.seabrief.Monitor.Jobs.Heartbeat;
import com.seabrief.Monitor.Jobs.OperatingSystem;
import com.seabrief.Services.MQTT.MQTTClient;
import com.seabrief.Services.MQTT.MQTTClient.MQTTOptions;
import com.seabrief.Services.Tools.EnvFile;
import com.seabrief.Services.Tools.ScheduleManager;

public class App {
    public static void main(String[] args) throws Exception {
        try {
            ScheduleManager scheduler = new ScheduleManager();

            scheduler.scheduleJob(Heartbeat.class, Duration.ofMinutes(10));
            scheduler.scheduleJob(DiskSpace.class, Duration.ofHours(6));
            scheduler.scheduleJob(AppCheck.class, Duration.ofHours(10));
            scheduler.scheduleJob(OperatingSystem.class, Duration.ofHours(10));

            MQTTOptions mqttOptions = new MQTTOptions()
                    .setAddress(EnvFile.get("MQTT_ADDRESS"))
                    .setClient("MonoEdge")
                    .setPoolSize(4);

            MQTTClient.getInstance().connect(mqttOptions);

            MQTTClient.getInstance()
                    .subscribe("local/health")
                    .subscribe("external/log/+/req")
                    .subscribe("external/log/+/+/req");

            MQTTClient.getInstance()
                    .addMessageReceiver("external/log/bounds/req", new GetBounds())
                    .addMessageReceiver("external/log/defs/req", new GetDefinitions())
                    .addMessageReceiver("external/log/database/req", new GetDatabases())
                    .addMessageReceiver("external/log/hist/.+/req", new GetHistoric());
        } catch (Exception ex) {
            // TODO: print trace for debugging
        } finally {
            // TODO: Clean up in event of crash
        }
    }
}
