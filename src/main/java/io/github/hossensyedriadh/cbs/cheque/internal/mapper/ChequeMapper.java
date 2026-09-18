package io.github.hossensyedriadh.cbs.cheque.internal.mapper;

import io.github.hossensyedriadh.cbs.cheque.api.model.ChequeView;
import io.github.hossensyedriadh.cbs.cheque.internal.domain.Cheque;
import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface ChequeMapper {
    ChequeView toView(Cheque entity);
}
