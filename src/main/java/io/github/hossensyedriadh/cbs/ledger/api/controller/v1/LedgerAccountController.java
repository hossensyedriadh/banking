package io.github.hossensyedriadh.cbs.ledger.api.controller.v1;

import io.github.hossensyedriadh.cbs.ledger.api.command.CreateLedgerAccountCommand;
import io.github.hossensyedriadh.cbs.ledger.api.command.UpdateLedgerAccountCommand;
import io.github.hossensyedriadh.cbs.ledger.api.model.LedgerAccountView;
import io.github.hossensyedriadh.cbs.ledger.api.service.LedgerAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping(value = "/ledger-accounts/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
public class LedgerAccountController {
    private final LedgerAccountService ledgerAccountService;

    @Autowired
    public LedgerAccountController(LedgerAccountService ledgerAccountService) {
        this.ledgerAccountService = ledgerAccountService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LedgerAccountView> getLedgerAccount(@PathVariable("id") Long id) {
        Optional<LedgerAccountView> ledgerAccountView = this.ledgerAccountService.getLedgerAccount(id);

        return ledgerAccountView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<LedgerAccountView> getLedgerAccount(@PathVariable("code") String accountCode) {
        Optional<LedgerAccountView> ledgerAccountView = this.ledgerAccountService.getLedgerAccountByCode(accountCode);

        return ledgerAccountView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<LedgerAccountView> createLedgerAccount(@RequestBody CreateLedgerAccountCommand command) {
        LedgerAccountView saved = this.ledgerAccountService.createLedgerAccount(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<LedgerAccountView> updateLedgerAccount(@PathVariable("id") Long id, @RequestBody UpdateLedgerAccountCommand command) {
        LedgerAccountView updated = this.ledgerAccountService.updateLedgerAccount(id, command);

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<Void> activateLedgerAccount(@PathVariable("id") Long id) {
        this.ledgerAccountService.activateLedgerAccount(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateLedgerAccount(@PathVariable("id") Long id) {
        this.ledgerAccountService.deactivateLedgerAccount(id);

        return ResponseEntity.noContent().build();
    }
}
