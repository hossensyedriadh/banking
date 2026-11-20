package io.github.hossensyedriadh.internet_banking.http.cbs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.internet_banking.http.cbs.enumerator.TransactionStatus;
import io.github.hossensyedriadh.internet_banking.http.cbs.enumerator.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CbsTransactionResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -3794164847964303039L;

    private Long id;

    @JsonProperty("transaction_ref")
    private String transactionReference;

    @JsonProperty("source_account_id")
    private Long accountId;

    @JsonProperty("currency_id")
    private Long currencyId;

    @JsonProperty("transaction_type")
    private TransactionType transactionType;

    private BigDecimal amount;

    private TransactionStatus status;

    private String description;

    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    @JsonProperty("reversal_of_trx_id")
    private Long reversalOfTransactionId;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("completed_at")
    private OffsetDateTime completedAt;
}
