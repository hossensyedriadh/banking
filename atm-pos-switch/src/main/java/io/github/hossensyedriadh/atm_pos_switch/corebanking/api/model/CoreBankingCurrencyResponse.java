package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CoreBankingCurrencyResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 6469096524633275908L;

    private Long id;

    private String code;

    @JsonProperty("numeric_code")
    private String numericCode;

    private String name;

    private String symbol;

    @JsonProperty("decimal_places")
    private Short decimalPlaces;

    private boolean active;
}
