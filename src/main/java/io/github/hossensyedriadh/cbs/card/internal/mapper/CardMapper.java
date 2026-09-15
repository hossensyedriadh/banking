package io.github.hossensyedriadh.cbs.card.internal.mapper;

import io.github.hossensyedriadh.cbs.card.api.model.CardView;
import io.github.hossensyedriadh.cbs.card.internal.domain.Card;
import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface CardMapper {
    CardView toView(Card entity);
}
