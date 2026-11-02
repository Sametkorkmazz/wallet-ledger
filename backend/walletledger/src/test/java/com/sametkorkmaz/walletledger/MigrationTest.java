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
    void usersTableExistsAfterMigration() {
        Integer count = jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.tables
                WHERE table_name = 'users'
                """, Integer.class);
        assertThat(count).isEqualTo(1);
    }
}
