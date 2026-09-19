package io.github.hossensyedriadh.cbs.ledger.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.ledger.api.enumerator.LedgerAccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CreateLedgerAccountCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 8768852460851988509L;

    @NotBlank
    @JsonProperty("account_code")
    private String accountCode;

    @NotBlank
    private String name;

    @NotNull
    @JsonProperty("currency_id")
    private Long currencyId;

    @NotNull
    @JsonProperty("account_type")
    private LedgerAccountType accountType;
}
