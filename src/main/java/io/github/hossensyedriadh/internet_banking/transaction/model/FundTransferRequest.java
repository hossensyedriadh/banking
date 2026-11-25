package io.github.hossensyedriadh.internet_banking.transaction.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
public class FundTransferRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -8609686310827933477L;

    @NotNull
    private Long accountId;

    @NotNull
    private UUID beneficiaryId;

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
