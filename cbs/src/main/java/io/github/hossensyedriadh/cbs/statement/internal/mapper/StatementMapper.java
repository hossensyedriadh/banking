package io.github.hossensyedriadh.cbs.statement.internal.mapper;

import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import io.github.hossensyedriadh.cbs.statement.api.model.StatementView;
import io.github.hossensyedriadh.cbs.statement.internal.domain.Statement;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface StatementMapper {
    StatementView toView(Statement entity);
}
