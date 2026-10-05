package io.github.hossensyedriadh.cbs.product.api.controller.v1;

import io.github.hossensyedriadh.cbs.product.api.command.CreateCardProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateCardProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.CardProductView;
import io.github.hossensyedriadh.cbs.product.api.service.CardProductService;
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
@RequestMapping(value = "/card-products/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Card Product API", description = "API endpoints for managing card products")
public class CardProductController {
    private final CardProductService cardProductService;

    @Autowired
    public CardProductController(CardProductService cardProductService) {
        this.cardProductService = cardProductService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @Operation(summary = "Get all card products", description = "Get all existing card products",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CardProductView.class)))),
                    @ApiResponse(responseCode = "204", description = "No card products found", content = @Content)
            })
    @GetMapping("/")
    public ResponseEntity<List<CardProductView>> getCardProducts() {
        List<CardProductView> cardProductViews = this.cardProductService.getCardProducts();

        if (cardProductViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(cardProductViews);
    }

    @Operation(summary = "Get card product by ID", description = "Get a card product by its ID",
            parameters = {
                    @Parameter(name = "id", description = "ID of the card product to retrieve", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CardProductView.class))),
                    @ApiResponse(responseCode = "404", description = "Card product not found", content = @Content)
            })
    @GetMapping("/{id}")
    public ResponseEntity<CardProductView> getCardProductById(@PathVariable("id") Long id) {
        Optional<CardProductView> cardProductView = this.cardProductService.getCardProduct(id);

        return cardProductView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get card product by code", description = "Get a card product by its code",
            parameters = {
                    @Parameter(name = "code", description = "Code of the card product to retrieve", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CardProductView.class))),
                    @ApiResponse(responseCode = "404", description = "Card product not found", content = @Content)
            })
    @GetMapping("/code/{code}")
    public ResponseEntity<CardProductView> getCardProductByCode(@PathVariable("code") String code) {
        Optional<CardProductView> cardProductView = this.cardProductService.getCardProductByCode(code);

        return cardProductView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new card product", description = "Create a new card product",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Command to create a new card product",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateCardProductCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = CardProductView.class)))
            })
    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardProductView> createCardProduct(@RequestBody CreateCardProductCommand command) {
        CardProductView saved = this.cardProductService.createProduct(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @Operation(summary = "Update an existing card product", description = "Update an existing card product",
            parameters = {
                    @Parameter(name = "id", description = "ID of the card product to update", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Command object containing the details of the card product to update", required = true,
                    content = @Content(schema = @Schema(implementation = UpdateCardProductCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CardProductView.class))),
                    @ApiResponse(responseCode = "404", description = "Card product not found", content = @Content)
            })
    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardProductView> updateCardProduct(@PathVariable("id") Long id, @RequestBody UpdateCardProductCommand command) {
        CardProductView updated = this.cardProductService.updateProduct(id, command);

        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Activate a card product", description = "Activate a card product by its ID",
            parameters = {
                    @Parameter(name = "id", description = "The ID of the card product to activate", required = true)
            }, responses = {
            @ApiResponse(responseCode = "204", description = "No content", content = @Content),
            @ApiResponse(responseCode = "404", description = "Card product not found", content = @Content)
    })
    @PostMapping("/activation/{id}")
    public ResponseEntity<Void> activateProduct(@PathVariable("id") Long id) {
        this.cardProductService.activateProduct(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deactivate a card product", description = "Deactivate a card product by its ID",
            parameters = {
                    @Parameter(name = "id", description = "The ID of the card product to deactivate", required = true)
            }, responses = {
            @ApiResponse(responseCode = "204", description = "No content", content = @Content),
            @ApiResponse(responseCode = "404", description = "Card product not found", content = @Content)
    })
    @PostMapping("/deactivation/{id}")
    public ResponseEntity<Void> deactivateProduct(@PathVariable("id") Long id) {
        this.cardProductService.deactivateProduct(id);

        return ResponseEntity.noContent().build();
    }
}
