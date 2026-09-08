package io.github.hossensyedriadh.cbs.product.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CreateCurrencyCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -109698950084191822L;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotBlank
    private String symbol;

    @JsonProperty("decimal_places")
    private Short decimalPlaces;
}
