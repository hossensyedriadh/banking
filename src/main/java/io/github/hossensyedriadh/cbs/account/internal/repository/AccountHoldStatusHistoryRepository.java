package io.github.hossensyedriadh.cbs.account.internal.repository;

import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHoldStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountHoldStatusHistoryRepository extends JpaRepository<AccountHoldStatusHistory, Long> {
    List<AccountHoldStatusHistory> findAllByAccountHold_IdOrderByCreatedAtDescIdDesc(Long accountHoldId);

    Optional<AccountHoldStatusHistory> findFirstByAccountHold_IdOrderByCreatedAtDescIdDesc(Long accountHoldId);
}