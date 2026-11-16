package com.sametkorkmaz.walletledger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MigrationTest {
    @Autowired
    JdbcTemplate jdbc;

    @Test
    void appliesAllMigrationsWhenApplicationStarts() {
        Integer latestVersion = jdbc.queryForObject("""
                SELECT max(version::int) FROM flyway_schema_history
                WHERE success
                """, Integer.class);
        assertThat(latestVersion).isEqualTo(5); // each migration bumps it
    }
}
