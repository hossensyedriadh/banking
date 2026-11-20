package io.github.hossensyedriadh.internet_banking.common;

import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BranchModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 6012444283228569839L;

    private Long id;

    private String branchCode;

    private String name;

    private String address;

    private String phone;
}
