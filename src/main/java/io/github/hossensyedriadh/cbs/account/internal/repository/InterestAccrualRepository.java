package io.github.hossensyedriadh.cbs.account.internal.repository;

import io.github.hossensyedriadh.cbs.account.api.enumerator.InterestAccrualStatus;
import io.github.hossensyedriadh.cbs.account.internal.domain.InterestAccrual;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterestAccrualRepository extends JpaRepository<InterestAccrual, Long> {
    @Query("select a from InterestAccrual a where a.status = 'ACCRUED' and YEAR(a.calculationDate) = :calculationDateYear and MONTH(a.calculationDate) = :calculationMonth")
    List<InterestAccrual> findAllByStatusMonthAndYear(InterestAccrualStatus status, @Param("calculationMonth") int calculationMonth, @Param("calculationYear") int calculationDateYear);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InterestAccrual i where i.id = :id")
    Optional<InterestAccrual> findByIdForUpdate(@Param("id") Long id);

    boolean existsByAccountIdAndCalculationDate(Long accountId, LocalDate calculationDate);
}
