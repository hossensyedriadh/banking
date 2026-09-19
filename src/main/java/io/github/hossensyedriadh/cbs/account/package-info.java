@ApplicationModule(
        allowedDependencies = {"exception", "configuration :: mapper", "ledger :: service", "ledger :: model", "customer :: service", "branch :: service", "branch :: model", "product :: service", "product :: enumerator", "product :: model"}
)
package io.github.hossensyedriadh.cbs.account;

import org.springframework.modulith.ApplicationModule;