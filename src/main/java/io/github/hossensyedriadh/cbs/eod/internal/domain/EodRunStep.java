package io.github.hossensyedriadh.cbs.eod.internal.domain;

import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStep;
import io.github.hossensyedriadh.cbs.eod.api.enumerator.EodStepStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
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
@Table(name = "eod_run_steps", schema = "eod", indexes = {
        @Index(name = "idx_eod_run_steps_run_id",
                columnList = "eod_run_id"),
        @Index(name = "idx_eod_run_steps_status",
                columnList = "status")}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_eod_run_step_name",
                columnNames = {
                        "eod_run_id",
                        "step_name"}),
        @UniqueConstraint(name = "uq_eod_run_step_order",
                columnNames = {
                        "eod_run_id",
                        "step_order"})})
public class EodRunStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eod_run_id", referencedColumnName = "id")
    private EodRun eodRun;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_name", updatable = false, nullable = false, length = 50)
    private EodStep stepName;

    @Column(name = "step_order", updatable = false, nullable = false)
    private Integer stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EodStepStatus status;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "error_message", length = Integer.MAX_VALUE)
    private String errorMessage;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}