package io.github.hossensyedriadh.internet_banking.customer.controller.v1;

import io.github.hossensyedriadh.internet_banking.customer.model.CustomerIdentityModel;
import io.github.hossensyedriadh.internet_banking.customer.model.PrincipalIdentityModel;
import io.github.hossensyedriadh.internet_banking.customer.service.CustomerIdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/customers", produces = {MediaType.APPLICATION_JSON_VALUE})
public class CustomerIdentityController {
    private final CustomerIdentityService customerIdentityService;

    @Autowired
    public CustomerIdentityController(CustomerIdentityService customerIdentityService) {
        this.customerIdentityService = customerIdentityService;
    }

    @GetMapping("/me")
    public ResponseEntity<PrincipalIdentityModel> me() {
        PrincipalIdentityModel model = this.customerIdentityService.getPrincipal();

        return ResponseEntity.ok(model);
    }

    @GetMapping("/{sub}")
    public ResponseEntity<CustomerIdentityModel> customerBySubject(@PathVariable("sub") String subject) {
        Optional<CustomerIdentityModel> model = this.customerIdentityService.getByIamSubject(subject);

        return model.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active/{sub}")
    public ResponseEntity<CustomerIdentityModel> activeCustomerBySubject(@PathVariable("sub") String subject) {
        Optional<CustomerIdentityModel> model = this.customerIdentityService.getActiveByIamSubject(subject);

        return model.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/block/{id}")
    public ResponseEntity<Void> blockIdentity(@PathVariable("id") Long id) {
        this.customerIdentityService.block(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/unblock/{id}")
    public ResponseEntity<Void> unblockIdentity(@PathVariable("id") Long id) {
        this.customerIdentityService.unblock(id);

        return ResponseEntity.noContent().build();
    }
}
