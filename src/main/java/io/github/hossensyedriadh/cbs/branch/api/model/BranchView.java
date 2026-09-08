package io.github.hossensyedriadh.cbs.branch.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class BranchView implements Serializable {
    @Serial
    private static final long serialVersionUID = -4528574879592942318L;

    private Long id;

    @JsonProperty("branch_code")
    private String branchCode;

    private String name;

    private String address;

    private String phone;

    private boolean active;
}
