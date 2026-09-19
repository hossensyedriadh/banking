package io.github.hossensyedriadh.cbs.account.internal.mapper;

import io.github.hossensyedriadh.cbs.account.api.command.CreateAccountHoldCommand;
import io.github.hossensyedriadh.cbs.account.api.model.AccountHoldView;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHold;
import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface AccountHoldMapper {
    AccountHoldView toView(AccountHold entity);

    AccountHold toEntity(CreateAccountHoldCommand command);
}
