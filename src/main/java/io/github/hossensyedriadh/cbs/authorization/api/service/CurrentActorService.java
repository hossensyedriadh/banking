package io.github.hossensyedriadh.cbs.authorization.api.service;

import org.jspecify.annotations.NonNull;

public interface CurrentActorService {
    @NonNull String getSubject();
}
