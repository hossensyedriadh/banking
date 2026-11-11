package io.github.hossensyedriadh.cbs.statement.api.controller.v1;

import io.github.hossensyedriadh.cbs.statement.api.command.GenerateMonthlyStatementCommand;
import io.github.hossensyedriadh.cbs.statement.api.command.GenerateOnDemandStatementCommand;
import io.github.hossensyedriadh.cbs.statement.api.model.StatementView;
import io.github.hossensyedriadh.cbs.statement.api.service.AccountStatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
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
import java.util.Optional;

@RestController
@RequestMapping(value = "/statements/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Statement API", description = "API endpoints for managing Account statements")
public class StatementController {
    private final AccountStatementService accountStatementService;

    @Autowired
    public StatementController(AccountStatementService accountStatementService) {
        this.accountStatementService = accountStatementService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @Operation(summary = "Get account statements", description = "Retrieves a paginated list of statements generated for the specified account",
            parameters = {
                    @Parameter(name = "accountId", description = "ID of the account to retrieve statements of", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StatementView.class))),
                    @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
            })
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<StatementView>> getStatements(@PathVariable("accountId") Long accountId, Pageable pageable) {
        Page<StatementView> statements = this.accountStatementService.getStatements(accountId, pageable);

        return ResponseEntity.ok(statements);
    }

    @Operation(summary = "Get account statement", description = "Retrieves a specific statement belonging to the specified account",
            parameters = {
                    @Parameter(name = "accountId", description = "ID of the account to retrieve statements of", required = true),
                    @Parameter(name = "statementId", description = "ID of the statement to retrieve", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StatementView.class))),
                    @ApiResponse(responseCode = "404", description = "Statement or account not found", content = @Content)
            })
    @GetMapping("/account/{accountId}/statement/{statementId}")
    public ResponseEntity<StatementView> getStatement(@PathVariable("accountId") Long accountId, @PathVariable("statementId") Long statementId) {
        Optional<StatementView> statement = this.accountStatementService.getStatement(accountId, statementId);

        return statement.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Generate monthly statement", description = "Generates a monthly account statement for the specified account and statement period",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Monthly statement generation request", required = true, content = @Content(schema = @Schema(implementation = GenerateMonthlyStatementCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "CREATED", content = @Content(schema = @Schema(implementation = StatementView.class)),
                            headers = @Header(name = "Location", description = "URI of the newly generated statement", schema = @Schema(type = "string", format = "uri"))
                    ),
                    @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
            }
    )
    @PostMapping(value = "/monthly", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StatementView> generateMonthlyStatement(@RequestBody GenerateMonthlyStatementCommand command) {
        StatementView statement = this.accountStatementService.generateMonthlyStatement(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/account/{accountId}/statement/{statementId}")
                .buildAndExpand(statement.getAccountId(), statement.getId()).toUri();

        return ResponseEntity.created(location).body(statement);
    }

    @Operation(summary = "Generate on-demand statement", description = "Generates an account statement for a custom date range",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "On-demand statement generation request", required = true, content = @Content(schema = @Schema(implementation = GenerateOnDemandStatementCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "CREATED", content = @Content(schema = @Schema(implementation = StatementView.class)),
                            headers = @Header(name = "Location", description = "URI of the newly generated statement", schema = @Schema(type = "string", format = "uri"))
                    ),
                    @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
            }
    )
    @PostMapping(value = "/on-demand", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StatementView> generateOnDemandStatement(@RequestBody GenerateOnDemandStatementCommand command) {
        StatementView statement = this.accountStatementService.generateOnDemandStatement(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/account/{accountId}/statement/{statementId}")
                .buildAndExpand(statement.getAccountId(), statement.getId()).toUri();

        return ResponseEntity.created(location).body(statement);
    }
}
