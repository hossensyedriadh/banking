@ApplicationModule(
        allowedDependencies = {"exception", "product :: api", "product :: model"}
)
package io.github.hossensyedriadh.cbs.ledger;

import org.springframework.modulith.ApplicationModule;