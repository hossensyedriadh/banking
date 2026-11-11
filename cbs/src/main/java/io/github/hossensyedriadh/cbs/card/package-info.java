@ApplicationModule(
        allowedDependencies = {"exception", "configuration :: mapper", "product :: service", "account :: service", "transaction :: service", "product :: model", "account :: model", "product :: enumerator", "account :: enumerator", "transaction :: command"}
)
package io.github.hossensyedriadh.cbs.card;

import org.springframework.modulith.ApplicationModule;
