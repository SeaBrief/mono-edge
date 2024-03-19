package com.seabrief.Services.MQTT.Pool;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.eclipse.paho.mqttv5.common.MqttMessage;

import com.seabrief.Services.MQTT.MQTTClient.MQTTOptions;
import com.seabrief.Services.Tools.Logger;

public class MQTTPool {
    private int pool_size;
    private CompletableFuture<Boolean> started;
    private ArrayList<MQTTSimpleClient> clients;
    private int clientCursor = 0;

    public MQTTPool(MQTTOptions options) throws Exception {
        Logger.log("Starting MQTT Client Pool...");
        this.pool_size = options.getPoolSize();
        started = new CompletableFuture<>();
        clients = new ArrayList<>();

        loadClients(options);
    }

    public CompletableFuture<Boolean> isStarted() {
        return started;
    }

    public void publish(String topic, MqttMessage message) throws Exception {
        int count = 0;

        do {
            if (count >= pool_size) {
                throw new Exception("Failed to publish because no clients are connected!");
            }

            count++;
            clientCursor = moveCursor(clientCursor);
        } while (!clients.get(clientCursor).isConnected());

        clients.get(clientCursor).publish(topic, message);
    }

    private int moveCursor(int cursor) {
        return (cursor + 1) % pool_size;
    }

    private void loadClients(MQTTOptions options) throws Exception {
        ArrayList<CompletableFuture<Boolean>> status = new ArrayList<>();

        for (int i = 0; i <= pool_size; i++) {
            MQTTOptions clientOptions = new MQTTOptions()
                    .setAddress(options.getAddress())
                    .setClient(options.getClient() + "-pool-" + i);

            MQTTSimpleClient client = new MQTTSimpleClient(clientOptions);
            status.add(client.isStarted());
            clients.add(client);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(status.toArray(new CompletableFuture[status.size()]));

        allOf.thenAcceptAsync(v -> {
            boolean result = status.stream()
                    .map(CompletableFuture::join)
                    .allMatch(Boolean::booleanValue);
            started.complete(result);
        });
    }
}
