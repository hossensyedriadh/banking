package io.github.hossensyedriadh.cbs.account.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class ReleaseAccountHoldCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -2291113890837427677L;

    @NotNull
    @JsonProperty("hold_id")
    private Long holdId;

    @NotBlank
    @Size(max = 255)
    private String reason;
}
