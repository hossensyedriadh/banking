package io.github.hossensyedriadh.internet_banking.account.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AccountBalanceModel implements Serializable {
    @Serial
    private static final long serialVersionUID = -4782589590615619048L;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal availableBalance;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ledgerBalance;
}
