package io.github.hossensyedriadh.cbs.product.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.product.api.enumerator.AccountProductStatus;
import io.github.hossensyedriadh.cbs.product.api.enumerator.AccountProductType;
import io.github.hossensyedriadh.cbs.product.api.enumerator.InterestCalculationMethod;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class AccountProductView implements Serializable {
    @Serial
    private static final long serialVersionUID = 5079154915663864112L;

    private Long id;

    @JsonProperty("product_code")
    private String productCode;

    private String name;

    private String description;

    @JsonProperty("product_type")
    private AccountProductType productType;

    private AccountProductStatus status;

    @JsonProperty("interest_bearing")
    private boolean interestBearing;

    @JsonProperty("interest_rate")
    private BigDecimal interestRate;

    @JsonProperty("interest_calculation_method")
    private InterestCalculationMethod interestCalculationMethod;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("last_updated_at")
    private OffsetDateTime lastUpdatedAt;
}
