package io.github.hossensyedriadh.cbs.statement.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.YearMonth;

@Getter
@Setter
public class GenerateMonthlyStatementCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 6695908039323547814L;

    @NotNull
    @JsonProperty("account_id")
    private Long accountId;

    @NotNull
    @PastOrPresent
    private YearMonth period;
}
