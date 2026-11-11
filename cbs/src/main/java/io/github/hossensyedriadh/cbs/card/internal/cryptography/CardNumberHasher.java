package io.github.hossensyedriadh.cbs.card.internal.cryptography;

public interface CardNumberHasher {
    String hash(String cardNumber);
}
