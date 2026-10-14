package io.github.hossensyedriadh.atm_pos_switch.network;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "switch.tcp")
public record TcpServerProperties(String host, int port, int readTimeout, int idleTimeout) {
}
