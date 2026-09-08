package io.github.hossensyedriadh.cbs.customer.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Date;

@Getter
@Setter
public class UpdateCustomerCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -69398137051700940L;

    @JsonProperty("fist_name")
    private String firstName;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("date_of_birth")
    private Date dateOfBirth;

    private String email;

    private String phone;
}
