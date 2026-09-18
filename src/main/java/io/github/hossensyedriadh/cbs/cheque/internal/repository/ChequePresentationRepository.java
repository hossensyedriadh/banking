package io.github.hossensyedriadh.cbs.cheque.internal.repository;

import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequePresentationStatus;
import io.github.hossensyedriadh.cbs.cheque.internal.domain.ChequePresentation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChequePresentationRepository extends JpaRepository<ChequePresentation, Long> {
    List<ChequePresentation> findAllByChequeIdOrderByPresentedAtDesc(Long chequeId);

    List<ChequePresentation> findByStatus(ChequePresentationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cp FROM ChequePresentation cp WHERE cp.id = :id")
    Optional<ChequePresentation> findByIdForUpdate(@Param("id") Long id);
}
