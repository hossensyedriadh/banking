package io.github.hossensyedriadh.cbs.account.internal.mapper;

import io.github.hossensyedriadh.cbs.account.api.model.AccountRestrictionView;
import io.github.hossensyedriadh.cbs.account.api.model.CreateAccountRestrictionCommand;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountRestriction;
import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface AccountRestrictionMapper {
    AccountRestrictionView toView(AccountRestriction entity);

    AccountRestriction toEntity(CreateAccountRestrictionCommand command);
}
