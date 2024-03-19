package com.seabrief.Services.MQTT.Pool;

import java.util.concurrent.CompletableFuture;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import com.seabrief.Services.MQTT.MQTTClient.MQTTOptions;
import com.seabrief.Services.Tools.Logger;


public class MQTTSimpleClient {
    private MqttAsyncClient client;
    private boolean connected;
    private CompletableFuture<Boolean> started;

    public MQTTSimpleClient(MQTTOptions options) throws Exception {
        connected = false;

        started = new CompletableFuture<>();

        client = new MqttAsyncClient(options.getAddress(), options.getClient(), null);

        client.setCallback(getCallbacks());

        MqttConnectionOptions settings = new MqttConnectionOptions();
        settings.setCleanStart(true);
        settings.setAutomaticReconnect(true);
        settings.setAutomaticReconnectDelay(120, 240);

        client.connect(settings).waitForCompletion(10000);
    }

    public void publish(String topic, MqttMessage message) throws MqttException {
        client.publish(topic, message);
    }

    public boolean isConnected() {
        return connected;
    }

    public CompletableFuture<Boolean> isStarted() {
        return started;
    }

    public MqttCallback getCallbacks() {
        return new MqttCallback() {
            public void disconnected(MqttDisconnectResponse disconnectResponse) {

                Logger.error(
                        "MQTT " + client.getClientId() + " disconnected: \n" + disconnectResponse.getReasonString());
                connected = false;
            }

            public void mqttErrorOccurred(MqttException exception) {
                Logger.error("MQTT " + client.getClientId() + " error: \n" + exception.toString());
                started.complete(false);
            }

            public void connectComplete(boolean reconnect, String serverURI) {
                String connectionType = reconnect ? "Reconnected" : "Initial Connection";
                Logger.log("MQTT " + client.getClientId() + " connected: " + connectionType);
                connected = true;
                started.complete(true);
            }

            public void deliveryComplete(IMqttToken token) {
            }

            public void messageArrived(String topic, MqttMessage message) throws Exception {
            }

            public void authPacketArrived(int reasonCode, MqttProperties properties) {
            }
        };
    }
}
