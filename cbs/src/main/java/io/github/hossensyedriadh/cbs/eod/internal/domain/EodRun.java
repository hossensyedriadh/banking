package io.github.hossensyedriadh.cbs.eod.internal.domain;

import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodRunStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
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
@Table(name = "eod_runs", schema = "eod", indexes = {
        @Index(name = "idx_eod_runs_business_date",
                columnList = "business_date"),
        @Index(name = "idx_eod_runs_status",
                columnList = "status")})
public class EodRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "business_date", updatable = false, nullable = false)
    private LocalDate businessDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EodRunStatus status;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "started_at", updatable = false, nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "error_message", length = Integer.MAX_VALUE)
    private String errorMessage;

    @Column(name = "triggered_by", updatable = false, length = 100)
    private String triggeredBy;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}