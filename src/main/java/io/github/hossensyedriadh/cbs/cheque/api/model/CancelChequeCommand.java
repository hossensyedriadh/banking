package io.github.hossensyedriadh.cbs.cheque.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CancelChequeCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -8434280552753695220L;

    @NotNull
    private Long id;

    @NotBlank
    private String reason;
}
