package io.github.hossensyedriadh.cbs.account.api.controller.v1;

import io.github.hossensyedriadh.cbs.account.api.command.ScheduleFeeCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.ScheduledFeeStatus;
import io.github.hossensyedriadh.cbs.account.api.model.ScheduledFeeView;
import io.github.hossensyedriadh.cbs.account.api.service.ScheduledFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
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
import java.util.Optional;

@RestController
@RequestMapping(value = "/scheduled-fees/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Scheduled Fee API", description = "API endpoints for managing scheduled fees")
public class ScheduledFeeController {
    private final ScheduledFeeService scheduledFeeService;

    @Autowired
    public ScheduledFeeController(ScheduledFeeService scheduledFeeService) {
        this.scheduledFeeService = scheduledFeeService;
    }

    @InitBinder
    public void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @Operation(summary = "Get scheduled fees", description = "Retrieve a list of scheduled fees based on optional status and pagination parameters",
            parameters = {
                    @Parameter(name = "status", description = "Filter scheduled fees by status", in = ParameterIn.QUERY)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Page.class))),
            })
    @GetMapping
    public ResponseEntity<Page<ScheduledFeeView>> getScheduledFees(@RequestParam(value = "status", required = false) ScheduledFeeStatus status, Pageable pageable) {
        Page<ScheduledFeeView> scheduledFeeViews = this.scheduledFeeService.getScheduledFees(status, pageable);

        return ResponseEntity.ok(scheduledFeeViews);
    }

    @Operation(summary = "Get scheduled fees", description = "Retrieve a list of scheduled fees based on optional status and pagination parameters",
            parameters = {
                    @Parameter(name = "accountId", description = "Filter scheduled fees by account ID", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "status", description = "Filter scheduled fees by status", in = ParameterIn.QUERY)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Page.class))),
            })
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<ScheduledFeeView>> getScheduledFees(@PathVariable("accountId") Long accountId, @RequestParam(value = "status", required = false) ScheduledFeeStatus status,
                                                                   Pageable pageable) {
        Page<ScheduledFeeView> scheduledFeeViews = this.scheduledFeeService.getScheduledFees(accountId, status, pageable);

        return ResponseEntity.ok(scheduledFeeViews);
    }

    @Operation(summary = "Get scheduled fee by ID", description = "Retrieve a scheduled fee by its ID",
            parameters = {
                    @Parameter(name = "id", description = "ID of the scheduled fee", in = ParameterIn.PATH, required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Scheduled fee retrieved successfully", content = @Content(schema = @Schema(implementation = ScheduledFeeView.class))),
                    @ApiResponse(responseCode = "404", description = "Scheduled fee not found", content = @Content)
            })
    @GetMapping("/{id}")
    public ResponseEntity<ScheduledFeeView> getScheduledFee(@PathVariable("id") Long scheduledFeeId) {
        Optional<ScheduledFeeView> scheduledFeeView = this.scheduledFeeService.getScheduledFee(scheduledFeeId);

        return scheduledFeeView.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get scheduled fee by ID and account ID", description = "Retrieve a scheduled fee by its ID and account ID",
            parameters = {
                    @Parameter(name = "accountId", description = "ID of the account", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "id", description = "ID of the scheduled fee", in = ParameterIn.PATH, required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Scheduled fee retrieved successfully", content = @Content(schema = @Schema(implementation = ScheduledFeeView.class))),
                    @ApiResponse(responseCode = "404", description = "Scheduled fee not found", content = @Content)
            })
    @GetMapping("/account/{accountId}/{id}")
    public ResponseEntity<ScheduledFeeView> getScheduledFee(@PathVariable("accountId") Long accountId, @PathVariable("id") Long scheduledFeeId) {
        Optional<ScheduledFeeView> scheduledFeeView = this.scheduledFeeService.getScheduledFee(accountId, scheduledFeeId);

        return scheduledFeeView.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Schedule a fee", description = "Schedule a fee for an account",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Command to schedule a fee", content = @Content(schema = @Schema(implementation = ScheduleFeeCommand.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Fee scheduled successfully", content = @Content(schema = @Schema(implementation = ScheduledFeeView.class)))
            })
    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ScheduledFeeView> scheduleFee(@RequestBody ScheduleFeeCommand command) {
        ScheduledFeeView scheduledFeeView = this.scheduledFeeService.schedule(command);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(scheduledFeeView.getId()).toUri();

        return ResponseEntity.created(uri).body(scheduledFeeView);
    }

    @Operation(summary = "Cancel a scheduled fee", description = "Cancel a scheduled fee by its ID",
            parameters = {
                    @Parameter(name = "id", description = "ID of the scheduled fee", in = ParameterIn.PATH, required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Scheduled fee cancelled successfully"),
                    @ApiResponse(responseCode = "404", description = "Scheduled fee not found", content = @Content)
            })
    @PostMapping("/cancellation/{id}")
    public ResponseEntity<Void> cancel(@PathVariable("id") Long id) {
        this.scheduledFeeService.cancelScheduledFee(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Waive a scheduled fee", description = "Waive a scheduled fee by its ID",
            parameters = {
                    @Parameter(name = "id", description = "ID of the scheduled fee", in = ParameterIn.PATH, required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Scheduled fee waived successfully"),
                    @ApiResponse(responseCode = "404", description = "Scheduled fee not found", content = @Content)
            })
    @PostMapping("/waiving/{id}")
    public ResponseEntity<Void> waive(@PathVariable("id") Long id) {
        this.scheduledFeeService.waiveScheduledFee(id);

        return ResponseEntity.noContent().build();
    }
}
