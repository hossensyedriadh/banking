@ApplicationModule(
        allowedDependencies = {"exception", "customer :: api", "product :: api", "branch :: api", "ledger :: api", "configuration :: mapper", "ledger :: model", "product :: model"}
)
package io.github.hossensyedriadh.cbs.account;

import org.springframework.modulith.ApplicationModule;