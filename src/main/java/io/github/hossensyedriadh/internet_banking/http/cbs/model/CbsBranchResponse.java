package io.github.hossensyedriadh.internet_banking.http.cbs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CbsBranchResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -1512906182522740762L;

    private Long id;

    @JsonProperty("branch_code")
    private String branchCode;

    private String name;

    private String address;

    private String phone;

    private boolean active;
}
