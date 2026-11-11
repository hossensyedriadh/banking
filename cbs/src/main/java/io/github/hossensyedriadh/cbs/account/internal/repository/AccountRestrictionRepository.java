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

    @Query("""
            select r from AccountRestriction r
            join r.history rh
            where r.accountId = :accountId
              and rh.status = :status
              and rh.id = (
                  select max(rh2.id)
                  from AccountRestrictionStatusHistory rh2
                  where rh2.accountRestriction.id = r.id
              )""")
    List<AccountRestriction> findByAccountIdAndStatus(@Param("accountId") Long accountId, @Param("status") AccountRestrictionStatus status);

    @Query("""
            select case when count(r) > 0 then true else false end
            from AccountRestriction r join r.history rh
            where r.accountId = :accountId
              and r.restrictionType = :type
              and rh.status = :status
              and rh.id = (
                  select max(rh2.id)
                  from AccountRestrictionStatusHistory rh2
                  where rh2.accountRestriction.id = r.id
              )""")
    boolean existsByAccountIdAndRestrictionTypeAndStatus(@Param("accountId") Long accountId, @Param("type") AccountRestrictionType type, @Param("status") AccountRestrictionStatus status);

    @Query("""
            select distinct r.restrictionType from AccountRestriction r
            join r.history rh
            where r.accountId = :accountId
              and rh.status = 'ACTIVE'
              and rh.id = (
                  select max(rh2.id)
                  from AccountRestrictionStatusHistory rh2
                  where rh2.accountRestriction.id = r.id
              )""")
    List<AccountRestrictionType> findActiveRestrictionTypes(@Param("accountId") Long accountId);

    @Query("""
            select case when count(r) > 0 then true else false end
            from AccountRestriction r join r.history rh
            where r.accountId = :accountId
              and rh.status = :status
              and rh.id = (
                  select max(rh2.id)
                  from AccountRestrictionStatusHistory rh2
                  where rh2.accountRestriction.id = r.id
              )""")
    boolean existsByAccountIdAndStatus(@Param("accountId") Long accountId, @Param("status") AccountRestrictionStatus status);
}
