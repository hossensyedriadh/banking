package io.github.hossensyedriadh.cbs.branch.api.controller.v1;

import io.github.hossensyedriadh.cbs.branch.api.command.CreateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.command.UpdateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.model.BranchView;
import io.github.hossensyedriadh.cbs.branch.api.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/branches/v1/", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Branch API", description = "API endpoints for managing branches")
public class BranchController {
    private final BranchService branchService;

    @Autowired
    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @InitBinder
    public void binder(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @Operation(summary = "Get all branches", description = "Retrieves a list of all branches",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = {
                            @Content(array = @ArraySchema(schema = @Schema(implementation = BranchView.class)))
                    }),
                    @ApiResponse(responseCode = "204", description = "No Content")
            })
    @GetMapping("/")
    public ResponseEntity<List<BranchView>> getBranches() {
        List<BranchView> branches = this.branchService.getBranches();

        if (branches.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(branches);
    }

    @Operation(summary = "Get branch by ID", description = "Retrieves a branch by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = {
                            @Content(schema = @Schema(implementation = BranchView.class))
                    }),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @PreAuthorize("hasAuthority(T(io.github.hossensyedriadh.cbs.util.OAuth2Scopes).BRANCH_READ)")
    @GetMapping("/{id}")
    public ResponseEntity<BranchView> getBranch(@PathVariable Long id) {
        Optional<BranchView> branch = this.branchService.getBranch(id);

        return branch.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get branch by code", description = "Retrieves a branch by its code",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = {
                            @Content(schema = @Schema(implementation = BranchView.class))
                    }),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @GetMapping("/code/{code}")
    public ResponseEntity<BranchView> getBranch(@PathVariable("code") String branchCode) {
        Optional<BranchView> branch = this.branchService.getBranchByCode(branchCode);

        return branch.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create branch", description = "Creates a new branch",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Branch creation command", required = true, content = @Content(schema = @Schema(implementation = CreateBranchCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created", content = {
                            @Content(schema = @Schema(implementation = BranchView.class))
                    }),
                    @ApiResponse(responseCode = "409", description = "Branch already exists with code", content = @Content)
            })
    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BranchView> createBranch(@RequestBody CreateBranchCommand command) {
        BranchView branch = this.branchService.createBranch(command);

        return new ResponseEntity<>(branch, HttpStatus.CREATED);
    }

    @Operation(summary = "Update branch", description = "Updates an existing branch",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Branch update command", required = true, content = @Content(schema = @Schema(implementation = UpdateBranchCommand.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Branch updated successfully", content = @Content(schema = @Schema(implementation = BranchView.class))),
                    @ApiResponse(responseCode = "404", description = "Branch not found")
            })
    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BranchView> updateBranch(@PathVariable Long id, @RequestBody UpdateBranchCommand command) {
        BranchView branch = this.branchService.updateBranch(id, command);

        return ResponseEntity.ok(branch);
    }

    @Operation(summary = "Activate branch", description = "Activates an existing branch",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Branch activated successfully"),
                    @ApiResponse(responseCode = "404", description = "Branch not found")
            })
    @PostMapping("/activation/{id}")
    public ResponseEntity<Void> activateBranch(@PathVariable Long id) {
        this.branchService.activateBranch(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deactivate branch", description = "Deactivates an existing branch",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Branch deactivated successfully"),
                    @ApiResponse(responseCode = "404", description = "Branch not found")
            })
    @PostMapping("/deactivation/{id}")
    public ResponseEntity<Void> deactivateBranch(@PathVariable Long id) {
        this.branchService.deactivateBranch(id);

        return ResponseEntity.noContent().build();
    }
}
