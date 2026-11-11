package io.github.hossensyedriadh.cbs.account.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.account.api.enumerator.InterestAccrualStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
public class InterestAccrualView implements Serializable {
    @Serial
    private static final long serialVersionUID = 8168638377132924261L;

    private Long id;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("calculation_date")
    private LocalDate calculationDate;

    @JsonProperty("base_amount")
    private BigDecimal baseAmount;

    @JsonProperty("interest_rate")
    private BigDecimal interestRate;

    @JsonProperty("interest_amount")
    private BigDecimal interestAmount;

    private InterestAccrualStatus status;

    @JsonProperty("posted_at")
    private OffsetDateTime postedAt;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
