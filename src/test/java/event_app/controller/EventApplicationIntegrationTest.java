
package event_app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EventApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void ログイン済みユーザーが申し込むとDBに登録される() throws Exception {
        String suffix = UUID.randomUUID().toString();

        Integer userId = null;
        Integer eventId = null;

        try {
            userId = jdbcTemplate.queryForObject(
                "INSERT INTO users (name, email, password) " +
                "VALUES (?, ?, ?) RETURNING user_id",
                Integer.class,
                "統合テストユーザー",
                "integration-" + suffix + "@example.com",
                "test-password"
            );

            Timestamp eventDatetime = Timestamp.valueOf(
                LocalDateTime.now(ZoneId.of("Asia/Tokyo"))
                    .plusDays(30)
                    .withHour(10)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0)
            );

            eventId = jdbcTemplate.queryForObject(
                "INSERT INTO event " +
                "(event_name, capacity, event_datetime, location, organizer_id) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING event_id",
                Integer.class,
                "統合テストイベント",
                10,
                eventDatetime,
                "テスト会場",
                userId
            );

            mockMvc.perform(
                post("/event")
                    .param("eventId", eventId.toString())
                    .sessionAttr("userId", userId)
            )
            .andExpect(status().isOk())
            .andExpect(view().name("application-completed"));

            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM event_application " +
                "WHERE event_id = ? AND user_id = ?",
                Integer.class,
                eventId,
                userId
            );

            assertEquals(1, count);

        } finally {
            if (eventId != null) {
                jdbcTemplate.update(
                    "DELETE FROM event_application WHERE event_id = ?",
                    eventId
                );

                jdbcTemplate.update(
                    "DELETE FROM event WHERE event_id = ?",
                    eventId
                );
            }

            if (userId != null) {
                jdbcTemplate.update(
                    "DELETE FROM users WHERE user_id = ?",
                    userId
                );
            }
        }
    }
}