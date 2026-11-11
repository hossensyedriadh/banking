package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.AccountProductStatus;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.AccountProductType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CoreBankingAccountProductResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -3672495369837667278L;

    private Long id;

    @JsonProperty("product_code")
    private String productCode;

    private String name;

    private String description;

    @JsonProperty("product_type")
    private AccountProductType productType;

    private AccountProductStatus status;
}
