package io.github.hossensyedriadh.cbs.cheque.internal.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class PostgresChequeBookNumberGenerator implements ChequeBookNumberGenerator {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PostgresChequeBookNumberGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String generate() {
        Long sequence = this.jdbcTemplate.queryForObject("SELECT nextval('cheque.cheque_book_number_sequence')", Long.class);

        if (sequence == null) {
            throw new IllegalStateException("Failed to generate cheque book number");
        }

        return String.valueOf(sequence);
    }
}
