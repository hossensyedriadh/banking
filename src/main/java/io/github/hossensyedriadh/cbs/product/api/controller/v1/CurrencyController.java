package io.github.hossensyedriadh.cbs.product.api.controller.v1;

import io.github.hossensyedriadh.cbs.product.api.command.CreateCurrencyCommand;
import io.github.hossensyedriadh.cbs.product.api.model.CurrencyView;
import io.github.hossensyedriadh.cbs.product.api.service.CurrencyService;
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

    @GetMapping("/")
    public ResponseEntity<List<CurrencyView>> getCurrencies() {
        List<CurrencyView> currencyViews = this.currencyService.getCurrencies();

        if (currencyViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(currencyViews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CurrencyView> getCurrencyById(@PathVariable("id") Long id) {
        Optional<CurrencyView> currencyView = this.currencyService.getCurrency(id);

        return currencyView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CurrencyView> getCurrencyByCode(@PathVariable("code") String code) {
        Optional<CurrencyView> currencyView = this.currencyService.getCurrencyByCode(code);

        return currencyView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CurrencyView> createCurrency(@RequestBody CreateCurrencyCommand command) {
        CurrencyView saved = this.currencyService.createCurrency(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<Void> activateCurrency(@PathVariable("id") Long id) {
        this.currencyService.activateCurrency(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateCurrency(@PathVariable("id") Long id) {
        this.currencyService.deactivateCurrency(id);

        return ResponseEntity.noContent().build();
    }
}
