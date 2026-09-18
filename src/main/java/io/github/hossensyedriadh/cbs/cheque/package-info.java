@ApplicationModule(
        allowedDependencies = {"exception", "configuration :: mapper", "account :: api", "transaction :: api", "product :: api", "account :: model", "product :: model", "account :: enumerator", "product :: enumerator", "transaction :: model", "ledger :: api"}
)
package io.github.hossensyedriadh.cbs.cheque;

import org.springframework.modulith.ApplicationModule;