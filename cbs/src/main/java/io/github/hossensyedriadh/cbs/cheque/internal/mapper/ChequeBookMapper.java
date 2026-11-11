package io.github.hossensyedriadh.cbs.cheque.internal.mapper;

import io.github.hossensyedriadh.cbs.cheque.api.model.ChequeBookView;
import io.github.hossensyedriadh.cbs.cheque.internal.domain.ChequeBook;
import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface ChequeBookMapper {
    ChequeBookView toView(ChequeBook entity);
}
