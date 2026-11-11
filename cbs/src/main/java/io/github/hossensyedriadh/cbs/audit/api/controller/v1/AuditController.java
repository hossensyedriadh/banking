package io.github.hossensyedriadh.cbs.audit.api.controller.v1;

import io.github.hossensyedriadh.cbs.audit.api.AuditService;
import io.github.hossensyedriadh.cbs.audit.api.model.AuditLogView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/audit/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Audit API", description = "API endpoints for viewing audit logs")
public class AuditController {
    private final AuditService auditService;

    @Autowired
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @Operation(summary = "Get audit logs for an entity", description = "Retrieve paginated audit logs for a specific entity type and ID",
            parameters = {
                    @Parameter(name = "entityType", description = "The type of the entity"),
                    @Parameter(name = "entityId", description = "The ID of the entity")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuditLogView.class)))
            })
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditLogView>> getAuditLogs(@PathVariable("entityType") String entityType, @PathVariable("entityId") Long entityId, Pageable pageable) {
        Page<AuditLogView> auditLogs = this.auditService.findByEntity(entityType, entityId, pageable);

        return ResponseEntity.ok(auditLogs);
    }
}
