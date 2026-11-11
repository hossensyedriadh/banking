@ApplicationModule(
        allowedDependencies = {"exception", "configuration :: mapper", "account :: service", "ledger :: service", "transaction :: service", "transaction :: model", "account :: model", "account :: enumerator", "transaction :: enumerator"}
)
package io.github.hossensyedriadh.cbs.statement;

import org.springframework.modulith.ApplicationModule;