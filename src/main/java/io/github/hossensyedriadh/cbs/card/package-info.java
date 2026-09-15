@ApplicationModule(
        allowedDependencies = {"exception", "configuration :: mapper", "account :: api", "product :: api", "product :: model", "product :: enumerator", "account :: model", "account :: enumerator", "transaction :: api", "transaction :: model"}
)
package io.github.hossensyedriadh.cbs.card;

import org.springframework.modulith.ApplicationModule;
