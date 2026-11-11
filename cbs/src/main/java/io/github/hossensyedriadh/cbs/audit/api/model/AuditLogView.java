package io.github.hossensyedriadh.cbs.audit.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
public class AuditLogView implements Serializable {
    @Serial
    private static final long serialVersionUID = -4665350468037723885L;

    private Long id;

    @JsonProperty("module_name")
    private String moduleName;

    @JsonProperty("entity_type")
    private String entityType;

    @JsonProperty("entity_id")
    private Long entityId;

    private String action;

    private String description;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
