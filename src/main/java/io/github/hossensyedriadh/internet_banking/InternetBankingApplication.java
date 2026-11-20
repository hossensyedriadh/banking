package io.github.hossensyedriadh.internet_banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider", modifyOnCreate = false)
@EnableJpaRepositories(basePackages = {"io.github.hossensyedriadh.internet_banking.customer.repository", "io.github.hossensyedriadh.internet_banking.beneficiary.repository"})
@EnableTransactionManagement
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@SpringBootApplication
public class InternetBankingApplication {

    static void main(String[] args) {
        SpringApplication.run(InternetBankingApplication.class, args);
    }

}
