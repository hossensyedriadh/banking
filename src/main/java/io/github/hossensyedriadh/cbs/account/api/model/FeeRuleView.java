package io.github.hossensyedriadh.cbs.account.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.account.api.enumerator.FeeRuleStatus;
import io.github.hossensyedriadh.cbs.account.api.enumerator.FeeType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class FeeRuleView implements Serializable {
    @Serial
    private static final long serialVersionUID = -8473694529438112301L;

    private Long id;

    @JsonProperty("fee_code")
    private String feeCode;

    @JsonProperty("fee_name")
    private String feeName;

    @JsonProperty("fee_type")
    private FeeType feeType;

    @JsonProperty("fixed_amount")
    private BigDecimal fixedAmount;

    private BigDecimal percentage;

    @JsonProperty("currency_id")
    private Long currencyId;

    private FeeRuleStatus status;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
