package event_app.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;

@Repository
public class EventRepository {

    private final JdbcTemplate jdbcTemplate;

    public EventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByEventId(int eventId) {
        String sql =
                "SELECT COUNT(*) " +
                "FROM event " +
                "WHERE event_id = ?";

        Integer count = jdbcTemplate.queryForObject(
            sql, 
            Integer.class,
            eventId
        );

        return count != null && count > 0;

    }

    public Timestamp findEventDatetime(Integer eventId) {

        String sql =
                "SELECT event_datetime " +
                "FROM event " +
                "WHERE event_id = ?";

        return jdbcTemplate.queryForObject(
            sql, 
            Timestamp.class,
            eventId
        );
    }

    public Integer getCapacityForUpdate(Integer eventId) {

        String sql =
            "SELECT capacity " +
            "FROM event " +
            "WHERE event_id = ? " +
            "FOR UPDATE";

        return jdbcTemplate.queryForObject(
            sql, Integer.class, eventId
        );
    }
}