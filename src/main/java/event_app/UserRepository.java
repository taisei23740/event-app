package event_app;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer findUserId(String email, String password) {

        String sql =
                "SELECT user_id " +
                "FROM users " +
                "WHERE email = ? AND password = ?";

        Integer userId = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                email,
                password
        );

        return userId;
    }
}