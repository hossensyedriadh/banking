package io.github.hossensyedriadh.cbs.audit.api;

import io.github.hossensyedriadh.cbs.audit.api.model.AuditLogView;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    void record(CreateAuditLogCommand command);

    Page<AuditLogView> findByEntity(String entityType, Long entityId, Pageable pageable);
}
