package io.github.hossensyedriadh.cbs.card.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.card.api.enumerator.CardReplacementReason;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class ReplaceCardCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = -8444128562469961892L;

    @NotNull
    @JsonProperty("card_id")
    private Long cardId;

    @NotNull
    private CardReplacementReason reason;
}
