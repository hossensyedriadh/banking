package io.github.hossensyedriadh.cbs.account.api.controller.v1;

import io.github.hossensyedriadh.cbs.account.api.command.CreateFeeRuleCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.FeeRuleStatus;
import io.github.hossensyedriadh.cbs.account.api.model.FeeRuleView;
import io.github.hossensyedriadh.cbs.account.api.service.FeeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/fee-rules/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
public class FeeRuleController {
    private final FeeRuleService feeRuleService;

    @Autowired
    public FeeRuleController(FeeRuleService feeRuleService) {
        this.feeRuleService = feeRuleService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/with-status/{status}")
    public ResponseEntity<Page<FeeRuleView>> getRules(Pageable pageable, @PathVariable("status") FeeRuleStatus status) {
        Page<FeeRuleView> ruleViews = this.feeRuleService.getFeeRules(status, pageable);

        return ResponseEntity.ok(ruleViews);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<FeeRuleView>> getRules(@PathVariable("status") FeeRuleStatus status) {
        List<FeeRuleView> ruleViews = this.feeRuleService.getFeeRules(status);

        if (ruleViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(ruleViews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeeRuleView> getFeeRule(@PathVariable("id") Long id) {
        Optional<FeeRuleView> feeRuleView = this.feeRuleService.getFeeRule(id);

        return feeRuleView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/fee-code/{code}/status/{status}")
    public ResponseEntity<FeeRuleView> getFeeRule(@PathVariable("code") String feeCode, @PathVariable("status") FeeRuleStatus feeRuleStatus) {
        Optional<FeeRuleView> feeRuleView = this.feeRuleService.getFeeRule(feeCode, feeRuleStatus);

        return feeRuleView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<FeeRuleView> createFeeRule(@RequestBody CreateFeeRuleCommand command) {
        FeeRuleView saved = this.feeRuleService.createFeeRule(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<Void> activateFeeRule(@PathVariable("id") Long id) {
        this.feeRuleService.activateFeeRule(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateFeeRule(@PathVariable("id") Long id) {
        this.feeRuleService.deactivateFeeRule(id);

        return ResponseEntity.noContent().build();
    }
}
