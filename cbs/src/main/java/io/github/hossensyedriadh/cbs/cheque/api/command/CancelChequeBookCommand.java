package io.github.hossensyedriadh.cbs.cheque.api.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CancelChequeBookCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -5690065708771263925L;

    @NotNull
    private Long id;

    @NotBlank
    private String reason;
}
