package com.seabrief.Logger.Endpoints;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.paho.mqttv5.common.MqttMessage;

import com.seabrief.Logger.Parser.Parser;
import com.seabrief.Models.LogData.DatabaseFile;
import com.seabrief.Models.LogData.DatabaseResponse;
import com.seabrief.Services.MQTT.Pattern.IMessageReceiver;
import com.seabrief.Services.MQTT.Pattern.MessageBuilder;
import com.seabrief.Services.Tools.Logger;
import com.seabrief.Services.Tools.MQTTUtils;

public class GetDatabases implements IMessageReceiver {

    @Override
    public CompletableFuture<Void> onMessageReceived(String topic, MqttMessage message) {
        try {
            HashMap<String, Long> sizes = new HashMap<>();
            DatabaseResponse.Builder payload = DatabaseResponse.newBuilder();

            List<String> indexFiles = Parser.getInstance().getDatabases();

            for (String index : indexFiles) {
                long size = getFileSize(index);

                for (String data : getDatabasePartitions(index)) {
                    size += getFileSize(data);
                }

                payload.addDatabases(DatabaseFile.newBuilder().setName(index).setSize(size).build());

                sizes.put(index, size);
            }

            new MessageBuilder()
                    .withTopic(MQTTUtils.getResponseTopic(topic))
                    .withCorrelation(MQTTUtils.getCorrelationData(message))
                    .withPayload(payload.build().toByteArray())
                    .publish();

            Logger.log("Handled database Request");
        } catch (Exception e) {
            Logger.error("Failed to handle database request");
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture(null);
    }

    private List<String> getDatabasePartitions(String filePath) {
        List<String> dataFiles = new ArrayList<>();

        File indexFile = new File(filePath);

        File parentDirectory = indexFile.getParentFile();

        String baseName = indexFile.getName().replaceFirst("[.][^.]+$", "");

        File[] files = parentDirectory
                .listFiles((dir, name) -> name.startsWith(baseName) && name.matches(baseName + "[0-9]*\\.db"));

        if (files != null) {
            for (File file : files) {
                dataFiles.add(file.getAbsolutePath());
            }
        }

        return dataFiles;
    }

    private static long getFileSize(String filePath) {
        File file = new File(filePath);

        if (file.exists() && file.isFile()) {
            return file.length();
        } else {
            Logger.error("Skipping invalid file: " + file.getAbsolutePath());
            return 0;
        }
    }

}
