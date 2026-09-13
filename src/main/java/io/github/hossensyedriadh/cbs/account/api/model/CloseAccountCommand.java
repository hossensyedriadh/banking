package io.github.hossensyedriadh.cbs.account.api.model;

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
public class CloseAccountCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -8086580081236151042L;

    @NotNull
    @JsonProperty("account_id")
    private Long accountId;

    @NotBlank
    @Size(max = 255)
    private String reason;
}
