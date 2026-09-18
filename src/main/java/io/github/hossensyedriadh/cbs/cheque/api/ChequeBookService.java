package io.github.hossensyedriadh.cbs.cheque.api;

import io.github.hossensyedriadh.cbs.cheque.api.model.CancelChequeBookCommand;
import io.github.hossensyedriadh.cbs.cheque.api.model.ChequeBookView;
import io.github.hossensyedriadh.cbs.cheque.api.model.IssueChequeBookCommand;

import java.util.List;
import java.util.Optional;

public interface ChequeBookService {
    List<ChequeBookView> getChequeBooks(Long accountId);

    Optional<ChequeBookView> getChequeBook(Long id);

    ChequeBookView issueChequeBook(IssueChequeBookCommand command);

    void cancelChequeBook(CancelChequeBookCommand command);
}
