package io.github.hossensyedriadh.cbs.product.api.controller.v1;

import io.github.hossensyedriadh.cbs.product.api.command.CreateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.AccountProductView;
import io.github.hossensyedriadh.cbs.product.api.service.AccountProductService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/account-products/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Account Product API", description = "API endpoints for managing account products")
public class AccountProductController {
    private final AccountProductService accountProductService;

    @Autowired
    public AccountProductController(AccountProductService accountProductService) {
        this.accountProductService = accountProductService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @Operation(summary = "Get account products", description = "Retrieve a list of account products",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountProductView.class)))),
                    @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
            })
    @GetMapping("/")
    public ResponseEntity<List<AccountProductView>> getAccountProducts() {
        List<AccountProductView> accountProductViews = this.accountProductService.getAccountProducts();

        if (accountProductViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(accountProductViews);
    }

    @Operation(summary = "Get account product by ID", description = "Retrieve an account product by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountProductView.class))),
                    @ApiResponse(responseCode = "404", description = "Account product not found", content = @Content)
            })
    @PreAuthorize("hasAuthority(T(io.github.hossensyedriadh.cbs.util.OAuth2Scopes).ACCOUNT_PRODUCT_READ)")
    @GetMapping("/{id}")
    public ResponseEntity<AccountProductView> getAccountProductById(@PathVariable("id") Long id) {
        Optional<AccountProductView> accountProductView = this.accountProductService.getProduct(id);

        return accountProductView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get account product by code", description = "Retrieve an account product by its code",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountProductView.class))),
                    @ApiResponse(responseCode = "404", description = "Account product not found", content = @Content)
            })
    @PreAuthorize("hasAuthority(T(io.github.hossensyedriadh.cbs.util.OAuth2Scopes).ACCOUNT_PRODUCT_READ)")
    @GetMapping("/code/{code}")
    public ResponseEntity<AccountProductView> getAccountProductByCode(@PathVariable("code") String code) {
        Optional<AccountProductView> accountProductView = this.accountProductService.getProductByCode(code);

        return accountProductView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create account product", description = "Create a new account product",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Command to create a new account product", required = true,
                    content = @Content(schema = @Schema(implementation = CreateAccountProductCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created product successfully", content = @Content(schema = @Schema(implementation = AccountProductView.class)))
            })
    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountProductView> createAccountProduct(@RequestBody CreateAccountProductCommand command) {
        AccountProductView saved = this.accountProductService.createProduct(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @Operation(summary = "Update account product", description = "Update an existing account product",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Command to update an existing account product", required = true,
                    content = @Content(schema = @Schema(implementation = UpdateAccountProductCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated product successfully", content = @Content(schema = @Schema(implementation = AccountProductView.class))),
                    @ApiResponse(responseCode = "404", description = "Account product not found", content = @Content)
            })
    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountProductView> updateAccountProduct(@PathVariable("id") Long id, @RequestBody UpdateAccountProductCommand command) {
        AccountProductView updated = this.accountProductService.updateProduct(id, command);

        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Activate account product", description = "Activate an existing account product",
            parameters = {
                    @Parameter(name = "id", description = "Account product ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Product activated successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Account product not found", content = @Content)
            })
    @PostMapping("/activation/{id}")
    public ResponseEntity<Void> activateProduct(@PathVariable("id") Long id) {
        this.accountProductService.activateProduct(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deactivate account product", description = "Deactivate an existing account product",
            parameters = {
                    @Parameter(name = "id", description = "Account product ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Product deactivated successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Account product not found", content = @Content)
            })
    @PostMapping("/deactivation/{id}")
    public ResponseEntity<Void> deactivateProduct(@PathVariable("id") Long id) {
        this.accountProductService.deactivateProduct(id);

        return ResponseEntity.noContent().build();
    }
}
