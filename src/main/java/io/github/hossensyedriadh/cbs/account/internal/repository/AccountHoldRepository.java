package io.github.hossensyedriadh.cbs.account.internal.repository;

import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHoldStatus;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AccountHoldRepository extends JpaRepository<AccountHold, Long> {
    List<AccountHold> findByAccountIdAndStatus(Long accountId, AccountHoldStatus status);

    @Query(value = "select coalesce(sum(h.amount), 0.0) from AccountHold h where h.accountId = :accountId and h.status = :status")
    BigDecimal sumAmountByAccountIdAndStatus(@Param("accountId") Long accountId, @Param("status") AccountHoldStatus status);
}
