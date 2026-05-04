package com.usp3.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaPatchRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        ensureApiKeyDisplayColumn();
    }

    private void ensureApiKeyDisplayColumn() {
        try {
            String existsSql = "select count(*) as cnt from information_schema.columns where table_name = 'api_keys' and column_name = 'display_value'";
            Integer cnt = jdbcTemplate.queryForObject(existsSql, Integer.class);
            if (cnt != null && cnt > 0) {
                return;
            }

            log.warn("Applying schema patch: adding api_keys.display_value");
            jdbcTemplate.execute("alter table api_keys add column display_value varchar(64)");
            // Backfill existing rows with a masked prefix/suffix of the stored hash
            jdbcTemplate.execute("update api_keys set display_value = substring(key_hash, 1, 12) || '***' || right(key_hash, 4) where display_value is null");
        } catch (org.springframework.dao.DataAccessException | IllegalStateException e) {
            log.error("Schema patch failed for api_keys.display_value", e);
        }
    }
}