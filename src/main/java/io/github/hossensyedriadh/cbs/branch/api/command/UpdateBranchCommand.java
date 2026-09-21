package io.github.hossensyedriadh.cbs.branch.api.command;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class UpdateBranchCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -100676699835291610L;

    @Size(max = 150)
    private String name;

    private String address;

    @Size(max = 30)
    private String phone;
}
