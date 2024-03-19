package com.seabrief.Services.Tools;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.mqttv5.common.MqttMessage;

public class MQTTUtils {
    public static String getSignalRoute(String topic) {
        String[] parts = topic.split("/");
        return parts[parts.length - 2];
    }

    public static String getSystem(String topic) {
        return topic.split("/")[2];
    }

    public static String getResponseTopic(String topic) {
        return topic.replace("req", "res");
    }

    public static String getRequestTopic(String topic) {
        return topic.replace("res", "req");
    }

    public static String getEdgeTopic(String topic) {
        return topic.replace("cloud", "edge");
    }

    public static String getCloudTopic(String topic) {
        return topic.replace("edge", "cloud");
    }

    public static String getCorrelationData(MqttMessage message) {
        return new String(message.getProperties().getCorrelationData(), StandardCharsets.UTF_8);
    }
}
