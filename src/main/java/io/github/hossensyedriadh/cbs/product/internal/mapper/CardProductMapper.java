package io.github.hossensyedriadh.cbs.product.internal.mapper;

import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import io.github.hossensyedriadh.cbs.product.api.model.CardProductView;
import io.github.hossensyedriadh.cbs.product.api.model.CreateCardProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.UpdateCardProductCommand;
import io.github.hossensyedriadh.cbs.product.internal.domain.CardProduct;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfiguration.class)
public interface CardProductMapper {
    CardProductView toView(CardProduct entity);

    CardProduct toEntity(CreateCardProductCommand command);

    void toPatchedEntity(UpdateCardProductCommand command, @MappingTarget CardProduct entity);
}
