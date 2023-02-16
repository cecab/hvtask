package ccb.pgames.dao;

import ccb.pgames.dao.models.QuestionDB;
import jakarta.inject.Named;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.postgres.PostgresPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Named("default")
public class JdbiCustomizer implements io.micronaut.configuration.jdbi.JdbiCustomizer {
    @Override
    public void customize(Jdbi jdbi) {
        jdbi.installPlugin(new PostgresPlugin()).registerRowMapper(new QuestionMapper());
    }
}

class QuestionMapper implements RowMapper<QuestionDB> {
    @Override
    public QuestionDB map(ResultSet rs, StatementContext ctx) throws SQLException {
        List<String> tagList = rs.getArray("tags") != null ?
                Arrays.asList((String[]) rs.getArray("tags").getArray()) : null;
        QuestionDB q = new QuestionDB();
        q.setId(rs.getInt("id"));
        q.setTitle(rs.getString("title"));
        q.setTags(tagList);
        q.setAnswered(rs.getBoolean("is_answered"));
        q.setViewCount(rs.getInt("view_count"));
        q.setAnswerCount(rs.getInt("answer_count"));
        q.setCreation_date(rs.getLong("creation_date"));
        q.setUserId(rs.getInt("user_id"));
        return q;
    }
}
