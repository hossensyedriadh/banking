package io.github.hossensyedriadh.cbs.product.api.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class UpdateAccountProductCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -1825951808541646875L;

    private String name;

    private String description;
}
