package io.github.hossensyedriadh.cbs.product.internal.mapper;

import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import io.github.hossensyedriadh.cbs.product.api.command.CreateChequeProductCommand;
import io.github.hossensyedriadh.cbs.product.api.command.UpdateChequeProductCommand;
import io.github.hossensyedriadh.cbs.product.api.model.ChequeProductView;
import io.github.hossensyedriadh.cbs.product.internal.domain.ChequeProduct;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfiguration.class)
public interface ChequeProductMapper {
    ChequeProductView toView(ChequeProduct entity);

    ChequeProduct toEntity(CreateChequeProductCommand command);

    void toPatchedEntity(UpdateChequeProductCommand command, @MappingTarget ChequeProduct entity);
}
