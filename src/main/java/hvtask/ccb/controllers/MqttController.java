package hvtask.ccb.controllers;

import hvtask.ccb.models.Broker;
import hvtask.ccb.models.BrokerPutRequest;
import hvtask.ccb.storage.BrokerDao;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Put;
import org.jdbi.v3.core.Jdbi;

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
}
