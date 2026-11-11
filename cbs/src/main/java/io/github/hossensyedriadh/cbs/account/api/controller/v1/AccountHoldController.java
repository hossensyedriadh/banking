package io.github.hossensyedriadh.cbs.account.api.controller.v1;

import io.github.hossensyedriadh.cbs.account.api.command.CreateAccountHoldCommand;
import io.github.hossensyedriadh.cbs.account.api.command.ReleaseAccountHoldCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHoldStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountHoldView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountHoldService;
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
@RequestMapping(value = "/account-holds/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Account Hold API", description = "API endpoints for managing account holds")
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

    @Operation(summary = "Get account holds by account ID and status", description = "Retrieve a list of account holds based on the provided account ID and status.", parameters = {
            @Parameter(name = "accountId", description = "The ID of the account to retrieve holds for", required = true),
            @Parameter(name = "status", description = "The status of the holds to retrieve", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountHoldView.class)))),
            @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
    })
    @GetMapping("/account/{accountId}/status/{status}")
    public ResponseEntity<List<AccountHoldView>> getHolds(@PathVariable("accountId") Long accountId, @PathVariable("status") AccountHoldStatus status) {
        List<AccountHoldView> holds = this.accountHoldService.getHolds(accountId, status);

        if (holds.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(holds);
    }

    @Operation(summary = "Get account hold by ID", description = "Retrieve an account hold based on the provided ID.", parameters = {
            @Parameter(name = "id", description = "The ID of the hold to retrieve", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountHoldView.class))),
            @ApiResponse(responseCode = "404", description = "Account hold not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccountHoldView> getHold(@PathVariable("id") Long holdId) {
        Optional<AccountHoldView> hold = this.accountHoldService.getHold(holdId);

        return hold.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new account hold", description = "Create a new account hold based on the provided command.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The command to create the hold", required = true, content = @Content(schema = @Schema(implementation = CreateAccountHoldCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created hold successfully", content = @Content(schema = @Schema(implementation = AccountHoldView.class))),
                    @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
            })
    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountHoldView> createHold(@RequestBody CreateAccountHoldCommand command) {
        AccountHoldView saved = this.accountHoldService.createHold(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @Operation(summary = "Release an account hold", description = "Release an account hold based on the provided hold ID.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Release account hold command", required = true, content = @Content(schema = @Schema(implementation = ReleaseAccountHoldCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountHoldView.class))),
                    @ApiResponse(responseCode = "404", description = "Account hold not found", content = @Content)
            })
    @PostMapping(value = "/release", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountHoldView> releaseHold(@RequestBody ReleaseAccountHoldCommand command) {
        AccountHoldView released = this.accountHoldService.releaseHold(command);

        return ResponseEntity.ok(released);
    }
}
