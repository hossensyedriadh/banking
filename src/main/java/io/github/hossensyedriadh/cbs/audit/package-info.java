@ApplicationModule(allowedDependencies = {"account :: event", "branch :: event", "customer :: event", "product :: accountproduct", "product :: currency", "ledger :: ledger", "ledger :: ledgeraccount", "transaction :: event", "transaction :: enumerator", "product :: cardproduct", "product :: chequeproduct", "configuration :: mapper", "card :: event", "eod :: event", "statement :: event"})
package io.github.hossensyedriadh.cbs.audit;

import org.springframework.modulith.ApplicationModule;