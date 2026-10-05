package io.github.hossensyedriadh.cbs.cheque.api.controller.v1;

import io.github.hossensyedriadh.cbs.cheque.api.command.CancelChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.command.StopChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.model.ChequeView;
import io.github.hossensyedriadh.cbs.cheque.api.service.ChequeService;
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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/cheques/v1", consumes = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Cheque API", description = "API endpoint for managing cheques")
public class ChequeController {
    private final ChequeService chequeService;

    @Autowired
    public ChequeController(ChequeService chequeService) {
        this.chequeService = chequeService;
    }

    @InitBinder
    private void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @Operation(summary = "Get cheques by book ID", description = "Retrieve a list of cheques associated with a specific cheque book",
            parameters = {
                    @Parameter(name = "bookId", description = "ID of the cheque book to retrieve cheques from", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of cheques retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChequeView.class)))),
                    @ApiResponse(responseCode = "204", description = "No cheques found for the given book ID"),
                    @ApiResponse(responseCode = "404", description = "Cheque book not found", content = @Content)
            }
    )
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ChequeView>> getCheques(@PathVariable("bookId") Long bookId) {
        List<ChequeView> chequeViews = this.chequeService.getCheques(bookId);

        if (chequeViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(chequeViews);
    }

    @Operation(summary = "Get cheque by ID", description = "Retrieve a specific cheque by its ID",
            parameters = {
                    @Parameter(name = "id", description = "ID of the cheque to retrieve", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cheque retrieved successfully", content = @Content(schema = @Schema(implementation = ChequeView.class))),
                    @ApiResponse(responseCode = "404", description = "Cheque not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ChequeView> getCheque(@PathVariable("id") Long id) {
        Optional<ChequeView> chequeView = this.chequeService.getCheque(id);

        return chequeView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get cheque by number", description = "Retrieve a specific cheque by its number",
            parameters = {
                    @Parameter(name = "chequeNo", description = "Number of the cheque to retrieve", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cheque retrieved successfully", content = @Content(schema = @Schema(implementation = ChequeView.class))),
                    @ApiResponse(responseCode = "404", description = "Cheque not found", content = @Content)
            }
    )
    @GetMapping("/number/{chequeNo}")
    public ResponseEntity<ChequeView> getChequeByNumber(@PathVariable("chequeNo") Long chequeNo) {
        Optional<ChequeView> chequeView = this.chequeService.getChequeByNumber(chequeNo);

        return chequeView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Stop cheque", description = "Stop a specific cheque",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Command to stop a cheque", required = true, content = @Content(schema = @Schema(implementation = StopChequeCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Cheque stopped successfully"),
                    @ApiResponse(responseCode = "404", description = "Cheque not found", content = @Content)
            }
    )
    @PostMapping(value = "/stop", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> stopCheque(@RequestBody StopChequeCommand command) {
        this.chequeService.stopCheque(command);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancel cheque", description = "Cancel a specific cheque",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Command to cancel a cheque", required = true, content = @Content(schema = @Schema(implementation = CancelChequeCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Cheque cancelled successfully"),
                    @ApiResponse(responseCode = "404", description = "Cheque not found", content = @Content)
            }
    )
    @PostMapping(value = "/cancel", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> cancelCheque(@RequestBody CancelChequeCommand command) {
        this.chequeService.cancelCheque(command);

        return ResponseEntity.noContent().build();
    }
}
