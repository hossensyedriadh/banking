package io.github.hossensyedriadh.atm_terminal.service;

import io.github.hossensyedriadh.atm_terminal.iso.IsoTcpClient;
import io.github.hossensyedriadh.atm_terminal.util.ProcessingCodes;
import io.github.hossensyedriadh.atm_terminal.util.ResponseCodes;
import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Log4j2
@Service
public class AtmSessionTerminationService {
    private static final String REQUEST_MTI = "0200";
    private static final String RESPONSE_MTI = "0210";
    private static final String CURRENCY_CODE = "050";

    private final IsoTcpClient isoTcpClient;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${atm.terminal-id}")
    private String terminalId;

    @Value("${atm.timezone}")
    private ZoneId timezone;

    @Autowired
    public AtmSessionTerminationService(IsoTcpClient isoTcpClient) {
        this.isoTcpClient = isoTcpClient;
    }

    public boolean terminate(String pan, String sessionToken) {
        try {
            ISOMsg request = new ISOMsg(REQUEST_MTI);
            request.set(2, pan);
            request.set(3, ProcessingCodes.SESSION_TERMINATION);
            request.set(4, "000000000000");
            request.set(7, LocalDateTime.now(this.timezone).format(DateTimeFormatter.ofPattern("MMddHHmmss")));
            request.set(11, this.generateNumericString(6));
            request.set(37, this.generateNumericString(12));
            request.set(41, this.terminalId);
            request.set(49, CURRENCY_CODE);
            request.set(62, sessionToken);

            ISOMsg response = this.isoTcpClient.send(request);

            boolean approved = RESPONSE_MTI.equals(response.getMTI())
                    && ResponseCodes.APPROVED.equals(response.getString(39));

            if (!approved) {
                log.warn("ATM session termination was not approved");
            }

            return approved;
        } catch (Exception e) {
            log.warn("Unable to terminate ATM session with the Switch");
            return false;
        }
    }

    private String generateNumericString(int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(this.secureRandom.nextInt(10));
        }

        return sb.toString();
    }
}
