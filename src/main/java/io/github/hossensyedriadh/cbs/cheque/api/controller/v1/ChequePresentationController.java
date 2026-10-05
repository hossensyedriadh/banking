package io.github.hossensyedriadh.cbs.cheque.api.controller.v1;

import io.github.hossensyedriadh.cbs.cheque.api.command.PresentChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.command.ReturnChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.model.ChequePresentationView;
import io.github.hossensyedriadh.cbs.cheque.api.service.ChequePresentationService;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/cheque-presentations/v1", consumes = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Cheque Presentation API", description = "API endpoints for managing cheque presentations")
public class ChequePresentationController {
    private final ChequePresentationService chequePresentationService;

    @Autowired
    public ChequePresentationController(ChequePresentationService chequePresentationService) {
        this.chequePresentationService = chequePresentationService;
    }

    @InitBinder
    private void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @Operation(summary = "Get cheque presentations", description = "Retrieve all cheque presentations for a specific cheque",
            parameters = {
                    @Parameter(name = "chequeId", description = "The ID of the cheque to retrieve presentations for", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cheque presentations retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChequePresentationView.class)))),
                    @ApiResponse(responseCode = "204", description = "No cheque presentations found", content = @Content)
            }
    )
    @GetMapping("/cheque/{chequeId}")
    public ResponseEntity<List<ChequePresentationView>> getPresentations(@PathVariable("chequeId") Long chequeId) {
        List<ChequePresentationView> chequePresentations = this.chequePresentationService.getPresentations(chequeId);

        if (chequePresentations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(chequePresentations);
    }

    @Operation(summary = "Get cheque presentation", description = "Retrieve a specific cheque presentation by ID",
            parameters = {
                    @Parameter(name = "id", description = "The ID of the cheque presentation to retrieve", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cheque presentation retrieved successfully", content = @Content(schema = @Schema(implementation = ChequePresentationView.class))),
                    @ApiResponse(responseCode = "404", description = "Cheque presentation not found", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ChequePresentationView> getPresentation(@PathVariable("id") Long id) {
        Optional<ChequePresentationView> chequePresentationView = this.chequePresentationService.getPresentation(id);

        return chequePresentationView.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Present cheque", description = "Present a cheque for presentation",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Command to present the cheque", required = true, content = @Content(schema = @Schema(implementation = PresentChequeCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cheque presented successfully", content = @Content(schema = @Schema(implementation = ChequePresentationView.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid command")
            }
    )
    @PostMapping(value = "/presentation", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ChequePresentationView> presentCheque(@RequestBody PresentChequeCommand command) {
        ChequePresentationView presented = this.chequePresentationService.presentCheque(command);

        URI location = UriComponentsBuilder.fromPath("/{id}").buildAndExpand(presented.getId()).toUri();

        return ResponseEntity.created(location).body(presented);
    }

    @Operation(summary = "Accept cheque", description = "Accept presented cheque",
            parameters = {
                    @Parameter(name = "id", description = "Cheque ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cheque accepted successfully", content = @Content(schema = @Schema(implementation = ChequePresentationView.class))),
                    @ApiResponse(responseCode = "404", description = "Cheque presentation not found", content = @Content)
            }
    )
    @PostMapping(value = "/acceptance/{id}")
    public ResponseEntity<ChequePresentationView> acceptCheque(@PathVariable("id") Long id) {
        ChequePresentationView accepted = this.chequePresentationService.acceptCheque(id);

        return ResponseEntity.ok(accepted);
    }

    @Operation(summary = "Pay cheque", description = "Pay presented cheque",
            parameters = {
                    @Parameter(name = "id", description = "Cheque ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cheque accepted successfully", content = @Content(schema = @Schema(implementation = ChequePresentationView.class))),
                    @ApiResponse(responseCode = "404", description = "Cheque presentation not found", content = @Content)
            }
    )
    @PostMapping(value = "/payment/{id}")
    public ResponseEntity<ChequePresentationView> payCheque(@PathVariable("id") Long id) {
        ChequePresentationView paid = this.chequePresentationService.payCheque(id);

        return ResponseEntity.ok(paid);
    }

    @Operation(summary = "Return cheque", description = "Return presented cheque",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Return cheque command",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ReturnChequeCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cheque returned successfully", content = @Content(schema = @Schema(implementation = ChequePresentationView.class))),
                    @ApiResponse(responseCode = "404", description = "Cheque presentation not found", content = @Content)
            }
    )
    @PostMapping(value = "/return", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ChequePresentationView> presentCheque(@RequestBody ReturnChequeCommand command) {
        ChequePresentationView returned = this.chequePresentationService.returnCheque(command);

        return ResponseEntity.ok(returned);
    }
}
