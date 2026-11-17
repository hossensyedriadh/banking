package io.github.hossensyedriadh.cbs.account.api.controller.v1;

import io.github.hossensyedriadh.cbs.account.api.command.CloseAccountCommand;
import io.github.hossensyedriadh.cbs.account.api.command.CreateAccountCommand;
import io.github.hossensyedriadh.cbs.account.api.model.AccountBalanceView;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountBalanceService;
import io.github.hossensyedriadh.cbs.account.api.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/accounts/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Account API", description = "API endpoints for managing accounts")
public class AccountController {
    private final AccountService accountService;
    private final AccountBalanceService accountBalanceService;

    @Autowired
    public AccountController(AccountService accountService, AccountBalanceService accountBalanceService) {
        this.accountService = accountService;
        this.accountBalanceService = accountBalanceService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @Operation(summary = "Get list of accounts for customer", description = "Get accounts for a customer by customer ID", parameters = {
            @Parameter(name = "customerId", description = "Customer ID", required = true, examples = {
                    @ExampleObject(value = "1", summary = "Customer ID example")
            })
    }, responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = AccountView.class)))),
            @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
    })
    @PreAuthorize("hasAuthority(T(io.github.hossensyedriadh.cbs.util.OAuth2Scopes).ACCOUNT_LIST)")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountView>> getAccountsForCustomer(@PathVariable("customerId") Long customerId) {
        List<AccountView> accountViews = this.accountService.getAccountsByCustomerId(customerId);

        if (accountViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(accountViews);
    }

    @Operation(summary = "Get list of active accounts for customer", description = "Get active accounts for a customer by customer ID", parameters = {
            @Parameter(name = "customerId", description = "Customer ID", required = true, examples = {
                    @ExampleObject(value = "1", summary = "Customer ID example")
            })
    }, responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = AccountView.class)))),
            @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
    })
    @PreAuthorize("hasAuthority(T(io.github.hossensyedriadh.cbs.util.OAuth2Scopes).ACCOUNT_LIST)")
    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<List<AccountView>> getActiveAccountsForCustomer(@PathVariable("customerId") Long customerId) {
        List<AccountView> accountViews = this.accountService.getActiveAccountsByCustomerId(customerId);

        if (accountViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(accountViews);
    }

    @Operation(
            summary = "Get account by ID",
            description = "Get an account by account ID",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Account ID",
                            required = true,
                            examples = {
                                    @ExampleObject(value = "1", summary = "Account ID example")
                            }
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(schema = @Schema(implementation = AccountView.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Account not found",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority(T(io.github.hossensyedriadh.cbs.util.OAuth2Scopes).ACCOUNT_READ)")
    @GetMapping("/{id}")
    public ResponseEntity<AccountView> getAccount(@PathVariable("id") Long id) {
        Optional<AccountView> accountView = this.accountService.getAccount(id);

        return accountView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get account by ID",
            description = "Get an account by account ID for authenticated customer",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Account ID",
                            required = true,
                            examples = {
                                    @ExampleObject(value = "1", summary = "Account ID example")
                            }
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(schema = @Schema(implementation = AccountView.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Account not found",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority(T(io.github.hossensyedriadh.cbs.util.OAuth2Scopes).ACCOUNT_READ)")
    @GetMapping("/principal/{id}")
    public ResponseEntity<AccountView> getPrincipalAccount(@PathVariable("id") Long id) {
        Optional<AccountView> accountView = this.accountService.getPrincipalAccount(id);

        return accountView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get account by account number",
            description = "Get an account by account number",
            parameters = {
                    @Parameter(
                            name = "number",
                            description = "Account number",
                            required = true,
                            examples = {
                                    @ExampleObject(value = "1000000001", summary = "Account number example")
                            }
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(schema = @Schema(implementation = AccountView.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Account not found",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority(T(io.github.hossensyedriadh.cbs.util.OAuth2Scopes).ACCOUNT_READ)")
    @GetMapping("/account-number/{number}")
    public ResponseEntity<AccountView> getAccount(@PathVariable("number") String accountNumber) {
        Optional<AccountView> accountView = this.accountService.getAccountByNumber(accountNumber);

        return accountView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get account balance",
            description = "Get the current balance of an account by account ID",
            parameters = {
                    @Parameter(
                            name = "accountId",
                            description = "Account ID",
                            required = true,
                            examples = {
                                    @ExampleObject(value = "1", summary = "Account ID example")
                            }
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(schema = @Schema(implementation = AccountBalanceView.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Account not Found",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority(T(io.github.hossensyedriadh.cbs.util.OAuth2Scopes).BALANCE_READ)")
    @GetMapping("/balance/{accountId}")
    public ResponseEntity<AccountBalanceView> getBalance(@PathVariable("accountId") Long accountId) {
        AccountBalanceView accountBalanceView = this.accountBalanceService.getBalance(accountId);

        return ResponseEntity.ok(accountBalanceView);
    }

    @Operation(
            summary = "Get account balance",
            description = "Get the current balance of an account by account ID and card ID",
            parameters = {
                    @Parameter(
                            name = "accountId",
                            description = "Account ID",
                            required = true,
                            examples = {
                                    @ExampleObject(value = "1", summary = "Account ID example")
                            }
                    ),
                    @Parameter(
                            name = "cardId",
                            description = "Card ID",
                            required = true,
                            examples = {
                                    @ExampleObject(value = "2", summary = "Card ID example")
                            }
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(schema = @Schema(implementation = AccountBalanceView.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Account not Found",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority(T(io.github.hossensyedriadh.cbs.util.OAuth2Scopes).BALANCE_READ)")
    @GetMapping("/balance/{accountId}/card/{cardId}")
    public ResponseEntity<AccountBalanceView> getBalance(@PathVariable("accountId") Long accountId, @PathVariable("cardId") Long cardId) {
        AccountBalanceView accountBalanceView = this.accountBalanceService.getBalance(accountId, cardId);

        return ResponseEntity.ok(accountBalanceView);
    }

    @Operation(
            summary = "Create account",
            description = "Create a new customer account",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Account creation details",
                    content = @Content(
                            schema = @Schema(implementation = CreateAccountCommand.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Account created successfully",
                            content = @Content(
                                    schema = @Schema(implementation = AccountView.class)
                            )
                    )
            }
    )
    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountView> createAccount(@RequestBody CreateAccountCommand command) {
        AccountView accountView = this.accountService.createAccount(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(accountView.getId()).toUri();

        return ResponseEntity.created(location).body(accountView);
    }

    @Operation(
            summary = "Activate account",
            description = "Activate an account by account ID",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Account ID",
                            required = true,
                            examples = {
                                    @ExampleObject(value = "1", summary = "Account ID example")
                            }
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Account activated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Account not found",
                            content = @Content
                    )
            }
    )
    @PostMapping("/activation/{id}")
    public ResponseEntity<Void> activateAccount(@PathVariable("id") Long id) {
        this.accountService.activateAccount(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Deactivate account",
            description = "Deactivate an account by account ID",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Account ID",
                            required = true,
                            examples = {
                                    @ExampleObject(value = "1", summary = "Account ID example")
                            }
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Account deactivated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Account not found",
                            content = @Content
                    )
            }
    )
    @PostMapping("/deactivation/{id}")
    public ResponseEntity<Void> deactivateAccount(@PathVariable("id") Long id) {
        this.accountService.deactivateAccount(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Close account",
            description = "Close an account",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Account closure details",
                    content = @Content(
                            schema = @Schema(implementation = CloseAccountCommand.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Account closed successfully",
                            content = @Content(
                                    schema = @Schema(implementation = AccountView.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Account not found",
                            content = @Content
                    )
            }
    )
    @PostMapping(value = "/closure", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountView> closeAccount(@RequestBody CloseAccountCommand command) {
        AccountView accountView = this.accountService.closeAccount(command);

        return ResponseEntity.ok(accountView);
    }
}
