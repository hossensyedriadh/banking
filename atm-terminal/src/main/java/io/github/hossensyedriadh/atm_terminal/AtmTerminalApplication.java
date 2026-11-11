package io.github.hossensyedriadh.atm_terminal;

import io.github.hossensyedriadh.atm_terminal.configuration.properties.AtmTerminalProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AtmTerminalProperties.class)
public class AtmTerminalApplication {

    static void main(String[] args) {
        SpringApplication.run(AtmTerminalApplication.class, args);
    }

}
