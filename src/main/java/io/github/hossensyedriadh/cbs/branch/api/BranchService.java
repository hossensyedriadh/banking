package io.github.hossensyedriadh.cbs.branch.api;

import io.github.hossensyedriadh.cbs.branch.api.model.BranchView;
import io.github.hossensyedriadh.cbs.branch.api.model.CreateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.model.UpdateBranchCommand;

import java.util.Optional;

public interface BranchService {
    Optional<BranchView> getBranch(Long id);

    Optional<BranchView> getBranchByCode(String code);

    BranchView createBranch(CreateBranchCommand command);

    Optional<BranchView> updateBranch(Long id, UpdateBranchCommand command);

    void activateBranch(Long id);

    void deactivateBranch(Long id);
}
