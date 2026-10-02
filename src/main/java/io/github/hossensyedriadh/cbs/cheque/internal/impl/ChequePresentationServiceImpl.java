package io.github.hossensyedriadh.cbs.cheque.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountService;
import io.github.hossensyedriadh.cbs.cheque.api.command.PresentChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.command.ReturnChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequePresentationStatus;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequePresentationType;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequeStatus;
import io.github.hossensyedriadh.cbs.cheque.api.model.ChequePresentationView;
import io.github.hossensyedriadh.cbs.cheque.api.service.ChequePresentationService;
import io.github.hossensyedriadh.cbs.cheque.internal.domain.Cheque;
import io.github.hossensyedriadh.cbs.cheque.internal.domain.ChequeBook;
import io.github.hossensyedriadh.cbs.cheque.internal.domain.ChequePresentation;
import io.github.hossensyedriadh.cbs.cheque.internal.mapper.ChequePresentationMapper;
import io.github.hossensyedriadh.cbs.cheque.internal.repository.ChequePresentationRepository;
import io.github.hossensyedriadh.cbs.cheque.internal.repository.ChequeRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.transaction.api.command.CreateWithdrawalCommand;
import io.github.hossensyedriadh.cbs.transaction.api.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class ChequePresentationServiceImpl implements ChequePresentationService {
    private final ChequePresentationRepository chequePresentationRepository;
    private final ChequeRepository chequeRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final ChequePresentationMapper chequePresentationMapper;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public ChequePresentationServiceImpl(ChequePresentationRepository chequePresentationRepository, ChequeRepository chequeRepository,
                                         AccountService accountService, TransactionService transactionService,
                                         ChequePresentationMapper chequePresentationMapper, HttpServletRequest httpServletRequest) {
        this.chequePresentationRepository = chequePresentationRepository;
        this.chequeRepository = chequeRepository;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.chequePresentationMapper = chequePresentationMapper;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public List<ChequePresentationView> getPresentations(Long chequeId) {
        return this.chequePresentationRepository.findAllByChequeIdOrderByPresentedAtDesc(chequeId).stream().map(this.chequePresentationMapper::toView)
                .toList();
    }

    @Override
    public Optional<ChequePresentationView> getPresentation(Long id) {
        return this.chequePresentationRepository.findById(id).map(this.chequePresentationMapper::toView);
    }

    @Transactional
    @Override
    public ChequePresentationView presentCheque(@Valid PresentChequeCommand command) {
        Cheque cheque = this.chequeRepository.findByChequeNumberForUpdate(command.getChequeNumber()).orElseThrow(() -> {
            log.warn("Cheque with chequeNumber {} not found", command.getChequeNumber());
            return new ApiResourceException("Cheque not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (cheque.getStatus() != ChequeStatus.AVAILABLE) {
            log.warn("Cheque with id {} is not available for presentation", cheque.getId());
            throw new ApiResourceException("Cheque is not available for presentation", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (cheque.getExpiryDate().isBefore(LocalDate.now())) {
            log.warn("Cheque with id {} is expired for presentation", cheque.getId());
            throw new ApiResourceException("Cheque has expired", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        OffsetDateTime now = OffsetDateTime.now();

        ChequePresentation presentation = new ChequePresentation();
        presentation.setCheque(cheque);
        presentation.setPresentingBankCode(command.getPresentingBankCode());
        presentation.setPresentingAccountNo(command.getPresentingAccountNo());
        presentation.setPresentationType(command.getPresentationType());
        presentation.setPresentedAmount(command.getAmount());
        presentation.setStatus(ChequePresentationStatus.PENDING);
        presentation.setPresentedAt(now);

        ChequePresentation savedPresentation = this.chequePresentationRepository.save(presentation);

        cheque.setStatus(ChequeStatus.PRESENTED);
        cheque.setPresentedAt(now);

        this.chequeRepository.save(cheque);

        log.info("Cheque with id {} marked as presented", cheque.getId());

        return this.chequePresentationMapper.toView(savedPresentation);
    }

    @Transactional
    @Override
    public ChequePresentationView acceptCheque(Long id) {
        ChequePresentation presentation = this.chequePresentationRepository.findByIdForUpdate(id).orElseThrow(() -> {
            log.warn("Cheque presentation with id {} not found", id);
            return new ApiResourceException("Cheque presentation not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (presentation.getStatus() != ChequePresentationStatus.PENDING) {
            log.warn("Cheque with id {} is not in pending status for presentation", id);
            throw new ApiResourceException("Only pending presentations can be accepted", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        this.chequeRepository.findById(presentation.getCheque().getId()).orElseThrow(() -> {
            log.warn("Cheque with id {} not found", presentation.getCheque().getId());
            return new ApiResourceException("Cheque not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        OffsetDateTime now = OffsetDateTime.now();
        presentation.setStatus(ChequePresentationStatus.ACCEPTED);
        presentation.setAcceptedAt(now);

        ChequePresentation updatedPresentation = this.chequePresentationRepository.save(presentation);

        return this.chequePresentationMapper.toView(updatedPresentation);
    }

    @Transactional
    @Override
    public ChequePresentationView payCheque(Long id) {
        ChequePresentation presentation = this.chequePresentationRepository.findByIdForUpdate(id).orElseThrow(() -> {
            log.warn("Cheque presentation with id {} not found", id);
            return new ApiResourceException("Cheque presentation not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (presentation.getStatus() == ChequePresentationStatus.PAID) {
            log.warn("Cheque presentation with id {} is already paid", id);
            throw new ApiResourceException("Cheque presentation already paid", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (presentation.getStatus() != ChequePresentationStatus.ACCEPTED) {
            log.warn("Cheque presentation with id {} is not in accepted status for payment", id);
            throw new ApiResourceException("Only accepted cheque presentations can be paid", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        Cheque cheque = presentation.getCheque();

        if (cheque.getStatus() != ChequeStatus.PRESENTED) {
            log.warn("Cheque presentation with id {} is not in presented status for payment", id);
            throw new ApiResourceException("Only presented cheques can be paid", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        ChequeBook chequeBook = cheque.getChequeBook();
        Long drawerAccountId = chequeBook.getAccountId();

        AccountView account = this.accountService.getAccount(drawerAccountId).orElseThrow(() -> {
            log.warn("Account with id {} not found", drawerAccountId);
            return new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        this.transactionService.withdraw(new CreateWithdrawalCommand(account.getId(), account.getCurrencyId(), presentation.getPresentedAmount(),
                "CHEQUE_PRESENTATION:" + presentation.getId() + ":PAY", "Cheque " + cheque.getChequeNumber() + " payment to "
                + (presentation.getPresentationType() == ChequePresentationType.ACCOUNT_CREDIT
                ? (String.join(" ", presentation.getPresentingBankCode(), presentation.getPresentingAccountNo()))
                : "CASH")));

        OffsetDateTime now = OffsetDateTime.now();

        presentation.setStatus(ChequePresentationStatus.PAID);
        presentation.setPaidAt(now);

        ChequePresentation updatedPresentation = this.chequePresentationRepository.save(presentation);

        cheque.setStatus(ChequeStatus.PAID);
        cheque.setPaidAt(now);

        this.chequeRepository.save(cheque);

        log.info("Cheque with id {} has been paid", presentation.getId());
        log.info("Cheque presentation with id {} has been marked paid", presentation.getId());

        return this.chequePresentationMapper.toView(updatedPresentation);
    }

    @Transactional
    @Override
    public ChequePresentationView returnCheque(@Valid ReturnChequeCommand command) {
        ChequePresentation presentation = this.chequePresentationRepository.findByIdForUpdate(command.getPresentationId()).orElseThrow(() -> {
            log.warn("Cheque presentation with id {} not found", command.getPresentationId());
            return new ApiResourceException("Cheque Presentation not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (presentation.getStatus() != ChequePresentationStatus.PENDING
                && presentation.getStatus() != ChequePresentationStatus.ACCEPTED) {
            log.warn("Cheque presentation with id {} is not in pending/accepted status for return", command.getPresentationId());
            throw new ApiResourceException("Only pending or accepted presentations can be returned", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        Cheque cheque = this.chequeRepository.findByIdForUpdate(presentation.getCheque().getId()).orElseThrow(() -> {
            log.warn("Cheque not found for presentation with id {} not found", presentation.getCheque().getId());
            return new ApiResourceException("Cheque not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        OffsetDateTime now = OffsetDateTime.now();

        presentation.setStatus(ChequePresentationStatus.RETURNED);
        presentation.setReturnedAt(now);
        presentation.setReturnReason(command.getReason());

        cheque.setStatus(ChequeStatus.BOUNCED);
        cheque.setBouncedAt(now);

        ChequePresentation saved = this.chequePresentationRepository.save(presentation);

        this.chequeRepository.save(cheque);

        return this.chequePresentationMapper.toView(saved);
    }
}
