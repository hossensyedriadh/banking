package io.github.hossensyedriadh.cbs.enumerator;

import lombok.Getter;

@Getter
public enum CustomHttpAttributes {
    X_REQUEST_ID("X-Request-ID");

    private final String attributeName;

    CustomHttpAttributes(String attributeName) {
        this.attributeName = attributeName;
    }
}
