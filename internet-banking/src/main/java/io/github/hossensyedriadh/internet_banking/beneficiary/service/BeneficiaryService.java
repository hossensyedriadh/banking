package io.github.hossensyedriadh.internet_banking.beneficiary.service;

import io.github.hossensyedriadh.internet_banking.beneficiary.model.BeneficiaryModel;
import io.github.hossensyedriadh.internet_banking.beneficiary.model.CreateBeneficiaryRequest;
import io.github.hossensyedriadh.internet_banking.beneficiary.model.UpdateBeneficiaryRequest;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeneficiaryService {
    List<BeneficiaryModel> findAll();

    Optional<BeneficiaryModel> findById(UUID id);

    BeneficiaryModel create(@Valid CreateBeneficiaryRequest request);

    BeneficiaryModel update(UUID id, @Valid UpdateBeneficiaryRequest request);

    void deactivate(UUID id);
}
