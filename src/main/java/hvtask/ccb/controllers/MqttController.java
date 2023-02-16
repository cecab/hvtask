package hvtask.ccb.controllers;

import hvtask.ccb.models.Broker;
import hvtask.ccb.models.BrokerPutRequest;
import hvtask.ccb.storage.BrokerDao;
import io.micronaut.http.annotation.*;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

@Controller("/mqtt")
public class MqttController {
    private final Jdbi jdbi;

    public MqttController(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Put("/{brokername}")
    public Broker addBroker(@PathVariable("brokername") String brokername, @Body BrokerPutRequest newBroker) {
        var brokerDB = new Broker(brokername, newBroker.getHostname(), newBroker.getPort());
        jdbi.withExtension(BrokerDao.class, dao -> dao.insert(brokerDB));
        return brokerDB;
    }

    @Get("/{brokername}")
    public Optional<Broker> getBroker(@PathVariable("brokername") String brokername) {
        return jdbi.withExtension(BrokerDao.class, dao -> dao.findByName(brokername));
    }
}
