package io.github.hossensyedriadh.cbs.audit.api;

import io.github.hossensyedriadh.cbs.audit.api.model.AuditLogView;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;

import java.util.List;

public interface AuditService {
    void record(CreateAuditLogCommand command);

    List<AuditLogView> findByEntity(String entityType, Long entityId);
}
