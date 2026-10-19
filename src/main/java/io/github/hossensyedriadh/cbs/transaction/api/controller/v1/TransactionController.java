package io.github.hossensyedriadh.cbs.transaction.api.controller.v1;

import io.github.hossensyedriadh.cbs.transaction.api.command.CreateDepositCommand;
import io.github.hossensyedriadh.cbs.transaction.api.command.CreateTransferCommand;
import io.github.hossensyedriadh.cbs.transaction.api.command.CreateWithdrawalCommand;
import io.github.hossensyedriadh.cbs.transaction.api.command.ReverseTransactionCommand;
import io.github.hossensyedriadh.cbs.transaction.api.model.AccountTransactionView;
import io.github.hossensyedriadh.cbs.transaction.api.model.TransactionView;
import io.github.hossensyedriadh.cbs.transaction.api.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;

@RestController
@RequestMapping(value = "/transactions/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Transaction API", description = "API endpoints for transactions")
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

    @Operation(summary = "Get transactions in date range", description = "Retrieve a paginated list of transactions within a specified date range",
            parameters = {
                    @Parameter(name = "from", description = "Start date of the date range", required = true, example = "2023-01-01T00:00:00Z"),
                    @Parameter(name = "to", description = "End date of the date range", required = true, example = "2023-01-31T23:59:59Z"),
                    @Parameter(name = "accountId", description = "Account ID to filter transactions by", example = "1234567890")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TransactionView.class)))
            })
    @GetMapping(params = {"from", "to"})
    public ResponseEntity<Page<TransactionView>> getTransactionsInDateRange(@RequestParam("from") OffsetDateTime startDate, @RequestParam("to") OffsetDateTime endDate,
                                                                            @RequestParam(value = "accountId", required = false) Long accountId, Pageable pageable) {
        Page<TransactionView> transactions = this.transactionService.getTransactionsInDateRange(startDate, endDate, accountId, pageable);

        return ResponseEntity.ok(transactions);
    }

    @Operation(summary = "Get account transactions", description = "Retrieve a paginated list of transactions for a specific account",
            parameters = {
                    @Parameter(name = "id", description = "Account ID", required = true, example = "1234567890")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountTransactionView.class)))
            })
    @GetMapping("/account/{id}")
    public ResponseEntity<Page<AccountTransactionView>> getAccountTransaction(@PathVariable("id") Long accountId, Pageable pageable) {
        Page<AccountTransactionView> page = this.transactionService.getAccountTransactions(accountId, pageable);

        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Get a transaction by ID", description = "Retrieve a transaction by its ID",
            parameters = {
                    @Parameter(name = "id", description = "Transaction ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TransactionView.class))),
                    @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content)
            })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionView> getTransaction(@PathVariable("id") Long id) {
        Optional<TransactionView> transaction = this.transactionService.getTransaction(id);

        return transaction.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get a transaction by reference", description = "Retrieve a transaction by its reference",
            parameters = {
                    @Parameter(name = "ref", description = "Transaction reference", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TransactionView.class))),
                    @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content)
            })
    @GetMapping("/reference/{ref}")
    public ResponseEntity<TransactionView> getTransaction(@PathVariable("ref") String reference) {
        Optional<TransactionView> transaction = this.transactionService.getTransactionByReference(reference);

        return transaction.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get a transaction by idempotency key", description = "Retrieve a transaction by its idempotency key",
            parameters = {
                    @Parameter(name = "key", description = "Transaction idempotency key", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TransactionView.class))),
                    @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content)
            })
    @GetMapping("/idempotency/{key}")
    public ResponseEntity<TransactionView> getTransactionByIdempotencyKey(@PathVariable("key") String key) {
        Optional<TransactionView> transaction = this.transactionService.getTransactionByIdempotencyKey(key);

        return transaction.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new deposit transaction", description = "Create a new deposit transaction",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The deposit command", required = true, content = @Content(schema = @Schema(implementation = CreateDepositCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = TransactionView.class)))
            })
    @PostMapping(value = "/deposits", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<TransactionView> deposit(@RequestBody CreateDepositCommand command) {
        TransactionView deposit = this.transactionService.deposit(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(deposit.getId()).toUri();

        return ResponseEntity.created(location).body(deposit);
    }

    @Operation(summary = "Create a new withdrawal transaction", description = "Create a new withdrawal transaction",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The withdrawal command", required = true, content = @Content(schema = @Schema(implementation = CreateWithdrawalCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = TransactionView.class)))
            })
    @PostMapping(value = "/withdrawals", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<TransactionView> withdraw(@RequestBody CreateWithdrawalCommand command) {
        TransactionView withdrawal = this.transactionService.withdraw(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(withdrawal.getId()).toUri();

        return ResponseEntity.created(location).body(withdrawal);
    }

    @Operation(summary = "Reverse a transaction", description = "Reverse a transaction",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The reversal command", required = true, content = @Content(schema = @Schema(implementation = ReverseTransactionCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = TransactionView.class)))
            })
    @PostMapping(value = "/reversals", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<TransactionView> reverse(@RequestBody ReverseTransactionCommand command) {
        TransactionView reversal = this.transactionService.reverse(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(reversal.getId()).toUri();

        return ResponseEntity.created(location).body(reversal);
    }

    @Operation(summary = "Create a new transfer transaction", description = "Create a new transfer transaction",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The transfer command", required = true, content = @Content(schema = @Schema(implementation = CreateTransferCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = TransactionView.class)))
            })
    @PostMapping(value = "/transfers", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<TransactionView> transfer(@RequestBody CreateTransferCommand command) {
        TransactionView transfer = this.transactionService.transfer(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(transfer.getId()).toUri();

        return ResponseEntity.created(location).body(transfer);
    }
}
