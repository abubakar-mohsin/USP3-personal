package com.usp3.payment.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.usp3.payment.entity.enums.AttemptStatus;

@Component
public class PaymentAttemptStatusConstraintFixer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PaymentAttemptStatusConstraintFixer.class);
    private final JdbcTemplate jdbcTemplate;

    public PaymentAttemptStatusConstraintFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            String allowed = Arrays.stream(AttemptStatus.values())
                .map(v -> "'" + v.name() + "'")
                .collect(Collectors.joining(", "));

            jdbcTemplate.execute("ALTER TABLE payment_attempts DROP CONSTRAINT IF EXISTS payment_attempts_status_check");
            jdbcTemplate.execute("ALTER TABLE payment_attempts ADD CONSTRAINT payment_attempts_status_check CHECK (status IN (" + allowed + "))");

            log.info("Ensured payment_attempts_status_check allows: {}", allowed);
        } catch (org.springframework.dao.DataAccessException | IllegalStateException ex) {
            log.warn("Skipping payment_attempts_status_check update: {}", ex.getMessage());
        }
    }
}
