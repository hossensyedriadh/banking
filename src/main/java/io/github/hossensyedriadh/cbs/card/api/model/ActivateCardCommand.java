package io.github.hossensyedriadh.cbs.card.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.card.api.validation.ValidCardPin;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.LuhnCheck;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class ActivateCardCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -1804535730472360669L;

    @NotNull
    @JsonProperty("card_id")
    private Long cardId;

    @NotBlank
    @LuhnCheck
    @Pattern(regexp = "^[0-9]+$", message = "Only digits (0-9) allowed")
    @Size(min = 15, max = 16)
    @JsonProperty("card_number")
    private String cardNumber;

    @NotNull
    @FutureOrPresent
    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @NotBlank
    @Pattern(regexp = "^[0-9]+$", message = "Only digits (0-9) allowed")
    @Size(min = 3, max = 4)
    private String cvv;

    @ValidCardPin
    @NotBlank
    @JsonProperty("card_pin")
    private String cardPin;
}
