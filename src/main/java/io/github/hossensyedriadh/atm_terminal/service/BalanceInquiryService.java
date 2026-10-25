package io.github.hossensyedriadh.atm_terminal.service;

import io.github.hossensyedriadh.atm_terminal.configuration.properties.AtmTerminalProperties;
import io.github.hossensyedriadh.atm_terminal.enumerator.BalanceAmountType;
import io.github.hossensyedriadh.atm_terminal.iso.IsoTcpClient;
import io.github.hossensyedriadh.atm_terminal.iso.decoder.AdditionalAmountDecoder;
import io.github.hossensyedriadh.atm_terminal.model.BalanceInquiryResult;
import io.github.hossensyedriadh.atm_terminal.util.ProcessingCodes;
import io.github.hossensyedriadh.atm_terminal.util.ResponseCodes;
import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Log4j2
@Service
public class BalanceInquiryService {
    private final IsoTcpClient isoTcpClient;
    private final AtmTerminalProperties atmTerminalProperties;
    private static final DateTimeFormatter TRANSMISSION_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMddHHmmss");

    @Value("${atm.timezone}")
    private ZoneId timezone;

    @Autowired
    public BalanceInquiryService(IsoTcpClient isoTcpClient, AtmTerminalProperties atmTerminalProperties) {
        this.isoTcpClient = isoTcpClient;
        this.atmTerminalProperties = atmTerminalProperties;
    }

    public BalanceInquiryResult inquire(String pan, String sessionToken) throws Exception {
        if (sessionToken == null || sessionToken.isBlank()) {
            throw new IllegalArgumentException("An active ATM session is required");
        }

        ISOMsg request = new ISOMsg("0200");
        request.set(2, pan);
        request.set(3, ProcessingCodes.BALANCE_INQUIRY);
        request.set(4, "000000000000");
        request.set(7, LocalDateTime.now(this.timezone).format(TRANSMISSION_DATE_TIME_FORMATTER));
        request.set(11, "%06d".formatted(ThreadLocalRandom.current().nextInt(1_000_000)));
        request.set(37, "%012d".formatted(ThreadLocalRandom.current().nextLong(1_000_000_000_000L)));
        request.set(41, this.atmTerminalProperties.terminalId());
        request.set(49, "050"); // BDT currency code
        request.set(62, sessionToken);

        ISOMsg response = isoTcpClient.send(request);

        if (!"0210".equals(response.getMTI()) || !response.hasField(39)) {
            log.error("Invalid balance inquiry response");
            throw new IllegalStateException("Invalid balance inquiry response");
        }

        String responseCode = response.getString(39);

        if (!ResponseCodes.APPROVED.equals(responseCode)) {
            log.info("Balance inquiry declined with response code {}", responseCode);
            throw new IllegalStateException("Balance inquiry failed. Response code: " + responseCode);
        }

        if (!response.hasField(54)) {
            throw new IllegalStateException("Balance inquiry response contains no DE54");
        }

        String de54 = response.getString(54);

        List<AdditionalAmountDecoder.AdditionalAmount> amounts = AdditionalAmountDecoder.decode(de54);

        return amounts.stream().filter(a -> BalanceAmountType.AVAILABLE.getDe54Code().equals(a.amountType()))
                .filter(a -> "050".equals(a.currencyCode()))
                .findFirst()
                .map(a -> new BalanceInquiryResult(true, a.amount(2), "BDT", null))
                .orElseThrow(() -> new IllegalStateException("Available BDT balance was not returned"));
    }
}
