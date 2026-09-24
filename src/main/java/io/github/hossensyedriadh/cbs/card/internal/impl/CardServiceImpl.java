package io.github.hossensyedriadh.cbs.card.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountService;
import io.github.hossensyedriadh.cbs.card.api.command.*;
import io.github.hossensyedriadh.cbs.card.api.enumerator.CardReplacementReason;
import io.github.hossensyedriadh.cbs.card.api.enumerator.CardStatus;
import io.github.hossensyedriadh.cbs.card.api.model.CardView;
import io.github.hossensyedriadh.cbs.card.api.service.CardService;
import io.github.hossensyedriadh.cbs.card.internal.cryptography.CardNumberHasher;
import io.github.hossensyedriadh.cbs.card.internal.domain.Card;
import io.github.hossensyedriadh.cbs.card.internal.domain.CardStatusHistory;
import io.github.hossensyedriadh.cbs.card.internal.mapper.CardMapper;
import io.github.hossensyedriadh.cbs.card.internal.repository.CardRepository;
import io.github.hossensyedriadh.cbs.card.internal.repository.CardStatusHistoryRepository;
import io.github.hossensyedriadh.cbs.card.internal.util.CardFeeCodes;
import io.github.hossensyedriadh.cbs.card.internal.util.CardNumberGenerator;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.product.api.enumerator.CardProductStatus;
import io.github.hossensyedriadh.cbs.product.api.model.CardProductView;
import io.github.hossensyedriadh.cbs.product.api.service.CardProductService;
import io.github.hossensyedriadh.cbs.transaction.api.command.ChargeFeeCommand;
import io.github.hossensyedriadh.cbs.transaction.api.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
    private final CardPinServiceImpl cardPinService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final CardNumberHasher cardNumberHasher;
    private final CardMapper cardMapper;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public CardServiceImpl(CardRepository cardRepository, CardStatusHistoryRepository cardStatusHistoryRepository,
                           CardProductService cardProductService, CardPinServiceImpl cardPinService,
                           AccountService accountService, TransactionService transactionService,
                           CardNumberHasher cardNumberHasher, CardMapper cardMapper, HttpServletRequest httpServletRequest) {
        this.cardRepository = cardRepository;
        this.cardStatusHistoryRepository = cardStatusHistoryRepository;
        this.cardProductService = cardProductService;
        this.cardPinService = cardPinService;
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
    public CardView activateCard(@Valid ActivateCardCommand command) {
        Card card = this.cardRepository.findByIdForUpdate(command.getCardId()).orElseThrow(() -> {
            log.warn("Card with id {} not found", command.getCardId());
            return new ApiResourceException("Card not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (card.getStatus() != CardStatus.INACTIVE) {
            log.warn("Card with id {} is not inactive", card.getId());
            throw new ApiResourceException("Card is not inactive", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        String expectedCardNoHash = card.getCardNumberHash();
        String actualCardNoHash = this.cardNumberHasher.hash(command.getCardNumber());

        if (!MessageDigest.isEqual(expectedCardNoHash.getBytes(StandardCharsets.UTF_8), actualCardNoHash.getBytes(StandardCharsets.UTF_8))) {
            log.warn("Card with id {} can not be activated as entered Card Number does not match with actual", card.getId());
            throw new ApiResourceException("Card Number does not match", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (!card.getExpiryDate().isEqual(command.getExpiryDate())) {
            log.warn("Card with id {} can not be activated as entered expiry does not match with actual", card.getId());
            throw new ApiResourceException("Card expiry does not match", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        this.cardPinService.setPin(new SetCardPinCommand(card.getId(), command.getCardPin()));

        card.setStatus(CardStatus.ACTIVE);
        Card activated = this.cardRepository.save(card);

        log.info("Card with id {} has been activated", card.getId());

        this.createStatusHistory(card.getId(), CardStatus.ACTIVE, "Customer activated");

        return this.cardMapper.toView(activated);
    }

    @Transactional
    @Override
    public CardView issueCard(@Valid IssueCardCommand command) {
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

    private Card issueReplacement(Card oldCard, CardReplacementReason replacementReason) {
        CardProductView cardProduct = this.cardProductService.getCardProduct(oldCard.getCardProductId()).orElseThrow(() -> {
            log.warn("Card product not found with id {}", oldCard.getCardProductId());
            return new ApiResourceException("Card product not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (cardProduct.getStatus() != CardProductStatus.ACTIVE) {
            log.warn("Card product with id: {} is not ACTIVE", oldCard.getCardProductId());
            throw new ApiResourceException("Card product is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        AccountView account = this.accountService.getAccount(oldCard.getAccountId()).orElseThrow(() -> {
            log.warn("Account not found with id {}", oldCard.getAccountId());
            return new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Account with id: {} is not ACTIVE", oldCard.getAccountId());
            throw new ApiResourceException("Account is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        String generatedPan = CardNumberGenerator.generate(cardProduct.getCardNetwork());
        OffsetDateTime issuedAt = OffsetDateTime.now();

        Card card = new Card();
        card.setAccountId(oldCard.getAccountId());
        card.setCardProductId(oldCard.getCardProductId());
        card.setCardNumberHash(this.cardNumberHasher.hash(generatedPan));
        card.setLastFour(generatedPan.substring(Math.max(0, generatedPan.length() - 4)));
        card.setIssuedAt(issuedAt);
        card.setExpiryDate(issuedAt.toLocalDate().plusYears(cardProduct.getValidityYears())
                .with(TemporalAdjusters.firstDayOfMonth()));
        card.setReplacedCard(oldCard.getId());
        card.setReplacementReason(replacementReason);

        Card issued = this.cardRepository.save(card);

        log.info("Replacement for card [Network: {}] with id: {} has been issued", cardProduct.getCardNetwork().toString(), oldCard.getId());

        this.createStatusHistory(issued.getId(), CardStatus.INACTIVE, "Card replacement issued");

        ChargeFeeCommand chargingCommand = new ChargeFeeCommand();
        chargingCommand.setAccountId(oldCard.getAccountId());
        chargingCommand.setCurrencyId(account.getCurrencyId());
        chargingCommand.setAmount(cardProduct.getIssuanceFee());
        chargingCommand.setFeeCode(CardFeeCodes.CARD_REPLACEMENT_FEE);
        chargingCommand.setIdempotencyKey(CardFeeCodes.CARD_REPLACEMENT_FEE.concat(":").concat(String.valueOf(issued.getId())));
        chargingCommand.setDescription(cardProduct.getCardNetwork().toString() + " card replacement fee");

        this.transactionService.charge(chargingCommand);

        return issued;
    }

    @Transactional
    @Override
    public CardView replaceCard(ReplaceCardCommand command) {
        Card oldCard = this.cardRepository.findByIdForUpdate(command.getCardId())
                .orElseThrow(() -> {
                    log.warn("Card not found with id {}", command.getCardId());
                    return new ApiResourceException("Card not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
                });

        this.validateReplacement(oldCard, command.getReason());

        Card replacement = this.issueReplacement(oldCard, command.getReason());

        oldCard.setStatus(CardStatus.REPLACED);

        this.createStatusHistory(oldCard.getId(), CardStatus.REPLACED, command.getReason().toString());

        this.cardRepository.save(oldCard);

        return this.cardMapper.toView(replacement);
    }

    private void validateReplacement(Card card, CardReplacementReason reason) {
        if (reason == null) {
            throw new IllegalArgumentException("Replacement reason is required");
        }

        switch (card.getStatus()) {
            case REPLACED -> {
                log.warn("Card with id: {} has already been replaced", card.getId());
                throw new ApiResourceException("Card has already been replaced", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }
            case CLOSED -> {
                log.warn("Card with id: {} is already closed", card.getId());
                throw new ApiResourceException("Closed card cannot be replaced", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }
            case PERMANENTLY_BLOCKED -> {
                log.warn("Card with id: {} is permanently blocked", card.getId());
                throw new ApiResourceException("Permanently blocked card cannot be replaced", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }
            case EXPIRED -> {
                if (reason != CardReplacementReason.EXPIRED) {
                    log.warn("Expired card with id: {} can only be replaced due to expiration", card.getId());
                    throw new ApiResourceException("Expired card can only be replaced due to expiration", HttpStatus.BAD_REQUEST, this.httpServletRequest);
                }
            }

            default -> {
                // ACTIVE, INACTIVE, TEMPORARILY_BLOCKED
            }
        }

        if (reason == CardReplacementReason.EXPIRED && card.getStatus() != CardStatus.EXPIRED) {
            log.warn("Only an expired card with id: {} can be replaced due to expiration", card.getId());
            throw new ApiResourceException("Only an expired card can be replaced due to expiration", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }
    }

    @Transactional
    @Override
    public CardView temporarilyBlockCard(@Valid BlockCardCommand command) {
        Card card = this.cardRepository.findByIdForUpdate(command.getCardId()).orElseThrow(() -> {
            log.warn("Card not found with id: {}", command.getCardId());
            return new ApiResourceException("Card not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (card.getStatus() == CardStatus.CLOSED) {
            log.warn("Card with id: {} is already closed", command.getCardId());
            throw new ApiResourceException("Card is already closed", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (card.getStatus() == CardStatus.PERMANENTLY_BLOCKED) {
            log.warn("Card with id: {} is already permanently blocked", command.getCardId());
            throw new ApiResourceException("Card is already permanently blocked", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (card.getStatus() == CardStatus.TEMPORARILY_BLOCKED) {
            log.warn("Card with id: {} is already temporarily blocked", command.getCardId());
            throw new ApiResourceException("Card is already temporarily blocked", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        card.setStatus(CardStatus.TEMPORARILY_BLOCKED);
        Card updated = this.cardRepository.save(card);

        this.createStatusHistory(command.getCardId(), CardStatus.TEMPORARILY_BLOCKED, command.getReason());

        log.info("Card with id: {} status updated to TEMPORARILY_BLOCKED", command.getCardId());

        return this.cardMapper.toView(updated);
    }

    @Transactional
    @Override
    public CardView permanentlyBlockCard(@Valid BlockCardCommand command) {
        Card card = this.cardRepository.findByIdForUpdate(command.getCardId()).orElseThrow(() -> {
            log.warn("Card not found with id: {}", command.getCardId());
            return new ApiResourceException("Card not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (card.getStatus() == CardStatus.CLOSED) {
            log.warn("Card with id: {} is already closed", command.getCardId());
            throw new ApiResourceException("Card is already closed", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (card.getStatus() == CardStatus.PERMANENTLY_BLOCKED) {
            log.warn("Card with id: {} is already permanently blocked", command.getCardId());
            throw new ApiResourceException("Card is already permanently blocked", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        card.setStatus(CardStatus.PERMANENTLY_BLOCKED);
        Card updated = this.cardRepository.save(card);

        this.createStatusHistory(command.getCardId(), CardStatus.PERMANENTLY_BLOCKED, command.getReason());

        log.info("Card with id: {} status updated to PERMANENTLY_BLOCKED", command.getCardId());

        return this.cardMapper.toView(updated);
    }

    @Transactional
    @Override
    public CardView unblockCard(@Valid UnblockCardCommand command) {
        Card card = this.cardRepository.findByIdForUpdate(command.getCardId()).orElseThrow(() -> {
            log.warn("Card not found with id: {}", command.getCardId());
            return new ApiResourceException("Card not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (card.getStatus() == CardStatus.CLOSED) {
            log.warn("Card with id: {} is already closed", command.getCardId());
            throw new ApiResourceException("Card is already closed", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (card.getStatus() == CardStatus.ACTIVE) {
            log.warn("Card with id: {} is already active", command.getCardId());
            throw new ApiResourceException("Card is already active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        card.setStatus(CardStatus.ACTIVE);
        Card updated = this.cardRepository.save(card);

        this.createStatusHistory(command.getCardId(), CardStatus.ACTIVE, command.getReason());

        log.info("Card with id: {} status updated to ACTIVE", command.getCardId());

        return this.cardMapper.toView(updated);
    }

    @Transactional
    @Override
    public CardView closeCard(@Valid CloseCardCommand command) {
        Card card = this.cardRepository.findByIdForUpdate(command.getCardId()).orElseThrow(() -> {
            log.warn("Card not found with id: {}", command.getCardId());
            return new ApiResourceException("Card not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (card.getStatus() == CardStatus.CLOSED) {
            log.warn("Card with id: {} is already closed", command.getCardId());
            throw new ApiResourceException("Card is already closed", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        card.setStatus(CardStatus.CLOSED);
        Card closed = this.cardRepository.save(card);

        this.createStatusHistory(command.getCardId(), CardStatus.CLOSED, command.getReason());

        log.info("Card with id: {} status updated to CLOSED", command.getCardId());

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
