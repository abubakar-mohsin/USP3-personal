package com.usp3.payment.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.usp3.payment.entity.enums.TransactionStatus;

@Component
public class TransactionStatusConstraintFixer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TransactionStatusConstraintFixer.class);
    private final JdbcTemplate jdbcTemplate;

    public TransactionStatusConstraintFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            String allowed = Arrays.stream(TransactionStatus.values())
                .map(v -> "'" + v.name() + "'")
                .collect(Collectors.joining(", "));

            jdbcTemplate.execute("ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_status_check");
            jdbcTemplate.execute("ALTER TABLE transactions ADD CONSTRAINT transactions_status_check CHECK (status IN (" + allowed + "))");

            log.info("Ensured transactions_status_check allows: {}", allowed);
        } catch (org.springframework.dao.DataAccessException | IllegalStateException ex) {
            log.warn("Skipping transactions_status_check update: {}", ex.getMessage());
        }
    }
}
