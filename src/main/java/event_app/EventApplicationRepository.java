package event_app;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EventApplicationRepository {

    private final JdbcTemplate jdbcTemplate;

    public EventApplicationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByEventIdAndUserId(Integer eventId, Integer userId) {

        String sql =
                "SELECT count(*) " +
                "FROM event_application " +
                "WHERE event_id = ? AND user_id = ?";

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                eventId,
                userId
        );

        return count != null && count > 0;
    }

    public boolean isFull(Integer eventId) {

        String application_sql =
                "SELECT count(*) " +
                "FROM event_application " +
                "WHERE event_id = ?";

        Integer count = jdbcTemplate.queryForObject(
                application_sql,
                Integer.class,
                eventId
        );

         String event_sql =
                "SELECT capacity " +
                "FROM event " +
                "WHERE event_id = ?";

        Integer capacity = jdbcTemplate.queryForObject(
                event_sql,
                Integer.class,
                eventId
        );

        return count >= capacity; 
    }

    public void add_apply(Integer eventId,Integer userId){

        String application_sql =
                "INSERT INTO event_application(event_id,user_id) " +
                "VALUES (?,?)";

        jdbcTemplate.update(
                application_sql,
                eventId,
                userId
        );
    }
}