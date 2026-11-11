package io.github.hossensyedriadh.internet_banking.beneficiary.repository;

import io.github.hossensyedriadh.internet_banking.beneficiary.entity.Beneficiary;
import io.github.hossensyedriadh.internet_banking.beneficiary.enumerator.BeneficiaryStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Beneficiary b WHERE b.id = :id")
    Optional<Beneficiary> findByIdForUpdate(@Param("id") UUID id);

    List<Beneficiary> findAllByStatus(BeneficiaryStatus status);

    Optional<Beneficiary> findByIdAndStatus(UUID id, BeneficiaryStatus status);
}
