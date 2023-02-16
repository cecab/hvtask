package hvtask.ccb.storage;

import hvtask.ccb.models.Broker;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface BrokerDao {
    @SqlQuery("SELECT * from broker")
    List<Broker> findAll();

    @SqlQuery("SELECT * from broker where name=:name")
    Optional<Broker> findById(@Bind("name") String name);


    @SqlUpdate("INSERT INTO broker(name,hostname,port) " + "values(:name,:hostname, :port)")
    int insert(@BindBean Broker broker);

    @SqlUpdate("DELETE FROM broker")
    int clearAll();

    @SqlUpdate("DELETE from broker where name=:name")
    int deleteByName(@Bind("name") String name);
}
