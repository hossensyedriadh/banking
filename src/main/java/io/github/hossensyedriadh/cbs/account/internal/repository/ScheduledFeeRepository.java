package io.github.hossensyedriadh.cbs.account.internal.repository;

import io.github.hossensyedriadh.cbs.account.api.enumerator.ScheduledFeeStatus;
import io.github.hossensyedriadh.cbs.account.internal.domain.ScheduledFee;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledFeeRepository extends JpaRepository<ScheduledFee, Long> {
    Page<ScheduledFee> findAllByStatus(ScheduledFeeStatus status, Pageable pageable);

    Page<ScheduledFee> findAllByAccountIdAndStatus(Long accountId, ScheduledFeeStatus status, Pageable pageable);

    Page<ScheduledFee> findAllByAccountId(Long accountId, Pageable pageable);

    Optional<ScheduledFee> findByIdAndAccountId(Long id, Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select sf from ScheduledFee sf where sf.id = :id")
    Optional<ScheduledFee> findByIdForUpdate(@Param("id") Long id);

    List<ScheduledFee> findAllByScheduledDateLessThanEqualAndStatusInOrderByScheduledDateAscIdAsc(LocalDate businessDate, Collection<ScheduledFeeStatus> statuses);

    @Modifying
    @Query("""
            UPDATE ScheduledFee sf
            SET sf.status = :processing,
                sf.processingStartedAt = :claimedAt,
                sf.attemptCount = sf.attemptCount + 1
            WHERE sf.id = :id
              AND sf.status IN :eligibleStatuses""")
    int claimScheduledFee(@Param("id") Long id, @Param("processing") ScheduledFeeStatus processing, @Param("claimedAt") OffsetDateTime claimedAt,
                          @Param("eligibleStatuses") Collection<ScheduledFeeStatus> eligibleStatuses);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("""
            UPDATE ScheduledFee sf
            SET sf.status = :failed,
                sf.processingStartedAt = NULL,
                sf.failureReason = :reason
            WHERE sf.status = :processing
              AND sf.processingStartedAt < :cutoff""")
    int recoverStaleProcessingFees(@Param("processing") ScheduledFeeStatus processing, @Param("failed") ScheduledFeeStatus failed,
                                   @Param("cutoff") OffsetDateTime cutoff, @Param("reason") String reason);

    boolean existsByFeeRuleIdAndAccountIdAndScheduledDateAndCardIdIsNull(Long feeRuleId, Long accountId, LocalDate scheduledDate);

    boolean existsByFeeRuleIdAndCardIdAndScheduledDate(Long feeRuleId, Long cardId, LocalDate scheduledDate);
}
