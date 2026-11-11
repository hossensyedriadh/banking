package io.github.hossensyedriadh.cbs.eod.api.service;

import java.time.LocalDate;

public interface BusinessDateService {
    LocalDate getCurrentBusinessDate();

    void rolloverBusinessDate();
}
