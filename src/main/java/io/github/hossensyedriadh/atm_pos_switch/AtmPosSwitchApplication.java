package io.github.hossensyedriadh.atm_pos_switch;

import io.github.hossensyedriadh.atm_pos_switch.network.TcpServerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TcpServerProperties.class)
public class AtmPosSwitchApplication {

    static void main(String[] args) {
        SpringApplication.run(AtmPosSwitchApplication.class, args);
    }

}
