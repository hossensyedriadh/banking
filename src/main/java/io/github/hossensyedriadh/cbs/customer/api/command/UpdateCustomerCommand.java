package io.github.hossensyedriadh.cbs.customer.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateCustomerCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -69398137051700940L;

    @Size(max = 100)
    @JsonProperty("fist_name")
    private String firstName;

    @Size(max = 100)
    @JsonProperty("middle_name")
    private String middleName;

    @Size(max = 100)
    @JsonProperty("last_name")
    private String lastName;

    @Past
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    @Email
    private String email;

    @Size(max = 30)
    private String phone;
}
