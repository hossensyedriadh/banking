package io.github.hossensyedriadh.cbs.statement.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.statement.api.enumerator.StatementStatus;
import io.github.hossensyedriadh.cbs.statement.api.enumerator.StatementType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class StatementView implements Serializable {
    @Serial
    private static final long serialVersionUID = 4397302276910625764L;

    private Long id;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("currency_id")
    private Long currencyId;

    @JsonProperty("statement_type")
    private StatementType statementType;

    @JsonProperty("statement_period_start")
    private LocalDate statementPeriodStart;

    @JsonProperty("statement_period_end")
    private LocalDate statementPeriodEnd;

    @JsonProperty("opening_balance")
    private BigDecimal openingBalance;

    @JsonProperty("closing_balance")
    private BigDecimal closingBalance;

    @JsonProperty("total_debits")
    private BigDecimal totalDebits;

    @JsonProperty("total_credits")
    private BigDecimal totalCredits;

    private StatementStatus status;

    @JsonProperty("statement_entries")
    private List<StatementEntryView> statementEntries;

    @JsonProperty("generated_at")
    private OffsetDateTime generatedAt;

    @JsonProperty("issued_at")
    private OffsetDateTime issuedAt;
    
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}