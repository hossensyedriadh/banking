package io.github.hossensyedriadh.cbs.card.api.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class IssueCardCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 5256870299464110693L;

    @NotNull
    @JsonProperty("account_id")
    private Long accountId;

    @NotNull
    @JsonProperty("card_product_id")
    private Long cardProductId;
}
