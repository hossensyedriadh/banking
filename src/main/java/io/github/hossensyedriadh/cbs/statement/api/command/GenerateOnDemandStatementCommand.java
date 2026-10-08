package io.github.hossensyedriadh.cbs.statement.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class GenerateOnDemandStatementCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -5705863352684405068L;

    @NotNull
    @JsonProperty("account_id")
    private Long accountId;

    @NotNull
    @PastOrPresent
    @JsonProperty("from_date")
    private LocalDate fromDate;

    @NotNull
    @PastOrPresent
    @JsonProperty("to_date")
    private LocalDate toDate;
}
