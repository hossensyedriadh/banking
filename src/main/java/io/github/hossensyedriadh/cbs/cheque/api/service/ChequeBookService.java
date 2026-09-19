package io.github.hossensyedriadh.cbs.cheque.api.service;

import io.github.hossensyedriadh.cbs.cheque.api.command.CancelChequeBookCommand;
import io.github.hossensyedriadh.cbs.cheque.api.command.IssueChequeBookCommand;
import io.github.hossensyedriadh.cbs.cheque.api.model.ChequeBookView;

import java.util.List;
import java.util.Optional;

public interface ChequeBookService {
    List<ChequeBookView> getChequeBooks(Long accountId);

    Optional<ChequeBookView> getChequeBook(Long id);

    ChequeBookView issueChequeBook(IssueChequeBookCommand command);

    void cancelChequeBook(CancelChequeBookCommand command);
}
