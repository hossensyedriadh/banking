package io.github.hossensyedriadh.internet_banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@EnableJpaRepositories(basePackages = {"io.github.hossensyedriadh.internet_banking.customer.repository"})
@EnableTransactionManagement
@SpringBootApplication
public class InternetBankingApplication {

	static void main(String[] args) {
		SpringApplication.run(InternetBankingApplication.class, args);
	}

}
