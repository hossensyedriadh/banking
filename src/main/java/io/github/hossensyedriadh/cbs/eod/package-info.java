@ApplicationModule(
        allowedDependencies = {"exception", "configuration :: mapper", "account :: service"}
)
package io.github.hossensyedriadh.cbs.eod;

import org.springframework.modulith.ApplicationModule;