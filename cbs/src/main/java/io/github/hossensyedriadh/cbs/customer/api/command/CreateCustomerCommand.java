package io.github.hossensyedriadh.cbs.customer.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class CreateCustomerCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 9091724310596889014L;

    @NotBlank
    @Size(max = 100)
    @JsonProperty("first_name")
    private String firstName;

    @Size(max = 100)
    @JsonProperty("middle_name")
    private String middleName;

    @Size(max = 100)
    @JsonProperty("last_name")
    private String lastName;

    @NotNull
    @Past
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 30)
    private String phone;
}
