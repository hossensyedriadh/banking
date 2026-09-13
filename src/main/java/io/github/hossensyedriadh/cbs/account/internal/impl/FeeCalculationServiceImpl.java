package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.FeeCalculationService;
import io.github.hossensyedriadh.cbs.account.api.enumerator.FeeRuleStatus;
import io.github.hossensyedriadh.cbs.account.internal.domain.FeeRule;
import io.github.hossensyedriadh.cbs.account.internal.repository.FeeRuleRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Log4j2
@Service
@Transactional(readOnly = true)
public class FeeCalculationServiceImpl implements FeeCalculationService {
    private final FeeRuleRepository feeRuleRepository;

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int MONEY_SCALE = 4;

    @Autowired
    public FeeCalculationServiceImpl(FeeRuleRepository feeRuleRepository) {
        this.feeRuleRepository = feeRuleRepository;
    }

    @Override
    public BigDecimal calculateFee(String feeCode, BigDecimal transactionAmount, Long currencyId) {
        if (transactionAmount == null || transactionAmount.signum() <= 0) {
            log.warn("Transaction amount must be greater than zero");
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }

        FeeRule feeRule = this.feeRuleRepository.findByFeeCodeAndStatus(feeCode, FeeRuleStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Active Fee rule with code {} not found", feeCode);
                    return new IllegalStateException("Active fee rule not found with code: " + feeCode);
                });

        this.validateCurrency(feeRule, currencyId);

        return switch (feeRule.getFeeType()) {
            case FIXED -> feeRule.getFixedAmount().setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            case PERCENTAGE -> this.calculatePercentageFee(transactionAmount, feeRule.getPercentage());
        };
    }

    private BigDecimal calculatePercentageFee(BigDecimal transactionAmount, BigDecimal percentage) {
        return transactionAmount.multiply(percentage).divide(ONE_HUNDRED, MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private void validateCurrency(FeeRule feeRule, Long currencyId) {
        if (feeRule.getCurrencyId() != null && !feeRule.getCurrencyId().equals(currencyId)) {
            log.warn("Fee currency id {} does not match currency id {}", feeRule.getCurrencyId(), currencyId);
            throw new IllegalArgumentException(String.format("Fee ID: %d currency does not match currency ID: %d", feeRule.getCurrencyId(), currencyId));
        }
    }
}
