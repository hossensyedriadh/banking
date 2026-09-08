package io.github.hossensyedriadh.cbs.account.internal.repository;

import io.github.hossensyedriadh.cbs.account.internal.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);

    @Query(value = "select * from account.accounts a inner join account.account_holders h on a.id = h.account_id where h.customer_id = :customer_id", nativeQuery = true)
    List<Account> findAllByCustomerId(@Param("customer_id") Long customerId);

    List<Account> findAllByIdIn(Collection<Long> ids);
}
