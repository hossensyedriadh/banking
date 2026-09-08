package io.github.hossensyedriadh.cbs.product.internal.mapper;

import io.github.hossensyedriadh.cbs.configuration.MapperConfiguration;
import io.github.hossensyedriadh.cbs.product.api.model.AccountProductView;
import io.github.hossensyedriadh.cbs.product.api.model.CreateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.UpdateAccountProductCommand;
import io.github.hossensyedriadh.cbs.product.internal.domain.AccountProduct;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfiguration.class)
public interface AccountProductMapper {
    AccountProductView toView(AccountProduct entity);

    AccountProduct toEntity(CreateAccountProductCommand command);

    void toPatchedEntity(UpdateAccountProductCommand command, @MappingTarget AccountProduct patchedEntity);
}
