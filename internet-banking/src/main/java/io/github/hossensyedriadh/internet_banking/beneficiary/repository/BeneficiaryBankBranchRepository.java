package io.github.hossensyedriadh.internet_banking.beneficiary.repository;

import io.github.hossensyedriadh.internet_banking.beneficiary.entity.BeneficiaryBankBranch;
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
public interface BeneficiaryBankBranchRepository extends JpaRepository<BeneficiaryBankBranch, UUID> {
    List<BeneficiaryBankBranch> findAllByBankId(UUID bankId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BeneficiaryBankBranch b WHERE b.id = :id")
    Optional<BeneficiaryBankBranch> findByIdForUpdate(@Param("id") UUID id);
}
