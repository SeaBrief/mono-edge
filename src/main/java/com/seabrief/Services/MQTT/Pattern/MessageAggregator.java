package com.seabrief.Services.MQTT.Pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.paho.mqttv5.common.MqttMessage;

public class MessageAggregator {
    private final HashMap<String, List<IMessageReceiver>> handlers = new HashMap<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public void subscribe(String topic, IMessageReceiver handler) {
        synchronized (handlers) {
            if (handlers.containsKey(topic)) {
                handlers.get(topic).add(handler);
            } else {
                handlers.put(topic, new ArrayList<IMessageReceiver>(Arrays.asList(handler)));
            }
        }
    }

    public void unsubscribe(String topic, IMessageReceiver handler) {
        synchronized (handlers) {
            if (handlers.containsKey(topic)) {
                handlers.get(topic).remove(handler);
            }
        }
    }

    public void publishAsync(String topic, MqttMessage message) {
        for (final IMessageReceiver handler : getMatchingReceivers(topic)) {
            executorService.submit(() -> handler.onMessageReceived(topic, message));
        }
    }

    public void close() {
        executorService.shutdown();
    }

    private List<IMessageReceiver> getMatchingReceivers(String topic) {
        List<IMessageReceiver> receivers = new ArrayList<>();

        if (handlers.containsKey(topic)) {
            receivers.addAll(handlers.get(topic));
        }

        for (Map.Entry<String, List<IMessageReceiver>> entry : handlers.entrySet()) {
            String regex = entry.getKey();
            if (isRegex(regex) && Pattern.matches(regex, topic) && (regex.contains("#") || regex.contains("+"))) {
                receivers.addAll(entry.getValue());
            }
        }

        return receivers;
    }

    private boolean isRegex(String str) {
        try {
            Pattern.compile(str);
            return true;
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }
}
