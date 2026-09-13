package io.github.hossensyedriadh.cbs.account.internal.repository;

import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionStatus;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionType;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountRestriction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRestrictionRepository extends JpaRepository<AccountRestriction, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from AccountRestriction r where r.id = :id")
    Optional<AccountRestriction> findByIdForUpdate(@Param("id") Long id);

    List<AccountRestriction> findByAccountIdAndStatus(Long accountId, AccountRestrictionStatus status);

    boolean existsByAccountIdAndRestrictionTypeAndStatus(Long accountId, AccountRestrictionType type, AccountRestrictionStatus status);

    @Query("select r.restrictionType from AccountRestriction r where r.accountId = :accountId and r.status = :status")
    List<AccountRestrictionType> findActiveRestrictionTypes(@Param("accountId") Long accountId, @Param("status") AccountRestrictionStatus status);

    boolean existsByAccountIdAndStatus(Long accountId, AccountRestrictionStatus status);
}
