package io.github.hossensyedriadh.cbs.cheque.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.AccountService;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.cheque.api.ChequeBookService;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequeBookStatus;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequeStatus;
import io.github.hossensyedriadh.cbs.cheque.api.model.CancelChequeBookCommand;
import io.github.hossensyedriadh.cbs.cheque.api.model.ChequeBookView;
import io.github.hossensyedriadh.cbs.cheque.api.model.ChequeNumberRange;
import io.github.hossensyedriadh.cbs.cheque.api.model.IssueChequeBookCommand;
import io.github.hossensyedriadh.cbs.cheque.internal.domain.Cheque;
import io.github.hossensyedriadh.cbs.cheque.internal.domain.ChequeBook;
import io.github.hossensyedriadh.cbs.cheque.internal.mapper.ChequeBookMapper;
import io.github.hossensyedriadh.cbs.cheque.internal.repository.ChequeBookRepository;
import io.github.hossensyedriadh.cbs.cheque.internal.repository.ChequeRepository;
import io.github.hossensyedriadh.cbs.cheque.internal.util.ChequeBookNumberGenerator;
import io.github.hossensyedriadh.cbs.cheque.internal.util.ChequeFeeCodes;
import io.github.hossensyedriadh.cbs.cheque.internal.util.ChequeNumberGenerator;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.product.api.ChequeProductService;
import io.github.hossensyedriadh.cbs.product.api.enumerator.ChequeProductStatus;
import io.github.hossensyedriadh.cbs.product.api.model.ChequeProductView;
import io.github.hossensyedriadh.cbs.transaction.api.TransactionService;
import io.github.hossensyedriadh.cbs.transaction.api.model.ChargeFeeCommand;
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
public class ChequeBookServiceImpl implements ChequeBookService {
    private final ChequeBookRepository chequeBookRepository;
    private final ChequeRepository chequeRepository;
    private final AccountService accountService;
    private final ChequeProductService chequeProductService;
    private final TransactionService transactionService;
    private final ChequeBookNumberGenerator chequeBookNumberGenerator;
    private final ChequeNumberGenerator chequeNumberGenerator;
    private final ChequeBookMapper chequeBookMapper;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public ChequeBookServiceImpl(ChequeBookRepository chequeBookRepository, ChequeRepository chequeRepository, AccountService accountService,
                                 ChequeProductService chequeProductService, TransactionService transactionService,
                                 ChequeBookNumberGenerator chequeBookNumberGenerator, ChequeNumberGenerator chequeNumberGenerator,
                                 ChequeBookMapper chequeBookMapper, HttpServletRequest httpServletRequest) {
        this.chequeBookRepository = chequeBookRepository;
        this.chequeRepository = chequeRepository;
        this.accountService = accountService;
        this.chequeProductService = chequeProductService;
        this.transactionService = transactionService;
        this.chequeBookNumberGenerator = chequeBookNumberGenerator;
        this.chequeNumberGenerator = chequeNumberGenerator;
        this.chequeBookMapper = chequeBookMapper;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public List<ChequeBookView> getChequeBooks(Long accountId) {
        return this.chequeBookRepository.findAllByAccountId(accountId).stream().map(this.chequeBookMapper::toView).toList();
    }

    @Override
    public Optional<ChequeBookView> getChequeBook(Long id) {
        return this.chequeBookRepository.findById(id).map(this.chequeBookMapper::toView);
    }

    @Transactional
    @Override
    public ChequeBookView issueChequeBook(IssueChequeBookCommand command) {
        AccountView accountView = this.accountService.getAccount(command.getAccountId())
                .orElseThrow(() -> {
                    log.warn("Account not found with id {}", command.getAccountId());
                    return new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
                });

        if (accountView.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Account with id {} is not ACTIVE", command.getAccountId());
            throw new ApiResourceException("Account is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        ChequeProductView product = this.chequeProductService.getChequeProduct(command.getChequeProductId())
                .orElseThrow(() -> {
                    log.warn("Cheque Product not found with id {}", command.getChequeProductId());
                    return new ApiResourceException("Cheque Product not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
                });

        if (product.getStatus() != ChequeProductStatus.ACTIVE) {
            log.warn("Cheque Product with id {} is not ACTIVE", command.getChequeProductId());
            throw new ApiResourceException("Cheque Product is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        OffsetDateTime issuedAt = OffsetDateTime.now();

        ChequeNumberRange range = this.chequeNumberGenerator.generate(product.getLeafCount());

        ChequeBook chequeBook = new ChequeBook();
        chequeBook.setAccountId(command.getAccountId());
        chequeBook.setChequeProductId(command.getChequeProductId());
        chequeBook.setBookNumber("CB-" + this.chequeBookNumberGenerator.generate());
        chequeBook.setFirstChequeNumber(range.firstNumber());
        chequeBook.setLastChequeNumber(range.lastNumber());
        chequeBook.setIssuedAt(issuedAt);

        ChequeBook savedChequeBook = this.chequeBookRepository.save(chequeBook);

        LocalDate expiry = issuedAt.toLocalDate().plusDays(product.getValidityDays());

        for (long chequeNumber = range.firstNumber(); chequeNumber <= range.lastNumber(); chequeNumber++) {
            Cheque cheque = new Cheque();
            cheque.setChequeBook(savedChequeBook);
            cheque.setChequeNumber(chequeNumber);
            cheque.setIssuedAt(issuedAt);
            cheque.setExpiryDate(expiry);

            this.chequeRepository.save(cheque);
        }

        if (product.getIssuanceFee().signum() > 0) {
            ChargeFeeCommand chargeFeeCommand = new ChargeFeeCommand();
            chargeFeeCommand.setAccountId(command.getAccountId());
            chargeFeeCommand.setCurrencyId(accountView.getCurrencyId());
            chargeFeeCommand.setAmount(product.getIssuanceFee());
            chargeFeeCommand.setFeeCode(ChequeFeeCodes.CHEQUE_ISSUANCE_FEE);
            chargeFeeCommand.setIdempotencyKey(ChequeFeeCodes.CHEQUE_ISSUANCE_FEE + ":" + savedChequeBook.getId());
            chargeFeeCommand.setDescription("New " + product.getProductCode() + " cheque book issuance fee");

            this.transactionService.charge(chargeFeeCommand);
        }

        log.info("{} Cheque book issued successfully for account: {}", product.getProductCode(), accountView.getId());

        return this.chequeBookMapper.toView(savedChequeBook);
    }

    @Transactional
    @Override
    public void cancelChequeBook(CancelChequeBookCommand command) {
        ChequeBook chequeBook = this.chequeBookRepository.findByIdForUpdate(command.getId())
                .orElseThrow(() -> {
                    log.warn("Cheque Book with id {} is not found", command.getId());
                    return new ApiResourceException("Cheque Book not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
                });

        if (chequeBook.getStatus() != ChequeBookStatus.ACTIVE) {
            log.warn("Cheque Book with id {} is not ACTIVE", command.getId());
            throw new ApiResourceException("Cheque Book is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        OffsetDateTime now = OffsetDateTime.now();

        chequeBook.setStatus(ChequeBookStatus.CANCELLED);

        //todo: create status history

        this.chequeBookRepository.save(chequeBook);

        List<Cheque> cheques = this.chequeRepository.findAllByChequeBook_Id(chequeBook.getId());

        for (Cheque cheque : cheques) {
            if (cheque.getStatus() == ChequeStatus.AVAILABLE) {
                cheque.setStatus(ChequeStatus.CANCELLED);
                cheque.setCancelledAt(now);

                this.chequeRepository.save(cheque);
            }
        }

        log.info("Cheque book with id: {} has been cancelled successfully", chequeBook.getId());
    }
}
