package io.github.hossensyedriadh.cbs.account.api.controller.v1;

import io.github.hossensyedriadh.cbs.account.api.command.CreateAccountRestrictionCommand;
import io.github.hossensyedriadh.cbs.account.api.command.ReleaseAccountRestrictionCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountRestrictionView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountRestrictionService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping(value = "/account-restrictions/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Account Restriction API", description = "API endpoints for managing account restrictions")
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

    @Operation(summary = "Get account restrictions", description = "Retrieve account restrictions based on the provided account ID and status.", responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountRestrictionView.class)))),
            @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
    })
    @GetMapping("/account/{accountId}/status/{status}")
    public ResponseEntity<List<AccountRestrictionView>> getRestrictions(@PathVariable("accountId") Long accountId, @PathVariable("status") AccountRestrictionStatus status) {
        List<AccountRestrictionView> restrictions = this.accountRestrictionService.getRestrictions(accountId, status);

        if (restrictions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(restrictions);
    }

    @Operation(summary = "Get account restriction", description = "Retrieve an account restriction based on the provided ID.", responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountRestrictionView.class))),
            @ApiResponse(responseCode = "404", description = "Account restriction not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccountRestrictionView> getRestriction(@PathVariable("id") Long holdId) {
        Optional<AccountRestrictionView> restriction = this.accountRestrictionService.getRestriction(holdId);

        return restriction.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create account restriction", description = "Create a new account restriction based on the provided command.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The command to create the restriction", required = true, content = @Content(schema = @Schema(implementation = CreateAccountRestrictionCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created restriction successfully", content = @Content(schema = @Schema(implementation = AccountRestrictionView.class))),
                    @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
            })
    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountRestrictionView> createRestriction(@RequestBody CreateAccountRestrictionCommand command) {
        AccountRestrictionView saved = this.accountRestrictionService.createRestriction(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @Operation(summary = "Release account restriction", description = "Release an existing account restriction based on the provided restriction ID.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Release account restriction command", required = true, content = @Content(schema = @Schema(implementation = ReleaseAccountRestrictionCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountRestrictionView.class))),
                    @ApiResponse(responseCode = "404", description = "Account restriction not found", content = @Content)
            })
    @PostMapping(value = "/release", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountRestrictionView> releaseRestriction(@RequestBody ReleaseAccountRestrictionCommand command) {
        AccountRestrictionView released = this.accountRestrictionService.releaseRestriction(command);

        return ResponseEntity.ok(released);
    }
}
