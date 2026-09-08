package io.github.hossensyedriadh.cbs.product.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.product.api.enumerator.AccountProductType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

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

    private boolean active;
}
