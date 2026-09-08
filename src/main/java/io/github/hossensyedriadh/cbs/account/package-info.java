@ApplicationModule(
        allowedDependencies = {"exception", "customer :: api", "product :: api", "branch :: api", "ledger :: api"}
)
package io.github.hossensyedriadh.cbs.account;

import org.springframework.modulith.ApplicationModule;