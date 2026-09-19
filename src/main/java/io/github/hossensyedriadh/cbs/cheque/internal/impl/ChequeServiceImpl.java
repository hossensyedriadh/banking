package io.github.hossensyedriadh.cbs.cheque.internal.impl;

import io.github.hossensyedriadh.cbs.cheque.api.command.CancelChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.command.StopChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequeStatus;
import io.github.hossensyedriadh.cbs.cheque.api.model.ChequeView;
import io.github.hossensyedriadh.cbs.cheque.api.service.ChequeService;
import io.github.hossensyedriadh.cbs.cheque.internal.domain.Cheque;
import io.github.hossensyedriadh.cbs.cheque.internal.mapper.ChequeMapper;
import io.github.hossensyedriadh.cbs.cheque.internal.repository.ChequeRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import jakarta.servlet.http.HttpServletRequest;
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
public class ChequeServiceImpl implements ChequeService {
    private final ChequeRepository chequeRepository;
    private final ChequeMapper chequeMapper;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public ChequeServiceImpl(ChequeRepository chequeRepository, ChequeMapper chequeMapper,
                             HttpServletRequest httpServletRequest) {
        this.chequeRepository = chequeRepository;
        this.chequeMapper = chequeMapper;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public List<ChequeView> getCheques(Long bookId) {
        return this.chequeRepository.findAllByChequeBook_Id(bookId).stream().map(this.chequeMapper::toView).toList();
    }

    @Override
    public Optional<ChequeView> getCheque(Long id) {
        return this.chequeRepository.findById(id).map(this.chequeMapper::toView);
    }

    @Override
    public Optional<ChequeView> getChequeByNumber(Long chequeNumber) {
        return this.chequeRepository.findByChequeNumber(chequeNumber).map(this.chequeMapper::toView);
    }

    @Transactional
    @Override
    public void stopCheque(StopChequeCommand command) {
        Cheque cheque = this.chequeRepository.findByIdForUpdate(command.getId()).orElseThrow(() -> {
            log.warn("Cheque with id {} not found", command.getId());
            return new ApiResourceException("Cheque not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (cheque.getStatus() != ChequeStatus.AVAILABLE) {
            log.warn("Cheque with id {} is not in available state", command.getId());
            throw new ApiResourceException("Only available cheques can be stopped", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        OffsetDateTime now = OffsetDateTime.now();
        cheque.setStatus(ChequeStatus.STOPPED);
        cheque.setStoppedAt(now);

        this.chequeRepository.save(cheque);

        log.info("Cheque with id {} has been stopped", command.getId());
    }

    @Transactional
    @Override
    public void cancelCheque(CancelChequeCommand command) {
        Cheque cheque = this.chequeRepository.findByIdForUpdate(command.getId()).orElseThrow(() -> {
            log.warn("Cheque with id {} not found", command.getId());
            return new ApiResourceException("Cheque not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (cheque.getStatus() != ChequeStatus.AVAILABLE) {
            log.warn("Cheque with id {} is not in available state", command.getId());
            throw new ApiResourceException("Only available cheques can be cancelled", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        OffsetDateTime now = OffsetDateTime.now();
        cheque.setStatus(ChequeStatus.CANCELLED);
        cheque.setCancelledAt(now);

        this.chequeRepository.save(cheque);

        log.info("Cheque with id {} has been cancelled", command.getId());
    }

    @Transactional
    @Override
    public void expireCheque(Long id) {
        Cheque cheque = this.chequeRepository.findByIdForUpdate(id).orElseThrow(() -> {
            log.warn("Cheque with id {} not found", id);
            return new ApiResourceException("Cheque not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (cheque.getStatus() != ChequeStatus.AVAILABLE) {
            log.warn("Cheque with id {} is not in available state", id);
            throw new ApiResourceException("Only available cheques can be cancelled", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (!cheque.getExpiryDate().isBefore(LocalDate.now())) {
            log.warn("Cheque with id {} has not expired yet", id);
            throw new ApiResourceException("Cheque not expired yet", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        cheque.setStatus(ChequeStatus.EXPIRED);

        this.chequeRepository.save(cheque);

        log.info("Cheque with id {} has been marked expired", id);
    }
}
