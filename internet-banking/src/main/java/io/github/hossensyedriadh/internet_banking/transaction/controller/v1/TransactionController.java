package io.github.hossensyedriadh.internet_banking.transaction.controller.v1;

import io.github.hossensyedriadh.internet_banking.http.cbs.enumerator.CbsTransactionStatus;
import io.github.hossensyedriadh.internet_banking.http.cbs.enumerator.TransactionType;
import io.github.hossensyedriadh.internet_banking.http.cbs.model.CbsTransactionsSearch;
import io.github.hossensyedriadh.internet_banking.transaction.model.FundTransferRequest;
import io.github.hossensyedriadh.internet_banking.transaction.model.TransactionModel;
import io.github.hossensyedriadh.internet_banking.transaction.service.TransactionService;
import io.github.hossensyedriadh.internet_banking.util.CustomAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/transactions", produces = {MediaType.APPLICATION_JSON_VALUE})
public class TransactionController {
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping(params = {"from", "to", "accountId"})
    public ResponseEntity<Page<TransactionModel>> transactions(Pageable pageable, @RequestParam("from") OffsetDateTime from, @RequestParam("to") OffsetDateTime to,
                                                               @RequestParam("accountId") Long accountId,
                                                               @RequestParam(value = "type", required = false) TransactionType type,
                                                               @RequestParam(value = "status", required = false) CbsTransactionStatus status) {
        Page<TransactionModel> transactionModels = this.transactionService.fetchTransactions(new CbsTransactionsSearch(from, to, type, status, accountId,
                pageable.getPageNumber(), pageable.getPageSize()));

        return ResponseEntity.ok(transactionModels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionModel> transaction(@PathVariable("id") Long id) {
        TransactionModel transaction = this.transactionService.fetchTransaction(id);

        return ResponseEntity.ok(transaction);
    }

    @PostMapping(value = "/transfers", consumes = {MediaType.APPLICATION_JSON_VALUE}, headers = {CustomAttributes.TRANSACTION_IDEMPOTENCY_KEY_HEADER})
    public ResponseEntity<TransactionModel> transfer(@RequestHeader(CustomAttributes.TRANSACTION_IDEMPOTENCY_KEY_HEADER) UUID idempotencyKey, @RequestBody FundTransferRequest request) {
        TransactionModel transfer = this.transactionService.transfer(idempotencyKey, request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(transfer.getId()).toUri();

        return ResponseEntity.created(location).body(transfer);
    }
}
