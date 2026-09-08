package io.github.hossensyedriadh.cbs.product.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.product.api.enumerator.AccountProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CreateAccountProductCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -8440033435156743717L;

    @NotBlank
    @JsonProperty("product_code")
    private String productCode;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    @JsonProperty("product_type")
    private AccountProductType productType;
}
