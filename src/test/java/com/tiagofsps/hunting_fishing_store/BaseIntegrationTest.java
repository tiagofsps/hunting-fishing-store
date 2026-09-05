package com.tiagofsps.hunting_fishing_store;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest extends AbstractContainerBaseTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void databaseCleanup() {
        try {
            List<String> tables = jdbcTemplate.queryForList(
                    "SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename != 'flyway_schema_history'",
                    String.class
            );

            if (!tables.isEmpty()) {
                String truncateQuery = "TRUNCATE TABLE " + String.join(", ", tables) + " RESTART IDENTITY CASCADE";
                jdbcTemplate.execute(truncateQuery);
            }
        } catch (Exception e) {
            // Fallback for H2 if needed
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
            List<String> tables = jdbcTemplate.queryForList(
                    "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
                    String.class
            );
            for (String table : tables) {
                jdbcTemplate.execute("TRUNCATE TABLE " + table);
            }
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }

    protected JwtRequestPostProcessor adminJwt() {
        return jwt().jwt(j -> j.subject("admin@example.com")
                        .claim("token_type", "access")
                        .claim("roles", List.of("ROLE_ADMIN")))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    protected JwtRequestPostProcessor userJwt(String email) {
        return jwt().jwt(j -> j.subject(email)
                        .claim("token_type", "access")
                        .claim("roles", List.of("ROLE_USER")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    protected JwtRequestPostProcessor userJwt() {
        return userJwt("test@example.com");
    }
}
