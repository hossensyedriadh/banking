package io.github.hossensyedriadh.internet_banking.beneficiary.repository;

import io.github.hossensyedriadh.internet_banking.beneficiary.entity.Beneficiary;
import io.github.hossensyedriadh.internet_banking.beneficiary.enumerator.BeneficiaryStatus;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
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
    @EntityGraph(attributePaths = {"bankBranch"})
    @Query("SELECT b FROM Beneficiary b WHERE b.id = :id and b.customerIdentityId = :customerIdentityId")
    Optional<Beneficiary> findByIdAndCustomerIdentityForUpdate(@Param("id") UUID id, @Param("customerIdentityId") Long customerIdentityId);

    @EntityGraph(attributePaths = {"bankBranch"})
    @Override
    @NonNull
    Optional<Beneficiary> findById(@NonNull UUID id);

    @EntityGraph(attributePaths = {"bankBranch"})
    List<Beneficiary> findAllByCustomerIdentityId(Long customerIdentityId);

    @EntityGraph(attributePaths = {"bankBranch"})
    List<Beneficiary> findAllByStatusAndCustomerIdentityId(BeneficiaryStatus status, Long customerIdentityId);

    @EntityGraph(attributePaths = {"bankBranch"})
    Optional<Beneficiary> findByIdAndCustomerIdentityId(UUID id, Long customerIdentityId);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM Beneficiary b
            WHERE b.customerIdentityId = :customerIdentityId
              AND b.accountNumber = :beneficiaryAccountNumber
              AND b.bankBranch.id = :branchId
            """)
    boolean beneficiaryExists(@Param("customerIdentityId") Long customerIdentityId, @Param("beneficiaryAccountNumber") String beneficiaryAccountNumber, @Param("branchId") UUID branchId);
}
