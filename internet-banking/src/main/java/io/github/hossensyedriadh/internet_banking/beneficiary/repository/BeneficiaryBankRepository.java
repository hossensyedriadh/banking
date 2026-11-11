package io.github.hossensyedriadh.internet_banking.beneficiary.repository;

import io.github.hossensyedriadh.internet_banking.beneficiary.entity.BeneficiaryBank;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeneficiaryBankRepository extends JpaRepository<BeneficiaryBank, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BeneficiaryBank b WHERE b.id = :id")
    Optional<BeneficiaryBank> findByIdForUpdate(@Param("id") UUID id);
}
