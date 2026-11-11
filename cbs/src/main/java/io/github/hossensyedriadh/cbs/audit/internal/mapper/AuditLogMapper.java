package io.github.hossensyedriadh.cbs.audit.internal.mapper;

import io.github.hossensyedriadh.cbs.audit.api.model.AuditLogView;
import io.github.hossensyedriadh.cbs.audit.api.model.CreateAuditLogCommand;
import io.github.hossensyedriadh.cbs.audit.internal.domain.AuditLog;
import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface AuditLogMapper {
    AuditLogView toView(AuditLog auditLog);

    AuditLog toEntity(CreateAuditLogCommand command);
}
