package io.github.hossensyedriadh.cbs.card.internal.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "card_pins", schema = "card", indexes = {@Index(name = "idx_card_pins_blocked",
        columnList = "blocked")}, uniqueConstraints = {@UniqueConstraint(name = "uk_card_pins_card_id",
        columnNames = {"card_id"})})
public class CardPin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "card_id", updatable = false, nullable = false)
    private Long cardId;

    @Column(name = "pin_verification_data", nullable = false, length = 128)
    private String pinVerificationData;

    @ColumnDefault("0")
    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts;

    @ColumnDefault("3")
    @Column(name = "max_failed_attempts", nullable = false)
    private Integer maxFailedAttempts;

    @ColumnDefault("false")
    @Column(name = "blocked", nullable = false)
    private Boolean blocked;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}