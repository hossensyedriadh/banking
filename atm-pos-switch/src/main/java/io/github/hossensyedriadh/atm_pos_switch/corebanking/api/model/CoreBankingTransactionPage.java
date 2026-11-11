package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import java.util.List;

public record CoreBankingTransactionPage(List<CoreBankingTransactionResponse> content, int page, int size, int number,
                                         int totalElements, int totalPages) {
}
