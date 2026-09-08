package io.github.hossensyedriadh.cbs.customer.internal.repository;

import io.github.hossensyedriadh.cbs.customer.internal.domain.Customer;
import io.github.hossensyedriadh.cbs.customer.api.enumerator.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByIdAndStatus(Long id, CustomerStatus status);

    Optional<Customer> findByCustomerNumber(String customerNumber);
}
