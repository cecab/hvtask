package hvtask.ccb.storage;

import hvtask.ccb.models.Broker;
import jakarta.inject.Named;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.postgres.PostgresPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;

@Named("default")
public class JdbiCustomizer implements io.micronaut.configuration.jdbi.JdbiCustomizer {
    @Override
    public void customize(Jdbi jdbi) {
        jdbi.installPlugin(new PostgresPlugin()).registerRowMapper(new BrokerMapper());
    }
}

class BrokerMapper implements RowMapper<Broker> {
    @Override
    public Broker map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Broker(rs.getString("name"), rs.getString("hostname"), rs.getInt("port"));
    }
}
