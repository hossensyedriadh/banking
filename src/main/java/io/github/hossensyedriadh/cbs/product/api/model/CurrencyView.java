package io.github.hossensyedriadh.cbs.product.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
public class CurrencyView implements Serializable {
    @Serial
    private static final long serialVersionUID = 4981247814530788407L;

    private Long id;

    private String code;

    @JsonProperty("numeric_code")
    private String numericCode;

    private String name;

    private String symbol;

    @JsonProperty("decimal_places")
    private Short decimalPlaces;

    private boolean active;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("last_updated_at")
    private OffsetDateTime lastUpdatedAt;
}
