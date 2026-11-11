package io.github.hossensyedriadh.cbs.ledger.internal.reconciliation;

import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.ledger.api.model.ReconciliationResult;
import io.github.hossensyedriadh.cbs.ledger.api.service.LedgerReconciliationService;
import io.github.hossensyedriadh.cbs.ledger.internal.domain.LedgerAccount;
import io.github.hossensyedriadh.cbs.ledger.internal.projection.LedgerReconciliationResult;
import io.github.hossensyedriadh.cbs.ledger.internal.repository.LedgerAccountRepository;
import io.github.hossensyedriadh.cbs.ledger.internal.repository.SubLedgerAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@Transactional(readOnly = true)
public class LedgerReconciliationServiceImpl implements LedgerReconciliationService {
    private final LedgerAccountRepository ledgerAccountRepository;
    private final SubLedgerAccountRepository subLedgerAccountRepository;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public LedgerReconciliationServiceImpl(LedgerAccountRepository ledgerAccountRepository, SubLedgerAccountRepository subLedgerAccountRepository, HttpServletRequest httpServletRequest) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.subLedgerAccountRepository = subLedgerAccountRepository;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public ReconciliationResult reconcileSubLedgerWithGL(Long glAccountId) {
        LedgerAccount glAccount = this.ledgerAccountRepository.findById(glAccountId).orElseThrow(() -> {
            log.warn("GL account not found with id {}", glAccountId);
            return new ApiResourceException("GL account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        BigDecimal glBalance = glAccount.getBalance();

        BigDecimal subLedgerBalance = this.subLedgerAccountRepository.sumBalanceByLedgerAccountId(glAccountId);

        BigDecimal difference = glBalance.subtract(subLedgerBalance);

        return new ReconciliationResult(glAccount.getId(), glAccount.getAccountCode(), glBalance,
                subLedgerBalance, difference, difference.compareTo(BigDecimal.ZERO) == 0);
    }

    @Override
    public void reconcile() {
        List<LedgerAccount> ledgerAccounts = this.ledgerAccountRepository.findAccountsWithSubLedgers();

        if (ledgerAccounts.isEmpty()) {
            log.info("No ledger accounts found for reconciliation");
            return;
        }

        List<LedgerReconciliationResult> discrepancies = new ArrayList<>();

        ledgerAccounts.forEach(ledgerAccount -> {
            BigDecimal glBalance = ledgerAccount.getBalance();
            BigDecimal subLedgerBalance = this.subLedgerAccountRepository.sumBalanceByLedgerAccountId(ledgerAccount.getId());

            if (glBalance.compareTo(subLedgerBalance) != 0) {
                BigDecimal difference = glBalance.subtract(subLedgerBalance);

                discrepancies.add(new LedgerReconciliationResult(ledgerAccount.getId(), ledgerAccount.getAccountCode(),
                        glBalance, subLedgerBalance, difference));
            }
        });

        if (!discrepancies.isEmpty()) {
            log.error("Ledger reconciliation failed, {} discrepancies found", discrepancies.size());

            discrepancies.forEach(d -> {
                log.error("Ledger reconciliation discrepancy: ledgerAccountId={}, code={}, glBalance={}, subLedgerBalance={}, difference={}",
                        d.ledgerAccountId(), d.ledgerAccountCode(), d.glBalance(), d.subLedgerBalance(), d.difference()
                );
            });

            throw new RuntimeException("Ledger reconciliation failed with " + discrepancies.size() + " discrepancies");
        }
    }
}
