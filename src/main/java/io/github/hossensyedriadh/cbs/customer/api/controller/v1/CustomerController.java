package io.github.hossensyedriadh.cbs.customer.api.controller.v1;

import io.github.hossensyedriadh.cbs.customer.api.command.CreateCustomerCommand;
import io.github.hossensyedriadh.cbs.customer.api.command.UpdateCustomerCommand;
import io.github.hossensyedriadh.cbs.customer.api.model.CustomerView;
import io.github.hossensyedriadh.cbs.customer.api.service.CustomerService;
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
import java.util.Optional;

@RestController
@RequestMapping(value = "/customers/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
public class CustomerController {
    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @InitBinder
    public void binder(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping
    public ResponseEntity<Page<CustomerView>> getCustomers(Pageable pageable) {
        Page<CustomerView> customers = this.customerService.getCustomers(pageable);

        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerView> getCustomer(@PathVariable("id") Long id) {
        Optional<CustomerView> customer = this.customerService.getCustomer(id);

        return customer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/no/{customerNo}")
    public ResponseEntity<CustomerView> getCustomer(@PathVariable("customerNo") String customerNo) {
        Optional<CustomerView> customer = this.customerService.getCustomerByCustomerNo(customerNo);

        return customer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CustomerView> createCustomer(@RequestBody CreateCustomerCommand command) {
        CustomerView customer = this.customerService.createCustomer(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/api/customers/v1/{id}")
                .buildAndExpand(customer.getId()).toUri();

        return ResponseEntity.created(location).body(customer);
    }

    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CustomerView> updateCustomer(@PathVariable("id") Long id, @RequestBody UpdateCustomerCommand command) {
        CustomerView customer = this.customerService.updateCustomer(id, command);

        return ResponseEntity.ok(customer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> closeCustomer(@PathVariable("id") Long id) {
        this.customerService.closeCustomer(id);

        return ResponseEntity.noContent().build();
    }
}
