package io.github.hossensyedriadh.cbs.product.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.product.api.enumerator.CardNetwork;
import io.github.hossensyedriadh.cbs.product.api.enumerator.CardProductStatus;
import io.github.hossensyedriadh.cbs.product.api.enumerator.CardType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class CardProductView implements Serializable {
    @Serial
    private static final long serialVersionUID = -5458288794437514039L;

    private Long id;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("product_name")
    private String productName;

    private String description;

    @JsonProperty("card_type")
    private CardType cardType;

    @JsonProperty("card_network")
    private CardNetwork cardNetwork;

    @JsonProperty("validity_years")
    private Integer validityYears;

    @JsonProperty("issuance_fee")
    private BigDecimal issuanceFee;

    @JsonProperty("annual_fee")
    private BigDecimal annualFee;

    @JsonProperty("replacement_fee")
    private BigDecimal replacementFee;

    private CardProductStatus status;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("last_updated_at")
    private OffsetDateTime lastUpdatedAt;
}
