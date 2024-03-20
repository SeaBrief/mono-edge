package com.seabrief.Logger.Endpoints;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.paho.mqttv5.common.MqttMessage;

import com.seabrief.Logger.Parser.Parser;
import com.seabrief.Services.Compression.Delta2;
import com.seabrief.Services.MQTT.Pattern.IMessageReceiver;
import com.seabrief.Services.MQTT.Pattern.MessageBuilder;
import com.seabrief.Services.Models.LogData.RangeRequest;
import com.seabrief.Services.Models.LogData.TimeseriesResponse;
import com.seabrief.Services.Tools.Logger;
import com.seabrief.Services.Tools.MQTTUtils;

public class GetHistoric implements IMessageReceiver {
    @Override
    public CompletableFuture<Void> onMessageReceived(String topic, MqttMessage message) {
        try {
            String route = MQTTUtils.getSignalRoute(topic);

            RangeRequest request = RangeRequest.parseFrom(message.getPayload());

            HashMap<Long, Double> historic = Parser.getInstance().getRange(route, request.getFrom(),request.getTo());

            List<Entry<Long, Double>> sorted = historic.entrySet().stream()
                    .sorted((a, b) -> a.getKey().compareTo(b.getKey())).collect(Collectors.toList());
            long[] timestamps = sorted.stream().mapToLong(Entry::getKey).toArray();
            double[] values = sorted.stream().mapToDouble(Entry::getValue).toArray();

            long[] encoded = Delta2.encode(timestamps);

            TimeseriesResponse.Builder builder = TimeseriesResponse.newBuilder();

            for (int i = 0; i < encoded.length; i++) {
                builder.addTimes(encoded[i]);
                builder.addValues(values[i]);
            }

            TimeseriesResponse payload = builder.build();

            new MessageBuilder()
                    .withTopic(MQTTUtils.getResponseTopic(topic))
                    .withCorrelation(MQTTUtils.getCorrelationData(message))
                    .withPayload(payload.toByteArray())
                    .publish();

            Logger.log("Handled hist Request");
        } catch (Exception ex) {
            Logger.error("Failed to handle hist request");
            ex.printStackTrace();
        }

        return CompletableFuture.completedFuture(null);
    }

}
