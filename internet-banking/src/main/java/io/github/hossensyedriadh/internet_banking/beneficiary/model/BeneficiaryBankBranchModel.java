package io.github.hossensyedriadh.internet_banking.beneficiary.model;

import io.github.hossensyedriadh.internet_banking.beneficiary.enumerator.BeneficiaryBankBranchStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class BeneficiaryBankBranchModel implements Serializable {
    @Serial
    private static final long serialVersionUID = -5280131597602340447L;

    private UUID id;

    private UUID bankId;

    private String branchName;

    private String routingNumber;

    private BeneficiaryBankBranchStatus status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}