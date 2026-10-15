package io.github.hossensyedriadh.atm_pos_switch.iso8583.encoder;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AdditionalAmountEncoder {
    private static final int AMOUNT_LENGTH = 12;

    public String encode(String accountType, String amountType, String currencyCode, char sign, long amountMinorUnits) {
        if (accountType == null || !accountType.matches("\\d{2}")) {
            throw new IllegalArgumentException("Account type must be 2 digits");
        }

        if (amountType == null || !amountType.matches("\\d{2}")) {
            throw new IllegalArgumentException("Amount type must be 2 digits");
        }

        if (currencyCode == null || !currencyCode.matches("\\d{3}")) {
            throw new IllegalArgumentException("Currency code must be 3 digits");
        }

        if (sign != 'C' && sign != 'D') {
            throw new IllegalArgumentException("Sign must be C or D");
        }

        if (amountMinorUnits < 0 || amountMinorUnits > 999_999_999_999L) {
            throw new IllegalArgumentException("Amount is out of range");
        }

        String amount = String.format(Locale.ROOT, "%0" + AMOUNT_LENGTH + "d", amountMinorUnits);

        return accountType + amountType + currencyCode + sign + amount;
    }
}
