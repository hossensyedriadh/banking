package io.github.hossensyedriadh.internet_banking.beneficiary.mapper;

import io.github.hossensyedriadh.internet_banking.beneficiary.entity.BeneficiaryBankBranch;
import io.github.hossensyedriadh.internet_banking.beneficiary.model.BeneficiaryBankBranchModel;
import io.github.hossensyedriadh.internet_banking.configuration.mapper.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface BeneficiaryBankBranchMapper {
    BeneficiaryBankBranchModel toModel(BeneficiaryBankBranch entity);
}
