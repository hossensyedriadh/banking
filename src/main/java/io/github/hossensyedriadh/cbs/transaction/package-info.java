@ApplicationModule(
        allowedDependencies = {"exception", "account :: api", "account :: enumerator", "account :: model", "ledger :: model", "ledger :: api", "ledger :: enumerator", "product :: api", "product :: model", "configuration :: mapper"}
)
package io.github.hossensyedriadh.cbs.transaction;

import org.springframework.modulith.ApplicationModule;