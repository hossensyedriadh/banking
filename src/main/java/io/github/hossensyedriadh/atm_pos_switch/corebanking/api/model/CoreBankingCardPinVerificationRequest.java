package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CoreBankingCardPinVerificationRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -6970539075157993288L;

    @JsonProperty("card_id")
    private Long cardId;

    private byte[] encryptedPinBlock;
}
