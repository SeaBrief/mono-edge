package com.seabrief.Logger.Endpoints;

import java.util.concurrent.CompletableFuture;

import org.eclipse.paho.mqttv5.common.MqttMessage;

import com.seabrief.Logger.Parser.Parser;
import com.seabrief.Models.LogData.BoundsResponse;
import com.seabrief.Services.MQTT.Pattern.IMessageReceiver;
import com.seabrief.Services.MQTT.Pattern.MessageBuilder;
import com.seabrief.Services.Tools.Logger;
import com.seabrief.Services.Tools.MQTTUtils;

public class GetBounds implements IMessageReceiver {
    @Override
    public CompletableFuture<Void> onMessageReceived(String topic, MqttMessage message) {
        try {
            Long[] bounds = Parser.getInstance().getBounds();

            BoundsResponse payload = BoundsResponse.newBuilder()
                    .setFrom(bounds[0])
                    .setTo(bounds[1])
                    .build();

            new MessageBuilder()
                    .withTopic(MQTTUtils.getResponseTopic(topic))
                    .withCorrelation(MQTTUtils.getCorrelationData(message))
                    .withPayload(payload.toByteArray())
                    .publish();

            Logger.log("Handled bounds Request");
        } catch (Exception ex) {
            Logger.error("Failed to handle bounds request");
            ex.printStackTrace();
        }

        return CompletableFuture.completedFuture(null);
    }

}
