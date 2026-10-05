package io.github.hossensyedriadh.cbs.customer.api.controller.v1;

import io.github.hossensyedriadh.cbs.customer.api.command.CreateCustomerCommand;
import io.github.hossensyedriadh.cbs.customer.api.command.UpdateCustomerCommand;
import io.github.hossensyedriadh.cbs.customer.api.model.CustomerView;
import io.github.hossensyedriadh.cbs.customer.api.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Customer API", description = "API endpoints for managing customers")
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

    @Operation(summary = "Get a list of customers", description = "Retrieve a list of customers with pagination",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CustomerView.class))),
            })
    @GetMapping
    public ResponseEntity<Page<CustomerView>> getCustomers(Pageable pageable) {
        Page<CustomerView> customers = this.customerService.getCustomers(pageable);

        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "Get a customer by ID", description = "Retrieve a customer by its unique identifier",
            parameters = {
                    @Parameter(name = "id", description = "The ID of the customer", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CustomerView.class))),
                    @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
            })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerView> getCustomer(@PathVariable("id") Long id) {
        Optional<CustomerView> customer = this.customerService.getCustomer(id);

        return customer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get a customer by customer number", description = "Retrieve a customer by its customer number",
            parameters = {
                    @Parameter(name = "customerNo", description = "The customer number of the customer", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CustomerView.class))),
                    @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
            })
    @GetMapping("/no/{customerNo}")
    public ResponseEntity<CustomerView> getCustomer(@PathVariable("customerNo") String customerNo) {
        Optional<CustomerView> customer = this.customerService.getCustomerByCustomerNo(customerNo);

        return customer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new customer", description = "Create a new customer with the provided information",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The customer information", required = true, content = @Content(schema = @Schema(implementation = CreateCustomerCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created customer", content = @Content(schema = @Schema(implementation = CustomerView.class)))
            })
    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CustomerView> createCustomer(@RequestBody CreateCustomerCommand command) {
        CustomerView customer = this.customerService.createCustomer(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/api/customers/v1/{id}")
                .buildAndExpand(customer.getId()).toUri();

        return ResponseEntity.created(location).body(customer);
    }

    @Operation(summary = "Update an existing customer", description = "Update an existing customer with the provided information",
            parameters = {
                    @Parameter(name = "id", description = "The ID of the customer", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The updated customer information", required = true, content = @Content(schema = @Schema(implementation = UpdateCustomerCommand.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CustomerView.class))),
                    @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
            })
    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CustomerView> updateCustomer(@PathVariable("id") Long id, @RequestBody UpdateCustomerCommand command) {
        CustomerView customer = this.customerService.updateCustomer(id, command);

        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "Close an existing customer", description = "Close an existing customer",
            parameters = {
                    @Parameter(name = "id", description = "The ID of the customer", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content"),
                    @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> closeCustomer(@PathVariable("id") Long id) {
        this.customerService.closeCustomer(id);

        return ResponseEntity.noContent().build();
    }
}
