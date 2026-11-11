package io.github.hossensyedriadh.cbs.cheque.internal.util;

import io.github.hossensyedriadh.cbs.cheque.api.model.ChequeNumberRange;

public interface ChequeNumberGenerator {
    ChequeNumberRange generate(int leafCount);
}
