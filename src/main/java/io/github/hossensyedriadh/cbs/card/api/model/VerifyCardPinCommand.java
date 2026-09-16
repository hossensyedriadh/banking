package io.github.hossensyedriadh.cbs.card.api.model;

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
public class VerifyCardPinCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 4075765593229365710L;

    @NotNull
    @JsonProperty("card_id")
    private Long cardId;

    @NotBlank
    @Size(min = 4, max = 4)
    private String pin;
}
