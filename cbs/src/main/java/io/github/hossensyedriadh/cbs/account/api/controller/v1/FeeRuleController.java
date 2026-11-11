package io.github.hossensyedriadh.cbs.account.api.controller.v1;

import io.github.hossensyedriadh.cbs.account.api.command.CreateFeeRuleCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.FeeRuleStatus;
import io.github.hossensyedriadh.cbs.account.api.model.FeeRuleView;
import io.github.hossensyedriadh.cbs.account.api.service.FeeRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/fee-rules/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Fee Rule API", description = "API endpoints for managing fee rules")
public class FeeRuleController {
    private final FeeRuleService feeRuleService;

    @Autowired
    public FeeRuleController(FeeRuleService feeRuleService) {
        this.feeRuleService = feeRuleService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @Operation(summary = "Get fee rules by status", description = "Retrieve a paginated list of fee rules with the specified status",
            parameters = {
                    @Parameter(name = "status", description = "Fee rule status", required = true,
                            examples = {
                                    @ExampleObject(value = "ACTIVE", summary = "Active fee rules")
                            }
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FeeRuleView.class)))
            }
    )
    @GetMapping("/with-status/{status}")
    public ResponseEntity<Page<FeeRuleView>> getRules(Pageable pageable, @PathVariable("status") FeeRuleStatus status) {
        Page<FeeRuleView> ruleViews = this.feeRuleService.getFeeRules(status, pageable);

        return ResponseEntity.ok(ruleViews);
    }

    @Operation(summary = "Get fee rules by status", description = "Retrieve a list of fee rules with the specified status",
            parameters = {
                    @Parameter(name = "status", description = "Fee rule status", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = FeeRuleView.class)))
                    ),
                    @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
            })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<FeeRuleView>> getRules(@PathVariable("status") FeeRuleStatus status) {
        List<FeeRuleView> ruleViews = this.feeRuleService.getFeeRules(status);

        if (ruleViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(ruleViews);
    }

    @Operation(summary = "Get a fee rule by ID", description = "Retrieve a fee rule by its unique identifier.",
            parameters = @Parameter(name = "id", description = "The unique identifier of the fee rule to retrieve.", required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = FeeRuleView.class))),
                    @ApiResponse(responseCode = "404", description = "Fee rule not found", content = @Content)
            })
    @GetMapping("/{id}")
    public ResponseEntity<FeeRuleView> getFeeRule(@PathVariable("id") Long id) {
        Optional<FeeRuleView> feeRuleView = this.feeRuleService.getFeeRule(id);

        return feeRuleView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get a fee rule by fee code and status", description = "Retrieve a fee rule by its fee code and status.",
            parameters = {
                    @Parameter(name = "code", description = "The fee code of the fee rule to retrieve.", required = true),
                    @Parameter(name = "status", description = "The status of the fee rule to retrieve.", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = FeeRuleView.class))),
                    @ApiResponse(responseCode = "404", description = "Fee rule not found", content = @Content)
            })
    @GetMapping("/fee-code/{code}/status/{status}")
    public ResponseEntity<FeeRuleView> getFeeRule(@PathVariable("code") String feeCode, @PathVariable("status") FeeRuleStatus feeRuleStatus) {
        Optional<FeeRuleView> feeRuleView = this.feeRuleService.getFeeRule(feeCode, feeRuleStatus);

        return feeRuleView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new fee rule", description = "Create a new fee rule with the provided details.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The details of the fee rule to create.", required = true, content = @Content(schema = @Schema(implementation = CreateFeeRuleCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Fee rule created successfully",
                            content = @Content(schema = @Schema(implementation = FeeRuleView.class)))
            })
    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<FeeRuleView> createFeeRule(@RequestBody CreateFeeRuleCommand command) {
        FeeRuleView saved = this.feeRuleService.createFeeRule(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @Operation(summary = "Activate a fee rule", description = "Activate a fee rule by its ID.",
            parameters = {
                    @Parameter(name = "id", description = "The ID of the fee rule to activate.", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Fee rule activated successfully"),
                    @ApiResponse(responseCode = "404", description = "Fee rule not found", content = @Content)
            })
    @PostMapping("/activation/{id}")
    public ResponseEntity<Void> activateFeeRule(@PathVariable("id") Long id) {
        this.feeRuleService.activateFeeRule(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deactivate a fee rule", description = "Deactivate a fee rule by its ID.",
            parameters = {
                    @Parameter(name = "id", description = "The ID of the fee rule to deactivate.", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Fee rule deactivated successfully"),
                    @ApiResponse(responseCode = "404", description = "Fee rule not found", content = @Content)
            })
    @PostMapping("/deactivation/{id}")
    public ResponseEntity<Void> deactivateFeeRule(@PathVariable("id") Long id) {
        this.feeRuleService.deactivateFeeRule(id);

        return ResponseEntity.noContent().build();
    }
}
