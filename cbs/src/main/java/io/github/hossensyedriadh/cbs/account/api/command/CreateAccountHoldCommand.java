package io.github.hossensyedriadh.cbs.account.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class CreateAccountHoldCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 2714967494631483666L;

    @NotNull
    @JsonProperty("account_id")
    private Long accountId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    @Size(max = 255)
    private String reason;

    @Size(max = 100)
    private String reference;

    @Future
    @JsonProperty("expires_at")
    private OffsetDateTime expiresAt;
}
