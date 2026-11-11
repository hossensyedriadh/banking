package io.github.hossensyedriadh.cbs.card.internal.repository;

import io.github.hossensyedriadh.cbs.card.internal.domain.CardStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardStatusHistoryRepository extends JpaRepository<CardStatusHistory, Long> {
}
