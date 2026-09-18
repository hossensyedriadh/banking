package io.github.hossensyedriadh.cbs.cheque.internal.repository;

import io.github.hossensyedriadh.cbs.cheque.internal.domain.ChequeBook;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChequeBookRepository extends JpaRepository<ChequeBook, Long> {
    List<ChequeBook> findAllByAccountId(Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cb from ChequeBook cb where cb.id = :id")
    Optional<ChequeBook> findByIdForUpdate(@Param("id") Long id);
}
