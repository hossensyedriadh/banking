package io.github.hossensyedriadh.atm_pos_switch.transaction.api.model;

public record OriginalDataElements(String originalMti, String originalStan,
                                   String originalTransmissionDateTime,
                                   String acquiringInstitutionId,
                                   String forwardingInstitutionId) {
    public static OriginalDataElements parse(String value) {
        if (value == null || !value.matches("\\d{42}")) {
            throw new IllegalArgumentException("Original data elements must be exactly 42 digits");
        }

        return new OriginalDataElements(value.substring(0, 4), value.substring(4, 10), value.substring(10, 20),
                value.substring(20, 31), value.substring(31, 42));
    }
}
