package io.github.hossensyedriadh.cbs.customer.internal.repository;

import io.github.hossensyedriadh.cbs.customer.api.enumerator.CustomerIdentityStatus;
import io.github.hossensyedriadh.cbs.customer.internal.domain.CustomerIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerIdentityRepository extends JpaRepository<CustomerIdentity, Long> {
    Optional<CustomerIdentity> findByIamSubjectAndStatus(String iamSubject, CustomerIdentityStatus status);
}
