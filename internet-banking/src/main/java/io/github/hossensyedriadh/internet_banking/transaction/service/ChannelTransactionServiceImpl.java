package io.github.hossensyedriadh.internet_banking.transaction.service;

import io.github.hossensyedriadh.internet_banking.exception.ResourceException;
import io.github.hossensyedriadh.internet_banking.http.cbs.model.CbsTransactionResponse;
import io.github.hossensyedriadh.internet_banking.transaction.entity.IbankingTransaction;
import io.github.hossensyedriadh.internet_banking.transaction.enumerator.ChannelTransactionStatus;
import io.github.hossensyedriadh.internet_banking.transaction.model.FundTransferRequest;
import io.github.hossensyedriadh.internet_banking.transaction.model.TransactionAcquisition;
import io.github.hossensyedriadh.internet_banking.transaction.repository.IbankingTransactionRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
class ChannelTransactionServiceImpl implements ChannelTransactionService {
    private final IbankingTransactionRepository ibankingTransactionRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ChannelTransactionServiceImpl(IbankingTransactionRepository ibankingTransactionRepository, ObjectMapper objectMapper) {
        this.ibankingTransactionRepository = ibankingTransactionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean existsWithIdempotencyKeyAndCustomerId(UUID key, Long customerId) {
        return this.ibankingTransactionRepository.existsByIdempotencyKeyAndCbsAccountId(key, customerId);
    }

    @Override
    public Optional<IbankingTransaction> findById(Long id) {
        return this.ibankingTransactionRepository.findById(id);
    }

    @Override
    public Optional<IbankingTransaction> findByIdempotencyKeyAndCustomerId(UUID key, Long customerId) {
        return this.ibankingTransactionRepository.findByIdempotencyKeyAndCbsAccountId(key, customerId);
    }

    @Transactional
    @Override
    public TransactionAcquisition createTransaction(UUID idempotencyKey, FundTransferRequest request) {
        byte[] requestHash = this.generateRequestHash(request);
        byte[] requestPayload = this.objectMapper.writeValueAsBytes(request);

        int inserted = this.ibankingTransactionRepository.insertIfAbsent(request.getAccountId(), idempotencyKey,
                requestHash, requestPayload);

        IbankingTransaction transaction = this.ibankingTransactionRepository.findByCbsAccountIdAndIdempotencyKey(request.getAccountId(), idempotencyKey)
                .orElseThrow(() -> {
                    log.error("Unable to retrieve channel transaction");
                    return new IllegalStateException("Unable to retrieve channel transaction");
                });

        if (!MessageDigest.isEqual(transaction.getRequestHash(), requestHash)) {
            throw new ResourceException("Idempotency key has already been used with a different request", HttpStatus.CONFLICT);
        }

        if (inserted == 1) {
            log.info("Created IBanking transaction [ID: {}]", transaction.getId());
        } else {
            log.info("Reusing existing IBanking transaction [ID: {}, Status: {}]", transaction.getId(), transaction.getStatus());
        }

        return new TransactionAcquisition(transaction, inserted == 1);
    }

    @Transactional
    @Override
    public void recordCbsResult(Long transactionId, CbsTransactionResponse cbsResponse) {
        IbankingTransaction transaction = this.ibankingTransactionRepository.findByIdForUpdate(transactionId).orElseThrow(() -> {
            log.warn("Channel transaction not found: {}", transactionId);
            return new ResourceException("Channel transaction not found: " + transactionId, HttpStatus.NOT_FOUND);
        });

        if (transaction.getStatus() != ChannelTransactionStatus.PROCESSING) {
            log.warn("Failed to record CBS result for transaction [ID: {}] in status: {}", transactionId, transaction.getStatus().toString());
            throw new ResourceException("Cannot record CBS result for transaction in status: " + transaction.getStatus(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ChannelTransactionStatus status = ChannelTransactionStatus.mapFromCbsTransactionStatus(cbsResponse.getStatus());

        transaction.setCbsCurrencyId(cbsResponse.getCurrencyId());
        transaction.setCbsTransactionId(cbsResponse.getId());
        transaction.setResponsePayload(this.objectMapper.writeValueAsBytes(cbsResponse));
        transaction.setStatus(status);

        if (status.isFinal()) {
            transaction.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }

        this.ibankingTransactionRepository.save(transaction);

        log.info("Recorded CBS result [Channel transaction ID: {}, CBS transaction ID: {}, Status: {}]", transactionId, cbsResponse.getId(), status);
    }

    @Transactional
    @Override
    public void markTransactionUnknown(Long id) {
        int updated = this.ibankingTransactionRepository.markTransactionUnknown(id, ChannelTransactionStatus.PROCESSING, ChannelTransactionStatus.UNKNOWN);

        if (updated == 1) {
            log.info("Marked IBanking transaction [ID: {}] as UNKNOWN", id);
        } else {
            log.warn("Could not mark IBanking transaction [ID: {}] as UNKNOWN; it may no longer be PROCESSING", id);
        }
    }

    @Transactional
    @Override
    public boolean claimForProcessing(Long transactionId) {
        return this.ibankingTransactionRepository.claimForProcessing(transactionId, ChannelTransactionStatus.RECEIVED,
                ChannelTransactionStatus.PROCESSING, OffsetDateTime.now(ZoneOffset.UTC)) == 1;
    }

    @Transactional
    @Override
    public boolean claimForReconciliation(Long transactionId) {
        return this.ibankingTransactionRepository.claimForReconciliation(transactionId, ChannelTransactionStatus.UNKNOWN,
                ChannelTransactionStatus.PROCESSING, OffsetDateTime.now(ZoneOffset.UTC)) == 1;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Long> findUnknownTransactionIds(int limit) {
        return this.ibankingTransactionRepository.findUnknownTransactionIds(ChannelTransactionStatus.UNKNOWN, PageRequest.of(0, limit));
    }

    private byte[] generateRequestHash(FundTransferRequest request) {
        try {
            String canonicalRequest = String.join("|",
                    request.getAccountId().toString(),
                    request.getDestinationAccountId().toString(),
                    request.getCurrencyId().toString(),
                    request.getAmount().stripTrailingZeros().toPlainString(),
                    request.getDescription()
            );

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(canonicalRequest.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
