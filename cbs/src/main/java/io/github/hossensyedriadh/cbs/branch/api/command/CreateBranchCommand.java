package io.github.hossensyedriadh.cbs.branch.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CreateBranchCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 8570334159687820961L;

    @Size(max = 20)
    @NotBlank
    @JsonProperty("branch_code")
    private String branchCode;

    @Size(max = 150)
    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @Size(max = 30)
    @NotBlank
    private String phone;
}
