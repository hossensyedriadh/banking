package io.github.hossensyedriadh.cbs.ledger.internal.reconciliation;

import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.ledger.api.model.ReconciliationResult;
import io.github.hossensyedriadh.cbs.ledger.api.service.LedgerReconciliationService;
import io.github.hossensyedriadh.cbs.ledger.internal.domain.LedgerAccount;
import io.github.hossensyedriadh.cbs.ledger.internal.repository.LedgerAccountRepository;
import io.github.hossensyedriadh.cbs.ledger.internal.repository.SubLedgerAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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

    protected void reconcileAllSubLedgers() {
        this.ledgerAccountRepository.findSubLedgerReconciliationRows().forEach(row -> {
            BigDecimal difference = row.glBalance().subtract(row.subLedgerBalance());

            if (difference.compareTo(BigDecimal.ZERO) != 0) {
                log.error("SubLedger reconciliation failed for GL {} ({}): GL balance={}, SubLedger balance={}, difference={}",
                        row.glAccountId(), row.accountCode(), row.glBalance(), row.subLedgerBalance(), difference);
            }
        });
    }
}
