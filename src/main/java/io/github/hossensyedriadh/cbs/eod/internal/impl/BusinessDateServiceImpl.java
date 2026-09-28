package io.github.hossensyedriadh.cbs.eod.internal.impl;

import io.github.hossensyedriadh.cbs.eod.api.service.BusinessDateService;
import io.github.hossensyedriadh.cbs.eod.internal.domain.BusinessDate;
import io.github.hossensyedriadh.cbs.eod.internal.repository.BusinessDateRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Log4j2
@Service
@Transactional(readOnly = true)
public class BusinessDateServiceImpl implements BusinessDateService {
    private final BusinessDateRepository businessDateRepository;

    @Autowired
    public BusinessDateServiceImpl(BusinessDateRepository businessDateRepository) {
        this.businessDateRepository = businessDateRepository;
    }

    @Override
    public LocalDate getCurrentBusinessDate() {
        return this.businessDateRepository.findCurrent().orElseThrow(() -> {
            log.warn("Business date not initialized");
            return new IllegalStateException("Business date not initialized");
        }).getCurrentBusinessDate();
    }

    @Transactional
    @Override
    public void rolloverBusinessDate() {
        BusinessDate businessDate = this.businessDateRepository.findCurrentForUpdate()
                .orElseThrow(() -> {
                    log.warn("Business date not initialized");
                    return new IllegalStateException("Business date not initialized");
                });

        businessDate.setCurrentBusinessDate(businessDate.getCurrentBusinessDate().plusDays(1));

        this.businessDateRepository.save(businessDate);

        log.info("Business date rolled over to {}", businessDate.getCurrentBusinessDate());
    }
}
