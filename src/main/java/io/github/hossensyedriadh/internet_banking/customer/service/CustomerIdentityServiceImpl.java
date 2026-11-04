package io.github.hossensyedriadh.internet_banking.customer.service;

import io.github.hossensyedriadh.internet_banking.customer.entity.CustomerIdentity;
import io.github.hossensyedriadh.internet_banking.customer.enumerator.CustomerIdentityStatus;
import io.github.hossensyedriadh.internet_banking.customer.mapper.CustomerIdentityMapper;
import io.github.hossensyedriadh.internet_banking.customer.model.CustomerIdentityModel;
import io.github.hossensyedriadh.internet_banking.customer.model.PrincipalIdentityModel;
import io.github.hossensyedriadh.internet_banking.customer.repository.CustomerIdentityRepository;
import io.github.hossensyedriadh.internet_banking.exception.ResourceException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
class CustomerIdentityServiceImpl implements CustomerIdentityService {
    private final CustomerIdentityRepository customerIdentityRepository;
    private final CustomerIdentityMapper customerIdentityMapper;

    @Autowired
    public CustomerIdentityServiceImpl(CustomerIdentityRepository customerIdentityRepository, CustomerIdentityMapper customerIdentityMapper) {
        this.customerIdentityRepository = customerIdentityRepository;
        this.customerIdentityMapper = customerIdentityMapper;
    }

    @Override
    public PrincipalIdentityModel getPrincipal() {
        OidcUser oidcUser = (OidcUser) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        Optional<CustomerIdentity> customerIdentityOptional = this.customerIdentityRepository.findByIamSubject(Objects.requireNonNull(oidcUser).getSubject());

        if (customerIdentityOptional.isEmpty()) {
            throw new ResourceException("Customer identity not found", HttpStatus.BAD_REQUEST);
        }

        CustomerIdentity identity = customerIdentityOptional.get();

        PrincipalIdentityModel model = new PrincipalIdentityModel();
        model.setId(identity.getId());
        model.setCbsCustomerId(identity.getCbsCustomerId());
        model.setStatus(identity.getStatus());
        model.setCreatedAt(identity.getCreatedAt());
        model.setUpdatedAt(identity.getUpdatedAt());
        model.setIamSubject(oidcUser.getSubject());
        model.setName(oidcUser.getName());
        model.setEmail(oidcUser.getEmail());
        model.setEmailVerified(oidcUser.getEmailVerified());
        model.setPhoneNumber(oidcUser.getPhoneNumber());
        model.setPhoneNumberVerified(oidcUser.getPhoneNumberVerified());

        return model;
    }

    @Override
    public Optional<CustomerIdentityModel> getByIamSubject(String iamSubject) {
        return this.customerIdentityRepository.findByIamSubject(iamSubject).map(this.customerIdentityMapper::toModel);
    }

    @Override
    public Optional<CustomerIdentityModel> getActiveByIamSubject(String iamSubject) {
        return this.customerIdentityRepository.findByIamSubjectAndStatus(iamSubject, CustomerIdentityStatus.ACTIVE)
                .map(this.customerIdentityMapper::toModel);
    }
}
