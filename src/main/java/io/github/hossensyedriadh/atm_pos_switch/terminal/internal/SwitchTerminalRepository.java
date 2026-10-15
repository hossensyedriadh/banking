package io.github.hossensyedriadh.atm_pos_switch.terminal.internal;

import io.github.hossensyedriadh.atm_pos_switch.terminal.api.model.SwitchTerminal;
import io.github.hossensyedriadh.atm_pos_switch.terminal.api.enumerator.TerminalStatus;
import io.github.hossensyedriadh.atm_pos_switch.terminal.api.enumerator.TerminalType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
class SwitchTerminalRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SwitchTerminalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<SwitchTerminal> findById(String terminalId) {
        String sql = """
                SELECT t.terminal_id, t.terminal_type,
                t.status, t.institution_code, t.created_at
                FROM switch.switch_terminals t WHERE t.terminal_id = ?""";

        return this.jdbcTemplate.query(sql, resultSet -> {
            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(new SwitchTerminal(
                    resultSet.getString("terminal_id"),
                    TerminalType.valueOf(resultSet.getString("terminal_type")),
                    TerminalStatus.valueOf(resultSet.getString("status")),
                    resultSet.getString("institution_code"),
                    resultSet.getObject("created_at", OffsetDateTime.class)
            ));
        }, terminalId);
    }

    public boolean isActive(String terminalId) {
        String sql = "SELECT 1 from switch.switch_terminals t WHERE t.terminal_id = ? and t.status = ?";

        return this.jdbcTemplate.query(sql, ResultSet::next, terminalId, TerminalStatus.ACTIVE.toString());
    }
}
