package event_app.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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

    public boolean isRegistrationOpen(Integer eventId) {

        String sql =
                "SELECT event_datetime " +
                "FROM event " +
                "WHERE event_id = ?";

        Timestamp eventDatetime = jdbcTemplate.queryForObject(
            sql, 
            Timestamp.class,
            eventId
        );

        LocalDateTime deadline =
                eventDatetime.toLocalDateTime()
                        .toLocalDate()
                        .minusDays(1)
                        .atTime(23,59,59);

        LocalDateTime now = LocalDateTime.now();

        return !now.isAfter(deadline);
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