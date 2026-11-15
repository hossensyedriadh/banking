@ApplicationModule(
        allowedDependencies = {"exception", "configuration :: mapper", "ledger :: service", "ledger :: model", "customer :: service", "branch :: service", "branch :: model", "product :: service", "product :: enumerator", "product :: model", "eod :: service", "transaction :: service", "transaction :: command", "transaction :: model", "aspect :: annotation", "card :: service", "card :: model", "card :: enumerator"}
)
package io.github.hossensyedriadh.cbs.account;

import org.springframework.modulith.ApplicationModule;