package io.github.hossensyedriadh.cbs.cheque.api;

import io.github.hossensyedriadh.cbs.cheque.api.model.ChequePresentationView;
import io.github.hossensyedriadh.cbs.cheque.api.model.PresentChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.model.ReturnChequeCommand;

import java.util.List;
import java.util.Optional;

public interface ChequePresentationService {
    List<ChequePresentationView> getPresentations(Long chequeId);

    Optional<ChequePresentationView> getPresentation(Long id);

    ChequePresentationView presentCheque(PresentChequeCommand command);

    ChequePresentationView acceptCheque(Long id);

    ChequePresentationView payCheque(Long id);

    ChequePresentationView returnCheque(ReturnChequeCommand command);
}
