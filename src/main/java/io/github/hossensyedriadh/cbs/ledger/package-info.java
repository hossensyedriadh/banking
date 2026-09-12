@ApplicationModule(
        allowedDependencies = {"exception", "product :: api", "product :: model", "configuration :: mapper"}
)
package io.github.hossensyedriadh.cbs.ledger;

import org.springframework.modulith.ApplicationModule;