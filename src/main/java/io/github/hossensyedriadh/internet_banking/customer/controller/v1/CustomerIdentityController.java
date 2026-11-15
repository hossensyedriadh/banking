package io.github.hossensyedriadh.internet_banking.customer.controller.v1;

import io.github.hossensyedriadh.internet_banking.customer.model.PrincipalIdentityModel;
import io.github.hossensyedriadh.internet_banking.customer.service.CustomerIdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
