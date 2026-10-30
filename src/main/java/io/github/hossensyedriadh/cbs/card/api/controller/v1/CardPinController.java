package io.github.hossensyedriadh.cbs.card.api.controller.v1;

import io.github.hossensyedriadh.cbs.card.api.command.ChangeCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.command.VerifyCardPinCommand;
import io.github.hossensyedriadh.cbs.card.api.enumerator.CardPinVerificationResult;
import io.github.hossensyedriadh.cbs.card.api.service.CardPinService;
import io.github.hossensyedriadh.cbs.exception.GlobalErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/card-pins/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Card PIN API", description = "API endpoints for card pin operations")
public class CardPinController {
    private final CardPinService cardPinService;

    @Autowired
    public CardPinController(CardPinService cardPinService) {
        this.cardPinService = cardPinService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @Operation(summary = "Verify card PIN", description = "Verify the PIN of a card",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "PIN verification command",
                    required = true,
                    content = @Content(schema = @Schema(implementation = VerifyCardPinCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "PIN verification result",
                            content = @Content(schema = @Schema(implementation = CardPinVerificationResult.class))),
                    @ApiResponse(responseCode = "400", description = "PIN has not been set for card",
                            content = @Content(schema = @Schema(implementation = GlobalErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Card PIN is blocked",
                            content = @Content(schema = @Schema(implementation = GlobalErrorResponse.class)))
            })
    @PreAuthorize("hasAuthority(T(io.github.hossensyedriadh.cbs.util.OAuth2Scopes).CARD_PIN_VERIFY)")
    @PostMapping(value = "/verification", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardPinVerificationResult> verify(@RequestBody VerifyCardPinCommand command) {
        CardPinVerificationResult result = this.cardPinService.verifyPin(command);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Change card PIN", description = "Change the PIN of a card",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "PIN change command",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ChangeCardPinCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Card PIN changed successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
            }
    )
    @PostMapping(value = "/pin-change", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> changePin(@RequestBody ChangeCardPinCommand command) {
        this.cardPinService.changePin(command);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Unblock card PIN", description = "Unblock the PIN of a card",
            parameters = {
                    @Parameter(name = "cardId", description = "Card ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Card PIN unblocked successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
            }
    )
    @PostMapping("/pin-unblock/{cardId}")
    public ResponseEntity<Void> unblockPin(@PathVariable("cardId") Long cardId) {
        this.cardPinService.unblockPin(cardId);

        return ResponseEntity.noContent().build();
    }
}
