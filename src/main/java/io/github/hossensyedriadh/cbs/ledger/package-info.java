@ApplicationModule(
        allowedDependencies = {"exception", "configuration :: mapper", "product :: service", "product :: model"}
)
package io.github.hossensyedriadh.cbs.ledger;

import org.springframework.modulith.ApplicationModule;