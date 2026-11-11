package io.github.hossensyedriadh.cbs.branch.internal.mapper;

import io.github.hossensyedriadh.cbs.branch.api.command.CreateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.command.UpdateBranchCommand;
import io.github.hossensyedriadh.cbs.branch.api.model.BranchView;
import io.github.hossensyedriadh.cbs.branch.internal.domain.Branch;
import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfiguration.class)
public interface BranchMapper {
    BranchView toView(Branch branch);

    Branch toEntity(CreateBranchCommand command);

    void toPatchedEntity(UpdateBranchCommand command, @MappingTarget Branch branch);
}
