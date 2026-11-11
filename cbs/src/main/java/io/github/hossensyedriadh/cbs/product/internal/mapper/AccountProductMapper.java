package io.github.hossensyedriadh.cbs.product.internal.mapper;

import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import io.github.hossensyedriadh.cbs.product.api.command.CreateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.AccountProductView;
import io.github.hossensyedriadh.cbs.product.internal.domain.AccountProduct;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfiguration.class)
public interface AccountProductMapper {
    AccountProductView toView(AccountProduct entity);

    AccountProduct toEntity(CreateAccountProductCommand command);

    void toPatchedEntity(UpdateAccountProductCommand command, @MappingTarget AccountProduct patchedEntity);
}
