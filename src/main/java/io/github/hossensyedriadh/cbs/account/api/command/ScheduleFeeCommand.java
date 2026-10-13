package io.github.hossensyedriadh.cbs.account.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ScheduleFeeCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -2770917512467563287L;

    @NotNull
    @JsonProperty("fee_rule_id")
    private Long feeRuleId;

    @NotNull
    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("card_id")
    private Long cardId;

    @NotNull
    @Future
    @JsonProperty("scheduled_date")
    private LocalDate scheduledDate;

    @NotNull
    @Positive
    private BigDecimal amount;
}
