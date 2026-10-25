package io.github.hossensyedriadh.atm_terminal.service;

import io.github.hossensyedriadh.atm_terminal.configuration.properties.AtmTerminalProperties;
import io.github.hossensyedriadh.atm_terminal.iso.IsoTcpClient;
import io.github.hossensyedriadh.atm_terminal.model.WithdrawalResult;
import io.github.hossensyedriadh.atm_terminal.util.ProcessingCodes;
import io.github.hossensyedriadh.atm_terminal.util.ResponseCodes;
import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Log4j2
@Service
public class WithdrawalService {
    private static final String CURRENCY_BDT = "050";
    private static final DateTimeFormatter TRANSMISSION_DATE_TIME = DateTimeFormatter.ofPattern("MMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final IsoTcpClient isoTcpClient;
    private final AtmTerminalProperties atmTerminalProperties;

    @Value("${atm.timezone}")
    private ZoneId timezone;

    @Autowired
    public WithdrawalService(IsoTcpClient isoTcpClient, AtmTerminalProperties atmTerminalProperties) {
        this.isoTcpClient = isoTcpClient;
        this.atmTerminalProperties = atmTerminalProperties;
    }

    public WithdrawalResult withdraw(String pan, BigDecimal amount) throws Exception {
        this.validateAmount(amount);

        long amountMinorUnits = amount.setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2).longValueExact();

        ISOMsg request = new ISOMsg();
        request.setMTI("0200");
        request.set(2, pan);
        request.set(3, ProcessingCodes.CASH_WITHDRAWAL);
        request.set(4, String.format(Locale.ROOT, "%012d", amountMinorUnits));
        request.set(7, ZonedDateTime.now(this.timezone).format(TRANSMISSION_DATE_TIME));
        request.set(11, String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000)));
        request.set(37, this.generateRetrievalReferenceNumber());
        request.set(41, this.atmTerminalProperties.terminalId());
        request.set(49, CURRENCY_BDT);

        ISOMsg response = this.isoTcpClient.send(request);

        if (response == null) {
            log.error("No response received from the ATM switch");
            throw new IllegalStateException("No response received from the ATM switch");
        }

        if (!"0210".equals(response.getMTI())) {
            log.error("Unexpected ISO response MTI: {} for withdrawal request", response.getMTI());
            throw new IllegalStateException("Unexpected ISO response MTI: {} for withdrawal request" + response.getMTI());
        }

        String responseCode = response.hasField(39) ? response.getString(39) : null;

        boolean successful = ResponseCodes.APPROVED.equals(responseCode);

        return new WithdrawalResult(successful, amount, "BDT",
                responseCode, successful ? "Withdrawal successful" : "Withdrawal declined (Response Code: " + responseCode + ")");
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            log.error("Withdrawal amount must be greater than zero");
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }

        if (amount.remainder(new BigDecimal(500)).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Withdrawal amount must be in multiples of 500");
        }

        long minorUnits;

        try {
            minorUnits = amount.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
        } catch (ArithmeticException e) {
            log.error("Withdrawal amount must have at most two decimal places", e);
            throw new IllegalArgumentException("Withdrawal amount must have at most two decimal places", e);
        }

        if (minorUnits > 999_999_999_999L) {
            log.error("Withdrawal amount exceeds the supported ISO field size");
            throw new IllegalArgumentException("Withdrawal amount exceeds the supported ISO field size");
        }
    }

    private String generateRetrievalReferenceNumber() {
        StringBuilder reference = new StringBuilder(12);

        for (int i = 0; i < 12; i++) {
            reference.append(RANDOM.nextInt(10));
        }

        return reference.toString();
    }
}
