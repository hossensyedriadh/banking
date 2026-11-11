package io.github.hossensyedriadh.cbs.card.internal.repository;

import io.github.hossensyedriadh.cbs.card.internal.domain.CardPin;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardPinRepository extends JpaRepository<CardPin, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from CardPin p where p.cardId = :cardId")
    Optional<CardPin> findByCardIdForUpdate(@Param("cardId") Long cardId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from CardPin p where p.id = :id")
    Optional<CardPin> findByIdForUpdate(@Param("id") Long id);

    Optional<CardPin> findByCardId(Long cardId);

    boolean existsByCardId(Long cardId);
}
