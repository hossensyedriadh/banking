package io.github.hossensyedriadh.cbs.cheque.internal.mapper;

import io.github.hossensyedriadh.cbs.cheque.api.model.ChequePresentationView;
import io.github.hossensyedriadh.cbs.cheque.internal.domain.ChequePresentation;
import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface ChequePresentationMapper {
    ChequePresentationView toView(ChequePresentation entity);
}
