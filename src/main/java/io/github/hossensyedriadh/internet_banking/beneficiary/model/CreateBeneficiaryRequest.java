package io.github.hossensyedriadh.internet_banking.beneficiary.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateBeneficiaryRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 4084067579412534669L;

    @NotNull
    private UUID bankBranchId;

    @NotBlank
    @Size(max = 150)
    private String beneficiaryName;

    @NotBlank
    @Size(max = 100)
    private String nickName;

    @NotBlank
    @Size(max = 50)
    private String accountNumber;
}
