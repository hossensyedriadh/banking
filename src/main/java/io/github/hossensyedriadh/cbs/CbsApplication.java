package io.github.hossensyedriadh.cbs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableScheduling
@EnableAsync
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider", modifyOnCreate = false)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"io.github.hossensyedriadh.cbs.account.internal.repository",
        "io.github.hossensyedriadh.cbs.audit.internal.repository",
        "io.github.hossensyedriadh.cbs.branch.internal.repository",
        "io.github.hossensyedriadh.cbs.customer.internal.repository",
        "io.github.hossensyedriadh.cbs.ledger.internal.repository",
        "io.github.hossensyedriadh.cbs.product.internal.repository",
        "io.github.hossensyedriadh.cbs.transaction.internal.repository"})
@SpringBootApplication
public class CbsApplication {

    static void main(String[] args) {
        SpringApplication.run(CbsApplication.class, args);
    }

}
