package io.github.hossensyedriadh.cbs.customer.internal.mapper;

import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import io.github.hossensyedriadh.cbs.customer.api.command.CreateCustomerCommand;
import io.github.hossensyedriadh.cbs.customer.api.command.UpdateCustomerCommand;
import io.github.hossensyedriadh.cbs.customer.api.model.CustomerView;
import io.github.hossensyedriadh.cbs.customer.internal.domain.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfiguration.class)
public interface CustomerMapper {
    CustomerView toView(Customer customer);

    Customer toEntity(CreateCustomerCommand command);

    void toPatchedEntity(UpdateCustomerCommand command, @MappingTarget Customer customer);
}
