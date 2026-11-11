package io.github.hossensyedriadh.internet_banking.account.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CurrencyModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1129842132131400974L;

    private Long id;

    private String code;

    private String name;

    private String symbol;

    @JsonProperty("decimal_places")
    private Short decimalPlaces;
}
