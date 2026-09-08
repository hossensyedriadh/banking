package io.github.hossensyedriadh.cbs.ledger.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.ledger.api.enumerator.JournalEntryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JournalEntryLine implements Serializable {
    @Serial
    private static final long serialVersionUID = 4480510113372651118L;

    @JsonProperty("ledger_account_id")
    private Long ledgerAccountId;

    @JsonProperty("entry_type")
    private JournalEntryType entryType;

    private BigDecimal amount;
}
