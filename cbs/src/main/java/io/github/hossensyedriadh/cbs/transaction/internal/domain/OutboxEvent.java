package io.github.hossensyedriadh.cbs.transaction.internal.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "outbox_events", schema = "banking_transaction", indexes = {@Index(name = "idx_outbox_events_unpublished",
        columnList = "created_at")}, uniqueConstraints = {@UniqueConstraint(name = "outbox_events_event_id_key",
        columnNames = {"event_id"})})
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "event_id", updatable = false, nullable = false)
    private UUID eventId;

    @Column(name = "aggregate_type", updatable = false, nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", updatable = false, nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "event_type", updatable = false, nullable = false)
    private String eventType;

    @Column(name = "payload", updatable = false, nullable = false)
    private byte[] payload;

    @Column(name = "occurred_at", updatable = false, nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "published_at", insertable = false)
    private OffsetDateTime publishedAt;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}