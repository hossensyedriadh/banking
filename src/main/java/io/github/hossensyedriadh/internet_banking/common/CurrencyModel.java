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
public class CurrencyModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1129842132131400974L;

    private Long id;

    private String code;

    private String name;

    private String symbol;

    private Short decimalPlaces;
}
