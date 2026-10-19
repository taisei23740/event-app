
package event_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import event_app.ApplyEventResult;

@SpringBootTest
class EventApplicationConcurrencyTest {

    @Autowired
    private EventApplicationService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 定員1名の同イベントに別ユーザー2人が同時申し込みすると1人だけ成功する()
            throws Exception {

        String suffix = UUID.randomUUID().toString();

        Integer userA = null;
        Integer userB = null;
        Integer eventId = null;

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // テスト用ユーザーを2人登録する
            userA = jdbcTemplate.queryForObject(
                "INSERT INTO users (name, email, password) " +
                "VALUES (?, ?, ?) RETURNING user_id",
                Integer.class,
                "テストユーザーA",
                "test-a-" + suffix + "@example.com",
                "test-password"
            );

            userB = jdbcTemplate.queryForObject(
                "INSERT INTO users (name, email, password) " +
                "VALUES (?, ?, ?) RETURNING user_id",
                Integer.class,
                "テストユーザーB",
                "test-b-" + suffix + "@example.com",
                "test-password"
            );

            // 定員1名のイベントを登録する
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
                "同時申し込みテスト",
                1,
                eventDatetime,
                "テスト会場",
                userA
            );

            // 2つの処理を同時にスタートさせる
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);

            int targetEventId = eventId;
            int targetUserA = userA;
            int targetUserB = userB;

            Future<ApplyEventResult> futureA = executor.submit(() -> {
                ready.countDown();
                start.await();
                return service.applyEvent(targetEventId, targetUserA);
            });

            Future<ApplyEventResult> futureB = executor.submit(() -> {
                ready.countDown();
                start.await();
                return service.applyEvent(targetEventId, targetUserB);
            });

            // 2つのスレッドが準備できたら同時に開始
            assertEquals(true, ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            ApplyEventResult resultA = futureA.get(15, TimeUnit.SECONDS);
            ApplyEventResult resultB = futureB.get(15, TimeUnit.SECONDS);

            List<ApplyEventResult> results = List.of(resultA, resultB);

            // 片方だけ成功し、もう片方は満員になる
            assertEquals(
                1,
                results.stream()
                    .filter(result -> result == ApplyEventResult.SUCCESS)
                    .count()
            );

            assertEquals(
                1,
                results.stream()
                    .filter(result -> result == ApplyEventResult.FULL)
                    .count()
            );

            // DB上の申込件数も1件であることを確認
            Integer applicationCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM event_application WHERE event_id = ?",
                Integer.class,
                eventId
            );

            assertEquals(1, applicationCount);

        } finally {
            executor.shutdownNow();

            // テストで作成したデータだけを削除する
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

            if (userA != null) {
                jdbcTemplate.update(
                    "DELETE FROM users WHERE user_id = ?",
                    userA
                );
            }

            if (userB != null) {
                jdbcTemplate.update(
                    "DELETE FROM users WHERE user_id = ?",
                    userB
                );
            }
        }
    }

    @Test
    void 同イベントに同ユーザーが同時申し込みすると1回目の申し込みだけ成功する()
            throws Exception {

        String suffix = UUID.randomUUID().toString();

        Integer userA = null;
        Integer eventId = null;

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // テスト用ユーザーを2人登録する
            userA = jdbcTemplate.queryForObject(
                "INSERT INTO users (name, email, password) " +
                "VALUES (?, ?, ?) RETURNING user_id",
                Integer.class,
                "テストユーザーA",
                "test-a-" + suffix + "@example.com",
                "test-password"
            );

            // 定員1名のイベントを登録する
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
                "同時申し込みテスト",
                1,
                eventDatetime,
                "テスト会場",
                userA
            );

            // 2つの処理を同時にスタートさせる
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);

            int targetEventId = eventId;
            int targetUserA = userA;

            Future<ApplyEventResult> futureA = executor.submit(() -> {
                ready.countDown();
                start.await();
                return service.applyEvent(targetEventId, targetUserA);
            });

            Future<ApplyEventResult> futureB = executor.submit(() -> {
                ready.countDown();
                start.await();
                return service.applyEvent(targetEventId, targetUserA);
            });

            // 2つのスレッドが準備できたら同時に開始
            assertEquals(true, ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            ApplyEventResult resultA = futureA.get(15, TimeUnit.SECONDS);
            ApplyEventResult resultB = futureB.get(15, TimeUnit.SECONDS);

            List<ApplyEventResult> results = List.of(resultA, resultB);

            // 片方だけ成功し、もう片方は二重申し込みとして拒否される
            assertEquals(
                1,
                results.stream()
                    .filter(result -> result == ApplyEventResult.SUCCESS)
                    .count()
            );

            assertEquals(
                1,
                results.stream()
                    .filter(result -> result == ApplyEventResult.ALREADY_APPLIED)
                    .count()
            );

            // DB上の申込件数も1件であることを確認
            Integer applicationCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM event_application WHERE event_id = ?",
                Integer.class,
                eventId
            );

            assertEquals(1, applicationCount);

        } finally {
            executor.shutdownNow();

            // テストで作成したデータだけを削除する
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

            if (userA != null) {
                jdbcTemplate.update(
                    "DELETE FROM users WHERE user_id = ?",
                    userA
                );
            }
        }
    }
}
