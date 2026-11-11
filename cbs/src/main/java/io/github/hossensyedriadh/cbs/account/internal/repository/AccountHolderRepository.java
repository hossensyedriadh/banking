package io.github.hossensyedriadh.cbs.account.internal.repository;

import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHolder;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHolderId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountHolderRepository extends JpaRepository<AccountHolder, AccountHolderId> {
    List<AccountHolder> findAllByIdCustomerId(Long customerId);

    boolean existsByIdCustomerId(Long customerId);
}
