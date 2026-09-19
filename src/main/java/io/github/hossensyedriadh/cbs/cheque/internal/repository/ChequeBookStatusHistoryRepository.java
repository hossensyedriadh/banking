package io.github.hossensyedriadh.cbs.cheque.internal.repository;

import io.github.hossensyedriadh.cbs.cheque.internal.domain.ChequeBookStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChequeBookStatusHistoryRepository extends JpaRepository<ChequeBookStatusHistory, Long> {
}
