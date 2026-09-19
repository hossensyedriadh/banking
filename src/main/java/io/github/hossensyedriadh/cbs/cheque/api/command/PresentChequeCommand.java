package io.github.hossensyedriadh.cbs.cheque.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class PresentChequeCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 8219889290011483216L;

    @NotNull
    @JsonProperty("cheque_number")
    private Long chequeNumber;

    @NotNull
    private BigDecimal amount;

    @JsonProperty("presenting_bank_code")
    private String presentingBankCode;

    @JsonProperty("presenting_account_no")
    private String presentingAccountNo;
}
