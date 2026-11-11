package io.github.hossensyedriadh.cbs.account.internal.mapper;

import io.github.hossensyedriadh.cbs.account.api.model.AccountHoldStatusHistoryView;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHoldStatusHistory;
import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface AccountHoldStatusHistoryMapper {
    AccountHoldStatusHistoryView toView(AccountHoldStatusHistory entity);
}
