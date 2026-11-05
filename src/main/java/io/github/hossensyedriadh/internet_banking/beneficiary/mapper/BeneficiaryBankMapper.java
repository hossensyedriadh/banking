package io.github.hossensyedriadh.internet_banking.beneficiary.mapper;

import io.github.hossensyedriadh.internet_banking.beneficiary.entity.BeneficiaryBank;
import io.github.hossensyedriadh.internet_banking.beneficiary.model.BeneficiaryBankModel;
import io.github.hossensyedriadh.internet_banking.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface BeneficiaryBankMapper {
    BeneficiaryBankModel toModel(BeneficiaryBank entity);
}
