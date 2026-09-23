package io.github.hossensyedriadh.cbs.transaction.api.controller.v1;

import io.github.hossensyedriadh.cbs.transaction.api.model.AccountTransactionView;
import io.github.hossensyedriadh.cbs.transaction.api.model.TransactionView;
import io.github.hossensyedriadh.cbs.transaction.api.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "/transactions/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
public class TransactionController {
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/account/{id}")
    public ResponseEntity<Page<AccountTransactionView>> getAccountTransaction(@PathVariable("id") Long accountId, Pageable pageable) {
        Page<AccountTransactionView> page = this.transactionService.getAccountTransactions(accountId, pageable);

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionView> getTransaction(@PathVariable("id") Long id) {
        Optional<TransactionView> transaction = this.transactionService.getTransaction(id);

        return transaction.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/reference/{ref}")
    public ResponseEntity<TransactionView> getTransaction(@PathVariable("ref") String reference) {
        Optional<TransactionView> transaction = this.transactionService.getTransactionByReference(reference);

        return transaction.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}
