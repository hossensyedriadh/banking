package io.github.hossensyedriadh.internet_banking.customer.mapper;

import io.github.hossensyedriadh.internet_banking.configuration.mapper.MapperConfiguration;
import io.github.hossensyedriadh.internet_banking.customer.entity.CustomerIdentity;
import io.github.hossensyedriadh.internet_banking.customer.model.CustomerIdentityModel;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface CustomerIdentityMapper {
    CustomerIdentityModel toModel(CustomerIdentity entity);
}
