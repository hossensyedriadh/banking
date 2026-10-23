package io.github.hossensyedriadh.cbs.card.internal.impl;

import io.github.hossensyedriadh.cbs.card.api.command.ChangeCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.command.PinBlockVerificationCommand;
import io.github.hossensyedriadh.cbs.card.api.command.SetCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.command.VerifyCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.enumerator.CardPinVerificationResult;
import io.github.hossensyedriadh.cbs.card.api.enumerator.CardStatus;
import io.github.hossensyedriadh.cbs.card.api.enumerator.PinScheme;
import io.github.hossensyedriadh.cbs.card.api.event.CardPinChangedEvent;
import io.github.hossensyedriadh.cbs.card.api.event.CardPinSetEvent;
import io.github.hossensyedriadh.cbs.card.api.event.CardPinUnblockedEvent;
import io.github.hossensyedriadh.cbs.card.api.service.CardPinService;
import io.github.hossensyedriadh.cbs.card.api.service.PinBlockVerifier;
import io.github.hossensyedriadh.cbs.card.api.service.PinVerificationDataService;
import io.github.hossensyedriadh.cbs.card.internal.cryptography.CardNumberHasher;
import io.github.hossensyedriadh.cbs.card.internal.domain.Card;
import io.github.hossensyedriadh.cbs.card.internal.domain.CardPin;
import io.github.hossensyedriadh.cbs.card.internal.repository.CardPinRepository;
import io.github.hossensyedriadh.cbs.card.internal.repository.CardRepository;
import io.github.hossensyedriadh.cbs.card.internal.util.CardPinUtil;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class CardPinServiceImpl implements CardPinService {
    private final CardPinRepository cardPinRepository;
    private final CardRepository cardRepository;
    private final CardNumberHasher cardNumberHasher;
    private final PinBlockVerifier pinBlockVerifier;
    private final PinVerificationDataService pinVerificationDataService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public CardPinServiceImpl(CardPinRepository cardPinRepository, CardRepository cardRepository, CardNumberHasher cardNumberHasher,
                              PinBlockVerifier pinBlockVerifier, PinVerificationDataService pinVerificationDataService,
                              ApplicationEventPublisher applicationEventPublisher) {
        this.cardPinRepository = cardPinRepository;
        this.cardRepository = cardRepository;
        this.cardNumberHasher = cardNumberHasher;
        this.pinBlockVerifier = pinBlockVerifier;
        this.pinVerificationDataService = pinVerificationDataService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    @Override
    public void setPin(@Valid SetCardPinCommand command) {
        Card card = this.getCard(command.getPan());

        this.validateCardForPinOperation(card);

        boolean isValidCardPin = CardPinUtil.isValidCardPin(command.getPin());

        if (!isValidCardPin) {
            log.warn("Card PIN not valid for card: {}", card.getId());
            throw new ApiResourceException("Card PIN not valid", HttpStatus.BAD_REQUEST);
        }

        if (this.cardPinRepository.existsByCardId(card.getId())) {
            log.warn("Card Pin for Card with id {} already set", card.getId());
            throw new ApiResourceException("PIN has already been set for card", HttpStatus.CONFLICT);
        }

        String verificationData = this.pinVerificationDataService.createVerificationData(command.getPan(), command.getPin());

        CardPin cardPin = new CardPin();
        cardPin.setCardId(card.getId());
        cardPin.setPinVerificationData(verificationData);
        cardPin.setPinScheme(PinScheme.ISO0.getScheme());
        cardPin.setPinLength(command.getPin().length());

        this.cardPinRepository.save(cardPin);

        this.applicationEventPublisher.publishEvent(new CardPinSetEvent(card.getId(), cardPin.getId()));

        log.info("Successfully set PIN for Card with id {}", card.getId());
    }

    @Transactional
    @Override
    public void changePin(@Valid ChangeCardPinCommand command) {
        Card card = this.getCard(command.getPan());

        CardPin cardPin = this.cardPinRepository.findByCardIdForUpdate(card.getId())
                .orElseThrow(() -> {
                    log.warn("PIN has not been set for Card with id {}", card.getId());
                    return new ApiResourceException("PIN has not been set for Card", HttpStatus.BAD_REQUEST);
                });

        if (cardPin.getBlocked() == Boolean.TRUE) {
            log.warn("Card PIN with id: {} is blocked", cardPin.getId());
            throw new ApiResourceException("Card PIN is blocked", HttpStatus.FORBIDDEN);
        }

        if (!CardPinVerifier.verify(command.getCurrentPin(), cardPin.getPinVerificationData())) {
            log.warn("Current PIN is incorrect for Card: {}", card.getId());
            throw new ApiResourceException("Current PIN is incorrect for Card", HttpStatus.BAD_REQUEST);
        }

        boolean isValid = CardPinUtil.isValidCardPin(command.getNewPin());

        if (!isValid) {
            log.warn("Card PIN not valid for card: {}", card.getId());
            throw new ApiResourceException("Card PIN not valid", HttpStatus.BAD_REQUEST);
        }

        String verificationData = this.pinVerificationDataService.createVerificationData(command.getPan(), command.getNewPin());

        cardPin.setPinVerificationData(verificationData);
        cardPin.setPinLength(command.getNewPin().length());
        cardPin.setFailedAttempts(0);

        this.cardPinRepository.save(cardPin);

        this.applicationEventPublisher.publishEvent(new CardPinChangedEvent(card.getId(), cardPin.getId()));

        log.info("Successfully changed PIN for Card with id {}", card.getId());
    }

    @Transactional
    @Override
    public CardPinVerificationResult verifyPin(@Valid VerifyCardPinCommand command) {
        Card card = this.getCard(command.getPan());

        this.validateCardForPinOperation(card);

        CardPin cardPin = this.cardPinRepository.findByCardIdForUpdate(card.getId()).orElseThrow(() -> {
            log.warn("PIN has not been set for Card with id {}", card.getId());
            return new ApiResourceException("PIN has not been set for Card", HttpStatus.BAD_REQUEST);
        });

        if (cardPin.getBlocked() == Boolean.TRUE) {
            log.warn("Card PIN with id: {} is blocked", cardPin.getId());
            throw new ApiResourceException("Card PIN is blocked", HttpStatus.FORBIDDEN);
        }

        boolean valid = this.pinBlockVerifier.verify(new PinBlockVerificationCommand(command.getPan(), command.getEncryptedPinBlock(),
                cardPin.getPinVerificationData(), cardPin.getPinLength()));

        if (valid) {
            cardPin.setFailedAttempts(0);
            this.cardPinRepository.save(cardPin);

            return CardPinVerificationResult.SUCCESS;
        }

        int failedAttempts = cardPin.getFailedAttempts() + 1;
        cardPin.setFailedAttempts(failedAttempts);

        this.cardPinRepository.save(cardPin);

        if (failedAttempts >= cardPin.getMaxFailedAttempts()) {
            cardPin.setBlocked(Boolean.TRUE);

            log.warn("Card PIN with id: {} is blocked after {} failed attempts", cardPin.getId(), failedAttempts);

            this.cardPinRepository.save(cardPin);

            return CardPinVerificationResult.PIN_BLOCKED;
        }

        log.warn("Entered PIN for card [ID: {}] is incorrect", card.getId());

        return CardPinVerificationResult.INVALID_PIN;
    }

    @Transactional
    @Override
    public void unblockPin(Long cardId) {
        Card card = this.getCard(cardId);

        this.validateCardForPinOperation(card);

        CardPin cardPin = this.cardPinRepository.findByCardIdForUpdate(cardId)
                .orElseThrow(() -> {
                    log.warn("PIN has not been set for Card with id {}", cardId);
                    return new ApiResourceException("PIN has not been set for card", HttpStatus.BAD_REQUEST);
                });

        cardPin.setBlocked(Boolean.FALSE);
        cardPin.setFailedAttempts(0);

        this.cardPinRepository.save(cardPin);

        this.applicationEventPublisher.publishEvent(new CardPinUnblockedEvent(card.getId(), cardPin.getId()));

        log.info("Successfully unblocked PIN for Card with id {}", cardId);
    }

    private Card getCard(Long cardId) {
        return this.cardRepository.findById(cardId).orElseThrow(() -> {
            log.warn("Card not found with id {}", cardId);
            return new ApiResourceException("Card not found", HttpStatus.NOT_FOUND);
        });
    }

    private Card getCard(String pan) {
        String panHash = this.cardNumberHasher.hash(pan);

        return this.cardRepository.findCardByPanHash(panHash).orElseThrow(() -> {
            log.warn("Card not found with PAN {}", pan);
            return new ApiResourceException("Card not found", HttpStatus.NOT_FOUND);
        });
    }

    private void validateCardForPinOperation(Card card) {
        if (card.getStatus() == CardStatus.CLOSED) {
            log.warn("Card with id: {} is closed", card.getId());
            throw new ApiResourceException("Card is closed", HttpStatus.BAD_REQUEST);
        }

        if (card.getStatus() == CardStatus.EXPIRED) {
            log.warn("Card with id: {} is expired", card.getId());
            throw new ApiResourceException("Card is expired", HttpStatus.BAD_REQUEST);
        }

        if (card.getStatus() == CardStatus.PERMANENTLY_BLOCKED) {
            log.warn("Card with id: {} is permanently blocked", card.getId());
            throw new ApiResourceException("Card is permanently blocked", HttpStatus.BAD_REQUEST);
        }
    }
}
