package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.AccountStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CoreBankingAccountResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 205338155632726477L;

    private Long id;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("currency_id")
    private Long currencyId;

    private AccountStatus status;
}
