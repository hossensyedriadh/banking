package io.github.hossensyedriadh.internet_banking.beneficiary.model;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class UpdateBeneficiaryRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -771794164358561333L;

    @Size(max = 150)
    private String beneficiaryName;

    @Size(max = 100)
    private String nickName;
}
