package io.github.hossensyedriadh.atm_terminal.iso.decoder;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class AdditionalAmountDecoder {
    private static final int ENTRY_LENGTH = 20;

    public record AdditionalAmount(String accountType, String amountType, String currencyCode, char sign,
                                   String amountMinorUnits) {
        public BigDecimal amount(int decimalPlaces) {
            BigDecimal value = new BigDecimal(amountMinorUnits).movePointLeft(decimalPlaces);

            return sign == 'D' ? value.negate() : value;
        }
    }

    public static List<AdditionalAmount> decode(String de54) {
        if (de54 == null || de54.isBlank()) {
            return List.of();
        }

        if (de54.length() % ENTRY_LENGTH != 0) {
            throw new IllegalArgumentException("Invalid DE54 length: " + de54.length());
        }

        List<AdditionalAmount> amounts = new ArrayList<>();

        for (int i = 0; i < de54.length(); i += ENTRY_LENGTH) {
            String entry = de54.substring(i, i + ENTRY_LENGTH);
            char sign = entry.charAt(7);

            if (sign != 'C' && sign != 'D') {
                throw new IllegalArgumentException("Invalid DE54 sign: " + sign);
            }

            amounts.add(new AdditionalAmount(entry.substring(0, 2), entry.substring(2, 4),
                    entry.substring(4, 7), sign, entry.substring(8, 20)));
        }

        return List.copyOf(amounts);
    }
}
