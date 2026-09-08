package io.github.hossensyedriadh.cbs.account.internal.mapper;

import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.model.CreateAccountCommand;
import io.github.hossensyedriadh.cbs.account.internal.domain.Account;
import io.github.hossensyedriadh.cbs.configuration.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface AccountMapper {
    AccountView toView(Account account);

    Account toEntity(CreateAccountCommand command);
}
