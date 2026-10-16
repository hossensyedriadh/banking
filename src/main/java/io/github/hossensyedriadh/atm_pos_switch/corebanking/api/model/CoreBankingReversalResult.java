package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CoreBankingReversalResult implements Serializable {
    @Serial
    private static final long serialVersionUID = -8685012511468411406L;

    private boolean successful;

    private Long cbsOriginalTransactionId;

    private Long cbsReversalTransactionId;

    private String responseCode;
}
