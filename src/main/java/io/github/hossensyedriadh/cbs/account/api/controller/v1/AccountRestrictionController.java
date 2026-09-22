package io.github.hossensyedriadh.cbs.account.api.controller.v1;

import io.github.hossensyedriadh.cbs.account.api.command.CreateAccountRestrictionCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountRestrictionView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountRestrictionService;
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
@RequestMapping(value = "/account-restrictions/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
public class AccountRestrictionController {
    private final AccountRestrictionService accountRestrictionService;

    @Autowired
    public AccountRestrictionController(AccountRestrictionService accountRestrictionService) {
        this.accountRestrictionService = accountRestrictionService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/account/{accountId}/status/{status}")
    public ResponseEntity<List<AccountRestrictionView>> getRestrictions(@PathVariable("accountId") Long accountId, @PathVariable("status") AccountRestrictionStatus status) {
        List<AccountRestrictionView> restrictions = this.accountRestrictionService.getRestrictions(accountId, status);

        if (restrictions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(restrictions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountRestrictionView> getRestriction(@PathVariable("id") Long holdId) {
        Optional<AccountRestrictionView> restriction = this.accountRestrictionService.getRestriction(holdId);

        return restriction.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountRestrictionView> createRestriction(@RequestBody CreateAccountRestrictionCommand command) {
        AccountRestrictionView saved = this.accountRestrictionService.createRestriction(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @PostMapping("/release/{id}")
    public ResponseEntity<AccountRestrictionView> releaseRestriction(@PathVariable("id") Long restrictionId) {
        AccountRestrictionView released = this.accountRestrictionService.releaseRestriction(restrictionId);

        return ResponseEntity.ok(released);
    }
}
