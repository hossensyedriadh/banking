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
            log.warn("Customer identity not found for IAM subject {}", oidcUser.getSubject());
            throw new ResourceException("Customer identity not found", HttpStatus.BAD_REQUEST);
        }

        CustomerIdentity identity = customerIdentityOptional.get();

        CustomerIdentityModel customerIdentityModel = new CustomerIdentityModel();
        customerIdentityModel.setId(identity.getId());
        customerIdentityModel.setIamSubject(oidcUser.getSubject());
        customerIdentityModel.setCbsCustomerId(identity.getCbsCustomerId());
        customerIdentityModel.setStatus(identity.getStatus());
        customerIdentityModel.setCreatedAt(identity.getCreatedAt());
        customerIdentityModel.setUpdatedAt(identity.getUpdatedAt());

        PrincipalIdentityModel model = new PrincipalIdentityModel();
        model.setName(oidcUser.getName());
        model.setEmail(oidcUser.getEmail());
        model.setEmailVerified(oidcUser.getEmailVerified());
        model.setPhoneNumber(oidcUser.getPhoneNumber());
        model.setPhoneNumberVerified(oidcUser.getPhoneNumberVerified());
        model.setCustomerIdentity(customerIdentityModel);

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

    @Override
    public Optional<CustomerIdentity> getById(Long id) {
        return this.customerIdentityRepository.findById(id);
    }

    @Transactional
    @Override
    public void block(Long id) {
        Optional<CustomerIdentity> identityOptional = this.customerIdentityRepository.findByIdForUpdate(id);

        identityOptional.ifPresentOrElse(identity -> {
            if (identity.getStatus() == CustomerIdentityStatus.CLOSED || identity.getStatus() == CustomerIdentityStatus.BLOCKED) {
                String status = identity.getStatus().toString().toLowerCase();
                log.warn("Customer identity with ID {} is already {}. Cannot block.", id, status);
                throw new ResourceException("Customer identity is already " + status, HttpStatus.CONFLICT);
            }

            identity.setStatus(CustomerIdentityStatus.BLOCKED);
            this.customerIdentityRepository.save(identity);

            log.info("Customer identity with ID {} has been blocked.", id);
        }, () -> {
            log.warn("Customer identity with ID {} does not exist.", id);
        });
    }

    @Transactional
    @Override
    public void unblock(Long id) {
        Optional<CustomerIdentity> identityOptional = this.customerIdentityRepository.findByIdForUpdate(id);

        identityOptional.ifPresentOrElse(identity -> {
            if (identity.getStatus() == CustomerIdentityStatus.CLOSED || identity.getStatus() == CustomerIdentityStatus.ACTIVE) {
                String status = identity.getStatus().toString().toLowerCase();
                log.warn("Customer identity with ID {} is already {}. Cannot unblock.", id, status);
                throw new ResourceException("Customer identity is already " + status, HttpStatus.CONFLICT);
            }

            identity.setStatus(CustomerIdentityStatus.ACTIVE);
            this.customerIdentityRepository.save(identity);

            log.info("Customer identity with ID {} has been unblocked.", id);
        }, () -> {
            log.warn("Customer identity with ID {} does not exist.", id);
        });
    }

    @Transactional
    @Override
    public void close(Long id) {
        Optional<CustomerIdentity> identityOptional = this.customerIdentityRepository.findByIdForUpdate(id);

        identityOptional.ifPresentOrElse(identity -> {
            if (identity.getStatus() == CustomerIdentityStatus.CLOSED) {
                String status = identity.getStatus().toString().toLowerCase();
                log.warn("Customer identity with ID {} is already {}. Cannot close.", id, status);
                throw new ResourceException("Customer identity is already " + status, HttpStatus.CONFLICT);
            }

            identity.setStatus(CustomerIdentityStatus.CLOSED);
            this.customerIdentityRepository.save(identity);

            log.info("Customer identity with ID {} has been closed.", id);
        }, () -> {
            log.warn("Customer identity with ID {} does not exist.", id);
        });
    }
}
