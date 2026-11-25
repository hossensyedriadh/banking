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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Optional<CustomerIdentity> customerIdentityOptional = this.customerIdentityRepository.findByIamSubject(this.getSubject());

        if (customerIdentityOptional.isEmpty()) {
            log.warn("Customer identity not found for IAM subject {}", this.getSubject());
            throw new ResourceException("Customer identity not found", HttpStatus.NOT_FOUND);
        }

        CustomerIdentityModel customerIdentityModel =
                this.customerIdentityMapper.toModel(customerIdentityOptional.get());

        PrincipalIdentityModel model = new PrincipalIdentityModel();
        model.setName(this.getClaim("name"));
        model.setEmail(this.getClaim("email"));
        model.setEmailVerified(this.getBooleanClaim("email_verified"));
        model.setPhoneNumber(this.getClaim("phone_number"));
        model.setPhoneNumberVerified(this.getBooleanClaim("phone_number_verified"));
        model.setCustomerIdentity(customerIdentityModel);

        return model;
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
        }, () -> log.warn("Customer identity with ID {} does not exist, failed to block", id));
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
        }, () -> log.warn("Customer identity with ID {} does not exist, failed to unblock.", id));
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
        }, () -> log.warn("Customer identity with ID {} does not exist, failed to close", id));
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        return authentication;
    }

    public String getSubject() {
        Authentication authentication = this.getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauth2) {
            return ((OidcUser) oauth2.getPrincipal()).getSubject();
        }

        if (authentication instanceof JwtAuthenticationToken jwt) {
            return jwt.getToken().getSubject();
        }

        throw new IllegalStateException("Unsupported authentication type");
    }

    private String getClaim(String claimName) {
        Authentication authentication = this.getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauth2) {
            return oauth2.getPrincipal().getAttribute(claimName);
        }

        if (authentication instanceof JwtAuthenticationToken jwt) {
            return jwt.getToken().getClaimAsString(claimName);
        }

        throw new IllegalStateException("Unsupported authentication type");
    }

    private Boolean getBooleanClaim(String claimName) {
        Authentication authentication = this.getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauth2) {
            return oauth2.getPrincipal().getAttribute(claimName);
        }

        if (authentication instanceof JwtAuthenticationToken jwt) {
            return jwt.getToken().getClaim(claimName);
        }

        throw new IllegalStateException("Unsupported authentication type");
    }
}
