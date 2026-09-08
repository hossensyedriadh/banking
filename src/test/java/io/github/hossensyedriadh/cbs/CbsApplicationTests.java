package io.github.hossensyedriadh.cbs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class CbsApplicationTests {

    @Test
    void verifiesModularity() {
        ApplicationModules.of(CbsApplication.class).verify();
    }

}
