package io.github.hossensyedriadh.cbs.ledger.internal.impl;

import io.github.hossensyedriadh.cbs.ledger.api.model.TrialBalanceResult;
import io.github.hossensyedriadh.cbs.ledger.api.service.TrialBalanceService;
import io.github.hossensyedriadh.cbs.ledger.internal.domain.LedgerAccount;
import io.github.hossensyedriadh.cbs.ledger.internal.repository.LedgerAccountRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Log4j2
@Service
@Transactional(readOnly = true)
public class TrialBalanceServiceImpl implements TrialBalanceService {
    private final LedgerAccountRepository ledgerAccountRepository;

    @Autowired
    public TrialBalanceServiceImpl(LedgerAccountRepository ledgerAccountRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
    }

    @Override
    public TrialBalanceResult generate() {
        List<LedgerAccount> ledgerAccounts = this.ledgerAccountRepository.findAll();

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (LedgerAccount ledgerAccount : ledgerAccounts) {
            BigDecimal balance = ledgerAccount.getBalance();

            if (ledgerAccount.getAccountType().isCredit()) {
                totalCredits = totalCredits.add(balance);
            } else {
                totalDebits = totalDebits.add(balance);
            }
        }

        BigDecimal difference = totalDebits.subtract(totalCredits);

        TrialBalanceResult result = new TrialBalanceResult(totalDebits, totalCredits, difference);

        if (!result.isBalanced()) {
            log.error("Trial balance is unbalanced. Total debits: {}, Total credits: {}, Difference: {}",
                    totalDebits, totalCredits, difference);
            throw new RuntimeException("Trial balance is unbalanced. Total debits: " + totalDebits +
                    ", Total Credits: " + totalCredits + ", Difference: " + difference);
        }

        return result;
    }
}
