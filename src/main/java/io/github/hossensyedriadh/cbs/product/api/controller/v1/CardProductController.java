package io.github.hossensyedriadh.cbs.product.api.controller.v1;

import io.github.hossensyedriadh.cbs.product.api.command.CreateCardProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateCardProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.CardProductView;
import io.github.hossensyedriadh.cbs.product.api.service.CardProductService;
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

    @GetMapping("/")
    public ResponseEntity<List<CardProductView>> getCardProducts() {
        List<CardProductView> cardProductViews = this.cardProductService.getCardProducts();

        if (cardProductViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(cardProductViews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardProductView> getCardProductById(@PathVariable("id") Long id) {
        Optional<CardProductView> cardProductView = this.cardProductService.getCardProduct(id);

        return cardProductView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CardProductView> getCardProductByCode(@PathVariable("code") String code) {
        Optional<CardProductView> cardProductView = this.cardProductService.getCardProductByCode(code);

        return cardProductView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardProductView> createCardProduct(@RequestBody CreateCardProductCommand command) {
        CardProductView saved = this.cardProductService.createProduct(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CardProductView> updateCardProduct(@PathVariable("id") Long id, @RequestBody UpdateCardProductCommand command) {
        CardProductView updated = this.cardProductService.updateProduct(id, command);

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<Void> activateProduct(@PathVariable("id") Long id) {
        this.cardProductService.activateProduct(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateProduct(@PathVariable("id") Long id) {
        this.cardProductService.deactivateProduct(id);

        return ResponseEntity.noContent().build();
    }
}
