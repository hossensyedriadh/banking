package io.github.hossensyedriadh.internet_banking.customer.repository;

import io.github.hossensyedriadh.internet_banking.customer.entity.CustomerIdentity;
import io.github.hossensyedriadh.internet_banking.customer.enumerator.CustomerIdentityStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerIdentityRepository extends JpaRepository<CustomerIdentity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ci FROM CustomerIdentity ci WHERE ci.id = :id")
    Optional<CustomerIdentity> findByIdForUpdate(@Param("id") Long id);

    Optional<CustomerIdentity> findByIamSubject(String iamSubject);

    Optional<CustomerIdentity> findByIamSubjectAndStatus(String iamSubject, CustomerIdentityStatus status);
}
