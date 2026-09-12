package io.github.hossensyedriadh.cbs.account.internal.mapper;

import io.github.hossensyedriadh.cbs.account.api.model.AccountHolderView;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHolder;
import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface AccountHolderMapper {
    AccountHolderView toView(AccountHolder accountHolder);
}
