package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.CardStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class CoreBankingCardResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1820429822579244427L;

    private Long id;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("card_product_id")
    private Long cardProductId;

    @JsonProperty("last_four")
    private String lastFour;

    private CardStatus status;

    @JsonProperty("expiry_date")
    private LocalDate expiryDate;
}
