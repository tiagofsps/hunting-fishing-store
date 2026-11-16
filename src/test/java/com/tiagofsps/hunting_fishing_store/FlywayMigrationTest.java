package com.tiagofsps.hunting_fishing_store;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Smoke test verifying that Flyway migrates the schema and that Hibernate
 * can validate against it (spring.jpa.hibernate.ddl-auto=validate).
 * Context load failure would surface as a startup error on this test.
 */
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@ActiveProfiles("test")
class FlywayMigrationTest extends AbstractContainerBaseTest {

    @Autowired
    private Flyway flyway;

    @Test
    void flywayAppliedBaselineAndHibernateValidates() {
        MigrationInfo current = flyway.info().current();
        assertEquals("7", current.getVersion().getVersion());
        assertEquals(org.flywaydb.core.api.MigrationState.SUCCESS, current.getState());
    }
}