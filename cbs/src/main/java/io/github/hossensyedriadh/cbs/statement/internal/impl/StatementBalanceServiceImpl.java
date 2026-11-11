package io.github.hossensyedriadh.cbs.statement.internal.impl;

import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.ledger.api.service.LedgerService;
import io.github.hossensyedriadh.cbs.statement.internal.service.StatementBalanceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Log4j2
@Service
public class StatementBalanceServiceImpl implements StatementBalanceService {
    private final LedgerService ledgerService;

    @Value("${cbs.timezone}")
    private ZoneId cbsTimezone;

    @Autowired
    public StatementBalanceServiceImpl(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @Override
    public BigDecimal getOpeningBalance(Long accountId, LocalDate statementPeriodStart) {
        OffsetDateTime cutOff = statementPeriodStart.atStartOfDay(this.cbsTimezone).toOffsetDateTime();

        Long subLedgerAccountId = this.ledgerService.getSubLedgerAccountIdByAccountId(accountId)
                .orElseThrow(() -> {
                    log.error("No subledger account found for account id {}", accountId);
                    return new ApiResourceException("No subledger account mapped with account", HttpStatus.INTERNAL_SERVER_ERROR);
                });

        return this.ledgerService.getBalanceAt(subLedgerAccountId, cutOff);
    }
}
