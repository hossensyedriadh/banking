package io.github.hossensyedriadh.cbs.account.internal.repository;

import io.github.hossensyedriadh.cbs.account.internal.domain.AccountRestrictionStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRestrictionStatusHistoryRepository extends JpaRepository<AccountRestrictionStatusHistory, Long> {
    List<AccountRestrictionStatusHistory> findAllByAccountRestriction_IdOrderByCreatedAtDescIdDesc(Long accountRestrictionId);

    Optional<AccountRestrictionStatusHistory> findFirstByAccountRestriction_IdOrderByCreatedAtDescIdDesc(Long accountRestrictionId);
}
