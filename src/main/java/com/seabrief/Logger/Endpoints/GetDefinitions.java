package com.seabrief.Logger.Endpoints;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.eclipse.paho.mqttv5.common.MqttMessage;

import com.seabrief.Logger.Models.SignalMetadata;
import com.seabrief.Logger.Parser.Parser;
import com.seabrief.Services.MQTT.Pattern.IMessageReceiver;
import com.seabrief.Services.MQTT.Pattern.MessageBuilder;
import com.seabrief.Services.Models.LogData.Signal;
import com.seabrief.Services.Models.LogData.SignalsResponse;
import com.seabrief.Services.Tools.Logger;
import com.seabrief.Services.Tools.MQTTUtils;

public class GetDefinitions implements IMessageReceiver {
    @Override
    public CompletableFuture<Void> onMessageReceived(String topic, MqttMessage message) {
        try {

            ArrayList<SignalMetadata> signals = Parser.getInstance().getSignals();

            SignalsResponse.Builder builder = SignalsResponse.newBuilder();

            for (SignalMetadata metadata : signals) {
                Signal.Builder signalBuilder = Signal.newBuilder();

                if (metadata.getName() != null) {
                    signalBuilder.setName(metadata.getName());
                }

                if (metadata.getRoute() != null) {
                    signalBuilder.setPath(metadata.getRoute());
                }

                if (metadata.getDescription() != null && metadata.getDescription().length() != 0) {
                    signalBuilder.setDescription(metadata.getDescription());
                } else {
                    signalBuilder.setDescription("n/a");
                }

                builder.addSignals(signalBuilder.build());

            }

            SignalsResponse payload = builder.build();

            new MessageBuilder()
                    .withTopic(MQTTUtils.getResponseTopic(topic))
                    .withCorrelation(MQTTUtils.getCorrelationData(message))
                    .withPayload(payload.toByteArray())
                    .publish();

            Logger.log("Handled defs Request");
        } catch (Exception ex) {
            Logger.error("Failed to handle defs request");
            ex.printStackTrace();
        }

        return CompletableFuture.completedFuture(null);
    }

}
