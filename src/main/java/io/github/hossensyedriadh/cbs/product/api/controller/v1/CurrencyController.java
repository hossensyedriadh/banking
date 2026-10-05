package io.github.hossensyedriadh.cbs.product.api.controller.v1;

import io.github.hossensyedriadh.cbs.product.api.command.CreateCurrencyCommand;
import io.github.hossensyedriadh.cbs.product.api.model.CurrencyView;
import io.github.hossensyedriadh.cbs.product.api.service.CurrencyService;
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
@RequestMapping(value = "/currencies/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Currency API", description = "API endpoints for managing currencies")
public class CurrencyController {
    private final CurrencyService currencyService;

    @Autowired
    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @Operation(summary = "Get all currencies", description = "Retrieve a list of all currencies",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CurrencyView.class)))),
                    @ApiResponse(responseCode = "204", description = "No content", content = @Content)
            })
    @GetMapping("/")
    public ResponseEntity<List<CurrencyView>> getCurrencies() {
        List<CurrencyView> currencyViews = this.currencyService.getCurrencies();

        if (currencyViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(currencyViews);
    }

    @Operation(summary = "Get currency by ID", description = "Retrieve a currency by its ID",
            parameters = {
                    @Parameter(name = "id", description = "Currency ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CurrencyView.class))),
                    @ApiResponse(responseCode = "404", description = "Currency not found", content = @Content)
            })
    @GetMapping("/{id}")
    public ResponseEntity<CurrencyView> getCurrencyById(@PathVariable("id") Long id) {
        Optional<CurrencyView> currencyView = this.currencyService.getCurrency(id);

        return currencyView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get currency by code", description = "Retrieve a currency by its code",
            parameters = {
                    @Parameter(name = "code", description = "Currency code", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CurrencyView.class))),
                    @ApiResponse(responseCode = "404", description = "Currency not found", content = @Content)
            })
    @GetMapping("/code/{code}")
    public ResponseEntity<CurrencyView> getCurrencyByCode(@PathVariable("code") String code) {
        Optional<CurrencyView> currencyView = this.currencyService.getCurrencyByCode(code);

        return currencyView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create currency", description = "Create a new currency",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Command to create currency", required = true, content = @Content(schema = @Schema(implementation = CreateCurrencyCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = CurrencyView.class)))
            })
    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CurrencyView> createCurrency(@RequestBody CreateCurrencyCommand command) {
        CurrencyView saved = this.currencyService.createCurrency(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @Operation(summary = "Activate currency", description = "Activate a currency",
            parameters = {
                    @Parameter(name = "id", description = "Currency ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "No content"),
                    @ApiResponse(responseCode = "404", description = "Currency not found", content = @Content)
            })
    @PostMapping("/activation/{id}")
    public ResponseEntity<Void> activateCurrency(@PathVariable("id") Long id) {
        this.currencyService.activateCurrency(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deactivate currency", description = "Deactivate a currency",
            parameters = {
                    @Parameter(name = "id", description = "Currency ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "No content"),
                    @ApiResponse(responseCode = "404", description = "Currency not found", content = @Content)
            })
    @PostMapping("/deactivation/{id}")
    public ResponseEntity<Void> deactivateCurrency(@PathVariable("id") Long id) {
        this.currencyService.deactivateCurrency(id);

        return ResponseEntity.noContent().build();
    }
}
