package io.github.hossensyedriadh.cbs.audit.internal.impl;

import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.model.AuditLogView;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import io.github.hossensyedriadh.cbs.audit.internal.domain.AuditLog;
import io.github.hossensyedriadh.cbs.audit.internal.mapper.AuditLogMapper;
import io.github.hossensyedriadh.cbs.audit.internal.repository.AuditLogRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
public class AuditServiceImpl implements AuditService {
    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Autowired
    public AuditServiceImpl(AuditLogRepository auditLogRepository, AuditLogMapper auditLogMapper) {
        this.auditLogRepository = auditLogRepository;
        this.auditLogMapper = auditLogMapper;
    }

    @Transactional
    @Override
    public void record(CreateAuditLogCommand command) {
        AuditLog auditLog = this.auditLogMapper.toEntity(command);

        this.auditLogRepository.save(auditLog);

        log.info("AuditLog recorded for entity: {}", auditLog.getEntityType());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<AuditLogView> findByEntity(String entityType, Long entityId, Pageable pageable) {
        return this.auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable)
                .map(this.auditLogMapper::toView);
    }
}
