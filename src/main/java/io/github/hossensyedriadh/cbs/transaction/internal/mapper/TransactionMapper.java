package io.github.hossensyedriadh.cbs.transaction.internal.mapper;

import io.github.hossensyedriadh.cbs.configuration.MapperConfiguration;
import io.github.hossensyedriadh.cbs.transaction.api.model.TransactionView;
import io.github.hossensyedriadh.cbs.transaction.internal.domain.Transaction;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface TransactionMapper {
    TransactionView toView(Transaction transaction);
}
