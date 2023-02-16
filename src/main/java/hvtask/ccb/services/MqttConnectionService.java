package hvtask.ccb.services;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import hvtask.ccb.models.Broker;
import hvtask.ccb.storage.BrokerDao;
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
    private final Map<String, Mqtt3BlockingClient> publishersMap;

    public MqttConnectionService(Jdbi jdbi) {
        this.jdbi = jdbi;
        publishersMap = new HashMap<>();
    }

    public Optional<Publisher> lookupPublisher(String brokerName) {
        return jdbi.withExtension(BrokerDao.class, dao -> dao.findByName(brokerName))
                .flatMap(brokerObj -> {
                    // lookup in our local Map
                    String identifier = brokerObj.getName() + "-" + UUID.randomUUID();
                    return Optional.ofNullable(publishersMap.get(brokerName))
                            .map(localClient -> {
                                // Check local client state. if they are not CONNECTED, they are discarded.
                                if (localClient.getState() != (MqttClientState.CONNECTED)) {
                                    return publishersMap.put(brokerName, createAndConnectClient(brokerObj, identifier));
                                }
                                return localClient;
                            }).or(() -> {
                                // Build a new client from scratch
                                return Optional.of(createAndConnectClient(brokerObj, identifier));
                            })
                            .map(Publisher::new);
                });
    }

    private static Mqtt3BlockingClient createAndConnectClient(Broker brokerObj, String identifier) {
        Mqtt3BlockingClient client = MqttClient.builder()
                .identifier(identifier)
                .serverHost(brokerObj.getHostname())
                .serverPort(brokerObj.getPort())
                .useMqttVersion3()
                .build()
                .toBlocking();
        logger.info("Connecting to {}:{} as ID: {} ", brokerObj.getHostname(), brokerObj.getPort(), identifier);
        //TODO handle timeout
        client.connect();
        return client;
    }
}
