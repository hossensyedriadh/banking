package io.github.hossensyedriadh.cbs.customer.api.service;

import io.github.hossensyedriadh.cbs.customer.api.command.CreateCustomerCommand;
import io.github.hossensyedriadh.cbs.customer.api.command.UpdateCustomerCommand;
import io.github.hossensyedriadh.cbs.customer.api.model.CustomerView;

import java.util.Optional;

public interface CustomerService {
    Optional<CustomerView> getCustomer(Long id);

    Optional<CustomerView> getCustomerByCustomerNo(String no);

    CustomerView createCustomer(CreateCustomerCommand command);

    Optional<CustomerView> updateCustomer(Long id, UpdateCustomerCommand command);

    void closeCustomer(Long id);
}
