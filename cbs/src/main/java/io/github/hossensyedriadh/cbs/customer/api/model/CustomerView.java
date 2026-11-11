package io.github.hossensyedriadh.cbs.customer.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.customer.api.enumerator.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Date;
import java.time.OffsetDateTime;

@Getter
@Setter
@Schema(name = "Customer")
public class CustomerView implements Serializable {
    @Serial
    private static final long serialVersionUID = -4410282039611093905L;

    private Long id;

    @JsonProperty("customer_no")
    private String customerNumber;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("date_of_birth")
    private Date dateOfBirth;

    private String email;

    private String phone;

    private CustomerStatus status;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
