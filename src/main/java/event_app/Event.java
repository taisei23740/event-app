package event_app;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Event {
    private final int eventId;
    private final int capacity;
    private final Timestamp eventDatetime;

    public Event(int eventId, int capacity, Timestamp eventDatetime) {
        this.eventId = eventId;
        this.capacity = capacity;
        this.eventDatetime = eventDatetime;
    }

    public int getEventId() {
        return eventId;
    }

    public int getCapacity() {
        return capacity;
    }

    public Timestamp getEventDatetime() {
        return eventDatetime;
    }

    public boolean isRegistrationOpen(LocalDateTime now) {
        LocalDateTime deadline = eventDatetime
            .toLocalDateTime()
            .toLocalDate().atStartOfDay();

        return now.isBefore(deadline);
    }

}
