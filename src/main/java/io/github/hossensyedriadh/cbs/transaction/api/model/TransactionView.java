package io.github.hossensyedriadh.cbs.transaction.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class TransactionView implements Serializable {
    @Serial
    private static final long serialVersionUID = -1674238866235735266L;

    private Long id;

    @JsonProperty("transaction_ref")
    private String transactionReference;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("destination_account_id")
    private Long destinationAccountId;

    @JsonProperty("currency_id")
    private Long currencyId;

    @JsonProperty("transaction_type")
    private TransactionType transactionType;

    private BigDecimal amount;

    private TransactionStatus status;

    private String description;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("completed_at")
    private OffsetDateTime completedAt;
}
