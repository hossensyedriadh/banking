package io.github.hossensyedriadh.cbs.ledger.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostJournalEntryCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -132826358248246299L;

    @NotNull
    @JsonProperty("transaction_id")
    private Long transactionId;

    @NotNull
    @Size(min = 1)
    private List<JournalEntryLine> entries;
}
