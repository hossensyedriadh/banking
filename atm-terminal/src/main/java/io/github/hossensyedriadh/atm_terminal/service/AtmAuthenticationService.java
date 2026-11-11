package io.github.hossensyedriadh.atm_terminal.service;

import io.github.hossensyedriadh.atm_terminal.enumerator.AuthenticationStatus;
import io.github.hossensyedriadh.atm_terminal.iso.IsoTcpClient;
import io.github.hossensyedriadh.atm_terminal.iso.request.PinVerificationRequestFactory;
import io.github.hossensyedriadh.atm_terminal.model.AuthenticationResult;
import io.github.hossensyedriadh.atm_terminal.util.ResponseCodes;
import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class AtmAuthenticationService {
    private final PinVerificationRequestFactory pinVerificationRequestFactory;
    private final IsoTcpClient isoTcpClient;

    @Autowired
    public AtmAuthenticationService(PinVerificationRequestFactory pinVerificationRequestFactory, IsoTcpClient isoTcpClient) {
        this.pinVerificationRequestFactory = pinVerificationRequestFactory;
        this.isoTcpClient = isoTcpClient;
    }

    public AuthenticationResult authenticate(String pan, String pin) throws Exception {
        ISOMsg request = this.pinVerificationRequestFactory.create(pan, pin);
        ISOMsg response = this.isoTcpClient.send(request);

        if (!"0110".equals(response.getMTI()) || !response.hasField(39)) {
            return AuthenticationResult.failure(AuthenticationStatus.SYSTEM_ERROR);
        }

        String responseCode = response.getString(39);

        if (responseCode == null || responseCode.isBlank()) {
            return AuthenticationResult.failure(AuthenticationStatus.SYSTEM_ERROR);
        }

        return switch (responseCode) {
            case ResponseCodes.APPROVED -> {
                String sessionToken = response.hasField(62) ? response.getString(62) : null;

                if (sessionToken == null || sessionToken.isBlank()) {
                    yield AuthenticationResult.failure(AuthenticationStatus.SYSTEM_ERROR);
                }

                yield AuthenticationResult.success(sessionToken);
            }
            case ResponseCodes.INVALID_PIN -> AuthenticationResult.failure(AuthenticationStatus.INVALID_PIN);
            case ResponseCodes.PIN_BLOCKED -> AuthenticationResult.failure(AuthenticationStatus.PIN_BLOCKED);
            default -> AuthenticationResult.failure(AuthenticationStatus.SYSTEM_ERROR);
        };
    }
}
