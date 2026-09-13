package io.github.hossensyedriadh.cbs.account.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CreateAccountRestrictionCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 7826962309817763608L;

    @NotNull
    @JsonProperty("account_id")
    private Long accountId;

    @NotNull
    private AccountRestrictionType type;

    @NotBlank
    private String reason;
}
