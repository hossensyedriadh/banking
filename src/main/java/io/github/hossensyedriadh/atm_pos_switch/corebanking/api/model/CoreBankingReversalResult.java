package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

public record CoreBankingReversalResult(boolean successful, Long cbsOriginalTransactionId,
                                        Long cbsReversalTransactionId, String responseCode) {
}
