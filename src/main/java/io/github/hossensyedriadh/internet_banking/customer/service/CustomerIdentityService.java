package io.github.hossensyedriadh.internet_banking.customer.service;

import io.github.hossensyedriadh.internet_banking.customer.model.CustomerIdentityModel;
import io.github.hossensyedriadh.internet_banking.customer.model.PrincipalIdentityModel;

import java.util.Optional;

public interface CustomerIdentityService {
    PrincipalIdentityModel getPrincipal();

    Optional<CustomerIdentityModel> getByIamSubject(String iamSubject);

    Optional<CustomerIdentityModel> getActiveByIamSubject(String iamSubject);
}
