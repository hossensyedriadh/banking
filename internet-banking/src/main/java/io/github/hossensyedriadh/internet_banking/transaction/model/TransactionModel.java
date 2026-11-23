package io.github.hossensyedriadh.internet_banking.transaction.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hossensyedriadh.internet_banking.common.AccountModel;
import io.github.hossensyedriadh.internet_banking.common.CurrencyModel;
import io.github.hossensyedriadh.internet_banking.http.cbs.enumerator.CbsTransactionStatus;
import io.github.hossensyedriadh.internet_banking.http.cbs.enumerator.TransactionType;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 5714214555833921083L;

    private Long id;

    private String transactionReference;

    private AccountModel account;

    private CurrencyModel currency;

    private TransactionType transactionType;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amount;

    private CbsTransactionStatus status;

    private String description;

    private OffsetDateTime createdAt;

    private OffsetDateTime completedAt;
}
