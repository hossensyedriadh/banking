package io.github.hossensyedriadh.internet_banking.beneficiary.service;

import io.github.hossensyedriadh.internet_banking.beneficiary.entity.Beneficiary;
import io.github.hossensyedriadh.internet_banking.beneficiary.entity.BeneficiaryBankBranch;
import io.github.hossensyedriadh.internet_banking.beneficiary.enumerator.BeneficiaryBankBranchStatus;
import io.github.hossensyedriadh.internet_banking.beneficiary.enumerator.BeneficiaryBankStatus;
import io.github.hossensyedriadh.internet_banking.beneficiary.enumerator.BeneficiaryStatus;
import io.github.hossensyedriadh.internet_banking.beneficiary.mapper.BeneficiaryMapper;
import io.github.hossensyedriadh.internet_banking.beneficiary.model.BeneficiaryModel;
import io.github.hossensyedriadh.internet_banking.beneficiary.model.CreateBeneficiaryRequest;
import io.github.hossensyedriadh.internet_banking.beneficiary.model.UpdateBeneficiaryRequest;
import io.github.hossensyedriadh.internet_banking.beneficiary.repository.BeneficiaryBankBranchRepository;
import io.github.hossensyedriadh.internet_banking.beneficiary.repository.BeneficiaryRepository;
import io.github.hossensyedriadh.internet_banking.customer.entity.CustomerIdentity;
import io.github.hossensyedriadh.internet_banking.customer.model.PrincipalIdentityModel;
import io.github.hossensyedriadh.internet_banking.customer.service.CustomerIdentityService;
import io.github.hossensyedriadh.internet_banking.exception.ResourceException;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@Service
class BeneficiaryServiceImpl implements BeneficiaryService {
    private final BeneficiaryRepository beneficiaryRepository;
    private final BeneficiaryBankBranchRepository beneficiaryBankBranchRepository;
    private final CustomerIdentityService customerIdentityService;
    private final BeneficiaryMapper beneficiaryMapper;

    @Autowired
    public BeneficiaryServiceImpl(BeneficiaryRepository beneficiaryRepository, BeneficiaryBankBranchRepository beneficiaryBankBranchRepository,
                                  CustomerIdentityService customerIdentityService, BeneficiaryMapper beneficiaryMapper) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.beneficiaryBankBranchRepository = beneficiaryBankBranchRepository;
        this.customerIdentityService = customerIdentityService;
        this.beneficiaryMapper = beneficiaryMapper;
    }

    @Override
    public List<BeneficiaryModel> findAll() {
        return this.beneficiaryRepository.findAllByStatus(BeneficiaryStatus.ACTIVE).stream().map(this.beneficiaryMapper::toModel).collect(Collectors.toList());
    }

    @Override
    public Optional<BeneficiaryModel> findById(UUID id) {
        return this.beneficiaryRepository.findByIdAndStatus(id, BeneficiaryStatus.ACTIVE).map(this.beneficiaryMapper::toModel);
    }

    @Transactional
    @Override
    public BeneficiaryModel create(@Valid CreateBeneficiaryRequest request) {
        PrincipalIdentityModel principal = this.customerIdentityService.getPrincipal();
        Optional<CustomerIdentity> customerIdentity = this.customerIdentityService.getById(principal.getCustomerIdentity().getId());

        if (customerIdentity.isEmpty()) {
            log.warn("Customer identity not found, failed to create beneficiary");
            throw new ResourceException("Customer identity not found", HttpStatus.BAD_REQUEST);
        }

        Optional<BeneficiaryBankBranch> bankBranchOptional = this.beneficiaryBankBranchRepository.findById(request.getBankBranchId());

        if (bankBranchOptional.isEmpty()) {
            log.warn("Bank branch not found with ID: {}, failed to create beneficiary", request.getBankBranchId());
            throw new ResourceException("Bank branch not found", HttpStatus.BAD_REQUEST);
        }

        BeneficiaryBankBranch bankBranch = bankBranchOptional.get();

        if (bankBranch.getBank().getStatus() != BeneficiaryBankStatus.ACTIVE) {
            log.warn("Bank with ID {} is not active, failed to create beneficiary", bankBranch.getBank().getId());
            throw new ResourceException("Bank is not active", HttpStatus.BAD_REQUEST);
        }

        if (bankBranch.getStatus() != BeneficiaryBankBranchStatus.ACTIVE) {
            log.warn("Bank branch with ID {} is not active, failed to create beneficiary", bankBranch.getId());
            throw new ResourceException("Bank branch is not active", HttpStatus.BAD_REQUEST);
        }

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setCustomerIdentity(customerIdentity.get());
        beneficiary.setBankBranch(bankBranch);
        beneficiary.setBeneficiaryName(request.getBeneficiaryName());
        beneficiary.setAccountNumber(request.getAccountNumber());
        beneficiary.setNickname(request.getNickName());
        beneficiary.setStatus(BeneficiaryStatus.ACTIVE);

        Beneficiary saved = this.beneficiaryRepository.save(beneficiary);

        log.info("Beneficiary with ID {} created successfully", saved.getId());

        return this.beneficiaryMapper.toModel(saved);
    }

    @Transactional
    @Override
    public BeneficiaryModel update(UUID id, @Valid UpdateBeneficiaryRequest request) {
        Optional<Beneficiary> beneficiaryOptional = this.beneficiaryRepository.findByIdForUpdate(id);

        if (beneficiaryOptional.isEmpty()) {
            log.warn("Beneficiary with ID {} not found", id);
            throw new ResourceException("Beneficiary not found", HttpStatus.NOT_FOUND);
        }

        Beneficiary beneficiary = beneficiaryOptional.get();
        beneficiary.setBeneficiaryName(request.getBeneficiaryName());
        beneficiary.setNickname(request.getNickName());

        Beneficiary updated = this.beneficiaryRepository.save(beneficiary);

        log.info("Beneficiary with ID {} updated successfully", updated.getId());

        return this.beneficiaryMapper.toModel(updated);
    }

    @Transactional
    @Override
    public void deactivate(UUID id) {
        Optional<Beneficiary> beneficiary = this.beneficiaryRepository.findByIdForUpdate(id);

        beneficiary.ifPresentOrElse(b -> {
            if (b.getStatus() == BeneficiaryStatus.INACTIVE) {
                log.warn("Beneficiary with ID {} is already inactive.", id);
                throw new ResourceException("Beneficiary is already inactive", HttpStatus.CONFLICT);
            }

            b.setStatus(BeneficiaryStatus.INACTIVE);
            this.beneficiaryRepository.save(b);

            log.info("Beneficiary with ID {} has been deactivated.", id);
        }, () -> {
            log.warn("Beneficiary with ID {} does not exist.", id);
        });
    }
}
