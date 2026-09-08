package io.github.hossensyedriadh.cbs.account.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class CreateAccountCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -5996324746946804103L;

    @NotNull
    @JsonProperty("product_id")
    private Long productId;

    @NotNull
    @JsonProperty("branch_id")
    private Long branchId;

    @NotNull
    @JsonProperty("currency_id")
    private Long currencyId;

    @NotEmpty
    @JsonProperty("customer_ids")
    private List<@NotNull Long> customerIds;
}
