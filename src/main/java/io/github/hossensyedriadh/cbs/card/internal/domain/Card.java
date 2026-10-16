package io.github.hossensyedriadh.cbs.card.internal.domain;

import io.github.hossensyedriadh.cbs.card.api.enumerator.CardReplacementReason;
import io.github.hossensyedriadh.cbs.card.api.enumerator.CardStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "cards", schema = "card", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cards_card_number_hash", columnNames = {"card_number_hash"})
}, indexes = {
        @Index(name = "idx_cards_account_id", columnList = "account_id"),
        @Index(name = "idx_cards_card_product_id", columnList = "card_product_id"),
        @Index(name = "idx_cards_status", columnList = "status")
})
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "account_id", updatable = false, nullable = false)
    private Long accountId;

    @Column(name = "card_product_id", updatable = false, nullable = false)
    private Long cardProductId;

    @Column(name = "card_number_hash", nullable = false, length = 128)
    private String cardNumberHash;

    @Column(name = "last_four", nullable = false, length = 4)
    private String lastFour;

    @ColumnDefault("'INACTIVE'")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CardStatus status;

    @Column(name = "issued_at", updatable = false, nullable = false)
    private OffsetDateTime issuedAt;

    /**
     * Represents the expiry month.
     * i.e.:
     * 2031-09-30 means the card expires
     * at the end (30th day) of September 2031.
     */
    @Column(name = "expiry_date", updatable = false, nullable = false)
    private LocalDate expiryDate;

    @Column(name = "replaced_card_id", insertable = false)
    private Long replacedCard;

    @Enumerated(EnumType.STRING)
    @Column(name = "replacement_reason", insertable = false)
    private CardReplacementReason replacementReason;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}