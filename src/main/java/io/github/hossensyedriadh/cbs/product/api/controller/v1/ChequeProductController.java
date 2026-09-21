package io.github.hossensyedriadh.cbs.product.api.controller.v1;

import io.github.hossensyedriadh.cbs.product.api.command.CreateChequeProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateChequeProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.ChequeProductView;
import io.github.hossensyedriadh.cbs.product.api.service.ChequeProductService;
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
@RequestMapping(value = "/cheque-products/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ChequeProductController {
    private final ChequeProductService chequeProductService;

    @Autowired
    public ChequeProductController(ChequeProductService chequeProductService) {
        this.chequeProductService = chequeProductService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/")
    public ResponseEntity<List<ChequeProductView>> getChequeProducts() {
        List<ChequeProductView> chequeProductViews = this.chequeProductService.getChequeProducts();

        if (chequeProductViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(chequeProductViews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChequeProductView> getChequeProductById(@PathVariable("id") Long id) {
        Optional<ChequeProductView> chequeProductView = this.chequeProductService.getChequeProduct(id);

        return chequeProductView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ChequeProductView> getChequeProductByCode(@PathVariable("code") String code) {
        Optional<ChequeProductView> chequeProductView = this.chequeProductService.getChequeProductByCode(code);

        return chequeProductView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ChequeProductView> createChequeProduct(@RequestBody CreateChequeProductCommand command) {
        ChequeProductView saved = this.chequeProductService.createProduct(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ChequeProductView> updateChequeProduct(@PathVariable("id") Long id, @RequestBody UpdateChequeProductCommand command) {
        ChequeProductView updated = this.chequeProductService.updateProduct(id, command);

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<Void> activateProduct(@PathVariable("id") Long id) {
        this.chequeProductService.activateProduct(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateProduct(@PathVariable("id") Long id) {
        this.chequeProductService.deactivateProduct(id);

        return ResponseEntity.noContent().build();
    }
}
