package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

public record CoreBankingFundTransferResult(boolean successful, Long cbsTransactionId, String responseCode) {
}
