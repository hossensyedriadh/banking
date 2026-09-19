package io.github.hossensyedriadh.cbs.cheque.api.service;

import io.github.hossensyedriadh.cbs.cheque.api.command.CancelChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.command.StopChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.model.ChequeView;

import java.util.List;
import java.util.Optional;

public interface ChequeService {
    List<ChequeView> getCheques(Long bookId);

    Optional<ChequeView> getCheque(Long id);

    Optional<ChequeView> getChequeByNumber(Long chequeNumber);

    void stopCheque(StopChequeCommand command);

    void cancelCheque(CancelChequeCommand command);

    void expireCheque(Long id);
}
