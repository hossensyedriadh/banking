package io.github.hossensyedriadh.internet_banking.customer.service;

import io.github.hossensyedriadh.internet_banking.customer.entity.CustomerIdentity;
import io.github.hossensyedriadh.internet_banking.customer.model.CustomerIdentityModel;
import io.github.hossensyedriadh.internet_banking.customer.model.PrincipalIdentityModel;

import java.util.Optional;

public interface CustomerIdentityService {
    PrincipalIdentityModel getPrincipal();

    Optional<CustomerIdentityModel> getByIamSubject(String iamSubject);

    Optional<CustomerIdentityModel> getActiveByIamSubject(String iamSubject);

    Optional<CustomerIdentity> getById(Long id);

    void block(Long id);

    void unblock(Long id);

    void close(Long id);
}
