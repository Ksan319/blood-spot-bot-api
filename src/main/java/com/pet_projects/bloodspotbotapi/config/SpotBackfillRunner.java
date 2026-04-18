package com.pet_projects.bloodspotbotapi.config;

import com.pet_projects.bloodspotbotapi.service.SpotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpotBackfillRunner implements ApplicationRunner {
    private final SpotService spotService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        migrateSiteCheckConstraint();
        log.info("Running spot site backfill...");
        spotService.backfillNullSites();
        log.info("Spot site backfill completed.");
    }

    private void migrateSiteCheckConstraint() {
        try {
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_site_check");
            jdbcTemplate.execute("ALTER TABLE users ADD CONSTRAINT users_site_check CHECK (site IN ('DONOR_MOS', 'DONOR_MOS_SAB', 'DONOR_MOS_ZAR', 'ALL'))");
            log.info("Migrated users_site_check constraint to include ALL");
        } catch (Exception e) {
            log.warn("Failed to migrate users_site_check constraint: {}", e.getMessage());
        }
    }
}
