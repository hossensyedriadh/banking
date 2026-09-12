package io.github.hossensyedriadh.cbs.product.internal.mapper;

import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import io.github.hossensyedriadh.cbs.product.api.model.CreateCurrencyCommand;
import io.github.hossensyedriadh.cbs.product.api.model.CurrencyView;
import io.github.hossensyedriadh.cbs.product.internal.domain.Currency;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface CurrencyMapper {
    CurrencyView toView(Currency currency);

    Currency toEntity(CreateCurrencyCommand command);
}
