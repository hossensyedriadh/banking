@ApplicationModule(allowedDependencies = {"account :: event", "branch :: event", "customer :: event", "product :: accountproduct", "product :: currency", "ledger :: ledger", "ledger :: ledgeraccount", "transaction :: event", "product :: cardproduct", "product :: chequeproduct"})
package io.github.hossensyedriadh.cbs.audit;

import org.springframework.modulith.ApplicationModule;