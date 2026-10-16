package io.github.hossensyedriadh.atm_pos_switch.transaction.internal;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.OriginalDataElements;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionSummary;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
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

    public Optional<Long> insertReversalReceived(String terminalId, String stan, String retrievalReference,
                                                 String processingCode, String requestMti, String transmissionDateTime, long originalTransactionId) {
        List<Long> ids = this.jdbcTemplate.query("""
                        INSERT INTO switch.switch_transactions (
                            terminal_id, stan, retrieval_reference,
                            processing_code, request_mti,
                            transmission_date_time, reversal_of_transaction_id,
                            status
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, 'RECEIVED')
                        ON CONFLICT (reversal_of_transaction_id)
                            WHERE reversal_of_transaction_id IS NOT NULL
                        DO NOTHING
                        RETURNING id
                        """,
                (rs, _) -> rs.getLong("id"),
                terminalId, stan, retrievalReference, processingCode, requestMti, transmissionDateTime, originalTransactionId
        );

        return ids.stream().findFirst();
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
                SET status = 'PROCESSING', processing_started_at = CURRENT_TIMESTAMP
                WHERE t.id = ? AND t.status = 'RECEIVED'""", id);
    }

    public int markStuckProcessingAsUnknown(Duration timeout) {
        return this.jdbcTemplate.update("""
                UPDATE switch.switch_transactions
                SET status = 'UNKNOWN',
                    completed_at = CURRENT_TIMESTAMP
                WHERE status = 'PROCESSING'
                  AND processing_started_at < CURRENT_TIMESTAMP - (? * INTERVAL '1 second')
                """, timeout.toSeconds());
    }

    public int markAsComplete(Long id, String responseCode, TransactionStatus status, byte[] responsePayload) {
        if (status != TransactionStatus.APPROVED && status != TransactionStatus.DECLINED) {
            throw new RuntimeException("Transaction completion status must be APPROVED or DECLINED");
        }

        return this.jdbcTemplate.update("""
                UPDATE switch.switch_transactions
                SET response_code = ?,
                    response_payload = ?,
                    status = ?,
                    completed_at = CURRENT_TIMESTAMP
                WHERE id = ? AND status = 'PROCESSING'
                """, responseCode, responsePayload, status.name(), id);
    }

    public int markAsFailed(long id, TransactionStatus status) {
        if (status != TransactionStatus.FAILED && status != TransactionStatus.UNKNOWN) {
            throw new RuntimeException("Status must be FAILED or UNKNOWN");
        }

        return this.jdbcTemplate.update("""
                UPDATE switch.switch_transactions
                SET status = ?,
                    completed_at = CURRENT_TIMESTAMP
                WHERE id = ? AND status = 'PROCESSING'
                """, status.name(), id);
    }

    public Optional<byte[]> findResponsePayloadById(long id) {
        return jdbcTemplate.query("SELECT response_payload FROM switch.switch_transactions WHERE id = ?",
                rs -> {
                    if (!rs.next()) {
                        return Optional.empty();
                    }

                    byte[] payload = rs.getBytes("response_payload");
                    return Optional.ofNullable(payload);
                },
                id
        );
    }

    public Optional<TransactionStatus> findStatusById(long id) {
        return this.jdbcTemplate.query("SELECT t.status FROM switch.switch_transactions t WHERE t.id = ?", resultSet -> {
            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(TransactionStatus.valueOf(resultSet.getString("status")));
        }, id);
    }

    public Optional<SwitchTransactionSummary> findByTerminalStanAndTransmissionDateTime(String terminalId, String stan, String transmissionDateTime) {
        return this.jdbcTemplate.query("""
                        SELECT id, terminal_id, stan,
                               retrieval_reference, processing_code,
                               transaction_amount, currency_code,
                               request_mti, response_code, status,
                               cbs_txn_id, cbs_reversal_txn_id,
                               received_at, processing_started_at, completed_at
                        FROM switch.switch_transactions
                        WHERE terminal_id = ?
                          AND stan = ?
                          AND transmission_date_time = ?
                        LIMIT 1
                        """, this.transactionSummaryMapper,
                terminalId, stan, transmissionDateTime
        ).stream().findFirst();
    }

    public Optional<SwitchTransactionSummary> findOriginalTransaction(String terminalId, OriginalDataElements originalData) {
        return this.jdbcTemplate.query("""
                        SELECT id, terminal_id, stan,
                               retrieval_reference, processing_code,
                               transaction_amount, currency_code,
                               request_mti, response_code, status,
                               cbs_txn_id, cbs_reversal_txn_id,
                               received_at, processing_started_at, completed_at
                        FROM switch.switch_transactions
                        WHERE terminal_id = ?
                          AND stan = ?
                          AND transmission_date_time = ?
                          AND request_mti = ?
                        LIMIT 1
                        """, this.transactionSummaryMapper,
                terminalId, originalData.originalStan(), originalData.originalTransmissionDateTime(), originalData.originalMti()
        ).stream().findFirst();
    }

    public int updateCbsTransactionIds(long id, Long cbsTxnId, @Nullable Long cbsReversalTxnId) {
        return this.jdbcTemplate.update("""
                        UPDATE switch.switch_transactions
                        SET cbs_txn_id = ?,
                            cbs_reversal_txn_id = ?
                        WHERE id = ?
                        """,
                cbsTxnId, cbsReversalTxnId, id
        );
    }

    public boolean hasReversal(long originalTransactionId) {
        Boolean exists = this.jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM switch.switch_transactions
                    WHERE reversal_of_transaction_id = ?
                )
                """, Boolean.class, originalTransactionId);

        return Boolean.TRUE.equals(exists);
    }

    private final RowMapper<SwitchTransactionSummary> transactionSummaryMapper =
            (rs, _) -> new SwitchTransactionSummary(
                    rs.getLong("id"),
                    rs.getString("terminal_id"),
                    rs.getString("stan"),
                    rs.getString("retrieval_reference"),
                    rs.getString("processing_code"),
                    rs.getBigDecimal("transaction_amount"),
                    rs.getString("currency_code"),
                    rs.getString("request_mti"),
                    rs.getString("response_code"),
                    TransactionStatus.valueOf(rs.getString("status")),
                    rs.getObject("cbs_txn_id", Long.class),
                    rs.getObject("cbs_reversal_txn_id", Long.class),
                    rs.getObject("received_at", OffsetDateTime.class),
                    rs.getObject("processing_started_at", OffsetDateTime.class),
                    rs.getObject("completed_at", OffsetDateTime.class)
            );
}
