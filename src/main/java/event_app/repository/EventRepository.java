package event_app.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import event_app.Event;

@Repository
public class EventRepository {

    private final JdbcTemplate jdbcTemplate;

    public EventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Event> findById(int eventId) {

        String sql = 
            "SELECT event_id, capacity, event_datetime " +
            "FROM event " +
            "WHERE event_id = ? " +
            "FOR UPDATE";
        
            List<Event> events = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new Event(
                    rs.getInt("event_id"),
                    rs.getInt("capacity"),
                    rs.getTimestamp("event_datetime")
            ),
            eventId
            );

        return events.stream().findFirst();
    }

}