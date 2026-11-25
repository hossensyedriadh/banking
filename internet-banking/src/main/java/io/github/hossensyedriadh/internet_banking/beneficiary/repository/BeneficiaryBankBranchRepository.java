package io.github.hossensyedriadh.internet_banking.beneficiary.repository;

import io.github.hossensyedriadh.internet_banking.beneficiary.entity.BeneficiaryBankBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BeneficiaryBankBranchRepository extends JpaRepository<BeneficiaryBankBranch, UUID> {
}
