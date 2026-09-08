package io.github.hossensyedriadh.cbs.ledger.api.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class UpdateLedgerAccountCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 8683497656641020570L;

    private String name;
}
