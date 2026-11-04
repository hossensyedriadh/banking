package io.github.hossensyedriadh.internet_banking.customer.repository;

import io.github.hossensyedriadh.internet_banking.customer.entity.CustomerIdentity;
import io.github.hossensyedriadh.internet_banking.customer.enumerator.CustomerIdentityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerIdentityRepository extends JpaRepository<CustomerIdentity, Long> {
    Optional<CustomerIdentity> findByIamSubject(String iamSubject);

    Optional<CustomerIdentity> findByIamSubjectAndStatus(String iamSubject, CustomerIdentityStatus status);
}
