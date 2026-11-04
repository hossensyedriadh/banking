package io.github.hossensyedriadh.internet_banking.customer.model;

import io.github.hossensyedriadh.internet_banking.customer.enumerator.CustomerIdentityStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PrincipalIdentityModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 6863759240372654288L;

    private Long id;

    private Long cbsCustomerId;

    private CustomerIdentityStatus status;

    private String iamSubject;

    private String name;

    private String email;

    private Boolean emailVerified;

    private String phoneNumber;

    private Boolean phoneNumberVerified;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
