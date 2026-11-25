package io.github.hossensyedriadh.internet_banking.customer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PrincipalIdentityModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 6863759240372654288L;

    private CustomerIdentityModel customerIdentity;

    private String name;

    private String email;

    private Boolean emailVerified;

    private String phoneNumber;

    private Boolean phoneNumberVerified;
}
