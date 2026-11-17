package io.github.hossensyedriadh.atm_terminal.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "atm.terminal.session")
public record AtmTerminalSessionProperties(Duration idleTimeout, Duration maxDuration) {
}
