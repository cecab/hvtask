package hvtask.ccb.services;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import hvtask.ccb.models.Broker;
import hvtask.ccb.storage.BrokerDao;
import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class MqttConnectionService {
    private static final Logger logger = LoggerFactory.getLogger(MqttConnectionService.class);
    private final Jdbi jdbi;
    private final Map<String, Mqtt3BlockingClient> brokerNameToClientMap;

    public MqttConnectionService(Jdbi jdbi) {
        this.jdbi = jdbi;
        brokerNameToClientMap = new HashMap<>();
    }

    public Optional<Publisher> lookupPublisher(String brokerName) {
        return checkBorkerInDBAndBuildAClient(brokerName)
                .map(Publisher::new);
    }

    private Optional<Mqtt3BlockingClient> checkBorkerInDBAndBuildAClient(String brokerName) {
        return jdbi.withExtension(BrokerDao.class, dao -> dao.findByName(brokerName))
                .flatMap(brokerObj -> {
                    // lookup in our local Map
                    String identifier = brokerObj.getName() + "-" + UUID.randomUUID();
                    return Optional.ofNullable(brokerNameToClientMap.get(brokerName))
                            .map(localClient -> {
                                if (localClient.getState() == MqttClientState.CONNECTED) {
                                    return localClient;
                                }
                                return createAndConnectClient(brokerObj, identifier);
                            })
                            .or(() -> {
                                // Build a new client from scratch and add it to our map
                                brokerNameToClientMap.put(brokerName, createAndConnectClient(brokerObj, identifier));
                                return Optional.of(brokerNameToClientMap.get(brokerName));
                            });
                });
    }

    public Optional<Observable<String>> lookupSubscriber(
            String brokerName,
            String topic) {
        return checkBorkerInDBAndBuildAClient(brokerName)
                .map(client -> {
                    var asyncClient = client.toAsync();
                    return Observable.create(emitter -> {
                        asyncClient.subscribeWith()
                                .topicFilter(topic)
                                .callback(publish -> {
                                    emitter.onNext(new String(publish.getPayloadAsBytes()));
                                })
                                .send()
                                .whenComplete((subAck, throwable1) -> {
                                    if (throwable1 != null) {
                                        logger.error(" Failed in my subscription attempt.");
                                    } else {
                                        // Handle successful subscription, e.g. logging or incrementing a metric
                                        logger.info(" Subscribed to the topic: " + topic);
                                    }
                                });
                    });
                });
    }

    private static Mqtt3BlockingClient createAndConnectClient(Broker brokerObj, String identifier) {
        var client = MqttClient.builder()
                .identifier(identifier)
                .serverHost(brokerObj.getHostname())
                .serverPort(brokerObj.getPort())
                .useMqttVersion3()
                .build()
                .toBlocking();
        logger.info("Connecting to {}:{} as ID: {} ", brokerObj.getHostname(), brokerObj.getPort(), identifier);
        client.connect();
        return client;
    }
}
