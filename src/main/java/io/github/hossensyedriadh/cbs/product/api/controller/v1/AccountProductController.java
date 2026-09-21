package io.github.hossensyedriadh.cbs.product.api.controller.v1;

import io.github.hossensyedriadh.cbs.product.api.command.CreateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.AccountProductView;
import io.github.hossensyedriadh.cbs.product.api.service.AccountProductService;
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
@RequestMapping(value = "/account-products/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
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

    @GetMapping("/")
    public ResponseEntity<List<AccountProductView>> getAccountProducts() {
        List<AccountProductView> accountProductViews = this.accountProductService.getAccountProducts();

        if (accountProductViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(accountProductViews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountProductView> getAccountProductById(@PathVariable("id") Long id) {
        Optional<AccountProductView> accountProductView = this.accountProductService.getProduct(id);

        return accountProductView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<AccountProductView> getAccountProductByCode(@PathVariable("code") String code) {
        Optional<AccountProductView> accountProductView = this.accountProductService.getProductByCode(code);

        return accountProductView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountProductView> createAccountProduct(@RequestBody CreateAccountProductCommand command) {
        AccountProductView saved = this.accountProductService.createProduct(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<AccountProductView> updateAccountProduct(@PathVariable("id") Long id, @RequestBody UpdateAccountProductCommand command) {
        AccountProductView updated = this.accountProductService.updateProduct(id, command);

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<Void> activateProduct(@PathVariable("id") Long id) {
        this.accountProductService.activateProduct(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateProduct(@PathVariable("id") Long id) {
        this.accountProductService.deactivateProduct(id);

        return ResponseEntity.noContent().build();
    }
}
