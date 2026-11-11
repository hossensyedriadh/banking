package io.github.hossensyedriadh.cbs.account.internal.mapper;

import io.github.hossensyedriadh.cbs.account.api.model.AccountRestrictionStatusHistoryView;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountRestrictionStatusHistory;
import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface AccountRestrictionStatusHistoryMapper {
    AccountRestrictionStatusHistoryView toView(AccountRestrictionStatusHistory entity);
}
