package io.github.hossensyedriadh.atm_terminal.service;

import io.github.hossensyedriadh.atm_terminal.enumerator.AuthenticationResult;
import io.github.hossensyedriadh.atm_terminal.iso.IsoTcpClient;
import io.github.hossensyedriadh.atm_terminal.iso.request.PinVerificationRequestFactory;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            return AuthenticationResult.SYSTEM_ERROR;
        }

        return switch (response.getString(39)) {
            case "00" -> AuthenticationResult.SUCCESS;
            case "55" -> AuthenticationResult.INVALID_PIN;
            case "75" -> AuthenticationResult.PIN_BLOCKED;
            default -> AuthenticationResult.SYSTEM_ERROR;
        };
    }
}
