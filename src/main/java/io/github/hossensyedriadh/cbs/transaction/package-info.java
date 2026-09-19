@ApplicationModule(
        allowedDependencies = {"exception", "configuration :: mapper", "account :: service", "ledger :: service", "product :: service", "account :: model", "ledger :: model", "product :: model", "account :: utility", "account :: enumerator"}
)
package io.github.hossensyedriadh.cbs.transaction;

import org.springframework.modulith.ApplicationModule;