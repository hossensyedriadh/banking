package io.github.hossensyedriadh.cbs.customer.api.service;

import io.github.hossensyedriadh.cbs.customer.api.command.CreateCustomerCommand;
import io.github.hossensyedriadh.cbs.customer.api.command.UpdateCustomerCommand;
import io.github.hossensyedriadh.cbs.customer.api.model.CustomerView;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomerService {
    Page<CustomerView> getCustomers(Pageable pageable);

    Optional<CustomerView> getCustomer(Long id);

    Optional<CustomerView> getCustomerByCustomerNo(String no);

    CustomerView createCustomer(@Valid CreateCustomerCommand command);

    CustomerView updateCustomer(Long id, @Valid UpdateCustomerCommand command);

    void closeCustomer(Long id);
}
