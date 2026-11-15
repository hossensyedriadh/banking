package io.github.hossensyedriadh.cbs.customer.internal.impl;

import io.github.hossensyedriadh.cbs.authorization.api.service.CurrentActorService;
import io.github.hossensyedriadh.cbs.customer.api.enumerator.CustomerIdentityStatus;
import io.github.hossensyedriadh.cbs.customer.api.service.CustomerIdentityService;
import io.github.hossensyedriadh.cbs.customer.internal.repository.CustomerIdentityRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Log4j2
@Service
class CustomerIdentityServiceImpl implements CustomerIdentityService {
    private final CustomerIdentityRepository customerIdentityRepository;
    private final CurrentActorService currentActorService;

    @Autowired
    public CustomerIdentityServiceImpl(CustomerIdentityRepository customerIdentityRepository, CurrentActorService currentActorService) {
        this.customerIdentityRepository = customerIdentityRepository;
        this.currentActorService = currentActorService;
    }

    @Override
    public Long getAuthenticatedCustomerId() {
        String subject = this.currentActorService.getSubject();

        return this.customerIdentityRepository.findByIamSubjectAndStatus(subject, CustomerIdentityStatus.ACTIVE)
                .map(identity -> identity.getCustomer().getId())
                .orElseThrow(() -> {
                    log.warn("No active customer identity associated with authenticated subject: {}", subject);
                    return new ApiResourceException("No active customer identity associated with authenticated subject", HttpStatus.UNAUTHORIZED);
                });
    }
}
