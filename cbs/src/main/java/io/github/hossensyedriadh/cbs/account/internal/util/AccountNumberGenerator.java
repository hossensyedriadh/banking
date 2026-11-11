package io.github.hossensyedriadh.cbs.account.internal.util;

import java.util.concurrent.atomic.AtomicLong;

public final class AccountNumberGenerator {
    private static final AtomicLong SEQUENCE = new AtomicLong(1);

    private AccountNumberGenerator() {
    }

    public static String generate(String branchCode, String productCode) {
        validateNumeric(branchCode, "Branch code");
        validateNumeric(productCode, "Product code");

        return generateInternal(branchCode, productCode);
    }

    public static String generate(long branchCode, long productCode) {
        return generate(String.valueOf(branchCode), String.valueOf(productCode));
    }

    private static String generateInternal(String branchCode, String productCode) {
        long sequence = SEQUENCE.getAndIncrement();

        if (sequence > 9_999_999) {
            throw new IllegalStateException("Account number sequence exhausted");
        }

        String base = String.format("%04d%02d%07d", Long.parseLong(branchCode), Long.parseLong(productCode), sequence);
        int checkDigit = calculateCheckDigit(base);

        return base + checkDigit;
    }

    private static int calculateCheckDigit(String accountNumber) {
        int sum = 0;

        for (int i = 0; i < accountNumber.length(); i++) {
            int digit = accountNumber.charAt(i) - '0';
            int weight = (i % 2 == 0) ? 2 : 1;
            int product = digit * weight;
            sum += product / 10;
            sum += product % 10;
        }

        return (10 - (sum % 10)) % 10;
    }

    private static void validateNumeric(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be null or blank");
        }
        if (!value.matches("\\d+")) {
            throw new IllegalArgumentException(field + " must contain only digits");
        }
    }
}
