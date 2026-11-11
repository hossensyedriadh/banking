package io.github.hossensyedriadh.cbs.audit.internal.domain;

import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
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
@Table(name = "audit_logs", schema = "audit")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "module_name", updatable = false, nullable = false, length = 50)
    private String moduleName;

    @Column(name = "entity_type", updatable = false, nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", updatable = false, nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", updatable = false, nullable = false, length = 30)
    private AuditAction action;

    @Column(name = "description", updatable = false, length = Integer.MAX_VALUE)
    private String description;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}