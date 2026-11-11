package io.github.hossensyedriadh.cbs.account.internal.repository;

import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHoldStatus;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHold;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountHoldRepository extends JpaRepository<AccountHold, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from AccountHold h where h.id = :id")
    Optional<AccountHold> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select h from AccountHold h
            join h.history sh
            where h.accountId = :accountId
              and sh.status = :status
              and sh.id = (
                  select max(sh2.id)
                  from AccountHoldStatusHistory sh2
                  where sh2.accountHold.id = h.id
              )""")
    List<AccountHold> findByAccountIdAndStatus(@Param("accountId") Long accountId, @Param("status") AccountHoldStatus status);

    @Query("""
            select coalesce(sum(h.amount), 0.0)
            from AccountHold h join h.history sh
            where h.accountId = :accountId
              and sh.status = :status
              and sh.id = (
                  select max(sh2.id)
                  from AccountHoldStatusHistory sh2
                  where sh2.accountHold.id = h.id
              )""")
    BigDecimal sumAmountByAccountIdAndStatus(@Param("accountId") Long accountId, @Param("status") AccountHoldStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select h from AccountHold h
            join h.history sh
            where sh.status = :status
              and sh.id = (
                  select max(sh2.id)
                  from AccountHoldStatusHistory sh2
                  where sh2.accountHold.id = h.id
              )
              and h.expiresAt <= :expiresAtBefore
            order by h.expiresAt asc, h.id asc""")
    List<AccountHold> findAllByStatusAndExpiresAtBefore(@Param("status") AccountHoldStatus status, @Param("expiresAtBefore") OffsetDateTime expiresAtBefore);

    @Query("""
            select case when count(h) > 0 then true else false end
            from AccountHold h join h.history sh
            where h.accountId = :accountId
              and sh.status = :status
              and sh.id = (
                  select max(sh2.id)
                  from AccountHoldStatusHistory sh2
                  where sh2.accountHold.id = h.id
              )""")
    boolean existsByAccountIdAndStatus(@Param("accountId") Long accountId, @Param("status") AccountHoldStatus status);
}
