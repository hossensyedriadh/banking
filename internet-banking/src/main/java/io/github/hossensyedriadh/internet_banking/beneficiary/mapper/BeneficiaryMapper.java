package io.github.hossensyedriadh.internet_banking.beneficiary.mapper;

import io.github.hossensyedriadh.internet_banking.beneficiary.entity.Beneficiary;
import io.github.hossensyedriadh.internet_banking.beneficiary.model.BeneficiaryModel;
import io.github.hossensyedriadh.internet_banking.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface BeneficiaryMapper {
    BeneficiaryModel toModel(Beneficiary entity);
}
