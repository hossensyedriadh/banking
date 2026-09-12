package io.github.hossensyedriadh.cbs.transaction.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionDirection;
import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionType;
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
public class AccountTransactionView implements Serializable {
    @Serial
    private static final long serialVersionUID = 7914226242633357644L;

    @JsonProperty("transaction_id")
    private Long transactionId;

    @JsonProperty("transaction_ref")
    private String transactionReference;

    private TransactionDirection direction;

    @JsonProperty("transaction_type")
    private TransactionType transactionType;

    @JsonProperty("currency_id")
    private Long currencyId;

    private BigDecimal amount;

    private TransactionStatus status;

    private String description;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("completed_at")
    private OffsetDateTime completedAt;
}
