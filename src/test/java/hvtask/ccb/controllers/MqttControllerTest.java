package hvtask.ccb.controllers;

import hvtask.ccb.models.Broker;
import hvtask.ccb.models.BrokerPutRequest;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

@MicronautTest
class MqttControllerTest {
    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    Jdbi jdbi;

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

        Assertions.assertTrue(persistedBroker.isPresent());
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

}