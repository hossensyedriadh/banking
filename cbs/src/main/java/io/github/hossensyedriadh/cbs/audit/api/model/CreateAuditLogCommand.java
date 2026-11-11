package io.github.hossensyedriadh.cbs.audit.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.audit.api.enumerator.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateAuditLogCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -5142197382942249386L;

    @JsonProperty("module_name")
    private String moduleName;

    @JsonProperty("entity_type")
    private String entityType;

    @JsonProperty("entity_id")
    private Long entityId;

    private AuditAction action;

    private String description;
}
