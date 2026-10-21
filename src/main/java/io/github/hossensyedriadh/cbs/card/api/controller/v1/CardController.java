package io.github.hossensyedriadh.cbs.card.api.controller.v1;

import io.github.hossensyedriadh.cbs.card.api.command.*;
import io.github.hossensyedriadh.cbs.card.api.model.CardView;
import io.github.hossensyedriadh.cbs.card.api.service.CardService;
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
@RequestMapping(value = "/cards/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Card API", description = "API endpoints for managing cards")
public class CardController {
    private final CardService cardService;

    @Autowired
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @Operation(summary = "Get all cards for an account", description = "Retrieves all cards associated with the specified account",
            parameters = {
                    @Parameter(name = "id", description = "Account ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of cards associated with the account", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CardView.class)))),
                    @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
            })
    @GetMapping("/account/{id}")
    public ResponseEntity<List<CardView>> getCards(@PathVariable("id") Long accountId) {
        List<CardView> cardViews = this.cardService.getCards(accountId);

        if (cardViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(cardViews);
    }

    @Operation(summary = "Get a card by ID", description = "Retrieves a card associated with the specified ID",
            parameters = {
                    @Parameter(name = "id", description = "Card ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card associated with the ID", content = @Content(schema = @Schema(implementation = CardView.class))),
                    @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
            })
    @GetMapping("/{id}")
    public ResponseEntity<CardView> getCard(@PathVariable("id") Long id) {
        Optional<CardView> cardView = this.cardService.getCard(id);

        return cardView.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Get card by PAN", description = "Retrieves a card associated with the specified PAN",
            parameters = {
                    @Parameter(name = "pan", description = "Card number", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card associated with the PAN", content = @Content(schema = @Schema(implementation = CardView.class))),
                    @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
            })
    @GetMapping("/pan/{pan}")
    public ResponseEntity<CardView> getCard(@PathVariable("pan") String pan) {
        Optional<CardView> cardView = this.cardService.getCard(pan);

        return cardView.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Issue a new card", description = "Issues a new card based on the provided command",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Card issue command",
                    content = {@Content(schema = @Schema(implementation = IssueCardCommand.class))},
                    required = true
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Card issued successfully", content = @Content(schema = @Schema(implementation = CardView.class)))
            })
    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardView> issueCard(@RequestBody IssueCardCommand command) {
        CardView cardView = this.cardService.issueCard(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(cardView.getId()).toUri();

        return ResponseEntity.created(location).body(cardView);
    }

    @Operation(summary = "Replace card", description = "Replaces card with a new one",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Replace card command",
                    content = {@Content(schema = @Schema(implementation = ReplaceCardCommand.class))},
                    required = true
            ), responses = {
            @ApiResponse(responseCode = "201", description = "Card replacement issued successfully", content = @Content(schema = @Schema(implementation = CardView.class))),
            @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    })
    @PostMapping(value = "/replacement", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardView> replaceCard(@RequestBody ReplaceCardCommand command) {
        CardView cardView = this.cardService.replaceCard(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(cardView.getId()).toUri();

        return ResponseEntity.created(location).body(cardView);
    }

    @Operation(summary = "Activate card", description = "Activate a card using the provided activation details",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Card activation command",
                    content = {@Content(schema = @Schema(implementation = ActivateCardCommand.class))},
                    required = true
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card activated successfully",
                            content = {@Content(schema = @Schema(implementation = CardView.class))}
                    ),
                    @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
            }
    )
    @PostMapping(value = "/activation", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardView> activation(@RequestBody ActivateCardCommand command) {
        CardView cardView = this.cardService.activateCard(command);

        return ResponseEntity.ok(cardView);
    }

    @Operation(summary = "Temporary block card", description = "Temporarily block a card",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Block card command temporarily", required = true,
                    content = {@Content(schema = @Schema(implementation = BlockCardCommand.class))}
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card temporarily blocked successfully", content = @Content(schema = @Schema(implementation = CardView.class))),
                    @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
            }
    )
    @PostMapping(value = "/temporary-block", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardView> temporaryBlockCard(@RequestBody BlockCardCommand command) {
        CardView cardView = this.cardService.temporarilyBlockCard(command);

        return ResponseEntity.ok(cardView);
    }

    @Operation(summary = "Permanently block card", description = "Permanently block a card",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Block card command permanently", required = true,
                    content = {@Content(schema = @Schema(implementation = BlockCardCommand.class))}
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card permanently blocked successfully", content = @Content(schema = @Schema(implementation = CardView.class))),
                    @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
            }
    )
    @PostMapping(value = "/permanent-block", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardView> permanentlyBlockCard(@RequestBody BlockCardCommand command) {
        CardView cardView = this.cardService.permanentlyBlockCard(command);

        return ResponseEntity.ok(cardView);
    }

    @Operation(summary = "Unblock card", description = "Unblock a card",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Unblock card command", required = true,
                    content = {@Content(schema = @Schema(implementation = UnblockCardCommand.class))}
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card unblocked successfully", content = @Content(schema = @Schema(implementation = CardView.class))),
                    @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
            }
    )
    @PostMapping(value = "/unblock", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardView> unblockCard(@RequestBody UnblockCardCommand command) {
        CardView cardView = this.cardService.unblockCard(command);

        return ResponseEntity.ok(cardView);
    }

    @Operation(summary = "Permanently close card", description = "Permanently close a card",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Close card command", required = true,
                    content = {@Content(schema = @Schema(implementation = CloseCardCommand.class))}
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card permanently closed successfully", content = @Content(schema = @Schema(implementation = CardView.class))),
                    @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
            }
    )
    @PostMapping(value = "/closure", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardView> closeCard(@RequestBody CloseCardCommand command) {
        CardView cardView = this.cardService.closeCard(command);

        return ResponseEntity.ok(cardView);
    }
}
