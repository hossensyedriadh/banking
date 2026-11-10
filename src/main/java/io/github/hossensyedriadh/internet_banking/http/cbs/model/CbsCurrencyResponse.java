package io.github.hossensyedriadh.internet_banking.http.cbs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CbsCurrencyResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -4276910935686205159L;

    private Long id;

    private String code;

    @JsonProperty("numeric_code")
    private String numericCode;

    private String name;

    private String symbol;

    @JsonProperty("decimal_places")
    private Short decimalPlaces;
}
