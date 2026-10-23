package io.github.hossensyedriadh.cbs.card.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class VerifyCardPinCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 4075765593229365710L;

    @NotBlank
    private String pan;

    @NotNull
    @JsonProperty("encrypted_pin_block")
    private byte[] encryptedPinBlock;
}
