package io.github.hossensyedriadh.cbs.card.api.enumerator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PinScheme {
    ISO0 ("SIMULATED_ISO0");

    private final String scheme;
}
