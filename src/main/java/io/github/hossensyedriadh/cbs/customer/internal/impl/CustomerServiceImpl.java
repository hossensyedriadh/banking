package io.github.hossensyedriadh.cbs.customer.internal.impl;

import io.github.hossensyedriadh.cbs.customer.api.command.CreateCustomerCommand;
import io.github.hossensyedriadh.cbs.customer.api.command.UpdateCustomerCommand;
import io.github.hossensyedriadh.cbs.customer.api.enumerator.CustomerStatus;
import io.github.hossensyedriadh.cbs.customer.api.model.CustomerView;
import io.github.hossensyedriadh.cbs.customer.api.service.CustomerService;
import io.github.hossensyedriadh.cbs.customer.internal.domain.Customer;
import io.github.hossensyedriadh.cbs.customer.internal.mapper.CustomerMapper;
import io.github.hossensyedriadh.cbs.customer.internal.repository.CustomerRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper,
                               HttpServletRequest httpServletRequest) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public Page<CustomerView> getCustomers(Pageable pageable) {
        return this.customerRepository.findAll(pageable).map(this.customerMapper::toView);
    }

    @Override
    public Optional<CustomerView> getCustomer(Long id) {
        return this.customerRepository.findById(id).map(this.customerMapper::toView);
    }

    @Override
    public Optional<CustomerView> getCustomerByCustomerNo(String no) {
        return this.customerRepository.findByCustomerNumber(no).map(this.customerMapper::toView);
    }

    @Transactional
    @Override
    public CustomerView createCustomer(@Valid CreateCustomerCommand command) {
        Customer customer = this.customerMapper.toEntity(command);
        Customer saved = this.customerRepository.save(customer);

        log.info("Customer created with ID: {}", saved.getId());

        return this.customerMapper.toView(saved);
    }

    @Transactional
    @Override
    public CustomerView updateCustomer(Long id, @Valid UpdateCustomerCommand command) {
        Customer customer = this.customerRepository.findByIdForUpdate(id).orElseThrow(() -> {
            log.warn("No customer found with ID: {}", id);
            return new ApiResourceException("Customer not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (customer.getStatus().equals(CustomerStatus.INACTIVE)) {
            log.warn("Customer with ID: {} is inactive", id);
            throw new ApiResourceException("Customer is inactive", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        this.customerMapper.toPatchedEntity(command, customer);
        Customer updated = this.customerRepository.save(customer);

        log.info("Updated customer with ID: {}", id);

        return this.customerMapper.toView(updated);
    }

    @Transactional
    @Override
    public void closeCustomer(Long id) {
        this.customerRepository.findByIdForUpdate(id).ifPresentOrElse(customer -> {
            if (customer.getStatus().equals(CustomerStatus.INACTIVE)) {
                log.warn("Customer with ID: {} is already inactive", id);
                throw new ApiResourceException("Customer already inactive", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            customer.setStatus(CustomerStatus.INACTIVE);
            customer.setClosedAt(OffsetDateTime.now());

            this.customerRepository.save(customer);

            log.info("Customer closed with ID: {}", id);
        }, () -> {
            log.warn("No customer found with ID: {}", id);
            throw new ApiResourceException("Customer not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
    }
}
