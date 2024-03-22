package com.seabrief.Services.MQTT;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttActionListener;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import com.seabrief.Services.MQTT.Pattern.IMessageReceiver;
import com.seabrief.Services.MQTT.Pattern.MessageAggregator;
import com.seabrief.Services.MQTT.Pool.MQTTPool;
import com.seabrief.Services.Tools.Logger;

/**
 * MQTT Client, with Pool
 * <p>
 * Wrapper for paho MQTTv5 Client
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * 
 * MQTTClient client = MQTTClient.getInstance()
 * 
 * }
 * </pre>
 */
public class MQTTClient {
    private static MQTTClient instance = null;
    private MqttAsyncClient client = null;
    private MQTTPool pool = null;
    private CompletableFuture<Boolean> started = new CompletableFuture<>();
    private MessageAggregator aggregator = new MessageAggregator();

    private MQTTClient() {
        try {
            this.connect(new MQTTOptions()
                    .setAddress("tcp://127.0.0.1:1883")
                    .setClient("MonoEdge")
                    .setPoolSize(4));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static MQTTClient getInstance() {
        if (instance == null) {
            synchronized (MQTTClient.class) {
                if (instance == null) {
                    instance = new MQTTClient();
                }
            }
        }
        return instance;
    }

    public void publish(String topic, MqttMessage message) throws Exception {
        if (pool == null) {
            client.publish(topic, message);
        } else {
            pool.publish(topic, message);
        }
    }

    public void connect(MQTTOptions options)
            throws Exception {
        ArrayList<CompletableFuture<Boolean>> futures = new ArrayList<>();

        client = new MqttAsyncClient(options.getAddress(), options.getClient(), new MemoryPersistence());
        client.setCallback(callbacks());

        if (options.getPoolSize() != null) {
            pool = new MQTTPool(options);
            futures.add(pool.isStarted());
        }

        futures.add(started);

        MqttConnectionOptions settings = new MqttConnectionOptions();
        settings.setAutomaticReconnect(true);
        settings.setAutomaticReconnectDelay(60, 120);

        client.connect(settings).waitForCompletion(10000);
        ;

        CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));

        all.get();
    }

    public void disconnect() throws MqttException {
        if (client != null && client.isConnected()) {
            client.disconnect().waitForCompletion();
        }
    }

    public MQTTClient subscribe(String topic) throws MqttException {
        client.subscribe(topic, 0).setActionCallback(new MqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Logger.error("Failed to subscribed to topic: " + topic);
            }
        });
        return this;
    }

    public MQTTClient addMessageReceiver(String topic, IMessageReceiver receiver) {
        aggregator.subscribe(topic, receiver);
        return this;
    }

    public MQTTClient removeMessageReceiver(String topic, IMessageReceiver receiver) {
        aggregator.unsubscribe(topic, receiver);
        return this;
    }

    public void close() {
        aggregator.close();
    }

    public MqttCallback callbacks() {
        return new MqttCallback() {

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                aggregator.publishAsync(topic, message);
            }

            @Override
            public void disconnected(MqttDisconnectResponse disconnectResponse) {
                Logger.error("MQTT Receiver disconnected: \n" + disconnectResponse.getReasonString());
            }

            @Override
            public void mqttErrorOccurred(MqttException exception) {
                Logger.error("MQTT Receiver error: \n" + exception.toString());
                started.complete(false);
            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                String connectionType = reconnect ? "Reconnected" : "Initial Connection";
                Logger.log("MQTT Receiver connected: " + connectionType);
                started.complete(true);
            }

            @Override
            public void deliveryComplete(IMqttToken token) {
            }

            @Override
            public void authPacketArrived(int reasonCode, MqttProperties properties) {
            }
        };
    }

    public static class MQTTOptions {
        private String address;
        private String client;
        private Integer poolSize;

        public MQTTOptions() {
            address = null;
            client = null;
            poolSize = null;
        }

        public MQTTOptions setAddress(String address) {
            this.address = address;
            return this;
        }

        public MQTTOptions setClient(String client) {
            this.client = client;
            return this;
        }

        public MQTTOptions setPoolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        public String getAddress() {
            return this.address;
        }

        public String getClient() {
            return this.client;
        }

        public Integer getPoolSize() {
            return this.poolSize;
        }
    }
}