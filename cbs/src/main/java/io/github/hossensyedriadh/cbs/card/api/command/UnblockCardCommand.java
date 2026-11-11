package io.github.hossensyedriadh.cbs.card.api.command;

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
public class UnblockCardCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -5132765682441435053L;

    @NotNull
    @JsonProperty("card_id")
    private Long cardId;

    @Size(max = 255)
    @NotBlank
    private String reason;
}
