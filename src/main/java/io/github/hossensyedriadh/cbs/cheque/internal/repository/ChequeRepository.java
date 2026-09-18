package io.github.hossensyedriadh.cbs.cheque.internal.repository;

import io.github.hossensyedriadh.cbs.cheque.internal.domain.Cheque;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChequeRepository extends JpaRepository<Cheque, Long> {
    List<Cheque> findAllByChequeBook_Id(Long chequeBookId);

    Optional<Cheque> findByChequeNumber(Long chequeNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cheque c where c.id = :id")
    Optional<Cheque> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cheque c where c.chequeNumber = :chequeNumber")
    Optional<Cheque> findByChequeNumberForUpdate(@Param("chequeNumber") Long chequeNumber);
}
