@ApplicationModule(
        allowedDependencies = {"exception", "configuration :: mapper", "authorization :: service"}
)
package io.github.hossensyedriadh.cbs.customer;

import org.springframework.modulith.ApplicationModule;