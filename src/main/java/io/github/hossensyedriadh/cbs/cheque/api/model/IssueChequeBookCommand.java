package io.github.hossensyedriadh.cbs.cheque.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class IssueChequeBookCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -5690065708771263925L;

    @NotNull
    @JsonProperty("account_id")
    private Long accountId;

    @NotNull
    @JsonProperty("cheque_product_id")
    private Long chequeProductId;
}
