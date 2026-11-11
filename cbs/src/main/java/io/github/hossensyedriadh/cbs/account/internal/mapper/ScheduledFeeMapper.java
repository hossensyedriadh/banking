package io.github.hossensyedriadh.cbs.account.internal.mapper;

import io.github.hossensyedriadh.cbs.account.api.model.ScheduledFeeView;
import io.github.hossensyedriadh.cbs.account.internal.domain.ScheduledFee;
import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface ScheduledFeeMapper {
    ScheduledFeeView toView(ScheduledFee entity);
}
