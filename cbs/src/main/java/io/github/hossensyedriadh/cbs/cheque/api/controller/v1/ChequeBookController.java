package io.github.hossensyedriadh.cbs.cheque.api.controller.v1;

import io.github.hossensyedriadh.cbs.cheque.api.command.CancelChequeBookCommand;
import io.github.hossensyedriadh.cbs.cheque.api.command.IssueChequeBookCommand;
import io.github.hossensyedriadh.cbs.cheque.api.model.ChequeBookView;
import io.github.hossensyedriadh.cbs.cheque.api.service.ChequeBookService;
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
@RequestMapping(value = "/cheque-books/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Cheque Book API", description = "API endpoints for managing cheque books")
public class ChequeBookController {
    private final ChequeBookService chequeBookService;

    @Autowired
    public ChequeBookController(ChequeBookService chequeBookService) {
        this.chequeBookService = chequeBookService;
    }

    @InitBinder
    private void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @Operation(summary = "Get cheque books by account ID", description = "Retrieve a list of cheque books associated with a specific account",
            parameters = {@Parameter(name = "id", description = "Account ID", required = true)},
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of cheque books retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChequeBookView.class)))),
                    @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
            }
    )
    @GetMapping("/account/{id}")
    public ResponseEntity<List<ChequeBookView>> getChequeBooks(@PathVariable("id") Long accountId) {
        List<ChequeBookView> chequeBooks = this.chequeBookService.getChequeBooks(accountId);

        if (chequeBooks.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(chequeBooks);
    }

    @Operation(summary = "Get cheque book by ID", description = "Retrieve a specific cheque book by its ID",
            parameters = {@Parameter(name = "id", description = "Cheque book ID", required = true)},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cheque book retrieved successfully", content = @Content(schema = @Schema(implementation = ChequeBookView.class))),
                    @ApiResponse(responseCode = "404", description = "Cheque book not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ChequeBookView> getChequeBook(@PathVariable("id") Long chequeBookId) {
        Optional<ChequeBookView> chequeBookView = this.chequeBookService.getChequeBook(chequeBookId);

        return chequeBookView.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Issue a new cheque book", description = "Create a new cheque book for a specific account",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Issue cheque book command", required = true, content = @Content(schema = @Schema(implementation = IssueChequeBookCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Cheque book issued successfully", content = @Content(schema = @Schema(implementation = ChequeBookView.class)))
            }
    )
    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ChequeBookView> issueChequeBook(@RequestBody IssueChequeBookCommand command) {
        ChequeBookView chequeBookView = chequeBookService.issueChequeBook(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(chequeBookView.getId()).toUri();

        return ResponseEntity.created(location).body(chequeBookView);
    }

    @Operation(summary = "Cancel a cheque book", description = "Cancel a specific cheque book",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Cancel cheque book command", required = true, content = @Content(schema = @Schema(implementation = CancelChequeBookCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Cheque book cancelled successfully"),
                    @ApiResponse(responseCode = "404", description = "Cheque book not found", content = @Content)
            }
    )
    @PostMapping(value = "/cancel", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> cancelChequeBook(@RequestBody CancelChequeBookCommand command) {
        this.chequeBookService.cancelChequeBook(command);

        return ResponseEntity.noContent().build();
    }
}
