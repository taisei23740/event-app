package event_app.repository;

import java.util.List;

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

        List<Integer> userId = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getInt("user_id"),
                email,
                password
        );

        if (userId.isEmpty()) {
            return null;
        }

        return userId.get(0);
    }
}