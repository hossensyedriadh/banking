package io.github.hossensyedriadh.cbs.product.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CreateCurrencyCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -109698950084191822L;

    @Size(max = 3)
    @NotBlank
    private String code;

    @Size(max = 100)
    @NotBlank
    private String name;

    @Size(max = 10)
    @NotBlank
    private String symbol;

    @JsonProperty("decimal_places")
    private Short decimalPlaces;
}
