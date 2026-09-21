package io.github.hossensyedriadh.cbs.branch.api.service;

import io.github.hossensyedriadh.cbs.branch.api.command.CreateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.command.UpdateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.model.BranchView;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface BranchService {
    List<BranchView> getBranches();

    Optional<BranchView> getBranch(Long id);

    Optional<BranchView> getBranchByCode(String code);

    BranchView createBranch(@Valid CreateBranchCommand command);

    BranchView updateBranch(Long id, @Valid UpdateBranchCommand command);

    void activateBranch(Long id);

    void deactivateBranch(Long id);
}
