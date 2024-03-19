package com.seabrief.Services.MQTT.Pattern;

import java.util.concurrent.CompletableFuture;
import org.eclipse.paho.mqttv5.common.MqttMessage;

public interface IMessageReceiver {
    CompletableFuture<Void> onMessageReceived(String topic, MqttMessage message);
}
