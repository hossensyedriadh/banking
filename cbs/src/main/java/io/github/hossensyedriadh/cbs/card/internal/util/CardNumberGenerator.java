package io.github.hossensyedriadh.cbs.card.internal.util;

import io.github.hossensyedriadh.cbs.product.api.enumerator.CardNetwork;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ThreadLocalRandom;

@Log4j2
public final class CardNumberGenerator {
    private static final int VISA_LENGTH = 16;
    private static final int MASTERCARD_LENGTH = 16;
    private static final int AMEX_LENGTH = 15;

    private CardNumberGenerator() {
    }

    public static String generate(CardNetwork network) {
        return switch (network) {
            case VISA -> generateVisa();
            case MASTERCARD -> generateMasterCard();
            case AMERICAN_EXPRESS -> generateAmex();
        };
    }

    private static String generateVisa() {
        return generateWithPrefix("4", VISA_LENGTH);
    }

    private static String generateMasterCard() {
        int prefix = ThreadLocalRandom.current().nextInt(51, 56);

        return generateWithPrefix(String.valueOf(prefix), MASTERCARD_LENGTH);
    }

    private static String generateAmex() {
        String prefix = ThreadLocalRandom.current().nextBoolean() ? "34" : "37";

        return generateWithPrefix(prefix, AMEX_LENGTH);
    }

    private static String generateWithPrefix(String prefix, int length) {
        if (prefix.length() >= length) {
            log.error("Card prefix must be shorter than card number length");
            throw new IllegalArgumentException("Prefix must be shorter than card number length");
        }

        StringBuilder cardNumber = new StringBuilder(length);
        cardNumber.append(prefix);

        while (cardNumber.length() < length - 1) {
            cardNumber.append(ThreadLocalRandom.current().nextInt(10));
        }

        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());

        cardNumber.append(checkDigit);

        return cardNumber.toString();
    }

    private static int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean doubleDigit = true;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';

            if (doubleDigit) {
                digit *= 2;

                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            doubleDigit = !doubleDigit;
        }

        return (10 - (sum % 10)) % 10;
    }
}
