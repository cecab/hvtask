package hvtask.ccb.controllers;

import hvtask.ccb.models.Broker;
import hvtask.ccb.models.BrokerPutRequest;
import hvtask.ccb.services.Publisher;
import hvtask.ccb.storage.BrokerDao;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import org.jdbi.v3.core.Jdbi;

import java.util.Map;
import java.util.Optional;

@Controller("/mqtt")
public class MqttController {
    private final Jdbi jdbi;
    private final Publisher publisher;

    public MqttController(Jdbi jdbi, Publisher publisher) {
        this.jdbi = jdbi;
        this.publisher = publisher;
    }

    @Put("/{brokername}")
    public HttpResponse<Object> addBroker(@PathVariable("brokername") String brokername,
                                          @Body BrokerPutRequest newBroker) {
        var brokerDB = new Broker(brokername, newBroker.getHostname(), newBroker.getPort());
        Optional<Broker> previousSavedBroker = jdbi.withExtension(BrokerDao.class, dao -> dao.findByName(brokername));
        return previousSavedBroker.map(prevBroker -> HttpResponse.<Object>badRequest(Map.of("error",
                        String.format("Broker with name '%s' already exists, delete it before sending a PUT request.",
                                brokername))))
                .orElseGet(() -> {
                    jdbi.withExtension(BrokerDao.class, dao -> dao.insert(brokerDB));
                    return HttpResponse.ok(brokerDB);
                });
    }

    @Get("/{brokername}")
    public Optional<Broker> getBroker(@PathVariable("brokername") String brokername) {
        return jdbi.withExtension(BrokerDao.class, dao -> dao.findByName(brokername));
    }

    @Delete("/{brokername}")
    public HttpResponse<Map<String, String>> deleteByName(@PathVariable String brokername) {
        int deleted = jdbi.withExtension(BrokerDao.class, dao -> dao.deleteByName(brokername));
        if (deleted == 0) {
            return HttpResponse.status(HttpStatus.valueOf(202))
                    .body(Map.of("error", "Broker name " + brokername + " was not removed"));
        }
        return HttpResponse.status(HttpStatus.valueOf(204));
    }

    @Post("/{brokername}/send/{topicname}")
    public HttpResponse<Object> sendMessage(
            @PathVariable("brokername") String brokername,
            @PathVariable("topicname") String topicname,
            @Body String message
    ) {
        publisher.publish(topicname,message);
        return HttpResponse.ok();
    }

}
