package com.seabrief;

import java.time.Duration;

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

            // MQTTClient.getInstance()
            // .addMessageReceiver("local/health", new Healthcheck());
        } catch (Exception ex) {

        } finally {
            // TODO: Clean up in event of crash
        }
    }
}
