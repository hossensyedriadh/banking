package io.github.hossensyedriadh.cbs.account.api.controller.v1;

import io.github.hossensyedriadh.cbs.account.api.command.CreateAccountHoldCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHoldStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountHoldView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountHoldService;
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
@RequestMapping(value = "/account-holds/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
public class AccountHoldController {
    private final AccountHoldService accountHoldService;

    @Autowired
    public AccountHoldController(AccountHoldService accountHoldService) {
        this.accountHoldService = accountHoldService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/account/{accountId}/status/{status}")
    public ResponseEntity<List<AccountHoldView>> getHolds(@PathVariable("accountId") Long accountId, @PathVariable("status") AccountHoldStatus status) {
        List<AccountHoldView> holds = this.accountHoldService.getHolds(accountId, status);

        if (holds.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(holds);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountHoldView> getHold(@PathVariable("id") Long holdId) {
        Optional<AccountHoldView> hold = this.accountHoldService.getHold(holdId);

        return hold.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountHoldView> createHold(@RequestBody CreateAccountHoldCommand command) {
        AccountHoldView saved = this.accountHoldService.createHold(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @PostMapping("/release/{id}")
    public ResponseEntity<AccountHoldView> releaseHold(@PathVariable("id") Long holdId) {
        AccountHoldView released = this.accountHoldService.releaseHold(holdId);

        return ResponseEntity.ok(released);
    }
}
