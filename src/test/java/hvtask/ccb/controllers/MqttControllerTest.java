package hvtask.ccb.controllers;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import hvtask.ccb.models.Broker;
import hvtask.ccb.models.BrokerPutRequest;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.core.Flowable;
import jakarta.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class MqttControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(MqttControllerTest.class);
    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    Jdbi jdbi;

    @Value("${mqtt.client.server-uri}")
    private String serverUri;

    @BeforeEach
    void setUp() {
        jdbi.useHandle(handler -> handler.execute("DELETE FROM broker"));
    }

    @Test
    void testPutBroker() {
        var newBroker = new BrokerPutRequest("hostLocalhost", 65423);
        var expectedCreatedBroker = new Broker("testBroker", "hostLocalhost", 65423);
        var result = client.toBlocking().exchange(HttpRequest.PUT("/mqtt/testBroker", newBroker), Broker.class);
        Optional<Broker> persistedBroker = result.getBody(Broker.class);

        assertTrue(persistedBroker.isPresent());
        Assertions.assertEquals(expectedCreatedBroker, persistedBroker.get());
    }
    @Test
    void testPutBrokerDetectDuplicates() {
        var newBroker = new BrokerPutRequest("hostLocalhost", 65423);
        var expectedCreatedBroker = new Broker("testBroker", "hostLocalhost", 65423);
        client.toBlocking().exchange(HttpRequest.PUT("/mqtt/testBroker", newBroker), Broker.class);

        var httpClientResponseException =
                Assertions.assertThrowsExactly(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.PUT("/mqtt/testBroker", newBroker), Broker.class);
        });

        Assertions.assertEquals(400, httpClientResponseException.getStatus().getCode());
    }

    @Test
    void testDeleteBroker() {
        var newBroker = new BrokerPutRequest("hostLocalhost", 65423);
        client.toBlocking().exchange(HttpRequest.PUT("/mqtt/testBroker", newBroker), Broker.class);
        HttpResponse<Object> exchange = client.toBlocking().exchange(HttpRequest.DELETE("/mqtt/testBroker"));

        Assertions.assertEquals(204, exchange.getStatus().getCode());
    }

    @Test
    void testSenMessage() throws InterruptedException {
        URI uriServer = URI.create(this.serverUri);
        var newBroker = new BrokerPutRequest(uriServer.getHost(), uriServer.getPort());
        client.toBlocking().exchange(HttpRequest.PUT("/mqtt/localBroker", newBroker), Broker.class);
        // Send message
        var message = "Test message for TopicOne";
        var logMessages = new ArrayList<String>();
        subscribe(uriServer.getHost(), uriServer.getPort(), "topicOne", logMessages, (ack) -> {
            var httpResponse = client.toBlocking().exchange(HttpRequest.POST("/mqtt/localBroker/send/topicOne",
                            message),
                    Object.class);
            assertEquals(200, httpResponse.getStatus().getCode());
        });
        // Wait until logMessages is not empty.
        int maxSleep = 0;
        int MAX_WAIT_COUNTER = 100;
        while (logMessages.isEmpty() && maxSleep < MAX_WAIT_COUNTER) {
            Thread.sleep(100);
            maxSleep++;
        }
        assertEquals(1, logMessages.size());
        assertEquals(message, logMessages.get(0));
    }

    private static void subscribe(String hostname, int port, String topic,
                                  List<String> messagesLog,
                                  Consumer<String> callBack) {
        Mqtt3AsyncClient client = MqttClient.builder()
                .useMqttVersion3()
                .identifier("test-id")
                .serverHost(hostname)
                .serverPort(port)
                .buildAsync();

        client.connect()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        logger.error(throwable.getMessage());
                    } else {
                        // setup subscribes or start publishing
                        logger.info("Connected OK ==>> " + connAck);
                        client.subscribeWith()
                                .topicFilter(topic)
                                .callback(publish -> {
                                    // Process the received message
                                    // This is a permanent reactive callback.
                                    logger.info(" NEW MESSAGE: " + new String(publish.getPayloadAsBytes()));
                                    messagesLog.add(new String(publish.getPayloadAsBytes()));
                                })
                                .send()
                                .whenComplete((subAck, throwable1) -> {
                                    if (throwable1 != null) {
                                        logger.error(" Failed in my subscription attempt.");
                                    } else {
                                        // Handle successful subscription, e.g. logging or incrementing a metric
                                        logger.info(" Subscribed to the topic: " + topic);
                                        callBack.accept(subAck.toString());
                                    }
                                });
                    }
                });
    }
}
