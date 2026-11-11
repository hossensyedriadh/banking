package io.github.hossensyedriadh.banking_api_gateway;

import io.github.hossensyedriadh.banking_api_gateway.configuration.properties.CbsProperties;
import io.github.hossensyedriadh.banking_api_gateway.configuration.properties.IamProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({IamProperties.class, CbsProperties.class})
public class BankingApiGatewayApplication {

    static void main(String[] args) {
        SpringApplication.run(BankingApiGatewayApplication.class, args);
    }

}
