package io.github.hossensyedriadh.cbs.branch.api.service;

import io.github.hossensyedriadh.cbs.branch.api.command.CreateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.command.UpdateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.model.BranchView;

import java.util.Optional;

public interface BranchService {
    Optional<BranchView> getBranch(Long id);

    Optional<BranchView> getBranchByCode(String code);

    BranchView createBranch(CreateBranchCommand command);

    Optional<BranchView> updateBranch(Long id, UpdateBranchCommand command);

    void activateBranch(Long id);

    void deactivateBranch(Long id);
}
