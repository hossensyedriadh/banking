package io.github.hossensyedriadh.cbs.account.internal.mapper;

import io.github.hossensyedriadh.cbs.account.api.model.AccountHolderView;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHolder;
import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(config = MapperConfiguration.class)
public interface AccountHolderMapper {
    @Mappings({
            @Mapping(target = "customerId", source = "id.customerId")
    })
    AccountHolderView toView(AccountHolder accountHolder);
}
