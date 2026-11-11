package io.github.hossensyedriadh.cbs.card.internal.domain;

import io.github.hossensyedriadh.cbs.card.api.enumerator.CardStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Getter
@Setter
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "card_status_history", schema = "card")
public class CardStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "card_id", updatable = false, nullable = false)
    private Long cardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", updatable = false, nullable = false, length = 20)
    private CardStatus status;

    @Column(name = "reason", updatable = false)
    private String reason;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}