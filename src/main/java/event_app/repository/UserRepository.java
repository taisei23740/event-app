package event_app.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public UserRepository(JdbcTemplate jdbcTemplate,PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public Integer findUserId(String email, String password) {

        String sql =
                "SELECT user_id ,password " +
                "FROM users " +
                "WHERE email = ?";

        List<Integer> userId = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    String dbPassword = rs.getString("password");
                    if (passwordEncoder.matches(password,dbPassword)) {
                        return rs.getInt("user_id");
                    }
                    return null;
                    },
                email
        );

        if (userId.isEmpty()) {
            return null;
        }

        return userId.get(0);
    }
}
