package io.github.hossensyedriadh.cbs.audit.internal.repository;

import io.github.hossensyedriadh.cbs.audit.internal.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
