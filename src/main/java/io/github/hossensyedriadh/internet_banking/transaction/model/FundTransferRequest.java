package io.github.hossensyedriadh.internet_banking.transaction.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FundTransferRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -8609686310827933477L;

    @NotNull
    private Long accountId;

    @NotNull
    private Long destinationAccountId;

    @NotNull
    private Long currencyId;

    @NotNull
    @Positive
    @Digits(integer = 15, fraction = 4)
    private BigDecimal amount;

    @NotBlank
    @Size(max = 255)
    private String description;
}
