package io.github.hossensyedriadh.internet_banking.beneficiary.model;

import io.github.hossensyedriadh.internet_banking.beneficiary.enumerator.BeneficiaryBankStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class BeneficiaryBankModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1354950488506172656L;

    private UUID id;

    private String bankCode;

    private String bankName;

    private BeneficiaryBankStatus status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
