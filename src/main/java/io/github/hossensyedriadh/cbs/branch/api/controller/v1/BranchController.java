package io.github.hossensyedriadh.cbs.branch.api.controller.v1;

import io.github.hossensyedriadh.cbs.branch.api.command.CreateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.command.UpdateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.model.BranchView;
import io.github.hossensyedriadh.cbs.branch.api.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/branches/v1/", produces = {MediaType.APPLICATION_JSON_VALUE})
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

    @GetMapping("/")
    public ResponseEntity<List<BranchView>> getBranches() {
        List<BranchView> branches = this.branchService.getBranches();

        if (branches.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(branches);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchView> getBranch(@PathVariable Long id) {
        Optional<BranchView> branch = this.branchService.getBranch(id);

        return branch.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<BranchView> getBranch(@PathVariable("code") String branchCode) {
        Optional<BranchView> branch = this.branchService.getBranchByCode(branchCode);

        return branch.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BranchView> createBranch(@RequestBody CreateBranchCommand command) {
        BranchView branch = this.branchService.createBranch(command);

        return new ResponseEntity<>(branch, HttpStatus.CREATED);
    }

    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BranchView> updateBranch(@PathVariable Long id, @RequestBody UpdateBranchCommand command) {
        BranchView branch = this.branchService.updateBranch(id, command);

        return ResponseEntity.ok(branch);
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<Void> activateBranch(@PathVariable Long id) {
        this.branchService.activateBranch(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateBranch(@PathVariable Long id) {
        this.branchService.deactivateBranch(id);

        return ResponseEntity.noContent().build();
    }
}
