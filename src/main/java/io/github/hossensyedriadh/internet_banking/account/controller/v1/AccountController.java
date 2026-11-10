package io.github.hossensyedriadh.internet_banking.account.controller.v1;

import io.github.hossensyedriadh.internet_banking.account.model.AccountOverviewModel;
import io.github.hossensyedriadh.internet_banking.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/accounts", produces = {MediaType.APPLICATION_JSON_VALUE})
public class AccountController {
    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<AccountOverviewModel>> getAccounts() {
        List<AccountOverviewModel> accounts = this.accountService.overview();

        if (accounts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(accounts);
    }
}
