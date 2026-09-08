package io.github.hossensyedriadh.cbs.configuration;

import org.jspecify.annotations.NonNull;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

@Component("dateTimeProvider")
public class DateTimeProviderImpl implements DateTimeProvider {
    @Override
    @NonNull
    public Optional<TemporalAccessor> getNow() {
        return Optional.of(OffsetDateTime.now(ZoneOffset.UTC));
    }
}
