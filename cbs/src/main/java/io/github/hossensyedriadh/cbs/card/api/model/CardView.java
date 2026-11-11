package io.github.hossensyedriadh.cbs.card.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.card.api.enumerator.CardStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
public class CardView implements Serializable {
    @Serial
    private static final long serialVersionUID = 2166753970716263113L;

    private Long id;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("card_product_id")
    private Long cardProductId;

    @JsonProperty("last_four")
    private String lastFour;

    private CardStatus status;

    @JsonProperty("issued_at")
    private OffsetDateTime issuedAt;

    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
