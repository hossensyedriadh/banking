package io.github.hossensyedriadh.internet_banking.beneficiary.controller.v1;

import io.github.hossensyedriadh.internet_banking.beneficiary.model.BeneficiaryModel;
import io.github.hossensyedriadh.internet_banking.beneficiary.model.CreateBeneficiaryRequest;
import io.github.hossensyedriadh.internet_banking.beneficiary.model.UpdateBeneficiaryRequest;
import io.github.hossensyedriadh.internet_banking.beneficiary.service.BeneficiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/beneficiaries", produces = {MediaType.APPLICATION_JSON_VALUE})
public class BeneficiaryController {
    private final BeneficiaryService beneficiaryService;

    @Autowired
    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @GetMapping("/")
    public ResponseEntity<List<BeneficiaryModel>> beneficiaries() {
        List<BeneficiaryModel> beneficiaries = this.beneficiaryService.findAll();

        if (beneficiaries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(beneficiaries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BeneficiaryModel> beneficiary(@PathVariable("id") UUID id) {
        Optional<BeneficiaryModel> beneficiaryModel = this.beneficiaryService.findById(id);

        return beneficiaryModel.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BeneficiaryModel> create(@RequestBody CreateBeneficiaryRequest request) {
        BeneficiaryModel beneficiary = this.beneficiaryService.create(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(beneficiary.getId()).toUri();

        return ResponseEntity.created(location).body(beneficiary);
    }

    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BeneficiaryModel> update(@PathVariable("id") UUID id, @RequestBody UpdateBeneficiaryRequest request) {
        BeneficiaryModel updated = this.beneficiaryService.update(id, request);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable("id") UUID id) {
        this.beneficiaryService.deactivate(id);

        return ResponseEntity.noContent().build();
    }
}
