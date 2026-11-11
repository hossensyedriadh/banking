package io.github.hossensyedriadh.cbs.cheque.api.service;

import io.github.hossensyedriadh.cbs.cheque.api.command.PresentChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.command.ReturnChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.model.ChequePresentationView;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface ChequePresentationService {
    List<ChequePresentationView> getPresentations(Long chequeId);

    Optional<ChequePresentationView> getPresentation(Long id);

    ChequePresentationView presentCheque(@Valid PresentChequeCommand command);

    ChequePresentationView acceptCheque(Long id);

    ChequePresentationView payCheque(Long id);

    ChequePresentationView returnCheque(@Valid ReturnChequeCommand command);
}
