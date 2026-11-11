package io.github.hossensyedriadh.banking_iam_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"io.github.hossensyedriadh.banking_iam_server.repository"})
@SpringBootApplication
public class BankingIamServerApplication {

    static void main(String[] args) {
        SpringApplication.run(BankingIamServerApplication.class, args);
    }

}
