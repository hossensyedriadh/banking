package io.github.hossensyedriadh.atm_pos_switch.corebanking.internal;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingErrorResponse;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ResponseCodes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CoreBankingResponseCodeMapper {
    public Optional<String> toIsoResponseCode(CoreBankingErrorResponse errorResponse) {
        if (errorResponse == null || errorResponse.getMessage() == null) {
            return Optional.empty();
        }

        if (errorResponse.getStatus() != HttpStatus.BAD_REQUEST.value()) {
            return Optional.empty();
        }

        return switch (errorResponse.getMessage()) {
            case "Insufficient funds" -> Optional.of(ResponseCodes.INSUFFICIENT_FUNDS);
            case "SubLedger account not found" -> Optional.of(ResponseCodes.INVALID_ACCOUNT);
            default -> Optional.empty();
        };
    }
}
