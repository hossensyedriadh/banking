package io.github.hossensyedriadh.cbs.branch.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CreateBranchCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 8570334159687820961L;

    @NotBlank
    @JsonProperty("branch_code")
    private String branchCode;

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String phone;
}
