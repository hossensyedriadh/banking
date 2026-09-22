package io.github.hossensyedriadh.cbs.account.api.controller.v1;

import io.github.hossensyedriadh.cbs.account.api.command.CloseAccountCommand;
import io.github.hossensyedriadh.cbs.account.api.command.CreateAccountCommand;
import io.github.hossensyedriadh.cbs.account.api.model.AccountBalanceView;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountBalanceService;
import io.github.hossensyedriadh.cbs.account.api.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/accounts/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
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

    @GetMapping("/customer/{customer}")
    public ResponseEntity<List<AccountView>> getAccountsForCustomer(@PathVariable("customer") Long customerId) {
        List<AccountView> accountViews = this.accountService.getAccountByCustomerId(customerId);

        if (accountViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(accountViews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountView> getAccount(@PathVariable("id") Long id) {
        Optional<AccountView> accountView = this.accountService.getAccount(id);

        return accountView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/account-number/{number}")
    public ResponseEntity<AccountView> getAccount(@PathVariable("number") String accountNumber) {
        Optional<AccountView> accountView = this.accountService.getAccountByNumber(accountNumber);

        return accountView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/balance/{accountId}")
    public ResponseEntity<AccountBalanceView> getBalance(@PathVariable("accountId") Long accountId) {
        AccountBalanceView accountBalanceView = this.accountBalanceService.getBalance(accountId);

        return ResponseEntity.ok(accountBalanceView);
    }

    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountView> createAccount(@RequestBody CreateAccountCommand command) {
        AccountView accountView = this.accountService.createAccount(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(accountView.getId()).toUri();

        return ResponseEntity.created(location).body(accountView);
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<Void> activateAccount(@PathVariable("id") Long id) {
        this.accountService.activateAccount(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateAccount(@PathVariable("id") Long id) {
        this.accountService.deactivateAccount(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/close", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountView> closeAccount(@Valid CloseAccountCommand command) {
        AccountView accountView = this.accountService.closeAccount(command);

        return ResponseEntity.ok(accountView);
    }
}
