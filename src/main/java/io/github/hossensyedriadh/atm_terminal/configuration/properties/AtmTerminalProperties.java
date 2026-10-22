package io.github.hossensyedriadh.atm_terminal.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "atm")
public record AtmTerminalProperties(String terminalId, SwitchProperties switchConfig) {
    public record SwitchProperties(String host, int port, int connectTimeout, int readTimeout) {
    }
}