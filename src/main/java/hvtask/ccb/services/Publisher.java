package hvtask.ccb.services;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Singleton
public class Publisher {
    private static final Logger logger = LoggerFactory.getLogger(Publisher.class);
    Mqtt3BlockingClient client;
    public Publisher() {
        String identifier = UUID.randomUUID().toString();
        client = MqttClient.builder()
                .identifier(identifier)
                .serverHost("localhost")
                .useMqttVersion3()
                .build()
                .toBlocking();
        logger.info("Connecting as ID: " + identifier);
        client.connect();
    }

    public void publish(String topic, Object message){
        logger.info("Publishing message: " + message);
        client.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(message.toString().getBytes())
                .send();

        logger.info("Done publishing.");
        // TODO: Connections handling missing, only client with 'CONNECTED' state should be used.
        // client.disconnect();
    }


}
