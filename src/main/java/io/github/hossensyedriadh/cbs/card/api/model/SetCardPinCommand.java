package io.github.hossensyedriadh.cbs.card.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SetCardPinCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -744940414374640667L;

    @NotNull
    @JsonProperty("card_id")
    private Long cardId;

    @NotBlank
    @Size(min = 4, max = 4)
    private String pin;
}
