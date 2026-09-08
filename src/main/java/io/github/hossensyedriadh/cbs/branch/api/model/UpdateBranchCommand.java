package io.github.hossensyedriadh.cbs.branch.api.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class UpdateBranchCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -100676699835291610L;

    private String name;

    private String address;

    private String phone;
}
