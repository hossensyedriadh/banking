package io.github.hossensyedriadh.cbs.statement.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
public class StatementEntryView implements Serializable {
    @Serial
    private static final long serialVersionUID = -7170206973871272521L;

    private Long id;

    private StatementView statement;

    @JsonProperty("transaction_id")
    private Long transactionId;

    @JsonProperty("transaction_reference")
    private String transactionReference;

    @JsonProperty("transaction_date")
    private OffsetDateTime transactionDate;

    @JsonProperty("value_date")
    private LocalDate valueDate;

    private String description;

    @JsonProperty("debit_amount")
    private BigDecimal debitAmount;

    @JsonProperty("credit_amount")
    private BigDecimal creditAmount;

    @JsonProperty("running_balance")
    private BigDecimal runningBalance;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}