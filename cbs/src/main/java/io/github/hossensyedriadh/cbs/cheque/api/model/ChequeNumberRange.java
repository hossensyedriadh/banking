package io.github.hossensyedriadh.cbs.cheque.api.model;

public record ChequeNumberRange(long firstNumber, long lastNumber) {
    public ChequeNumberRange {
        if (firstNumber <= 0) {
            throw new IllegalArgumentException("First cheque number must be greater than zero");
        }

        if (lastNumber < firstNumber) {
            throw new IllegalArgumentException("Last cheque number cannot be less than first cheque number");
        }
    }

    public int size() {
        return Math.toIntExact(lastNumber - firstNumber + 1);
    }
}