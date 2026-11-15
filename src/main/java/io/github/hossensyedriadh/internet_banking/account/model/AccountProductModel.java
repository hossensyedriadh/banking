package io.github.hossensyedriadh.internet_banking.account.model;

import io.github.hossensyedriadh.internet_banking.account.enumerator.AccountProductStatus;
import io.github.hossensyedriadh.internet_banking.account.enumerator.AccountProductType;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AccountProductModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 4829248600128129949L;

    private Long id;

    private String productCode;

    private String name;

    private String description;

    private AccountProductType type;

    private AccountProductStatus status;
}
