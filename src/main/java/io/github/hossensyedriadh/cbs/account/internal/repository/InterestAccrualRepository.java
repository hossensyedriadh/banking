package io.github.hossensyedriadh.cbs.account.internal.repository;

import io.github.hossensyedriadh.cbs.account.api.enumerator.InterestAccrualStatus;
import io.github.hossensyedriadh.cbs.account.internal.domain.InterestAccrual;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Page<InterestAccrual> findAllByAccountIdOrderByCalculationDateDesc(Long accountId, Pageable pageable);

    List<InterestAccrual> findAllByAccountIdOrderByCalculationDateDesc(Long accountId);

    List<InterestAccrual> findAllByStatus(InterestAccrualStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InterestAccrual i where i.id = :id")
    Optional<InterestAccrual> findByIdForUpdate(@Param("id") Long id);

    Optional<InterestAccrual> findByAccountIdAndCalculationDate(Long accountId, LocalDate calculationDate);
}
