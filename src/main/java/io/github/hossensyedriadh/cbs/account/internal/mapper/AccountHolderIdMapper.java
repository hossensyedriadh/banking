package io.github.hossensyedriadh.cbs.account.internal.mapper;

import io.github.hossensyedriadh.cbs.account.api.model.AccountHolderIdView;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHolderId;
import io.github.hossensyedriadh.cbs.configuration.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface AccountHolderIdMapper {
    AccountHolderIdView toView(AccountHolderId accountHolderId);
}
