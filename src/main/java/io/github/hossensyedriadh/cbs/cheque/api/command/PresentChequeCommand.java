package io.github.hossensyedriadh.cbs.cheque.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequePresentationType;
import io.github.hossensyedriadh.cbs.cheque.api.valdiation.ValidChequePresentation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@ValidChequePresentation
public class PresentChequeCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 8219889290011483216L;

    @NotNull
    @JsonProperty("cheque_number")
    private Long chequeNumber;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    @JsonProperty("presentation_type")
    private ChequePresentationType presentationType;

    @Size(max = 30)
    @JsonProperty("presenting_bank_code")
    private String presentingBankCode;

    @Size(max = 100)
    @JsonProperty("presenting_account_no")
    private String presentingAccountNo;
}
