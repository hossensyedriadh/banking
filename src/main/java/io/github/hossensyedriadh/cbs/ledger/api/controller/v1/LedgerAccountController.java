package io.github.hossensyedriadh.cbs.ledger.api.controller.v1;

import io.github.hossensyedriadh.cbs.ledger.api.command.CreateLedgerAccountCommand;
import io.github.hossensyedriadh.cbs.ledger.api.command.UpdateLedgerAccountCommand;
import io.github.hossensyedriadh.cbs.ledger.api.model.LedgerAccountView;
import io.github.hossensyedriadh.cbs.ledger.api.service.LedgerAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping(value = "/ledger-accounts/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Ledger Account API", description = "API endpoints for managing ledger accounts")
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

    @Operation(summary = "Get ledger accounts", description = "Retrieve a list of ledger accounts",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = LedgerAccountView.class)))),
                    @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
            })
    @GetMapping("/")
    public ResponseEntity<List<LedgerAccountView>> getLedgerAccounts() {
        List<LedgerAccountView> ledgerAccountViews = this.ledgerAccountService.getLedgerAccounts();

        if (ledgerAccountViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(ledgerAccountViews);
    }

    @Operation(summary = "Get ledger account by ID", description = "Retrieve a ledger account by ID",
            parameters = {
                    @Parameter(name = "id", description = "ID of the ledger account", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LedgerAccountView.class))),
                    @ApiResponse(responseCode = "404", description = "Ledger account not found", content = @Content)
            })
    @GetMapping("/{id}")
    public ResponseEntity<LedgerAccountView> getLedgerAccount(@PathVariable("id") Long id) {
        Optional<LedgerAccountView> ledgerAccountView = this.ledgerAccountService.getLedgerAccount(id);

        return ledgerAccountView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get ledger account by code", description = "Retrieve a ledger account by code",
            parameters = {
                    @Parameter(name = "code", description = "Code of the ledger account", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LedgerAccountView.class))),
                    @ApiResponse(responseCode = "404", description = "Ledger account not found", content = @Content)
            })
    @GetMapping("/code/{code}")
    public ResponseEntity<LedgerAccountView> getLedgerAccount(@PathVariable("code") String accountCode) {
        Optional<LedgerAccountView> ledgerAccountView = this.ledgerAccountService.getLedgerAccountByCode(accountCode);

        return ledgerAccountView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new ledger account", description = "Create a new ledger account",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Command to create a new ledger account", required = true, content = @Content(schema = @Schema(implementation = CreateLedgerAccountCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Ledger account created successfully", content = @Content(schema = @Schema(implementation = LedgerAccountView.class)))
            })
    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<LedgerAccountView> createLedgerAccount(@RequestBody CreateLedgerAccountCommand command) {
        LedgerAccountView saved = this.ledgerAccountService.createLedgerAccount(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @Operation(summary = "Update ledger account", description = "Update an existing ledger account",
            parameters = {
                    @Parameter(name = "id", description = "ID of the ledger account", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Command to update the ledger account", required = true, content = @Content(schema = @Schema(implementation = UpdateLedgerAccountCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LedgerAccountView.class))),
                    @ApiResponse(responseCode = "404", description = "Ledger account not found", content = @Content)
            })
    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<LedgerAccountView> updateLedgerAccount(@PathVariable("id") Long id, @RequestBody UpdateLedgerAccountCommand command) {
        LedgerAccountView updated = this.ledgerAccountService.updateLedgerAccount(id, command);

        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Activate ledger account", description = "Activate an existing ledger account",
            parameters = {
                    @Parameter(name = "id", description = "ID of the ledger account", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "No content"),
                    @ApiResponse(responseCode = "404", description = "Ledger account not found", content = @Content)
            })
    @PostMapping("/activation/{id}")
    public ResponseEntity<Void> activateLedgerAccount(@PathVariable("id") Long id) {
        this.ledgerAccountService.activateLedgerAccount(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deactivate ledger account", description = "Deactivate an existing ledger account",
            parameters = {
                    @Parameter(name = "id", description = "ID of the ledger account", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "No content"),
                    @ApiResponse(responseCode = "404", description = "Ledger account not found", content = @Content)
            })
    @PostMapping("/deactivation/{id}")
    public ResponseEntity<Void> deactivateLedgerAccount(@PathVariable("id") Long id) {
        this.ledgerAccountService.deactivateLedgerAccount(id);

        return ResponseEntity.noContent().build();
    }
}
