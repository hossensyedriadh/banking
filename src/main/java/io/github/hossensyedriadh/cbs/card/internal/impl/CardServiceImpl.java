package io.github.hossensyedriadh.cbs.card.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.AccountService;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.card.api.CardNumberHasher;
import io.github.hossensyedriadh.cbs.card.api.CardService;
import io.github.hossensyedriadh.cbs.card.api.enumerator.CardStatus;
import io.github.hossensyedriadh.cbs.card.api.model.ActivateCardCommand;
import io.github.hossensyedriadh.cbs.card.api.model.CardView;
import io.github.hossensyedriadh.cbs.card.api.model.ChangeCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.model.IssueCardCommand;
import io.github.hossensyedriadh.cbs.card.internal.domain.Card;
import io.github.hossensyedriadh.cbs.card.internal.domain.CardStatusHistory;
import io.github.hossensyedriadh.cbs.card.internal.mapper.CardMapper;
import io.github.hossensyedriadh.cbs.card.internal.repository.CardRepository;
import io.github.hossensyedriadh.cbs.card.internal.repository.CardStatusHistoryRepository;
import io.github.hossensyedriadh.cbs.card.internal.util.CardFeeCodes;
import io.github.hossensyedriadh.cbs.card.internal.util.CardNumberGenerator;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.product.api.CardProductService;
import io.github.hossensyedriadh.cbs.product.api.enumerator.CardProductStatus;
import io.github.hossensyedriadh.cbs.product.api.model.CardProductView;
import io.github.hossensyedriadh.cbs.transaction.api.TransactionService;
import io.github.hossensyedriadh.cbs.transaction.api.model.ChargeFeeCommand;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardStatusHistoryRepository cardStatusHistoryRepository;
    private final CardProductService cardProductService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final CardNumberHasher cardNumberHasher;
    private final CardMapper cardMapper;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public CardServiceImpl(CardRepository cardRepository, CardStatusHistoryRepository cardStatusHistoryRepository,
                           CardProductService cardProductService, AccountService accountService, TransactionService transactionService,
                           CardNumberHasher cardNumberHasher, CardMapper cardMapper, HttpServletRequest httpServletRequest) {
        this.cardRepository = cardRepository;
        this.cardStatusHistoryRepository = cardStatusHistoryRepository;
        this.cardProductService = cardProductService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.cardNumberHasher = cardNumberHasher;
        this.cardMapper = cardMapper;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public List<CardView> getCards(Long accountId) {
        return this.cardRepository.findAllByAccountId(accountId).stream().map(this.cardMapper::toView).toList();
    }

    @Override
    public Optional<CardView> getCard(Long cardId) {
        return this.cardRepository.findById(cardId).map(this.cardMapper::toView);
    }

    @Transactional
    @Override
    public CardView activateCard(ActivateCardCommand command) {
        return null;
    }

    @Transactional
    @Override
    public CardView changePin(ChangeCardPinCommand command) {
        return null;
    }

    @Transactional
    @Override
    public CardView issueCard(IssueCardCommand command) {
        CardProductView cardProduct = this.cardProductService.getCardProduct(command.getCardProductId()).orElseThrow(() -> {
            log.warn("Card product not found with id {}", command.getCardProductId());
            return new ApiResourceException("Card product not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (cardProduct.getStatus() != CardProductStatus.ACTIVE) {
            log.warn("Card product with id: {} is not ACTIVE", command.getCardProductId());
            throw new ApiResourceException("Card product is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        AccountView account = this.accountService.getAccount(command.getAccountId()).orElseThrow(() -> {
            log.warn("Account not found with id {}", command.getAccountId());
            return new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Account with id: {} is not ACTIVE", command.getAccountId());
            throw new ApiResourceException("Account is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        String generatedPan = CardNumberGenerator.generate(cardProduct.getCardNetwork());
        OffsetDateTime issuedAt = OffsetDateTime.now();

        Card card = new Card();
        card.setAccountId(command.getAccountId());
        card.setCardProductId(command.getCardProductId());
        card.setCardNumberHash(this.cardNumberHasher.hash(generatedPan));
        card.setLastFour(generatedPan.substring(Math.max(0, generatedPan.length() - 4)));
        card.setIssuedAt(issuedAt);
        card.setExpiryDate(issuedAt.toLocalDate().plusYears(cardProduct.getValidityYears())
                .with(TemporalAdjusters.firstDayOfMonth()));

        Card issued = this.cardRepository.save(card);

        log.info("Card [Network: {}] with id: {} has been issued", cardProduct.getCardNetwork().toString(), issued.getId());

        this.createStatusHistory(issued.getId(), CardStatus.INACTIVE, "Newly issued");

        ChargeFeeCommand chargingCommand = new ChargeFeeCommand();
        chargingCommand.setAccountId(command.getAccountId());
        chargingCommand.setCurrencyId(account.getCurrencyId());
        chargingCommand.setAmount(cardProduct.getIssuanceFee());
        chargingCommand.setFeeCode(CardFeeCodes.CARD_ISSUANCE_FEE);
        chargingCommand.setIdempotencyKey(CardFeeCodes.CARD_ISSUANCE_FEE.concat(":").concat(String.valueOf(issued.getId())));
        chargingCommand.setDescription("New " + cardProduct.getCardNetwork().toString() + " card issuance fee");

        this.transactionService.charge(chargingCommand);

        return this.cardMapper.toView(issued);
    }

    @Transactional
    @Override
    public CardView temporarilyBlockCard(Long cardId, String reason) {
        Card card = this.cardRepository.findByIdForUpdate(cardId).orElseThrow(() -> {
            log.warn("Card not found with id: {}", cardId);
            return new ApiResourceException("Card not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (card.getStatus() == CardStatus.CLOSED) {
            log.warn("Card with id: {} is already closed", cardId);
            throw new ApiResourceException("Card is already closed", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (card.getStatus() == CardStatus.PERMANENTLY_BLOCKED) {
            log.warn("Card with id: {} is already permanently blocked", cardId);
            throw new ApiResourceException("Card is already permanently blocked", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (card.getStatus() == CardStatus.TEMPORARILY_BLOCKED) {
            log.warn("Card with id: {} is already temporarily blocked", cardId);
            throw new ApiResourceException("Card is already temporarily blocked", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        card.setStatus(CardStatus.TEMPORARILY_BLOCKED);
        Card updated = this.cardRepository.save(card);

        this.createStatusHistory(cardId, CardStatus.TEMPORARILY_BLOCKED, reason);

        log.info("Card with id: {} status updated to TEMPORARILY_BLOCKED", cardId);

        return this.cardMapper.toView(updated);
    }

    @Transactional
    @Override
    public CardView permanentlyBlockCard(Long cardId, String reason) {
        Card card = this.cardRepository.findByIdForUpdate(cardId).orElseThrow(() -> {
            log.warn("Card not found with id: {}", cardId);
            return new ApiResourceException("Card not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (card.getStatus() == CardStatus.CLOSED) {
            log.warn("Card with id: {} is already closed", cardId);
            throw new ApiResourceException("Card is already closed", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (card.getStatus() == CardStatus.PERMANENTLY_BLOCKED) {
            log.warn("Card with id: {} is already permanently blocked", cardId);
            throw new ApiResourceException("Card is already permanently blocked", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        card.setStatus(CardStatus.PERMANENTLY_BLOCKED);
        Card updated = this.cardRepository.save(card);

        this.createStatusHistory(cardId, CardStatus.PERMANENTLY_BLOCKED, reason);

        log.info("Card with id: {} status updated to PERMANENTLY_BLOCKED", cardId);

        return this.cardMapper.toView(updated);
    }

    @Transactional
    @Override
    public CardView unblockCard(Long cardId, String reason) {
        Card card = this.cardRepository.findByIdForUpdate(cardId).orElseThrow(() -> {
            log.warn("Card not found with id: {}", cardId);
            return new ApiResourceException("Card not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (card.getStatus() == CardStatus.CLOSED) {
            log.warn("Card with id: {} is already closed", cardId);
            throw new ApiResourceException("Card is already closed", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (card.getStatus() == CardStatus.ACTIVE) {
            log.warn("Card with id: {} is already active", cardId);
            throw new ApiResourceException("Card is already active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        card.setStatus(CardStatus.ACTIVE);
        Card updated = this.cardRepository.save(card);

        this.createStatusHistory(cardId, CardStatus.ACTIVE, reason);

        log.info("Card with id: {} status updated to ACTIVE", cardId);

        return this.cardMapper.toView(updated);
    }

    @Transactional
    @Override
    public CardView closeCard(Long cardId, String reason) {
        Card card = this.cardRepository.findByIdForUpdate(cardId).orElseThrow(() -> {
            log.warn("Card not found with id: {}", cardId);
            return new ApiResourceException("Card not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (card.getStatus() == CardStatus.CLOSED) {
            log.warn("Card with id: {} is already closed", cardId);
            throw new ApiResourceException("Card is already closed", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        card.setStatus(CardStatus.CLOSED);
        Card closed = this.cardRepository.save(card);

        this.createStatusHistory(cardId, CardStatus.CLOSED, reason);

        log.info("Card with id: {} status updated to CLOSED", cardId);

        return this.cardMapper.toView(closed);
    }

    private void createStatusHistory(Long cardId, CardStatus status, String reason) {
        CardStatusHistory cardStatusHistory = new CardStatusHistory();
        cardStatusHistory.setCardId(cardId);
        cardStatusHistory.setStatus(status);
        cardStatusHistory.setReason(reason);

        this.cardStatusHistoryRepository.save(cardStatusHistory);

        log.info("Saved card status update history for card with id: {}, status: {}", cardId, status.toString());
    }
}
