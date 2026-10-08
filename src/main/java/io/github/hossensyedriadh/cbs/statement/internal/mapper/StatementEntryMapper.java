package io.github.hossensyedriadh.cbs.statement.internal.mapper;

import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import io.github.hossensyedriadh.cbs.statement.api.model.StatementEntryView;
import io.github.hossensyedriadh.cbs.statement.internal.domain.StatementEntry;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface StatementEntryMapper {
    StatementEntryView toView(StatementEntry entity);
}
