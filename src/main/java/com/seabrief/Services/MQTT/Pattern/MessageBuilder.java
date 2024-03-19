package com.seabrief.Services.MQTT.Pattern;

import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import com.seabrief.Services.MQTT.MQTTClient;

public class MessageBuilder {
    private String topic;
    private byte[] payload;
    private String correlation;

    public MessageBuilder() {
    }

    public MessageBuilder withTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public MessageBuilder withCorrelation(String correlation) {
        this.correlation = correlation;
        return this;
    }

    public MessageBuilder withPayload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    private MqttMessage build() {
        MqttMessage message = new MqttMessage(payload);

        if (correlation != null) {
            MqttProperties properties = new MqttProperties();
            properties.setCorrelationData(correlation.getBytes());
            message.setProperties(properties);
            message.setQos(0);
        }

        return message;
    }

    public void publish() throws Exception {
        MQTTClient.getInstance().publish(topic, build());
    }
}