package io.github.hossensyedriadh.cbs.customer.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Date;

@Getter
@Setter
public class CreateCustomerCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 9091724310596889014L;

    @NotBlank
    @JsonProperty("fist_name")
    private String firstName;

    @NotBlank
    @JsonProperty("middle_name")
    private String middleName;

    @NotBlank
    @JsonProperty("last_name")
    private String lastName;

    @NotNull
    @JsonProperty("date_of_birth")
    private Date dateOfBirth;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String phone;
}
