package io.github.hossensyedriadh.internet_banking.beneficiary.model;

import io.github.hossensyedriadh.internet_banking.beneficiary.enumerator.BeneficiaryStatus;
import io.github.hossensyedriadh.internet_banking.customer.model.CustomerIdentityModel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class BeneficiaryModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 553543068681726560L;

    private UUID id;

    private CustomerIdentityModel customerIdentity;

    private BeneficiaryBankBranchModel bankBranch;

    private String beneficiaryName;

    private String accountNumber;

    private String nickname;

    private BeneficiaryStatus status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
