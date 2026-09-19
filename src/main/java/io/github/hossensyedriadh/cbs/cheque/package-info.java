@ApplicationModule(
        allowedDependencies = {"exception", "configuration :: mapper", "account :: service", "product :: service", "transaction :: service", "product :: model", "account :: enumerator", "product :: enumerator", "account :: model", "transaction :: command"}
)
package io.github.hossensyedriadh.cbs.cheque;

import org.springframework.modulith.ApplicationModule;