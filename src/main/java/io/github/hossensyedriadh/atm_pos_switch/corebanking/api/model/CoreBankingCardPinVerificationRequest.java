package io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank
    private String pan;

    @NotNull
    @JsonProperty("encrypted_pin_block")
    private byte[] encryptedPinBlock;
}
