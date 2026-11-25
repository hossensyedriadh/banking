package io.github.hossensyedriadh.internet_banking.beneficiary.model;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BeneficiaryBankBranchModel implements Serializable {
    @Serial
    private static final long serialVersionUID = -5280131597602340447L;

    private UUID id;

    private String branchName;

    private String routingNumber;
}