package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CoreBankingErrorResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -4410518637667052941L;

    private OffsetDateTime timestamp;

    private int status;

    private String error;

    private String message;

    private String path;
}
