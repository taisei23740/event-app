
package event_app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class EventTest {

    @Test
    void 開催日前日の23時59分は申し込み可能() {
        Event event = new Event(
            1,
            10,
            Timestamp.valueOf(
                LocalDateTime.of(2026, 11, 1, 10, 0)
            )
        );

        assertTrue(
            event.isRegistrationOpen(
                LocalDateTime.of(2026, 10, 31, 23, 59)
            )
        );
    }

    @Test
    void 開催日の0時0分は申し込み不可() {
        Event event = new Event(
            1,
            10,
            Timestamp.valueOf(
                LocalDateTime.of(2026, 11, 1, 10, 0)
            )
        );

        assertFalse(
            event.isRegistrationOpen(
                LocalDateTime.of(2026, 11, 1, 0, 0)
            )
        );
    }
}
