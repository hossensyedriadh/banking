package io.github.hossensyedriadh.cbs.cheque.internal.util;

import io.github.hossensyedriadh.cbs.cheque.api.model.ChequeNumberRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class PostgresChequeNumberGenerator implements ChequeNumberGenerator {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PostgresChequeNumberGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ChequeNumberRange generate(int leafCount) {
        if (leafCount <= 0) {
            throw new IllegalArgumentException("Leaf count must be greater than zero");
        }

        return this.jdbcTemplate.queryForObject("SELECT first_number, last_number FROM cheque.allocate_cheque_number_range(?)",
                (rs, _) -> new ChequeNumberRange(
                        rs.getLong("first_number"),
                        rs.getLong("last_number"))
                , leafCount);
    }
}
