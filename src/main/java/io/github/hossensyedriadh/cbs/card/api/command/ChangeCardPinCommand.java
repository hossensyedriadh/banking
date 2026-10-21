package io.github.hossensyedriadh.cbs.card.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.card.api.validation.ValidCardPin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class ChangeCardPinCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -5516222106574111340L;

    @NotBlank
    private String pan;

    @NotBlank
    @Pattern(regexp = "^[0-9]+$", message = "Only digits (0-9) allowed")
    @Size(min = 4, max = 4, message = "Must be 4 digits long")
    @JsonProperty("current_pin")
    private String currentPin;

    @NotBlank
    @ValidCardPin
    @JsonProperty("new_pin")
    private String newPin;
}
