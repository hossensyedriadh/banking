package io.github.hossensyedriadh.internet_banking.http.cbs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
public class CbsAccountIdResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1489221332919027299L;

    @JsonProperty("bank_account_ids")
    private Set<Long> bankAccountIds;
}
