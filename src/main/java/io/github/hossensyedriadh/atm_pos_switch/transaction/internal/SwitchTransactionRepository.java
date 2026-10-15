package io.github.hossensyedriadh.atm_pos_switch.transaction.internal;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class SwitchTransactionRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SwitchTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long insertReceived(String terminalId, String stan, String retrievalReference,
                               String processingCode, String requestMti, String transmissionDateTime) {
        String sql = """
                INSERT INTO switch.switch_transactions
                    (terminal_id, stan, retrieval_reference,
                     processing_code, request_mti, transmission_date_time, status)
                     VALUES (?, ?, ?, ?, ?, ?, 'RECEIVED') RETURNING id
                """;

        Long id = this.jdbcTemplate.queryForObject(sql, Long.class, terminalId, stan, retrievalReference,
                processingCode, requestMti, transmissionDateTime);

        if (id == null) {
            throw new RuntimeException("Failed to persist transaction");
        }

        return id;
    }

    public Optional<Long> findIdByDuplicateKey(String terminalId, String stan, String transmissionDateTime) {
        String sql = """
                SELECT id FROM switch.switch_transactions
                WHERE terminal_id = ? AND stan = ? AND transmission_date_time = ?
                """;

        return this.jdbcTemplate.query(sql, resultSet -> {
            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(resultSet.getLong("id"));
        }, terminalId, stan, transmissionDateTime);
    }

    public int markAsProcessing(long id) {
        return this.jdbcTemplate.update("""
                UPDATE switch.switch_transactions t
                SET status = 'PROCESSING'
                WHERE t.id = ? AND t.status = 'RECEIVED'""", id);
    }

    public int markAsComplete(Long id, String responseCode, TransactionStatus status) {
        if (status != TransactionStatus.APPROVED && status != TransactionStatus.DECLINED) {
            throw new RuntimeException("Transaction completion status must be APPROVED or DECLINED");
        }

        return this.jdbcTemplate.update("""
                UPDATE switch.switch_transactions
                SET response_code = ?,
                    status = ?,
                    completed_at = CURRENT_TIMESTAMP
                WHERE id = ? AND status = 'PROCESSING'
                """, responseCode, status.name(), id);
    }

    public Optional<TransactionStatus> findStatusById(long id) {
        return this.jdbcTemplate.query("SELECT t.status FROM switch.switch_transactions t WHERE t.id = ?", resultSet -> {
            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(TransactionStatus.valueOf(resultSet.getString("status")));
        }, id);
    }
}
