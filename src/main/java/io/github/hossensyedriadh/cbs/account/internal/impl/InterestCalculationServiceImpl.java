package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.service.InterestCalculationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Log4j2
@Service
@Transactional(readOnly = true)
public class InterestCalculationServiceImpl implements InterestCalculationService {
    @Override
    public BigDecimal calculateDailyInterest(BigDecimal balance, BigDecimal interestRate) {
        if (balance.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        return balance.multiply(interestRate).divide(BigDecimal.valueOf(36500), 4, RoundingMode.HALF_UP); //36500 as "balance × rate / 100 / 365"
    }
}
