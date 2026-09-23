package io.github.hossensyedriadh.cbs.ledger.api.command;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class UpdateLedgerAccountCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 8683497656641020570L;

    @Size(max = 150)
    private String name;
}
