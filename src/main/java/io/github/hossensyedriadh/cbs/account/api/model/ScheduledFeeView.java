package io.github.hossensyedriadh.cbs.account.api.model;

import io.github.hossensyedriadh.cbs.account.api.enumerator.ScheduledFeeStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
public class ScheduledFeeView implements Serializable {
    @Serial
    private static final long serialVersionUID = 1914703030466208045L;

    private Long id;

    private Long feeRuleId;

    private Long accountId;

    private Long cardId;

    private LocalDate scheduledDate;

    private BigDecimal amount;

    private ScheduledFeeStatus status;

    private Long transactionId;

    private OffsetDateTime processedAt;

    private String failureReason;

    private OffsetDateTime createdAt;
}