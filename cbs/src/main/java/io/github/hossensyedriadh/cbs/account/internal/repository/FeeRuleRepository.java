package io.github.hossensyedriadh.cbs.account.internal.repository;

import io.github.hossensyedriadh.cbs.account.api.enumerator.FeeRuleStatus;
import io.github.hossensyedriadh.cbs.account.internal.domain.FeeRule;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeRuleRepository extends JpaRepository<FeeRule, Long> {
    Page<FeeRule> findAllByStatus(FeeRuleStatus status, Pageable pageable);

    List<FeeRule> findAllByStatus(FeeRuleStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FeeRule f where f.id = :id")
    Optional<FeeRule> findByIdForUpdate(Long id);

    Optional<FeeRule> findByFeeCode(String feeCode);

    Optional<FeeRule> findByFeeCodeAndStatus(String feeCode, FeeRuleStatus status);

    boolean existsByFeeCode(String feeCode);
}
