package io.github.hossensyedriadh.atm_pos_switch.transaction.internal;

import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ProcessingCodes;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ResponseCodes;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.ReversalRegistrationOutcome;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.*;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
                               String processingCode, String requestMti, String transmissionDateTime, BigDecimal transactionAmount,
                               String currencyCode, byte[] requestPayload) {
        String sql = """
                INSERT INTO switch.switch_transactions
                    (terminal_id, stan, retrieval_reference,
                     processing_code, request_mti, transmission_date_time, transaction_amount, currency_code, request_payload, status)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'RECEIVED') RETURNING id
                """;

        Long id = this.jdbcTemplate.queryForObject(sql, Long.class, terminalId, stan, retrievalReference,
                processingCode, requestMti, transmissionDateTime, transactionAmount, currencyCode, requestPayload);

        if (id == null) {
            throw new RuntimeException("Failed to persist transaction");
        }

        return id;
    }

    @Transactional
    public ReversalRegistrationResult insertReversalReceived(String terminalId, String stan, String retrievalReference, String processingCode, String requestMti, String transmissionDateTime, long originalTransactionId) {
        List<Long> ids = this.jdbcTemplate.query("""
                        INSERT INTO switch.switch_transactions (
                            terminal_id,
                            stan,
                            retrieval_reference,
                            processing_code,
                            request_mti,
                            transmission_date_time,
                            reversal_of_transaction_id,
                            status
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, 'RECEIVED')
                        ON CONFLICT DO NOTHING
                        RETURNING id
                        """,
                (rs, _) -> rs.getLong("id"),
                terminalId, stan, retrievalReference, processingCode, requestMti, transmissionDateTime, originalTransactionId
        );

        if (!ids.isEmpty()) {
            return new ReversalRegistrationResult(ids.getFirst(), ReversalRegistrationOutcome.REGISTERED);
        }

        List<Long> requestIds = this.jdbcTemplate.query("""
                        SELECT id, reversal_of_transaction_id
                        FROM switch.switch_transactions
                        WHERE terminal_id = ?
                          AND stan = ?
                          AND transmission_date_time = ?
                        """,
                (rs, _) -> {
                    long existingOriginalId = rs.getLong("reversal_of_transaction_id");

                    if (rs.wasNull()) {
                        return null;
                    }

                    return existingOriginalId == originalTransactionId ? rs.getLong("id") : -rs.getLong("id");
                }, terminalId, stan, transmissionDateTime
        );

        if (!requestIds.isEmpty()) {
            Long existingId = requestIds.getFirst();

            if (existingId > 0) {
                return new ReversalRegistrationResult(existingId, ReversalRegistrationOutcome.DUPLICATE_REQUEST);
            }

            return new ReversalRegistrationResult(null, ReversalRegistrationOutcome.REQUEST_IDENTITY_CONFLICT);
        }

        List<Long> reversalIds = this.jdbcTemplate.query("""
                        SELECT id
                        FROM switch.switch_transactions
                        WHERE reversal_of_transaction_id = ?
                        """,
                (rs, _) -> rs.getLong("id"),
                originalTransactionId
        );

        if (!reversalIds.isEmpty()) {
            return new ReversalRegistrationResult(reversalIds.getFirst(), ReversalRegistrationOutcome.ORIGINAL_ALREADY_REVERSED);
        }

        throw new IllegalStateException("Reversal registration conflicted with an unexpected database constraint");
    }

    public Optional<DuplicateTransactionCandidate> findDuplicateTransaction(String terminalId, String stan, String transmissionDateTime) {
        String sql = """
                SELECT id,
                       retrieval_reference,
                       processing_code,
                       request_mti,
                       transaction_amount,
                       currency_code
                FROM switch.switch_transactions
                WHERE terminal_id = ?
                  AND stan = ?
                  AND transmission_date_time = ?
                """;

        return this.jdbcTemplate.query(sql, resultSet -> {
            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(new DuplicateTransactionCandidate(
                    resultSet.getLong("id"),
                    resultSet.getString("retrieval_reference"),
                    resultSet.getString("processing_code"),
                    resultSet.getString("request_mti"),
                    resultSet.getBigDecimal("transaction_amount"),
                    resultSet.getString("currency_code")
            ));
        }, terminalId, stan, transmissionDateTime);
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
                UPDATE switch.switch_transactions
                SET status = 'PROCESSING',
                    processing_started_at = CURRENT_TIMESTAMP
                WHERE id = ?
                  AND status = 'RECEIVED'
                """, id);
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

    public int completeTransaction(long id, String responseCode, TransactionStatus status, byte[] responsePayload) {
        if (status != TransactionStatus.APPROVED && status != TransactionStatus.DECLINED) {
            throw new IllegalArgumentException("Transaction completion status must be APPROVED or DECLINED");
        }

        return this.jdbcTemplate.update("""
                UPDATE switch.switch_transactions
                SET response_code = ?,
                    response_payload = ?,
                    status = ?,
                    completed_at = CURRENT_TIMESTAMP
                WHERE id = ?
                  AND status = 'PROCESSING'
                """, responseCode, responsePayload, status.name(), id);
    }

    public Optional<TransactionCompletionResult> findCompletionResult(long id, boolean completed) {
        return this.jdbcTemplate.query("SELECT status, response_payload FROM switch.switch_transactions WHERE id = ?",
                rs -> {
                    if (!rs.next()) {
                        return Optional.empty();
                    }

                    return Optional.of(new TransactionCompletionResult(
                            completed,
                            TransactionStatus.valueOf(rs.getString("status")),
                            rs.getBytes("response_payload")));
                }, id);
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

    public Optional<byte[]> findRequestPayload(long transactionId) {
        List<byte[]> results = this.jdbcTemplate.query(
                """
                        SELECT request_payload
                        FROM switch.switch_transactions
                        WHERE id = ?
                          AND request_payload IS NOT NULL
                        """,
                (rs, _) -> rs.getBytes("request_payload"),
                transactionId);

        return results.stream().findFirst();
    }

    public Optional<TransactionStatus> findStatusById(long id) {
        return this.jdbcTemplate.query("SELECT t.status FROM switch.switch_transactions t WHERE t.id = ?", resultSet -> {
            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(TransactionStatus.valueOf(resultSet.getString("status")));
        }, id);
    }

    public Optional<SwitchTransactionSummary> findTransactionById(long transactionId) {
        List<SwitchTransactionSummary> results = this.jdbcTemplate.query("""
                SELECT id, terminal_id, stan, retrieval_reference, processing_code,
                       transaction_amount, currency_code, request_mti, response_code, status,
                       cbs_txn_id, cbs_reversal_txn_id, received_at, processing_started_at, completed_at
                FROM switch.switch_transactions
                WHERE id = ?
                """, this.transactionSummaryMapper, transactionId
        );

        return results.stream().findFirst();
    }

    public Optional<Long> findOriginalTransactionId(long reversalTransactionId) {
        List<Long> results = this.jdbcTemplate.query("""
                        SELECT reversal_of_transaction_id
                        FROM switch.switch_transactions
                        WHERE id = ?
                          AND reversal_of_transaction_id IS NOT NULL
                        """,
                (rs, _) -> rs.getLong("reversal_of_transaction_id"),
                reversalTransactionId
        );

        return results.stream().findFirst();
    }

    public Optional<SwitchTransactionSummary> findByTerminalStanAndTransmissionDateTime(String terminalId, String stan, String transmissionDateTime) {
        return this.jdbcTemplate.query("""
                        SELECT id, terminal_id, stan,
                               retrieval_reference, processing_code,
                               transaction_amount, currency_code,
                               request_mti, response_code, status,
                               cbs_txn_id, cbs_reversal_txn_id,
                               received_at, processing_started_at, completed_at, response_payload
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

    public List<SwitchTransactionSummary> findUnknownWithdrawals(int batchSize) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("Batch size must be positive");
        }

        return this.jdbcTemplate.query("""
                SELECT id, terminal_id, stan,
                       retrieval_reference, processing_code,
                       transaction_amount, currency_code,
                       request_mti, response_code, status,
                       cbs_txn_id, cbs_reversal_txn_id,
                       received_at, processing_started_at, completed_at
                FROM switch.switch_transactions
                WHERE status = 'UNKNOWN'
                  AND request_mti = '0200'
                  AND processing_code = ?
                ORDER BY completed_at NULLS FIRST, id
                LIMIT ?
                """, this.transactionSummaryMapper, ProcessingCodes.CASH_WITHDRAWAL, batchSize);
    }

    public List<SwitchTransactionSummary> findUnknownReversals(int batchSize) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("Batch size must be positive");
        }

        return this.jdbcTemplate.query("""
                SELECT id, terminal_id, stan, retrieval_reference, processing_code,
                       transaction_amount, currency_code, request_mti, response_code, status,
                       cbs_txn_id, cbs_reversal_txn_id, received_at, processing_started_at, completed_at
                FROM switch.switch_transactions
                WHERE status = 'UNKNOWN'
                  AND request_mti = '0400'
                  AND reversal_of_transaction_id IS NOT NULL
                ORDER BY completed_at NULLS FIRST, id
                LIMIT ?
                """, this.transactionSummaryMapper, batchSize
        );
    }

    public int resolveUnknownTransaction(long id, String responseCode, TransactionStatus resolvedStatus, Long cbsTransactionId, byte[] responsePayload) {
        if (resolvedStatus != TransactionStatus.APPROVED && resolvedStatus != TransactionStatus.DECLINED) {
            throw new IllegalArgumentException("Resolved status must be APPROVED or DECLINED");
        }

        return this.jdbcTemplate.update(
                """
                        UPDATE switch.switch_transactions
                        SET response_code = ?,
                            status = ?,
                            cbs_txn_id = COALESCE(?, cbs_txn_id),
                            response_payload = ?,
                            completed_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                          AND status = 'UNKNOWN'
                        """, responseCode, resolvedStatus.name(), cbsTransactionId, responsePayload, id);
    }

    @Transactional
    public int resolveUnknownReversalAndUpdateOriginal(long reversalTransactionId, long originalCbsTransactionId, String responseCode, Long cbsReversalTransactionId, byte[] responsePayload) {
        if (!ResponseCodes.APPROVED.equals(responseCode) || cbsReversalTransactionId == null) {
            throw new IllegalArgumentException("A reconciled successful reversal requires response code 00 and a CBS reversal ID");
        }

        List<Long> originalIds = jdbcTemplate.query("""
                        SELECT original.id
                        FROM switch.switch_transactions reversal
                        JOIN switch.switch_transactions original
                          ON original.id = reversal.reversal_of_transaction_id
                        WHERE reversal.id = ?
                          AND reversal.status = 'UNKNOWN'
                          AND reversal.request_mti = '0400'
                          AND original.cbs_txn_id = ?
                          AND (
                                original.cbs_reversal_txn_id IS NULL
                                OR original.cbs_reversal_txn_id = ?
                              )
                        FOR UPDATE OF reversal, original
                        """,
                (rs, _) -> rs.getLong("id"),
                reversalTransactionId, originalCbsTransactionId, cbsReversalTransactionId);

        if (originalIds.isEmpty()) {
            return 0;
        }

        int originalUpdated = jdbcTemplate.update("""
                        UPDATE switch.switch_transactions
                        SET cbs_reversal_txn_id = ?
                        WHERE id = ?
                          AND cbs_txn_id = ?
                          AND (
                                cbs_reversal_txn_id IS NULL
                                OR cbs_reversal_txn_id = ?
                              )
                        """,
                cbsReversalTransactionId, originalIds.getFirst(), originalCbsTransactionId, cbsReversalTransactionId);

        if (originalUpdated != 1) {
            throw new IllegalStateException("Could not update original transaction reversal reference");
        }

        int reversalUpdated = jdbcTemplate.update("""
                        UPDATE switch.switch_transactions
                        SET response_code = ?,
                            status = 'APPROVED',
                            cbs_txn_id = ?,
                            response_payload = ?,
                            completed_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                          AND status = 'UNKNOWN'
                          AND request_mti = '0400'
                          AND reversal_of_transaction_id = ?
                        """,
                responseCode, cbsReversalTransactionId, responsePayload, reversalTransactionId, originalIds.getFirst());

        if (reversalUpdated != 1) {
            throw new IllegalStateException("Could not resolve unknown reversal transaction");
        }

        return 1;
    }

    @Transactional
    public int completeReversalAtomically(long reversalTransactionId, long originalTransactionId, Long originalCbsTransactionId, String responseCode, TransactionStatus resolvedStatus, Long cbsReversalTransactionId, byte[] responsePayload) {
        if (resolvedStatus != TransactionStatus.APPROVED && resolvedStatus != TransactionStatus.DECLINED) {
            throw new IllegalArgumentException("Resolved status must be APPROVED or DECLINED");
        }

        if (resolvedStatus == TransactionStatus.APPROVED && (cbsReversalTransactionId == null || originalCbsTransactionId == null)) {
            throw new IllegalArgumentException("Successful reversal requires both CBS transaction IDs");
        }

        if (resolvedStatus == TransactionStatus.APPROVED && !ResponseCodes.APPROVED.equals(responseCode)) {
            throw new IllegalArgumentException("APPROVED status requires response code 00");
        }

        if (resolvedStatus == TransactionStatus.DECLINED && (responseCode == null || !responseCode.matches("\\d{2}")
                || ResponseCodes.APPROVED.equals(responseCode))) {
            throw new IllegalArgumentException("DECLINED status requires a non-approval response code");
        }

        List<Long> originalIds = this.jdbcTemplate.query("""
                        SELECT original.id
                        FROM switch.switch_transactions original
                        JOIN switch.switch_transactions reversal
                          ON reversal.reversal_of_transaction_id = original.id
                        WHERE reversal.id = ?
                          AND reversal.status = 'PROCESSING'
                          AND reversal.request_mti = '0400'
                          AND original.id = ?
                        FOR UPDATE OF original, reversal
                        """,
                (rs, _) -> rs.getLong("reversal_of_transaction_id"),
                reversalTransactionId,
                originalTransactionId
        );

        if (originalIds.isEmpty()) {
            return 0;
        }

        if (cbsReversalTransactionId != null) {
            if (originalCbsTransactionId == null) {
                throw new IllegalArgumentException("Original CBS transaction ID must not be null");
            }

            int originalUpdated = this.jdbcTemplate.update("""
                            UPDATE switch.switch_transactions
                            SET cbs_reversal_txn_id = ?
                            WHERE id = ?
                              AND cbs_txn_id = ?
                              AND (
                                  cbs_reversal_txn_id IS NULL
                                  OR cbs_reversal_txn_id = ?
                              )
                            """,
                    cbsReversalTransactionId, originalTransactionId, originalCbsTransactionId, cbsReversalTransactionId
            );

            if (originalUpdated != 1) {
                throw new IllegalStateException("Could not update original transaction with CBS reversal ID");
            }
        }

        int reversalUpdated = this.jdbcTemplate.update("""
                        UPDATE switch.switch_transactions
                        SET response_code = ?,
                            status = ?,
                            cbs_txn_id = COALESCE(?, cbs_txn_id),
                            response_payload = ?,
                            completed_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                          AND status = 'PROCESSING'
                          AND request_mti = '0400'
                          AND reversal_of_transaction_id = ?
                        """,
                responseCode, resolvedStatus.name(), cbsReversalTransactionId, responsePayload, reversalTransactionId, originalTransactionId
        );

        if (reversalUpdated != 1) {
            throw new IllegalStateException("Could not finalize reversal transaction");
        }

        return 1;
    }

    public int updateCbsAccountAndCurrencyIds(long transactionId, Long accountId, Long currencyId) {
        if (accountId == null || currencyId == null) {
            throw new IllegalArgumentException("CBS account ID and currency ID must not be null");
        }

        return this.jdbcTemplate.update("""
                UPDATE switch.switch_transactions
                SET cbs_account_id = ?,
                    cbs_currency_id = ?
                WHERE id = ?
                  AND status = 'PROCESSING'
                """, accountId, currencyId, transactionId
        );
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

    public Optional<Long> findCbsCurrencyId(long transactionId) {
        List<Long> results = this.jdbcTemplate.query("""
                        SELECT cbs_currency_id
                        FROM switch.switch_transactions
                        WHERE id = ?
                          AND cbs_currency_id IS NOT NULL
                        """,
                (rs, _) -> rs.getLong("cbs_currency_id"),
                transactionId
        );

        return results.stream().findFirst();
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

    public List<SwitchTransactionSummary> findUnknownTransactionsForReconciliation(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }

        String sql = """
                SELECT id, terminal_id, stan, retrieval_reference,
                       processing_code, transaction_amount, currency_code,
                       request_mti, response_code, status,
                       cbs_txn_id, cbs_reversal_txn_id, received_at,
                       processing_started_at, completed_at
                FROM switch.switch_transactions
                WHERE status = 'UNKNOWN'
                ORDER BY received_at ASC, id ASC
                LIMIT ?
                """;

        return this.jdbcTemplate.query(sql, this.transactionSummaryMapper, limit);
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
                    rs.getObject("completed_at", OffsetDateTime.class),
                    rs.getBytes("response_payload")
            );
}
