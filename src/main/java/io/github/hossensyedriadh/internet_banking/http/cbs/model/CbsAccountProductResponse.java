package io.github.hossensyedriadh.internet_banking.http.cbs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.internet_banking.account.enumerator.AccountProductStatus;
import io.github.hossensyedriadh.internet_banking.account.enumerator.AccountProductType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CbsAccountProductResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 3926293554339838997L;

    private Long id;

    @JsonProperty("product_code")
    private String productCode;

    private String name;

    private String description;

    @JsonProperty("product_type")
    private AccountProductType productType;

    private AccountProductStatus status;
}
